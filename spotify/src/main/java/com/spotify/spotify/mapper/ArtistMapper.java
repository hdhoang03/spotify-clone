package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.ArtistRequest;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.entity.Artist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = SongMapper.class)
public interface ArtistMapper {
    @Mapping(target = "avatarUrl", ignore = true)
    Artist toArtist(ArtistRequest request);
    ArtistResponse toArtistResponse(Artist artist);
    @Mapping(target = "avatarUrl", ignore = true)
    void updateArtist(@MappingTarget Artist artist, ArtistRequest request);
}