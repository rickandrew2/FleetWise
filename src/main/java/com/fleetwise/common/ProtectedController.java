package com.fleetwise.common;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/protected")
public class ProtectedController {

    @GetMapping("/me")
    public Map<String, String> me(Authentication authentication) {
        return Map.of(
                "status", "authenticated",
                "email", authentication.getName());
    }
}