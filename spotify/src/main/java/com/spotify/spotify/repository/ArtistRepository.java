package com.spotify.spotify.repository;

import com.spotify.spotify.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, String> {
    Optional<Artist> findByName(String artist);
    boolean existsByName(String artist);
    List<Artist> findByNameContaining(String keyword);
}