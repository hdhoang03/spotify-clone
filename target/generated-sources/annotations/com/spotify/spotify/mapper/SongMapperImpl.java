package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.SongRequest;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.Album;
import com.spotify.spotify.entity.Category;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-19T09:42:18+0700",
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
        songResponse.albumName( songAlbumName( song ) );
        songResponse.albumId( songAlbumId( song ) );
        songResponse.genre( songCategoryName( song ) );
        songResponse.id( song.getId() );
        songResponse.title( song.getTitle() );
        songResponse.artist( map( song.getArtist() ) );
        songResponse.coverUrl( song.getCoverUrl() );
        songResponse.audioUrl( song.getAudioUrl() );
        songResponse.createdAt( song.getCreatedAt() );

        return songResponse.build();
    }

    @Override
    public void updateSong(Song song, SongRequest request) {
        if ( request == null ) {
            return;
        }

        song.setTitle( request.getTitle() );
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

    private String songAlbumName(Song song) {
        if ( song == null ) {
            return null;
        }
        Album album = song.getAlbum();
        if ( album == null ) {
            return null;
        }
        String name = album.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String songAlbumId(Song song) {
        if ( song == null ) {
            return null;
        }
        Album album = song.getAlbum();
        if ( album == null ) {
            return null;
        }
        String id = album.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String songCategoryName(Song song) {
        if ( song == null ) {
            return null;
        }
        Category category = song.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
