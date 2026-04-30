package com.spotify.spotify.service;

import com.spotify.spotify.dto.response.SearchResponse;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.*;
import com.spotify.spotify.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SearchService {
    ArtistRepository artistRepository;
    SongRepository songRepository;
    AlbumRepository albumRepository;
    CategoryRepository categoryRepository;
    UserRepository userRepository;

    ArtistMapper artistMapper;
    SongMapper songMapper;
    AlbumMapper albumMapper;
    CategoryMapper categoryMapper;
    UserMapper userMapper;

    public SearchResponse searchEverything(String keyword){
        Pageable limit = PageRequest.of(0, 5); //Thay Pageable.unpaged() thành limit dể giới hạn kết quả
        var artists = CompletableFuture.supplyAsync(() ->
                artistRepository.findByNameContainingIgnoreCaseAndDeleted(keyword, false, limit)
                        .stream().map(artistMapper::toArtistResponse).toList()
        );

        var songs = CompletableFuture.supplyAsync(() ->
                songRepository.searchByKeyword(keyword, limit)
                        .stream().map(songMapper::toSongSearchResponse).toList()
        );

        var albums = CompletableFuture.supplyAsync(() ->
                albumRepository.searchByKeyword(keyword, limit)
                        .stream().map(albumMapper::toAlbumResponse).toList()
        );

        var categories = CompletableFuture.supplyAsync(() ->
                categoryRepository.searchCategoriesWithCount(keyword, false, limit)
                        .stream().map(categoryMapper::toCategoryResponseFromProjection).toList()
        );

        var users = CompletableFuture.supplyAsync(() ->
                userRepository.searchUsersForGlobalSearch(keyword, limit)
                        .stream().map(userMapper::toUserSummaryResponse).toList()
        );

        CompletableFuture.allOf(artists, songs, albums, categories, users).join();
        try {
            return SearchResponse.builder()
                    .artists(artists.get())
                    .songs(songs.get())
                    .albums(albums.get())
                    .categories(categories.get())
                    .users(users.get())
                    .build();
        } catch (Exception e){
            log.error("Search failed", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}











