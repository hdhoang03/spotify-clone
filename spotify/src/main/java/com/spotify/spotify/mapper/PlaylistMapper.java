package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.PlaylistRequest;
import com.spotify.spotify.dto.request.PlaylistUpdateRequest;
import com.spotify.spotify.dto.response.PlaylistResponse;
import com.spotify.spotify.dto.response.UserSummaryResponse;
import com.spotify.spotify.entity.Playlist;
import com.spotify.spotify.entity.User;
import jdk.jfr.Name;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = {SongMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)//BỎ qua các trường 0 update
public interface PlaylistMapper {
    @Mapping(target = "coverUrl", ignore = true)
    @Mapping(target = "songs", ignore = true)//Tạo playlist mới để trống
    Playlist toPlaylist(PlaylistRequest request);

    @Named("toPlaylistSummary")
//    @Mapping(target = "songs", ignore = true)
    @Mapping(target = "songCount", expression = "java(playlist.getSongs() != null ? playlist.getSongs().size() : 0)")
    @Mapping(target = "user", expression = "java(toUserSummary(playlist.getUser()))")
    @Mapping(source = "createdAt", target = "createdAt")
    PlaylistResponse toPlaylistResponse(Playlist playlist);

    @Name("toPlaylistDetail")
    @Mapping(target = "user", expression = "java(toUserSummary(playlist.getUser()))")
    @Mapping(target = "songCount", expression = "java(playlist.getSongs() != null ? playlist.getSongs().size() : 0)")
    PlaylistResponse toPlaylistDetailResponse(Playlist playlist);

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
//getSongs().size() vẫn có thể gây Lazy Exception nếu không FETCH