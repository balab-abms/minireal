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

        // Create a user with ADMIN role
        if(user_service.getByUsername("owner") == null)
        {
            User owner_user = new User();
            owner_user.setUsername("owner");
            owner_user.setName("Owner Name");
            owner_user.setHashedPassword(passwordEncoder.encode("12345"));
            Set<Role> ownerRoles = new HashSet<>();
            ownerRoles.add(Role.OWNER);
            owner_user.setRoles(ownerRoles);
            user_service.update(owner_user);
        }

    }
}
