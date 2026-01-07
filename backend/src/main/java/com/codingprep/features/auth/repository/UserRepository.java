
package com.codingprep.features.auth.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codingprep.features.auth.models.AuthenticationUser;

@Repository
public interface UserRepository extends JpaRepository<AuthenticationUser,Long> {
    
//WHERE u.password LIKE :password
//,@Param("password") String password
    @Query("SELECT u FROM users u WHERE u.username = :username and u.password = :password")
    AuthenticationUser checkUserCredentials(@Param("username") String username,@Param("password") String password);

    
    @Query("SELECT u FROM #{#entityName} u WHERE u.username = ?1")
   Optional<AuthenticationUser> checkUserExists(String username);

}
