//package com.project.parking.config;
//
//
//import com.project.parking.enums.Role;
//import com.project.parking.model.User;
//import com.project.parking.repository.UserRepository;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.List;
//
//@Configuration
//@RequiredArgsConstructor
//
//public class DefaultData implements ApplicationRunner {
//    private final UserRepository userRepo;
//    private final PasswordEncoder encoder;
//    public void run(ApplicationArguments args) throws Exception {
//        List<User> owner = userRepo.findByRole(Role.OWNER);
//        if(owner.isEmpty()) {
//            User newUser = new User();
//            newUser.setUsername("admin");
//            newUser.setPassword(encoder.encode("123456"));
//            newUser.setRole(Role.OWNER);
//            newUser.setActive(true);
//            userRepo.save(newUser);
//        }
//        List<User> employee = userRepo.findByRole(Role.EMPLOYEE);
//        if(employee.isEmpty()) {
//            User newUser = new User();
//            newUser.setUsername("employee");
//            newUser.setPassword(encoder.encode("123456"));
//            newUser.setRole(Role.EMPLOYEE);
//            newUser.setActive(true);
//            userRepo.save(newUser);
//        }
//        List<User> customer = userRepo.findByRole(Role.CUSTOMER);
//        if(customer.isEmpty()) {
//            User newUser = new User();
//            newUser.setUsername("customer");
//            newUser.setPassword(encoder.encode("123456"));
//            newUser.setRole(Role.CUSTOMER);
//            newUser.setActive(true);
//            userRepo.save(newUser);
//        }
//    }
//
//}
