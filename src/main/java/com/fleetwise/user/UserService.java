package com.fleetwise.user;

import com.fleetwise.user.dto.UserSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getUserSummaries() {
        return userRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((User user) -> safeLower(user.getName()))
                        .thenComparing(user -> safeLower(user.getEmail())))
                .map(UserSummaryResponse::from)
                .toList();
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
