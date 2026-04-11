package com.fluxo.user.repository;

import com.fluxo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // O Spring Boot cria a query SQL inteira só de ler o nome deste método!
    Optional<User> findByUsername(String username);
}