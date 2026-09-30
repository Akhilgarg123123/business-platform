package com.akhilgarg.businessplatform.service;

import com.akhilgarg.businessplatform.dto.AuthResponse;
import com.akhilgarg.businessplatform.dto.LoginRequest;
import com.akhilgarg.businessplatform.dto.RegisterRequest;
import com.akhilgarg.businessplatform.entity.User;
import com.akhilgarg.businessplatform.exception.DuplicateResourceException;
import com.akhilgarg.businessplatform.exception.UnauthorizedException;
import com.akhilgarg.businessplatform.repository.UserRepository;
import com.akhilgarg.businessplatform.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        User user = new User();
        user.setBusinessName(request.getBusinessName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getPhoneNumber());
        return new AuthResponse(token, user.getBusinessName(), user.getPhoneNumber());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new UnauthorizedException("Invalid phone number or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid phone number or password");
        }

        String token = jwtUtil.generateToken(user.getPhoneNumber());
        return new AuthResponse(token, user.getBusinessName(), user.getPhoneNumber());
    }
}