package com.fluxo.user.repository;

import com.fluxo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Busca o usuário comparando o parâmetro 'username' com o atributo 'name' da entidade User
    @Query("SELECT u FROM User u WHERE u.name = :username")
    Optional<User> findByUsername(@Param("username") String username);
}