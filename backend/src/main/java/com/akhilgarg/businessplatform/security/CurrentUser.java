package com.akhilgarg.businessplatform.security;

import com.akhilgarg.businessplatform.entity.User;
import com.akhilgarg.businessplatform.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long getId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phoneNumber = auth.getName(); // the "sub" claim JwtAuthFilter set as username

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));

        return user.getId();
    }
}