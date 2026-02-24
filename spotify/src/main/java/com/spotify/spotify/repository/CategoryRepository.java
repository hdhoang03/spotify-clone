package com.spotify.spotify.repository;

import com.spotify.spotify.constaint.CategoryType;
import com.spotify.spotify.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    interface CategoryWithSongCount{
        Category getCategory();
        Long getSongCount();
    }

    @Query("""
            SELECT c AS category, COUNT(s.id) AS songCount
            FROM Category c
            LEFT JOIN c.songs s ON s.deleted = false
            WHERE c.deleted = :isDeleted
            AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            GROUP BY c.id
        """)
    Page<CategoryWithSongCount> searchCategoriesWithCount(@Param("keyword") String keyword,
                                                          @Param("isDeleted") boolean isDeleted,
                                                          Pageable pageable);

    Optional<Category> findByName(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByName(String name);
    Page<Category> findAllByDeletedFalse(Pageable pageable);
    Optional<Category> findByIdAndDeletedFalse(String id);
    List<Category> findByTypeAndDeletedFalseOrderByDisplayOrderAsc(CategoryType type);
}