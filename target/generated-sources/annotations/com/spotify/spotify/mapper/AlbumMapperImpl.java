package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.AlbumRequest;
import com.spotify.spotify.dto.response.AlbumResponse;
import com.spotify.spotify.entity.Album;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-26T19:04:08+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class AlbumMapperImpl implements AlbumMapper {

    @Override
    public Album toAlbum(AlbumRequest request) {
        if ( request == null ) {
            return null;
        }

        Album.AlbumBuilder album = Album.builder();

        album.name( request.getName() );
        album.description( request.getDescription() );

        return album.build();
    }

    @Override
    public AlbumResponse toAlbumResponse(Album album) {
        if ( album == null ) {
            return null;
        }

        AlbumResponse.AlbumResponseBuilder albumResponse = AlbumResponse.builder();

        albumResponse.avatarUrl( album.getAlbumUrl() );
        albumResponse.id( album.getId() );
        albumResponse.name( album.getName() );
        albumResponse.description( album.getDescription() );

        return albumResponse.build();
    }

    @Override
    public AlbumResponse toAlbumSummary(Album album) {
        if ( album == null ) {
            return null;
        }

        AlbumResponse.AlbumResponseBuilder albumResponse = AlbumResponse.builder();

        albumResponse.id( album.getId() );
        albumResponse.name( album.getName() );
        albumResponse.description( album.getDescription() );

        return albumResponse.build();
    }

    @Override
    public void updateAlbum(Album album, AlbumRequest request) {
        if ( request == null ) {
            return;
        }

        album.setName( request.getName() );
        album.setDescription( request.getDescription() );
    }
}
