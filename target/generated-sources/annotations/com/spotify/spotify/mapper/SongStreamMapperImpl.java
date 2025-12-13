package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.request.SongStreamRequest;
import com.spotify.spotify.dto.response.SongStreamResponse;
import com.spotify.spotify.entity.Song;
import com.spotify.spotify.entity.SongStream;
import com.spotify.spotify.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import javax.annotation.processing.Generated;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-13T09:48:05+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class SongStreamMapperImpl implements SongStreamMapper {

    private final DatatypeFactory datatypeFactory;

    public SongStreamMapperImpl() {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        }
        catch ( DatatypeConfigurationException ex ) {
            throw new RuntimeException( ex );
        }
    }

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
        songStreamResponse.createdAt( xmlGregorianCalendarToLocalDate( localDateTimeToXmlGregorianCalendar( songStream.getCreatedAt() ) ) );

        return songStreamResponse.build();
    }

    private XMLGregorianCalendar localDateTimeToXmlGregorianCalendar( LocalDateTime localDateTime ) {
        if ( localDateTime == null ) {
            return null;
        }

        return datatypeFactory.newXMLGregorianCalendar(
            localDateTime.getYear(),
            localDateTime.getMonthValue(),
            localDateTime.getDayOfMonth(),
            localDateTime.getHour(),
            localDateTime.getMinute(),
            localDateTime.getSecond(),
            localDateTime.get( ChronoField.MILLI_OF_SECOND ),
            DatatypeConstants.FIELD_UNDEFINED );
    }

    private static LocalDate xmlGregorianCalendarToLocalDate( XMLGregorianCalendar xcal ) {
        if ( xcal == null ) {
            return null;
        }

        return LocalDate.of( xcal.getYear(), xcal.getMonth(), xcal.getDay() );
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
