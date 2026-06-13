package com.spotify.spotify.dto.request;

import com.spotify.spotify.constaint.PlanType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequest {
    long orderCode;
    int amount;
    String description;
    String cancelUrl;
    String returnUrl;
    String signature;
    PlanType planType;
}
