package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.PlaylistRequest;
import com.spotify.spotify.dto.request.PlaylistUpdateRequest;
import com.spotify.spotify.dto.response.PlaylistResponse;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.Playlist;
import com.spotify.spotify.entity.Song;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-25T21:56:37+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class PlaylistMapperImpl implements PlaylistMapper {

    @Autowired
    private SongMapper songMapper;

    @Override
    public Playlist toPlaylistRequest(PlaylistRequest request) {
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
        playlistResponse.songs( songSetToSongResponseList( playlist.getSongs() ) );

        playlistResponse.songCount( playlist.getSongs().size() );
        playlistResponse.user( toUserSummary(playlist.getUser()) );

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

    protected List<SongResponse> songSetToSongResponseList(Set<Song> set) {
        if ( set == null ) {
            return null;
        }

        List<SongResponse> list = new ArrayList<SongResponse>( set.size() );
        for ( Song song : set ) {
            list.add( songMapper.toSongResponse( song ) );
        }

        return list;
    }
}
