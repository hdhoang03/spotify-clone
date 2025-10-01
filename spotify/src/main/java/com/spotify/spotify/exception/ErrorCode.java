package com.spotify.spotify.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    INVALID_QUANTITY(1001, "Invalid quantity", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCESS_DENIED(403, "Access denied", HttpStatus.FORBIDDEN),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error.", HttpStatus.BAD_REQUEST),
    USER_BANNED_FROM_POSTING(0001,"User is banned form posting", HttpStatus.BAD_REQUEST),
    USER_BANNED_FROM_COMMENTING(0002, "User is banned from commenting", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1003, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    USER_NOT_EXISTED(1004, "User not existed", HttpStatus.NOT_FOUND),
    ACCOUNT_DISABLED(1005, "Account has been disabled", HttpStatus.NOT_ACCEPTABLE),
    EMAIL_EXISTED(1006, "Email existed", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(1007, "File upload filed", HttpStatus.BAD_REQUEST),
    SONG_NOT_FOUND(1008, "Song not found", HttpStatus.NOT_FOUND),
    ARTIST_NOT_FOUND(1009, "Artist not found", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS(1010, "Category already exists", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(1011, "Category not found", HttpStatus.NOT_FOUND),
    ALBUM_ALREADY_EXISTS(1012, "Album already exists", HttpStatus.NOT_FOUND),
    ALBUM_NOT_FOUND(1013, "Album not found", HttpStatus.NOT_FOUND),

    ;
    int code;
    String message;
    HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode){
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}