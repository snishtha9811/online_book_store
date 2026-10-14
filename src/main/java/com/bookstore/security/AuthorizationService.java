package com.bookstore.security;

import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authorizationService")
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;

    /**
     * Check if the current user is authorized to access the resource
     */
    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
            .map(user -> user.getId().equals(userId))
            .orElse(false);
    }
}

