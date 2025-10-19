package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.AlbumRequest;
import com.spotify.spotify.dto.response.AlbumResponse;
import com.spotify.spotify.dto.response.SongResponse;
import com.spotify.spotify.entity.Album;
import com.spotify.spotify.entity.Song;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-19T09:42:18+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class AlbumMapperImpl implements AlbumMapper {

    @Autowired
    private SongMapper songMapper;

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
        albumResponse.songs( songSetToSongResponseSet( album.getSongs() ) );

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

    protected Set<SongResponse> songSetToSongResponseSet(Set<Song> set) {
        if ( set == null ) {
            return null;
        }

        Set<SongResponse> set1 = new LinkedHashSet<SongResponse>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( Song song : set ) {
            set1.add( songMapper.toSongResponse( song ) );
        }

        return set1;
    }
}
