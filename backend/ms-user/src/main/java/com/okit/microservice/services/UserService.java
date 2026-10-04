package com.okit.microservice.services;

import com.okit.microservice.models.User;
import com.okit.microservice.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> findAll() {
        return repo.findAll();
    }

    public User findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User " + id + " introuvable"));
    }

    public User create(User u) {
        return repo.save(u);
    }

    public User update(Long id, User u) {
        User existing = findById(id);
        existing.setFirstName(u.getFirstName());
        existing.setLastName(u.getLastName());
        existing.setEmail(u.getEmail());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}