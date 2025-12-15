package com.spotify.spotify.service;

import com.spotify.spotify.dto.response.SearchResponse;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.AlbumMapper;
import com.spotify.spotify.mapper.ArtistMapper;
import com.spotify.spotify.mapper.CategoryMapper;
import com.spotify.spotify.mapper.SongMapper;
import com.spotify.spotify.repository.AlbumRepository;
import com.spotify.spotify.repository.ArtistRepository;
import com.spotify.spotify.repository.CategoryRepository;
import com.spotify.spotify.repository.SongRepository;
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

    ArtistMapper artistMapper;
    SongMapper songMapper;
    AlbumMapper albumMapper;
    CategoryMapper categoryMapper;

    public SearchResponse searchEverything(String keyword){
        Pageable limit = PageRequest.of(0, 6); //Thay Pageable.unpaged() thành limit dể giới hạn kết quả
        var artists = CompletableFuture.supplyAsync(() ->
                artistRepository.findByNameContainingIgnoreCaseAndDeletedFalse(keyword, limit)
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
                categoryRepository.findByNameContainingIgnoreCaseAndDeletedFalse(keyword, limit)
                        .stream().map(categoryMapper::toCategoryResponse).toList()
        );

        CompletableFuture.allOf(artists, songs, albums, categories).join();
        try {
            return SearchResponse.builder()
                    .artists(artists.get())
                    .songs(songs.get())
                    .albums(albums.get())
                    .categories(categories.get())
                    .build();
        } catch (Exception e){
            log.error("Search failed", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}











