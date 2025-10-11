package com.spotify.spotify.repository;

import com.spotify.spotify.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, String> {
    Optional<Album> findByName(String name);
    boolean existsByName(String name);
    List<Album> findByNameContaining(String keyword);
    List<Album> findByArtists_Id(String artistId);
}