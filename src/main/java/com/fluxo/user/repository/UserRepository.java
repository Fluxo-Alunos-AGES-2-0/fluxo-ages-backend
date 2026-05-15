package com.fluxo.user.repository;

import com.fluxo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // Busca o usuário comparando o parâmetro 'email' com o atributo 'email' da
    // entidade User
    @Query("SELECT e FROM User e WHERE e.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("""
                SELECT u
                FROM User u
                WHERE LOWER(u.email) = LOWER(:identifier)
                   OR LOWER(u.name) = LOWER(:identifier)
            """)
    Optional<User> findByIdentifierIgnoreCase(@Param("identifier") String identifier);
}
