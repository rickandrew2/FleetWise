package com.fleetwise.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldGenerateAndValidateToken() {
        UserDetails userDetails = User.withUsername("driver@fleetwise.test")
                .password("unused")
                .authorities("ROLE_DRIVER")
                .build();

        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("driver@fleetwise.test");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }
}