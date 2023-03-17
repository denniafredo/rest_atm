package com.example.api.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.api.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "select coalesce(max(u.id),0) +1 from User u")
    long findNextId();

    Optional<User> findByUsernameAndIsActiveIsTrue(String username);

    Optional<User> findByAccountNumber(String accountNumber);

    Optional<User> findByUsername(String username);

    User findUserByUsername(String username);
}
