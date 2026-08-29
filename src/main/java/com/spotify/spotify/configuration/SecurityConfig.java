package com.spotify.spotify.configuration;

import com.spotify.spotify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Autowired
    private CookieTokenFilter cookieTokenFilter;

    private final String[] PUBLIC_ENDPOINTS = {
            "/auth/token", "/auth/introspect", "/auth/logout", "/auth/refresh", "/auth/register", "/auth/verify", "/auth/forgot-password", "/auth/reset-password", "/auth/resend-otp",
            "/categories","/categories/**", "/categories/search",
            "/albums", "/albums/**", "/albums/all",
            "/artist", "/artist/**",
            "/song/**", "/song/allSongs", "/song/search", "/advanced-search",
            "/stream/count/**", "/stream/range/**",
            "/like/top",
            "/search",
            "/api/test-kafka",
            "/stream/**",
            "/auth/outbound/authentication",
            "/lyrics/{songId}/get",
            "/api/payment/webhook",
            "/user/{userId}/profile",
            "/user/{userId}/followers",
            "/user/{userId}/following-users"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity.authorizeHttpRequests(request ->
                request.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer-> jwtConfigurer.decoder(customJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .bearerTokenResolver(bearerTokenResolver())
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );
        // ── CSRF Protection: Tắt đi vì đây là Stateless REST API dùng JWT + httpOnly cookie ──
        // Lý do an toàn:
        //   1. JWT được ký (signed), attacker không thể giả mạo được dù gửi được cookie.
        //   2. CORS đã cấu hình chặt chẽ (chỉ cho phép localhost:5173 và Vercel domain).
        //   3. Giữ CSRF làm phức tạp hóa dev và gây lỗi 401 không rõ lý do trên browser.
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        httpSecurity.cors(Customizer.withDefaults());
        // CookieTokenFilter chạy trước BearerTokenAuthenticationFilter:
        // đọc cookie access_token → inject Authorization header → Spring xử lý bình thường
        httpSecurity.addFilterBefore(cookieTokenFilter, BearerTokenAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        jwtAuthenticationConverter.setPrincipalClaimName("sub");
        return jwtAuthenticationConverter;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }

//    @Bean
//    public CorsFilter corsFilter(){
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//
//        corsConfiguration.addAllowedMethod("*");
//        corsConfiguration.addAllowedHeader("*");
//        corsConfiguration.addAllowedOrigin("*");
////        corsConfiguration.setAllowCredentials(true); //Khi sử dụng cái này phải nêu rõ api
//
//        source.registerCorsConfiguration("/**", corsConfiguration);
//        return new CorsFilter(source);
//    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // Cấu hình cụ thể origin để bảo mật hơn thay vì dùng "*"
        corsConfiguration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://spotify-clone-fe-chi.vercel.app"
        ));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true); // Bắt buộc để browser gửi httpOnly cookie cross-origin

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver(){
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        resolver.setAllowUriQueryParameter(true); //lấy token từ ?access_token=
        return resolver;
    }

    /*
    * override cách Spring lấy Bearer token
    * Cho phép đọc token từ URL qua query param
    * phục vụ sse/eventSource vì browser không cho set authorization header
    * */
}
