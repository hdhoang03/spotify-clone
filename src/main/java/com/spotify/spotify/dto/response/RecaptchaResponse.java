package com.spotify.spotify.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecaptchaResponse {
    boolean success;
    double score;
    String action;
    @JsonProperty("challenge_ts")
    String challengeTs;
    String hostname;
    @JsonProperty("error-codes")
    List<String> errorCodes;
}
