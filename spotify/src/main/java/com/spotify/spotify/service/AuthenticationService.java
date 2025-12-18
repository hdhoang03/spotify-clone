package com.spotify.spotify.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.spotify.spotify.constaint.PredefinedRole;
import com.spotify.spotify.dto.request.*;
import com.spotify.spotify.dto.response.AuthenticationResponse;
import com.spotify.spotify.dto.response.IntrospectResponse;
import com.spotify.spotify.entity.InvalidatedToken;
import com.spotify.spotify.entity.Role;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.UserMapper;
import com.spotify.spotify.repository.InvalidTokenRepository;
import com.spotify.spotify.repository.RoleRepository;
import com.spotify.spotify.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidTokenRepository invalidTokenRepository;
    RedisTemplate<String, Object> redisTemplate;
    EmailService emailService;
    UserMapper userMapper;
    RoleRepository roleRepository;
    ObjectMapper objectMapper;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public void register(UserCreationRequest request){
        if (userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_ALREADY_EXIST);
        }
        if (userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        //Tạo key trong redis "REG_OTP:example@gmail.com"
        String redisKey = "REG_OTP:" + request.getEmail();

        Map<String, Object> data = new HashMap<>();
        data.put("request", request);
        data.put("otp", otp);

        redisTemplate.opsForValue().set(redisKey, data, 5, TimeUnit.MINUTES);
        emailService.sendHtmlEmail(request.getEmail(), "Account verification", "email-otp", Map.of("name", request.getUsername(), "otp", otp));
    }

    public AuthenticationResponse verifyAndCreateUser(String email, String otpCode){
        String redisKey = "REG_OTP:" + email;

        Map<String, Object> data = (Map<String, Object>) redisTemplate.opsForValue().get(redisKey);
        if (data == null){
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        String cachedOtp = (String) data.get("otp");
        if (!cachedOtp.equals(otpCode)){
            throw new AppException(ErrorCode.OTP_INVALID);
        }
        UserCreationRequest request = objectMapper.convertValue(data.get("request"), UserCreationRequest.class);

        User user = userMapper.toUser(request);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);
        user.setEnabled(true);

        userRepository.save(user);

        redisTemplate.delete(redisKey);

        return AuthenticationResponse.builder()
                .token(generateToken(user))
                .authenticated(true)
                .build();
    }

    public void changePassword(ChangePasswordRequest request){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void sendForgotPasswordOtp(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        String redisKey = "RESET_PW_OTP:" + email;

        redisTemplate.opsForValue().set(redisKey, otp, 5, TimeUnit.MINUTES);

        emailService.sendHtmlEmail(user.getEmail(), "Reset password OTP", "email-otp",
                Map.of("name", user.getUsername(), "otp", otp));
    }

    public void resetPassword(ResetPasswordRequest request){
        String redisKey = "RESET_PW_OTP:" + request.getEmail();
        String cachedOtp = (String) redisTemplate.opsForValue().get(redisKey);//Lấy trong Redis
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        if (cachedOtp == null){
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        if (!cachedOtp.equals(request.getOtpCode())){
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete(redisKey);
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e){
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                .toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);
        if (!verified && expiryTime.after(new Date()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private String generateToken(User user){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("hoang.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload); //Lấy 2 params ở trên

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e){
            log.error("Can't generate token", e);
            throw new RuntimeException(e);
        }
    }

    public String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });
        }
        return stringJoiner.toString();
    }

    public AuthenticationResponse authenticationResponse(AuthenticationRequest request){
        var user = userRepository.findByUsername(request.getUsername()).
                orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        if(!user.getEnabled()){
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword()); //So sánh password

        if(!authenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public void logout(LogoutRequest request) throws JOSEException, ParseException{
        try {
            var signToken = verifyToken(request.getToken(), true);
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiredToken = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .expiryTime(expiredToken)
                    .id(jit)
                    .build();
            invalidTokenRepository.save(invalidatedToken);
        } catch (AppException e){
            log.info("Token already expired.");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws JOSEException, ParseException{
        var signJWT = verifyToken(request.getToken(), true);
        var jit = signJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .expiryTime(expiryTime)
                .id(jit)
                .build();

        invalidTokenRepository.save(invalidatedToken);
        var username = signJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }
}
