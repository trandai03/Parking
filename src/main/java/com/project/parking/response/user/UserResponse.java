package com.project.parking.response.user;

import com.project.parking.model.Employee;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import com.project.parking.model.User;
import com.project.parking.repository.EmployeeRepository;

import javax.management.relation.Role;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Getter
@Setter
@Builder
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    //@Hidden
    private Long id;
    private String fullname;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String username;
    private String email;
    private String role;
    private Long employeeId;
    public static UserResponse fromUser(User user){
        if(user == null){
            log.error("UserResponse {}", (Object) null);
            return null;
        }

        return UserResponse.builder()
                .role(user.getRole().toString())
                .id(user.getId())
                .username(user.getUsername())
                .dateOfBirth(user.getDateOfBirth())
                .fullname(user.getFullname())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .build();
    }

    public static List<UserResponse> fromUsers(List<User> users){
        return users.stream().map(UserResponse::fromUser).collect(Collectors.toList());
    }

}
