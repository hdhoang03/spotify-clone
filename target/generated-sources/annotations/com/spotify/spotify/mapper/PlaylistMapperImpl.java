package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.PlaylistRequest;
import com.spotify.spotify.dto.request.PlaylistUpdateRequest;
import com.spotify.spotify.dto.response.PlaylistResponse;
import com.spotify.spotify.entity.Playlist;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-17T00:32:08+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class PlaylistMapperImpl implements PlaylistMapper {

    @Override
    public Playlist toPlaylist(PlaylistRequest request) {
        if ( request == null ) {
            return null;
        }

        Playlist.PlaylistBuilder playlist = Playlist.builder();

        playlist.name( request.getName() );
        playlist.description( request.getDescription() );
        playlist.isPublic( request.getIsPublic() );

        return playlist.build();
    }

    @Override
    public PlaylistResponse toPlaylistResponse(Playlist playlist) {
        if ( playlist == null ) {
            return null;
        }

        PlaylistResponse.PlaylistResponseBuilder playlistResponse = PlaylistResponse.builder();

        playlistResponse.createdAt( playlist.getCreatedAt() );
        playlistResponse.id( playlist.getId() );
        playlistResponse.name( playlist.getName() );
        playlistResponse.description( playlist.getDescription() );
        playlistResponse.coverUrl( playlist.getCoverUrl() );
        playlistResponse.isPublic( playlist.getIsPublic() );

        playlistResponse.songCount( playlist.getSongs() != null ? playlist.getSongs().size() : 0 );
        playlistResponse.user( toUserSummary(playlist.getUser()) );

        return playlistResponse.build();
    }

    @Override
    public PlaylistResponse toPlaylistDetailResponse(Playlist playlist) {
        if ( playlist == null ) {
            return null;
        }

        PlaylistResponse.PlaylistResponseBuilder playlistResponse = PlaylistResponse.builder();

        playlistResponse.id( playlist.getId() );
        playlistResponse.name( playlist.getName() );
        playlistResponse.description( playlist.getDescription() );
        playlistResponse.coverUrl( playlist.getCoverUrl() );
        playlistResponse.isPublic( playlist.getIsPublic() );
        playlistResponse.createdAt( playlist.getCreatedAt() );

        playlistResponse.user( toUserSummary(playlist.getUser()) );
        playlistResponse.songCount( playlist.getSongs() != null ? playlist.getSongs().size() : 0 );

        return playlistResponse.build();
    }

    @Override
    public void updatePlaylist(Playlist playlist, PlaylistUpdateRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            playlist.setName( request.getName() );
        }
        if ( request.getDescription() != null ) {
            playlist.setDescription( request.getDescription() );
        }
        if ( request.getIsPublic() != null ) {
            playlist.setIsPublic( request.getIsPublic() );
        }
    }
}
