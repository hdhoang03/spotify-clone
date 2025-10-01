package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.AlbumRequest;
import com.spotify.spotify.dto.response.AlbumResponse;
import com.spotify.spotify.entity.Album;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = SongMapper.class)
public interface AlbumMapper {
    @Mapping(target = "albumUrl", ignore = true)
    Album toAlbum(AlbumRequest request);
    @Mapping(source = "albumUrl", target = "avatarUrl")
    AlbumResponse toAlbumResponse(Album album);
    @Mapping(target = "albumUrl", ignore = true)
    void updateAlbum(@MappingTarget Album album, AlbumRequest request);
}