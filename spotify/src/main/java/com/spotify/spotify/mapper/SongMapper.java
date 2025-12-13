package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.SongRequest;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.Album;
import com.spotify.spotify.entity.Artist;
import com.spotify.spotify.entity.Category;
import com.spotify.spotify.entity.Song;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SongMapper {
    @Mapping(target = "coverUrl", ignore = true)
    @Mapping(target = "audioUrl", ignore = true)
    @Mapping(target = "album", ignore = true)
    @Mapping(target = "artist", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "playCount", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    Song toSong(SongRequest request);

    //dot notation "album.name" có khả năng null pointer nên dùng custom method
    @Mapping(source = "uploadedBy.username", target = "uploadedBy")
    @Mapping(source = "album", target = "albumName", qualifiedByName = "mapAlbumName")
    @Mapping(source = "album", target = "albumId", qualifiedByName = "mapAlbumId")
    @Mapping(source = "category", target = "category", qualifiedByName = "mapCategoryName")
    @Mapping(source = "artist", target = "artist", qualifiedByName = "mapArtistName")
    SongResponse toSongResponse(Song song);

    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "coverUrl", ignore = true)
    @Mapping(target = "audioUrl", ignore = true)
    @Mapping(target = "album", ignore = true)
    @Mapping(target = "artist", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "playCount", ignore = true)
    void updateSong(@MappingTarget Song song, SongRequest request);

    default String map(Artist artist){
        return artist != null ? artist.getName() : null;
    }

    default Artist map(String artistName){
        if (artistName == null) return null;
        Artist artist = new Artist();
        artist.setName(artistName);
        return artist;
    }

    @Named("mapAlbumName")
    default String mapAlbumName(Album album){
        if (album == null) return null;
        return album.getName();
    }

    @Named("mapAlbumId")
    default String mapAlbumId(Album album){
        if (album == null) return null;
        return album.getId();
    }

    @Named("mapArtistName")
    default String mapArtistName(Artist artist){
        if (artist == null) return null;
        return artist.getName();
    }

    //Category -> String
    @Named("mapCategoryName")
    default String mapCategoryName(Category category){
        return category != null ? category.getName() : null;
    }

    //String -> Category
    default Category mapToCategory(String categoryName){
        if(categoryName == null) return null;
        Category category = new Category();
        category.setName(categoryName);
        return category;
    }
}