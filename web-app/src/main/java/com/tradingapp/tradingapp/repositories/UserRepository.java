package com.tradingapp.tradingapp.repositories;

import com.tradingapp.tradingapp.entities.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByEmailAndIdNot(@Email(message = "Невалиден имейл адрес.") @NotBlank(message = "Имейлът е задължителен.") String email, UUID id);
}
