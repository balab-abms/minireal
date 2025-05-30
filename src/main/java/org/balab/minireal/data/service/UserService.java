package org.balab.minireal.data.service;

import org.apache.commons.io.FileUtils;
import org.balab.minireal.data.entity.User;
import org.balab.minireal.data.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService
{

    private final UserRepository repository;
    private final StorageProperties storage_properties;

    public UserService(
            UserRepository repository,
            StorageProperties storage_properties
    ) {
        this.repository = repository;
        this.storage_properties = storage_properties;
    }

    public User getByUsername(String username){
        return  repository.findByUsername(username);
    }
    public Optional<User> get(Long id) {
        return repository.findById(id);
    }

    public List<User> getAll(){
        return repository.findAll();
    }

    public User update(User entity) {
        return repository.save(entity);
    }

    public void delete(Long id) throws IOException
    {
        repository.deleteById(id);
        // delete the user folder
        String user_path = storage_properties.getUsers() + File.separator + id;
        File user_folder = new File(user_path);
        FileUtils.deleteDirectory(user_folder);
    }

    public Page<User> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
