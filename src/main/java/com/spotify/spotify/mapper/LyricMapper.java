package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.response.LyricResponse;
import com.spotify.spotify.entity.Lyrics;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LyricMapper {
    LyricResponse toLyricResponse(Lyrics  lyrics);
}
