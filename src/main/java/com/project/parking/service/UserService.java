package com.project.parking.service;

import com.project.parking.dto.request.UpdatePasswordDTO;
import com.project.parking.dto.request.UpdateUserDTO;
import com.project.parking.dto.request.UserDTO;
import com.project.parking.dto.request.VerifyUserDTO;
import com.project.parking.enums.Role;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.model.Token;
import com.project.parking.repository.TokenRepository;
import com.project.parking.repository.UserRepository;
import com.project.parking.response.user.LoginResponse;
import com.project.parking.response.user.UserResponse;
import com.project.parking.utils.JwtGenerator;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.project.parking.model.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    private final TokenRepository tokenRepository;
    // private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;
    private final ModelMapper modelMapper;

    @Transactional
    public User createUser(UserDTO userDTO) throws Exception {
        User userToSave = null;

        Optional<User> existingUserByUsername = userRepository.findByUsername(userDTO.getUsername());
        if (existingUserByUsername.isPresent()) {
            User existingUser = existingUserByUsername.get();
            if (existingUser.getActive()) {
                throw new DataIntegrityViolationException("Username already exists");
            }
            userToSave = existingUser;
        }

        if (userToSave == null) {
            Optional<User> existingUserByEmail = userRepository.findByEmail(userDTO.getEmail());
            if (existingUserByEmail.isPresent()) {
                User existingUser = existingUserByEmail.get();
                if (existingUser.getActive()) {
                    throw new DataIntegrityViolationException("Email already exists");
                }
                userToSave = existingUser;
            }
        } else {
            if (!userToSave.getEmail().equals(userDTO.getEmail()) && userRepository.existsByEmail(userDTO.getEmail())) {
                throw new DataIntegrityViolationException("Email already exists");
            }
        }

        if (userToSave == null) {
            userToSave = new User();
        }

        userToSave.setUsername(userDTO.getUsername());
        userToSave.setEmail(userDTO.getEmail());
        userToSave.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userToSave.setVerificationCode(generateVerificationCode()); // Tạo code mới
        userToSave.setActive(false);
        userToSave.setFullname(userDTO.getFullName());
        userToSave.setDateOfBirth(userDTO.getDateOfBirth());
        userToSave.setPhoneNumber(userDTO.getPhoneNumber());
        userToSave.setVerificationExpiration(LocalDateTime.now().plusMinutes(15)); // Reset thời gian

        Role role;
        try {
            role = Role.valueOf(userDTO.getRole().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            role = Role.CUSTOMER;
        }
        userToSave.setRole(role);

        User savedUser = userRepository.save(userToSave);

        try {
            sendVerificationEmail(savedUser);
        } catch (Exception e) {
            log.error("Error sending verification email", e);
            throw new RuntimeException("Lỗi gửi email xác thực: " + e.getMessage());
        }

        return savedUser;
    }

    public void verifyUser(VerifyUserDTO verifyUserDTO) throws Exception {
        User user = userRepository.findByEmail(verifyUserDTO.getEmail())
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        if (user.getVerificationCode().equals(verifyUserDTO.getVerificationCode())
                && user.getVerificationExpiration().isAfter(LocalDateTime.now())) {
            user.setActive(true);
            user.setVerificationCode(null);
            user.setVerificationExpiration(null);
            userRepository.save(user);
        } else {
            throw new DataNotFoundException("Invalid verification code");
        }
    }

    public void resendVerificationCode(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("User findByEmail not found" + email));
        if (user.getActive()) {
            throw new DataNotFoundException("User already verified ");
        }
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        sendVerificationEmail(user);
    }

    public void sendVerificationEmail(User user) throws MessagingException {
        String subject = "Verification code";
        String verificationCode = user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            user.setVerificationCode(verificationCode);
            user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            // Handle email sending exception
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {

        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    @Transactional
    public LoginResponse login(String username, String password) throws Exception {
        User userExist = userRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("User not exist")); // 1
        if (!passwordEncoder.matches(password, userExist.getPassword())) {
            throw new BadCredentialsException("Password not match");
        }
        if(userExist.getVerificationCode()!=null){
            throw new BadCredentialsException("Unverified user");
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                username, password, userExist.getAuthorities());
        authenticationManager.authenticate(authenticationToken);
        Token userToken = tokenRepository.findByUser(userExist);
        String token;
        try {
            if (jwtGenerator.isValidToken(userToken.getToken())) {
                token = userToken.getToken();
            } else {
                token = tokenService.addToken(userExist, jwtGenerator.generateToken(userExist)).getToken();
            }
        } catch (Exception e) {
            token = tokenService.addToken(userExist, jwtGenerator.generateToken(userExist)).getToken();
        }

        LoginResponse loginResponse = modelMapper.map(userExist, LoginResponse.class);
        loginResponse.setToken(token);

        return loginResponse;
    }

    @Transactional
    public User getUserDetailFromToken(String token) throws Exception {
        String username = jwtGenerator.extractUsername(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public User updateInfo(Long userId, UpdateUserDTO userDTO) throws Exception {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // update user
        if (userDTO.getFullName() != null) {
            existingUser.setFullname(userDTO.getFullName());
        }

        if (userDTO.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        }
        //
        // if (updatedUserDTO.getAddress() != null) {
        // existingUser.setAddress(updatedUserDTO.getAddress());
        // }
        if (userDTO.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(userDTO.getDateOfBirth());
        }

        if (userDTO.getEmail() != null) {
            existingUser.setEmail(userDTO.getEmail());
        }

        if (userDTO.getUsername() != null) {
            existingUser.setUsername(userDTO.getUsername());
        }

        return userRepository.save(existingUser);

    }

    @Transactional(rollbackFor = Exception.class)
    public User updatePassword(Long userId, UpdatePasswordDTO updatePasswordDTO) throws Exception {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!passwordEncoder.matches(updatePasswordDTO.getPassword(), existingUser.getPassword())) {
            throw new BadCredentialsException("Password not match");
        }
        if (updatePasswordDTO.getNewPassword() != null
                && !updatePasswordDTO.getNewPassword().isEmpty()) {
            if (!updatePasswordDTO.getNewPassword().equals(updatePasswordDTO.getRetypePassword())) { // check retype
                                                                                                     // password
                throw new DataNotFoundException("Password not match");
            }

            String newPassword = updatePasswordDTO.getNewPassword();
            String encodePassword = passwordEncoder.encode(newPassword);
            existingUser.setPassword(encodePassword);
        }
        return userRepository.save(existingUser);

    }

    @Transactional
    public User deleteUserByUserId(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.toString().equals("ROLE_OWNER"));
        if (isAdmin) {
            throw new IllegalStateException("Cannot delete admin account");
        }
        userRepository.delete(user);
        return user;

    }

    public Optional<com.project.parking.model.User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    private void sendEmail(User user, String subject, String template, String urlAttribute, String urlPath)
            throws MessagingException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("username", user.getUsername());
        attributes.put(urlAttribute, "http://" + "localhost:8080" + urlPath);
        emailService.sendMessageHtml(user.getEmail(), subject, template, attributes);
    }

    public void forgotPassword(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        String newPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        sendForgotPasswordEmail(email, newPassword);
    }

    public void sendForgotPasswordEmail(String email, String newPassword) throws MessagingException {
        String subject = "Forgot password";
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Forgot password?</h2>"
                + "<p style=\"font-size: 16px;\">Your new password is below:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">New Password:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + newPassword + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(email, subject, htmlMessage);
        } catch (MessagingException e) {
            // Handle email sending exception
            e.printStackTrace();
        }
    }

    public List<UserResponse> getUsersByRole(Role role) {
        try {
            List<User> users = userRepository.findByRole(role);
            return UserResponse.fromUsers(users);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid role: " + role + ". Valid roles are: CUSTOMER, OWNER, EMPLOYEE");
        }
    }

    public User findById(Long id) throws DataNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("User không tồn tại với ID: " + id));
    }

}
