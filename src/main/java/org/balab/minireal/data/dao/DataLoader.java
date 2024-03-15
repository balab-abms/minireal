package org.balab.minireal.data.dao;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.balab.minireal.data.entity.Role;
import org.balab.minireal.data.entity.User;
import org.balab.minireal.data.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@AllArgsConstructor
public class DataLoader
{
    private final UserService user_service;

    @PostConstruct
    private void loadData()
    {
        generateUsers();
    }

    private void generateUsers(){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Create a user with USER role
        if(user_service.getByUsername("user") == null)
        {
            User user = new User();
            user.setUsername("user");
            user.setName("User Name");
            user.setHashedPassword(passwordEncoder.encode("1234"));
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(Role.USER);
            user.setRoles(userRoles);
            user_service.update(user);
        }

        // Create a user with ADMIN role
        if(user_service.getByUsername("admin") == null)
        {
            User admin = new User();
            admin.setUsername("admin");
            admin.setName("Admin Name");
            admin.setHashedPassword(passwordEncoder.encode("1234"));
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(Role.ADMIN);
            admin.setRoles(adminRoles);
            user_service.update(admin);
        }

    }
}
