package com.example.RealMatch.user.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.AuthenticationMethod;
import com.example.RealMatch.user.domain.entity.enums.AuthProvider;

public interface AuthenticationMethodRepository extends JpaRepository<AuthenticationMethod, UUID> {

    Optional<AuthenticationMethod> findByProviderAndProviderId(AuthProvider provider, String providerId);

    List<AuthenticationMethod> findByUserId(Long userId);

    boolean existsByProviderAndProviderId(AuthProvider provider, String providerId);


}
