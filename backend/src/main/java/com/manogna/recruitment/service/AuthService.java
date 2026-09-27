package com.manogna.recruitment.service;

import com.manogna.recruitment.dto.AuthResponse;
import com.manogna.recruitment.dto.LoginRequest;
import com.manogna.recruitment.dto.RegisterRequest;
import com.manogna.recruitment.entity.Company;
import com.manogna.recruitment.entity.Role;
import com.manogna.recruitment.entity.User;
import com.manogna.recruitment.repository.CompanyRepository;
import com.manogna.recruitment.repository.UserRepository;
import com.manogna.recruitment.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, CompanyRepository companyRepository,
                        PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("An account with this email already exists");
        }

        Role role;
        try {
            role = Role.valueOf(req.getRole().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("role must be one of STUDENT, RECRUITER, ADMIN");
        }

        User user = new User(req.getFullName(), req.getEmail(), passwordEncoder.encode(req.getPassword()), role);

        if (role == Role.RECRUITER) {
            if (req.getCompanyName() == null || req.getCompanyName().isBlank()) {
                throw new IllegalArgumentException("companyName is required when registering as a RECRUITER");
            }
            Company company = companyRepository.findByName(req.getCompanyName())
                    .orElseGet(() -> companyRepository.save(new Company(req.getCompanyName())));
            user.setCompany(company);
        }

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole().name());
        return new AuthResponse(token, saved.getEmail(), saved.getFullName(), saved.getRole().name());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }
}
