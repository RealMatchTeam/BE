package com.example.RealMatch.user.application.service;

import java.util.Optional;

import com.example.RealMatch.user.domain.entity.User;

public interface UserService {

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    User saveOAuthUser(String email, String nickname, String provider, String providerId);
}
