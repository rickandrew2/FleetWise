package com.fleetwise.auth;

import com.fleetwise.auth.dto.AuthResponse;
import com.fleetwise.auth.dto.LoginRequest;
import com.fleetwise.auth.dto.RegisterRequest;
import com.fleetwise.auth.dto.RegisterResponse;
import com.fleetwise.user.User;
import com.fleetwise.user.UserRepository;
import com.fleetwise.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.DRIVER);

        User savedUser = userRepository.save(user);
        return new RegisterResponse(savedUser.getId().toString(), savedUser.getEmail(), savedUser.getRole().name());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, jwtService.getJwtExpirationMs(), user.getEmail(), user.getRole().name());
    }
}