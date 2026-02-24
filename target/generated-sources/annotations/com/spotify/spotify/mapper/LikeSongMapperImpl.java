package com.spotify.spotify.mapper;

import com.spotify.spotify.dto.response.LikeSongResponse;
import com.spotify.spotify.entity.LikeSong;
import com.spotify.spotify.entity.Song;
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
    date = "2025-12-26T19:04:08+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class LikeSongMapperImpl implements LikeSongMapper {

    private final DatatypeFactory datatypeFactory;

    public LikeSongMapperImpl() {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        }
        catch ( DatatypeConfigurationException ex ) {
            throw new RuntimeException( ex );
        }
    }

    @Override
    public LikeSongResponse toLikeSongResponse(LikeSong likeSong) {
        if ( likeSong == null ) {
            return null;
        }

        LikeSongResponse.LikeSongResponseBuilder likeSongResponse = LikeSongResponse.builder();

        likeSongResponse.likeId( likeSong.getId() );
        likeSongResponse.songId( likeSongSongId( likeSong ) );
        likeSongResponse.userId( likeSongUserId( likeSong ) );
        likeSongResponse.likedAt( xmlGregorianCalendarToLocalDate( localDateTimeToXmlGregorianCalendar( likeSong.getLikedAt() ) ) );

        return likeSongResponse.build();
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

    private String likeSongSongId(LikeSong likeSong) {
        if ( likeSong == null ) {
            return null;
        }
        Song song = likeSong.getSong();
        if ( song == null ) {
            return null;
        }
        String id = song.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String likeSongUserId(LikeSong likeSong) {
        if ( likeSong == null ) {
            return null;
        }
        User user = likeSong.getUser();
        if ( user == null ) {
            return null;
        }
        String id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
