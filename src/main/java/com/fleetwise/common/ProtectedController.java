package com.fleetwise.common;

import com.fleetwise.user.User;
import com.fleetwise.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/protected")
@RequiredArgsConstructor
public class ProtectedController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));

        return Map.of(
                "status", "authenticated",
                "email", authentication.getName(),
                "role", user.getRole().name(),
                "userId", user.getId().toString());
    }
}