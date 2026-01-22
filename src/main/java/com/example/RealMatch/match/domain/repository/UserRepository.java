package com.example.RealMatch.match.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RealMatch.user.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderIdAndProvider(String providerId, String provider);
}
