package com.example.RealMatch.user.application.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.RealMatch.user.domain.entity.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final Map<String, User> memoryStore = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findByProviderAndProviderId(final String provider, final String providerId) {
        final String key = provider + ":" + providerId;
        final User user = memoryStore.get(key);

        if (user != null) {
            log.info("User found in memory store: {} ({})", user.getName(), user.getEmail());
        } else {
            log.info("User not found in memory store for key: {}", key);
        }

        return Optional.ofNullable(user);
    }

    @Override
    public User saveOAuthUser(final String email,
                              final String nickname,
                              final String provider,
                              final String providerId) {
        final String key = provider + ":" + providerId;

        final User existingUser = memoryStore.get(key);
        if (existingUser != null) {
            log.info("Updating existing user: {}", existingUser.getId());
            existingUser.updateProfile(email, nickname);
            memoryStore.put(key, existingUser);
            return existingUser;
        }

        final User user = User.createOAuthUser(email, nickname, provider, providerId);
        memoryStore.put(key, user);

        log.info("User saved in memory: {} / Email: {} / ID: {}",
                user.getName(), user.getEmail(), user.getId());
        log.info("Total users in memory: {}", memoryStore.size());

        return user;
    }
}
