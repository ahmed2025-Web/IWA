package com.okit.microservice.controllers;

import com.okit.microservice.models.User;
import com.okit.microservice.repositories.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/me")
    public User me(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();              // le "sub" du token

        // déjà en base ? on le renvoie. Sinon on le crée.
        return repo.findByKeycloakId(keycloakId).orElseGet(() -> {
            User u = new User();
            u.setKeycloakId(keycloakId);
            u.setEmail(jwt.getClaimAsString("email"));
            u.setFirstName(jwt.getClaimAsString("given_name"));
            u.setLastName(jwt.getClaimAsString("family_name"));
            return repo.save(u);
        });
    }
}