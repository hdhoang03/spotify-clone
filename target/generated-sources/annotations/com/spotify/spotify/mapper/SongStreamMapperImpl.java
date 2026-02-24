package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.SongStreamRequest;
import com.spotify.spotify.dto.response.SongStreamResponse;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.entity.SongStream;
import com.spotify.spotify.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-26T19:04:08+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class SongStreamMapperImpl implements SongStreamMapper {

    @Override
    public SongStream toSongStream(SongStreamRequest request) {
        if ( request == null ) {
            return null;
        }

        SongStream.SongStreamBuilder songStream = SongStream.builder();

        songStream.duration( request.getDuration() );

        return songStream.build();
    }

    @Override
    public SongStreamResponse toSongStreamResponse(SongStream songStream) {
        if ( songStream == null ) {
            return null;
        }

        SongStreamResponse.SongStreamResponseBuilder songStreamResponse = SongStreamResponse.builder();

        songStreamResponse.streamId( songStream.getId() );
        songStreamResponse.userId( songStreamUserId( songStream ) );
        songStreamResponse.songId( songStreamSongId( songStream ) );
        songStreamResponse.createdAt( songStream.getCreatedAt() );

        return songStreamResponse.build();
    }

    private String songStreamUserId(SongStream songStream) {
        if ( songStream == null ) {
            return null;
        }
        User user = songStream.getUser();
        if ( user == null ) {
            return null;
        }
        String id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String songStreamSongId(SongStream songStream) {
        if ( songStream == null ) {
            return null;
        }
        Song song = songStream.getSong();
        if ( song == null ) {
            return null;
        }
        String id = song.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
