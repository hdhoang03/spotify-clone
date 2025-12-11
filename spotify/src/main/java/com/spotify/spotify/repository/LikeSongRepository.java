package com.spotify.spotify.repository;

import com.spotify.spotify.dto.response.TopLikeSongResponse;
import com.spotify.spotify.entity.LikeSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeSongRepository extends JpaRepository<LikeSong, String> {
    Boolean existsByUser_IdAndSong_Id(String userId, String songId);//Kiểm tra user đã like chưa

    @Query("""
            SELECT l FROM LikeSong l
            JOIN FETCH l.song
            WHERE l.user.id =:userId
            """)

    List<LikeSong> findAllByUserIdFetchSong(String userId); //Danh sách bài hát user đã like

    Long countBySong_Id(String songId); //Đếm số lượt like của bài hát

    void deleteByUser_IdAndSong_Id(String userId, String songId); //unlike

    @Query("""
            SELECT new com.spotify.spotify.dto.response.TopLikeSongResponse(
                l.song.title, l.song.id, l.song.artist.name, l.song.coverUrl, COUNT(l), l.song.duration
            )
            FROM LikeSong l
            GROUP BY l.song.title, l.song.id, l.song.artist.name, l.song.coverUrl, l.song.duration
            ORDER BY COUNT(l) DESC
            """)
    List<TopLikeSongResponse> findTopLikedSongs();
}
