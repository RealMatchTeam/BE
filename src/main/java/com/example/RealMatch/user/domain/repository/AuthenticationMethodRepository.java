package com.example.RealMatch.user.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.AuthenticationMethodEntity;
import com.example.RealMatch.user.domain.entity.enums.AuthProvider;

public interface AuthenticationMethodRepository extends JpaRepository<AuthenticationMethodEntity, Long> {

    Optional<AuthenticationMethodEntity> findByProviderAndProviderId(AuthProvider provider, String providerId);

    List<AuthenticationMethodEntity> findByUserId(Long userId);

    boolean existsByProviderAndProviderId(AuthProvider provider, String providerId);
}
