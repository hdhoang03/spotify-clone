package com.spotify.spotify.repository;

import com.spotify.spotify.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    Optional<Category> findByName(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByName(String name);
    Page<Category> findAllByDeletedFalse(Pageable pageable);
    Page<Category> findByNameContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);
    Optional<Category> findByIdAndDeletedFalse(String id);
}