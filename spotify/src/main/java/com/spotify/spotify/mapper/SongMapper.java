package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.SongRequest;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.Song;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SongMapper {
    @Mapping(target = "coverUrl", ignore = true)
    @Mapping(target = "audioUrl", ignore = true)
    Song toSong(SongRequest request);
    @Mapping(source = "uploadedBy.username", target = "uploadedBy")
    SongResponse toSongResponse(Song song);
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "coverUrl", ignore = true)
    @Mapping(target = "audioUrl", ignore = true)
    void updateSong(@MappingTarget Song song, SongRequest request);
}