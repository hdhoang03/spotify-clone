package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.LikeSongRequest;
import com.spotify.spotify.dto.response.LikeSongResponse;
import com.spotify.spotify.entity.LikeSong;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LikeSongMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "song", ignore = true)
    @Mapping(target = "likedAt", ignore = true)
    LikeSong toLikeSong(LikeSongRequest request);

    @Mapping(source = "id", target = "likeId")
    @Mapping(source = "song.id", target = "songId")
    @Mapping(source = "user.id", target = "userId")
    LikeSongResponse toLikeSongResponse(LikeSong likeSong);
}
