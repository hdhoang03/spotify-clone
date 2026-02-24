package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.ArtistRequest;
import com.spotify.spotify.dto.response.ArtistFollowResponse;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.entity.Artist;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-26T19:04:08+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class ArtistMapperImpl implements ArtistMapper {

    @Override
    public Artist toArtist(ArtistRequest request) {
        if ( request == null ) {
            return null;
        }

        Artist.ArtistBuilder artist = Artist.builder();

        artist.description( request.getDescription() );
        artist.name( request.getName() );

        return artist.build();
    }

    @Override
    public ArtistResponse toArtistResponse(Artist artist) {
        if ( artist == null ) {
            return null;
        }

        ArtistResponse.ArtistResponseBuilder artistResponse = ArtistResponse.builder();

        artistResponse.id( artist.getId() );
        artistResponse.name( artist.getName() );
        artistResponse.description( artist.getDescription() );
        artistResponse.avatarUrl( artist.getAvatarUrl() );

        return artistResponse.build();
    }

    @Override
    public ArtistFollowResponse toArtistFollowResponse(Artist artist) {
        if ( artist == null ) {
            return null;
        }

        ArtistFollowResponse.ArtistFollowResponseBuilder artistFollowResponse = ArtistFollowResponse.builder();

        artistFollowResponse.id( artist.getId() );
        artistFollowResponse.name( artist.getName() );
        artistFollowResponse.avatarUrl( artist.getAvatarUrl() );

        return artistFollowResponse.build();
    }

    @Override
    public void updateArtist(Artist artist, ArtistRequest request) {
        if ( request == null ) {
            return;
        }

        artist.setDescription( request.getDescription() );
        artist.setName( request.getName() );
    }
}
