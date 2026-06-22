package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.ArtistRequest;
import com.spotify.spotify.dto.response.ArtistFollowResponse;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.entity.Artist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = SongMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface ArtistMapper {
    @Mapping(target = "avatarUrl", ignore = true)
    Artist toArtist(ArtistRequest request);
//    @Mapping(target = "songs", ignore = true)
    ArtistResponse toArtistResponse(Artist artist);
    ArtistFollowResponse toArtistFollowResponse(Artist artist);
    @Mapping(target = "avatarUrl", ignore = true)
    void updateArtist(@MappingTarget Artist artist, ArtistRequest request);
}