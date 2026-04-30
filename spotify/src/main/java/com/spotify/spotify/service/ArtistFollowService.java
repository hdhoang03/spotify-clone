package com.spotify.spotify.service;

import com.spotify.spotify.dto.response.ArtistFollowResponse;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.entity.Artist;
import com.spotify.spotify.entity.ArtistFollow;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.ArtistMapper;
import com.spotify.spotify.repository.ArtistFollowRepository;
import com.spotify.spotify.repository.ArtistRepository;
import com.spotify.spotify.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArtistFollowService {
    ArtistFollowRepository artistFollowRepository;
    ArtistRepository artistRepository;
    UserRepository userRepository;
    ArtistMapper artistMapper;

    @Transactional //phải có để Modifying chạy
    public boolean toggleFollow(String artistId){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Optional<ArtistFollow> existing = artistFollowRepository.findByUserIdAndArtistId(user.getId(), artistId);

        if (existing.isPresent()){
            artistFollowRepository.delete(existing.get());
            artistRepository.decrementFollowerCount(artistId);//giảm lượt follow của nghệ sĩ
            return false; //unfollow
        } else {
            if(!artistRepository.existsById(artistId)){
                throw new AppException(ErrorCode.ARTIST_NOT_FOUND);
            }

            Artist artistProxy = artistRepository.getReferenceById(artistId);//tạo proxy đỡ tốn 1 dòng SELECT Artist

            artistFollowRepository.save(
                    ArtistFollow.builder()
                            .user(user)
                            .artist(artistProxy)
                            .followedAt(LocalDateTime.now())
                            .build()
            );
            artistRepository.incrementFollowerCount(artistId);
            return true;
        }
    }

    public Page<ArtistResponse> getMyFollowedArtists(Pageable pageable){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Page<Artist> follows = artistFollowRepository.findFollowedArtistByUserId(user.getId(), pageable);
        return follows.map(artistMapper::toArtistResponse);
    }

    public Page<ArtistFollowResponse> getFollowedArtists(String userId, Pageable pageable){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!Boolean.TRUE.equals(user.getIsPublicProfile())){//nếu null -> false
            throw new AppException(ErrorCode.USER_PROFILE_PRIVATE);
        }
        return artistFollowRepository.findFollowedArtistByUserId(userId, pageable)
                .map(artistMapper::toArtistFollowResponse);
    }

    public Long getFollowerCount(String artistId){ //chủ yếu báo lỗi 404
        if (!artistRepository.existsById(artistId))
            throw new AppException(ErrorCode.ARTIST_NOT_FOUND);
        return artistFollowRepository.countByArtistId(artistId);
    }
}
