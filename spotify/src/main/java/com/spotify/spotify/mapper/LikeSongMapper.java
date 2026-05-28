package com.spotify.spotify.mapper;
import com.spotify.spotify.dto.response.LikeSongResponse;
import com.spotify.spotify.entity.LikeSong;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {SongMapper.class})
public interface LikeSongMapper {
    @Mapping(source = "id", target = "likeId")
    LikeSongResponse toLikeSongResponse(LikeSong likeSong);
}
