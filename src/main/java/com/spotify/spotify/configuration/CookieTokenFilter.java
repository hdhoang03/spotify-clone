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
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Filter đọc JWT từ httpOnly cookie "access_token" và inject vào
 * Authorization header để BearerTokenAuthenticationFilter xử lý bình thường.
 *
 * <p>Thứ tự ưu tiên:
 * <ol>
 *   <li>Nếu request là public path → bỏ qua, KHÔNG inject token (tránh 401 khi cookie hết hạn).</li>
 *   <li>Nếu request đã có header "Authorization" hợp lệ → bỏ qua (ưu tiên SSE / query param).</li>
 *   <li>Nếu có cookie "access_token" → inject "Authorization: Bearer {token}".</li>
 *   <li>Không có gì → để chain xử lý (sẽ bị 401 nếu endpoint cần auth).</li>
 * </ol>
 *
 * <p>Tại sao inject vào header thay vì đọc SecurityContext trực tiếp?
 * → Tái dụng toàn bộ pipeline JWT validation của Spring Security OAuth2 Resource Server
 *   (CustomJwtDecoder, JwtAuthenticationConverter, blacklist check…) mà không cần viết lại.
 *
 * <p>Tại sao cần PUBLIC_PATHS?
 * → Nếu cookie access_token tồn tại nhưng hết hạn, và ta vẫn inject vào header cho
 *   public endpoint, thì CustomJwtDecoder sẽ gọi introspect() → isValid=false →
 *   JwtException → JwtAuthenticationEntryPoint trả 401, dù endpoint đã được permitAll().
 *   Bằng cách skip inject cho public path, request đi thẳng qua permitAll mà không bị chặn.
 */
@Component
public class CookieTokenFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "access_token";
    private static final String AUTH_HEADER  = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Path luôn public bất kể method (auth, webhook…).
     * Dùng String.contains() để match cả có/không có context-path prefix.
     */
    private static final List<String> ALWAYS_PUBLIC_PATHS = List.of(
            "/auth/token", "/auth/register", "/auth/verify",
            "/auth/forgot-password", "/auth/reset-password",
            "/auth/resend-otp", "/auth/outbound/authentication",
            "/auth/refresh", "/auth/logout", "/auth/introspect",
            "/api/payment/webhook",
            "/api/test-kafka"
    );

    /**
     * Path chỉ public với GET — POST/PUT/DELETE tới cùng base path cần auth.
     * Ví dụ: GET /categories (list) là public, nhưng POST /categories/create cần ADMIN.
     * Ví dụ: GET /lyrics/{id}/get là public, nhưng POST /lyrics/{id} cần ADMIN.
     *
     * ⚠️ Vì dùng String.contains(), pattern phải đủ cụ thể để tránh match nhầm.
     */
    private static final List<String> GET_ONLY_PUBLIC_PATHS = List.of(
            "/song/allSongs", "/song/search",
            "/like/top",
            "/stream/top", "/stream/count/", "/stream/range",
            "/albums/all",
            "/artist/all",
            "/categories",
            "/search", "/advanced-search"
    );

    /**
     * Suffix path chỉ public với GET (dùng endsWith thay vì contains).
     * Ví dụ: GET /lyrics/{songId}/get — public, nhưng POST /lyrics/{songId} — admin.
     */
    private static final List<String> GET_ONLY_SUFFIX_PATHS = List.of(
            "/get"
    );

    private boolean isPublicPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 1. Path luôn public (auth, webhook) — bỏ qua inject token
        if (ALWAYS_PUBLIC_PATHS.stream().anyMatch(uri::contains)) {
            return true;
        }

        // 2. Path chỉ public với GET — admin mutation cần token
        if ("GET".equalsIgnoreCase(method)) {
            if (GET_ONLY_PUBLIC_PATHS.stream().anyMatch(uri::contains)) {
                return true;
            }
            if (GET_ONLY_SUFFIX_PATHS.stream().anyMatch(uri::endsWith)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Bỏ qua các endpoint auth thủ công (kể cả khi token đã hết hạn)
        if (requestURI.endsWith("/auth/refresh") || requestURI.endsWith("/auth/logout") || requestURI.endsWith("/auth/token")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Public endpoint: KHÔNG inject cookie vào header.
        // Nếu inject cookie hết hạn → CustomJwtDecoder throw JwtException → 401 dù permitAll.
        // Guest (không có cookie) cũng đi qua đây bình thường.
        if (isPublicPath(request)) {
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
