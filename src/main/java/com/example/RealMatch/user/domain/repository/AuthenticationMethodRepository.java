package com.example.RealMatch.user.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.AuthenticationMethod;

public interface AuthenticationMethodRepository
        extends JpaRepository<AuthenticationMethod, Long> {

    Optional<AuthenticationMethod> findByProviderAndProviderId(
            String provider,
            String providerId
    );
}
