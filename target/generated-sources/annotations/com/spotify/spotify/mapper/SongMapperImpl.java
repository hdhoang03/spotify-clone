package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.SongRequest;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-06T10:42:29+0700",
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
        song.artist( request.getArtist() );
        song.album( request.getAlbum() );
        song.genre( request.getGenre() );

        return song.build();
    }

    @Override
    public SongResponse toSongResponse(Song song) {
        if ( song == null ) {
            return null;
        }

        SongResponse.SongResponseBuilder songResponse = SongResponse.builder();

        songResponse.uploadedBy( songUploadedByUsername( song ) );
        songResponse.id( song.getId() );
        songResponse.title( song.getTitle() );
        songResponse.artist( song.getArtist() );
        songResponse.album( song.getAlbum() );
        songResponse.genre( song.getGenre() );
        songResponse.coverUrl( song.getCoverUrl() );
        songResponse.audioUrl( song.getAudioUrl() );
        songResponse.createAt( song.getCreateAt() );

        return songResponse.build();
    }

    @Override
    public void updateSong(Song song, SongRequest request) {
        if ( request == null ) {
            return;
        }

        song.setTitle( request.getTitle() );
        song.setArtist( request.getArtist() );
        song.setAlbum( request.getAlbum() );
        song.setGenre( request.getGenre() );
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
