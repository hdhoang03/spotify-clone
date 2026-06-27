package com.spotify.spotify.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class PremiumPlanResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    LocalDateTime premiumExpiryDate;
    Long daysRemaining;
}
