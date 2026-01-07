package com.codingprep.features.auth.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codingprep.features.auth.models.AuthenticationUser;
import com.codingprep.features.auth.models.UserStatus;

@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus,Long> {
    

}
