package com.neroforte.goodfiction.repository;

import com.neroforte.goodfiction.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String name);

    void deleteByUsername(String username);

}
