package com.spotify.spotify.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filter đọc JWT từ httpOnly cookie "access_token" và inject vào
 * Authorization header để BearerTokenAuthenticationFilter xử lý bình thường.
 *
 * <p>Thứ tự ưu tiên:
 * <ol>
 *   <li>Nếu request đã có header "Authorization" → bỏ qua (ưu tiên cho SSE / query param).</li>
 *   <li>Nếu có cookie "access_token" → inject "Authorization: Bearer {token}".</li>
 *   <li>Không có gì → để chain xử lý (sẽ bị 401 nếu endpoint cần auth).</li>
 * </ol>
 *
 * <p>Tại sao inject vào header thay vì đọc SecurityContext trực tiếp?
 * → Tái dụng toàn bộ pipeline JWT validation của Spring Security OAuth2 Resource Server
 *   (CustomJwtDecoder, JwtAuthenticationConverter, blacklist check…) mà không cần viết lại.
 */
@Component
public class CookieTokenFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "access_token";
    private static final String AUTH_HEADER  = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Bỏ qua các endpoint cần xử lý token thủ công (kể cả khi token đã hết hạn)
        String requestURI = request.getRequestURI();
        if (requestURI.endsWith("/auth/refresh") || requestURI.endsWith("/auth/logout") || requestURI.endsWith("/auth/token")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Ưu tiên đọc header "Authorization" nếu client có gửi lên hợp lệ
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String tokenValue = authHeader.substring(BEARER_PREFIX.length()).trim();
            // Chỉ bỏ qua cookie nếu tokenValue thực sự có giá trị (không phải null, undefined, rỗng)
            if (!tokenValue.equals("null") && !tokenValue.equals("undefined") && !tokenValue.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Nếu không có header (hoặc header invalid như Bearer null), thử lấy token từ cookie "access_token"
        String token = extractTokenFromCookie(request);
        if (token != null && !token.isBlank()) {
            filterChain.doFilter(new BearerTokenRequestWrapper(request, token), response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * HttpServletRequestWrapper thêm header "Authorization: Bearer {token}"
     * vào request mà không thay đổi bất kỳ header nào khác.
     */
    private static class BearerTokenRequestWrapper extends HttpServletRequestWrapper {

        private final String bearerToken;

        BearerTokenRequestWrapper(HttpServletRequest request, String token) {
            super(request);
            this.bearerToken = BEARER_PREFIX + token;
        }

        @Override
        public String getHeader(String name) {
            if (AUTH_HEADER.equalsIgnoreCase(name)) {
                return bearerToken;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (AUTH_HEADER.equalsIgnoreCase(name)) {
                return Collections.enumeration(List.of(bearerToken));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            if (!names.contains(AUTH_HEADER)) {
                names.add(AUTH_HEADER);
            }
            return Collections.enumeration(names);
        }
    }
}
