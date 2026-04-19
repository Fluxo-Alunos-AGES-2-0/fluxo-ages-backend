package com.fluxo.user.repository;

import com.fluxo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // Busca o usuário comparando o parâmetro 'email' com o atributo 'email' da entidade User
    @Query("SELECT e FROM User e WHERE e.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    // Busca o usuário tanto pelo e-mail quanto pelo nome (usado como username)
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.name = :identifier")
    Optional<User> findByIdentifier(@Param("identifier") String identifier);
}
