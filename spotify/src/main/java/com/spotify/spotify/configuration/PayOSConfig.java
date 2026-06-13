package com.spotify.spotify.configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payos")//hứng payos từ yaml
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayOSConfig {
    String clientId;
    String apiKey;
    String checksumKey;
}
