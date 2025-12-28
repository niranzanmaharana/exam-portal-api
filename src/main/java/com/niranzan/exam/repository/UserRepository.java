package com.niranzan.exam.repository;

import com.niranzan.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByRegistrationCode(String registrationCode);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByRegistrationCode(String registrationCode);
    List<User> findByRole(User.Role role);
    Optional<User> findByFirstNameAndLastName(String firstName, String lastName);
}

