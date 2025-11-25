package com.spotify.spotify.repository;

import com.spotify.spotify.entity.LikeSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeSongRepository extends JpaRepository<LikeSong, String> {
    Optional<LikeSong> findByUserIdAndSongId(String userId, String songId);//Kiểm tra user đã like chưa
    List<LikeSong> findAllByUser_Id(String userId); //Danh sách bài hát user đã like
    Long countBySongId(String songId); //Đếm số lượt like của bài hát
    void deleteAllByUserId(String userId); //Khi user bị xóa tài khoản thì xóa like
}
