package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.SongStreamRequest;
import com.spotify.spotify.dto.response.SongStreamResponse;
import com.spotify.spotify.entity.SongStream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SongStreamMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "song", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    SongStream toSongStream(SongStreamRequest request);

    @Mapping(source = "id", target = "streamId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "song.id", target = "songId")
    SongStreamResponse toSongStreamResponse(SongStream songStream);
}
