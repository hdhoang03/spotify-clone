package com.spotify.spotify.service;

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

    @Transactional
    public boolean toggleFollow(String userId, String artistId){
        Optional<ArtistFollow> existing = artistFollowRepository.findByUserIdAndArtistId(userId, artistId);
        if (existing.isPresent()){
            artistFollowRepository.delete(existing.get());
            return false; //unfollow
        } else {
            Artist artist = artistRepository.findById(artistId)
                    .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND));
            artistFollowRepository.save(
                    ArtistFollow.builder()
                            .user(User.builder().id(userId).build())
                            .artist(artist)
                            .followedAt(LocalDateTime.now())
                            .build()
            );
            return true;
        }
    }

    public List<ArtistResponse> getMyFollowedArtists(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<ArtistFollow> follows = artistFollowRepository.findAllByUser_Id(user.getId());
        return follows.stream()
                .map(ArtistFollow::getArtist)
                .map(artistMapper::toArtistResponse)
                .toList();
    }

    public List<ArtistResponse> getFollowedArtists(String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.getIsPublicProfile()){
            throw new AppException(ErrorCode.USER_PROFILE_PRIVATE);
        }
        List<ArtistFollow> follows = artistFollowRepository.findAllByUser_Id(userId);

        return follows.stream()
                .map(ArtistFollow::getArtist)
                .map(artistMapper::toArtistResponse)
                .toList();
    }

    public Long getFollowerCount(String artistId){
        if (!artistRepository.existsById(artistId))
            throw new AppException(ErrorCode.ARTIST_NOT_FOUND);
        return artistFollowRepository.countByArtistId(artistId);
    }
}
