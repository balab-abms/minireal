package org.balab.minireal.data.dao;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.balab.minireal.data.entity.Role;
import org.balab.minireal.data.entity.SampleModel;
import org.balab.minireal.data.entity.User;
import org.balab.minireal.data.service.SampleModelService;
import org.balab.minireal.data.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Component
@AllArgsConstructor
public class DataLoader
{
    private final UserService user_service;
    private final SampleModelService samples_service;

    @PostConstruct
    private void loadData()
    {
        generateUsers();
        generateSamples();
    }

    private void generateUsers(){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Create a user with ADMIN role
        if(user_service.getByUsername("owner") == null)
        {
            User owner_user = new User();
            owner_user.setUsername("owner");
            owner_user.setName("Owner Name");
            owner_user.setHashedPassword(passwordEncoder.encode("1234"));
            Set<Role> ownerRoles = new HashSet<>();
            ownerRoles.add(Role.OWNER);
            owner_user.setRoles(ownerRoles);
            user_service.update(owner_user);
        }

    }

    private void generateSamples(){
        if(samples_service.findByModelName("Economy").isEmpty()){
            try {
                SampleModel economy = new SampleModel();
                economy.setModel("Economy");
                economy.setAgents(new String[]{"Person"});
                economy.setFields(new String[]{"Bag"});
                File economy_file = new File("simreal_data/sample_models/MiniReal_Economy.zip");
                samples_service.saveSampleModel(economy, economy_file.getName(), Files.readAllBytes(Path.of(economy_file.getPath())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(samples_service.findByModelName("Life").isEmpty()){
            try {
                SampleModel life = new SampleModel();
                life.setModel("Life");
                life.setAgents(new String[]{"Cell"});
                life.setFields(new String[]{"Grid2D"});
                File life_file = new File("simreal_data/sample_models/MiniReal_Life.zip");
                samples_service.saveSampleModel(life, life_file.getName(), Files.readAllBytes(Path.of(life_file.getPath())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
