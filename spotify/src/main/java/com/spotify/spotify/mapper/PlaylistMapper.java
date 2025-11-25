package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.PlaylistRequest;
import com.spotify.spotify.dto.request.PlaylistUpdateRequest;
import com.spotify.spotify.dto.response.PlaylistResponse;
import com.spotify.spotify.dto.response.UserSummaryResponse;
import com.spotify.spotify.entity.Playlist;
import com.spotify.spotify.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {SongMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)//BỎ qua các trường 0 update
public interface PlaylistMapper {
    @Mapping(target = "coverUrl", ignore = true)
    @Mapping(target = "songs", ignore = true)//Tạo playlist mới để trống
    Playlist toPlaylistRequest(PlaylistRequest request);

    @Mapping(target = "songCount", expression = "java(playlist.getSongs().size())")
    @Mapping(target = "user", expression = "java(toUserSummary(playlist.getUser()))")
    @Mapping(source = "createdAt", target = "createdAt")
    PlaylistResponse toPlaylistResponse(Playlist playlist);

    default UserSummaryResponse toUserSummary(User user){
        if(user == null) return  null;
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Mapping(target = "coverUrl", ignore = true)
    @Mapping(target = "songs", ignore = true) //Không cập nhật bài hát trong hàm này
    @Mapping(target = "user", ignore = true) //không cập nhật chủ playlist
    void updatePlaylist(@MappingTarget Playlist playlist, PlaylistUpdateRequest request);
}
