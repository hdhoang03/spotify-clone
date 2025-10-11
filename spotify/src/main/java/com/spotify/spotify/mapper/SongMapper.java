package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.SongRequest;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.Artist;
import com.spotify.spotify.entity.Song;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SongMapper {
    @Mapping(target = "coverUrl", ignore = true)
    @Mapping(target = "audioUrl", ignore = true)
    @Mapping(target = "album", ignore = true)
    Song toSong(SongRequest request);
    @Mapping(source = "uploadedBy.username", target = "uploadedBy")
    @Mapping(source = "album.name", target = "albumName")
    @Mapping(source = "album.id", target = "albumId")
    @Mapping(source = "category.name", target = "genre")
    SongResponse toSongResponse(Song song);
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "coverUrl", ignore = true)
    @Mapping(target = "audioUrl", ignore = true)
    @Mapping(target = "album", ignore = true)
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
}