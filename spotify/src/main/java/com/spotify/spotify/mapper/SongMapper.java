package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.SongRequest;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.Artist;
import com.spotify.spotify.entity.Category;
import com.spotify.spotify.entity.Song;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

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

    @Mapping(source = "uploadedBy.username", target = "uploadedBy")
    @Mapping(source = "album.name", target = "albumName")
    @Mapping(source = "album.id", target = "albumId")
    @Mapping(source = "category.name", target = "category")
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

    //Category -> String
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