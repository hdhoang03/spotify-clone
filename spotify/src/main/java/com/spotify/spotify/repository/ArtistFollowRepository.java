package com.spotify.spotify.repository;

import com.spotify.spotify.entity.ArtistFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistFollowRepository extends JpaRepository<ArtistFollow, String> {
    Optional<ArtistFollow> findByUserIdAndArtistId(String userId, String artistId); //Kiểm tra đã follow chưa
    List<ArtistFollow> findAllByUser_Id(String userId); //Lấy danh sách nghệ sĩ user đang theo dõi
    List<ArtistFollow> findAllByArtistId(String artistId); //Lấy danh sách người dùng theo dõi nghệ sĩ
    Long countByArtistId(String artistId); //Đếm số người theo dõi
}
