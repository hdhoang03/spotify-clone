package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.ArtistRequest;
import com.spotify.spotify.dto.response.ArtistResponse;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.Artist;
import com.spotify.spotify.entity.Song;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-11T10:11:52+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class ArtistMapperImpl implements ArtistMapper {

    @Autowired
    private SongMapper songMapper;

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
        artistResponse.songs( songSetToSongResponseList( artist.getSongs() ) );

        return artistResponse.build();
    }

    @Override
    public void updateArtist(Artist artist, ArtistRequest request) {
        if ( request == null ) {
            return;
        }

        artist.setDescription( request.getDescription() );
        artist.setName( request.getName() );
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
