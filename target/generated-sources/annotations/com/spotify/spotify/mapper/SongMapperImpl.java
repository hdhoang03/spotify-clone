package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.SongRequest;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-13T09:48:05+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class SongMapperImpl implements SongMapper {

    @Override
    public Song toSong(SongRequest request) {
        if ( request == null ) {
            return null;
        }

        Song.SongBuilder song = Song.builder();

        song.title( request.getTitle() );

        return song.build();
    }

    @Override
    public SongResponse toSongResponse(Song song) {
        if ( song == null ) {
            return null;
        }

        SongResponse.SongResponseBuilder songResponse = SongResponse.builder();

        songResponse.uploadedBy( songUploadedByUsername( song ) );
        songResponse.albumName( mapAlbumName( song.getAlbum() ) );
        songResponse.albumId( mapAlbumId( song.getAlbum() ) );
        songResponse.category( mapCategoryName( song.getCategory() ) );
        songResponse.artist( mapArtistName( song.getArtist() ) );
        songResponse.id( song.getId() );
        songResponse.title( song.getTitle() );
        songResponse.coverUrl( song.getCoverUrl() );
        songResponse.audioUrl( song.getAudioUrl() );
        songResponse.duration( song.getDuration() );
        songResponse.createdAt( song.getCreatedAt() );

        return songResponse.build();
    }

    @Override
    public void updateSong(Song song, SongRequest request) {
        if ( request == null ) {
            return;
        }

        if ( request.getTitle() != null ) {
            song.setTitle( request.getTitle() );
        }
    }

    private String songUploadedByUsername(Song song) {
        if ( song == null ) {
            return null;
        }
        User uploadedBy = song.getUploadedBy();
        if ( uploadedBy == null ) {
            return null;
        }
        String username = uploadedBy.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }
}
