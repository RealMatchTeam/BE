package com.example.RealMatch.global.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;

    public Optional<User> findByProviderAndProviderId(final String provider,
                                                      final String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    public User saveOAuthUser(final String email,
                              final String name,
                              final String provider,
                              final String providerId) {
        final User user = User.createOAuthUser(email, name, provider, providerId);
        return userRepository.save(user);
    }
}
