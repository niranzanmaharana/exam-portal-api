package com.niranzan.exam.repository;

import com.niranzan.exam.entity.Category;
import com.niranzan.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    List<Category> findByIsCommonTrue();
    List<Category> findByCreatedBy(User createdBy);
    
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.createdBy WHERE c.isCommon = true OR c.createdBy.id = :userId")
    List<Category> findByIsCommonTrueOrCreatedBy(@Param("userId") Long userId);
    
    Boolean existsByName(String name);
}

