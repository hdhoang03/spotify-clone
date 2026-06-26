package com.spotify.spotify.repository;

import com.spotify.spotify.dto.response.StreamStatResponse;
import com.spotify.spotify.dto.response.TopLikeSongResponse;
import com.spotify.spotify.dto.response.TopStreamResponse;
import com.spotify.spotify.entity.Artist;
import com.spotify.spotify.entity.SongStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SongStreamRepository extends JpaRepository<SongStream, String> {
    Long countBySongIdAndValidStreamTrue(String id);
    Long countBySongId(String songId); //đếm lượt stream
    Page<SongStream> findByUser_IdOrderByCreatedAtDesc(String userId, Pageable pageable);//Lấy danh sách lượt nghe theo user
    Boolean existsByUser_IdAndSong_Id(String userId, String songId);//Kiểm tra user nghe đúng bài đó chưa

    @Query("""
            SELECT new com.spotify.spotify.dto.response.StreamStatResponse(
                CAST(s.createdAt AS LocalDate), COUNT(s)
            )
            FROM SongStream s
            WHERE s.song.id =:songId AND s.createdAt BETWEEN :start AND :end AND s.validStream = true
            GROUP BY CAST(s.createdAt AS LocalDate)
            ORDER BY CAST(s.createdAt as LocalDate) ASC
            LIMIT 10
            """)
    List<StreamStatResponse> getStreamStats(@Param("songId") String songId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Query("""
            SELECT new com.spotify.spotify.dto.response.TopStreamResponse(
                s.song.title,
                s.song.id,
                s.song.artist.id,
                s.song.artist.name,
                s.song.coverUrl,
                s.song.audioUrl,
                COUNT(s),
                s.song.duration
            )
            FROM SongStream s
            WHERE s.validStream = true AND s.song.deleted = false AND s.song.artist.deleted = false
            GROUP BY s.song.id, s.song.title, s.song.artist.id, s.song.artist.name,
                s.song.coverUrl, s.song.audioUrl, s.song.duration
            ORDER BY COUNT(s) DESC
            LIMIT 10
    """)
    List<TopStreamResponse> findTopStreamSongs();//Top bài hát được nghe nhiều

    // Thống kê lượt nghe toàn hệ thống theo khoảng thời gian
    @Query("""
        SELECT new com.spotify.spotify.dto.response.StreamStatResponse(
            CAST(s.createdAt AS LocalDate), COUNT(s)
        )
        FROM SongStream s
        WHERE s.createdAt BETWEEN :start AND :end
        GROUP BY CAST(s.createdAt AS LocalDate)
        ORDER BY CAST(s.createdAt AS LocalDate) ASC
        """)
    List<StreamStatResponse> getOverallStreamStats(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    @Query("""
            SELECT COUNT(s) FROM SongStream s
            WHERE YEAR(s.createdAt) = :year
                AND MONTH(s.createdAt) = :month
                AND s.validStream = true
        """)
    Long countStreamsByMonth(@Param("year") int year, @Param("month") int month);

    @Query("""
            SELECT s FROM SongStream s
            WHERE s.user.id = :userId
                AND s.song.id =:songId
            ORDER BY s.createdAt DESC
            """)
    List<SongStream> findRecentStreams(@Param("userId") String userId,
                                       @Param("songId") String songId,
                                       Pageable pageable);

    @Query("""
        SELECT new com.spotify.spotify.dto.response.TopLikeSongResponse(
            s.song.title, s.song.id, s.song.artist.id, s.song.artist.name,
            s.song.coverUrl, s.song.audioUrl, COUNT(s), s.song.duration
        )
        FROM SongStream s
        WHERE s.user.id = :userId
            AND s.validStream = true
            AND MONTH(s.createdAt) = :month
            AND YEAR(s.createdAt) = :year
            AND s.song.deleted = false
            AND s.song.artist.deleted = false
        GROUP BY s.song.id, s.song.title, s.song.artist.id, s.song.artist.name,
                 s.song.coverUrl, s.song.audioUrl, s.song.duration
        ORDER BY COUNT(s) DESC
        LIMIT 10
    """)
    List<TopLikeSongResponse> findMyTopTracksOfThisMonth(@Param("userId") String userId,
                                                         @Param("month") int month,
                                                         @Param("year") int year);

    @Query("""
        SELECT s.song.artist
            FROM SongStream s
            WHERE s.user.id = :userId
                AND MONTH(s.createdAt) = :month
                AND YEAR(s.createdAt) = :year
                AND s.song.artist.deleted = false
            GROUP BY s.song.artist.id
            ORDER BY COUNT(s) DESC
            LIMIT 5
    """)
    List<Artist> findMyTopArtistsOfThisMonthEntity(@Param("userId") String userId, @Param("month") int month, @Param("year") int year);
}
