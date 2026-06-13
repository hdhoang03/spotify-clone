package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LyricResponse implements Serializable {
    static final long serialVersionUID = 1L; //định danh phiên bản của class

    boolean isInstrumental;
    List<LyricLine> content;
}
