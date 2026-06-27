package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.spotify.configuration.RabbitMQConfig;
import com.spotify.spotify.constaint.NotificationTargetType;
import com.spotify.spotify.constaint.PredefinedRole;
import com.spotify.spotify.dto.event.SseNotificationEvent;
import com.spotify.spotify.dto.request.*;
import com.spotify.spotify.dto.response.*;
import com.spotify.spotify.entity.Role;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.entity.UserBlock;
import com.spotify.spotify.entity.UserFollow;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
//import com.spotify.spotify.kafka.KafkaProducerService;
import com.spotify.spotify.mapper.UserMapper;
import com.spotify.spotify.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserMapper userMapper;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    PlaylistRepository playlistRepository;
    ArtistFollowRepository artistFollowRepository;
    UserFollowRepository userFollowRepository;
    Cloudinary cloudinary;
    EmailService emailService;
    UserBlockRepository userBlockRepository;
    RabbitMQProducerService rabbitMQProducerService;
    CloudinaryService cloudinaryService;
//    KafkaProducerService kafkaProducerService;
//    CaptchaService captchaService;

    @CacheEvict(value = "admin_users_page", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(UserCreationRequest request){
//        if (!captchaService.verifyCaptcha(request.getCaptchaToken())){
//            throw new AppException(ErrorCode.INVALID_CAPTCHA);
//        }
        if (userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_ALREADY_EXIST);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);
        user.setEnabled(true);

        try {
            user = userRepository.save(user);
//            playlistService.createDefaultPlaylist(user);
        } catch (DataIntegrityViolationException e){
            if (userRepository.existsByEmail(request.getEmail())){
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            }
        }
        return userMapper.toUserResponse(user);
    }

    @CacheEvict(value = {"user_profile", "my_info", "admin_users_page"}, allEntries = true)
    public UserResponse updateUser(UserUpdateRequest request, String userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found."));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @CacheEvict(value = {"user_profile", "my_info", "admin_users_page"}, allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.getUsername().equalsIgnoreCase("admin")){
            user.setEnabled(false);
            userRepository.save(user);
            log.info("Admin account disabled instead of deleted.");
            return;
        }

        user.getRoles().clear();
        userRepository.save(user);
        userRepository.deleteById(userId);
    }

    @Cacheable(value = "admin_users_page", key = "#keyword + '_' + #currentUserId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> searchUser(String keyword, String currentUserId, Pageable pageable){
        Page<UserResponse> page;
        if (keyword == null || keyword.trim().isEmpty()){
            page = userRepository.findAll(pageable)
                    .map(userMapper::toUserResponse);
        } else {
            page = userRepository.searchUsersMultiColumns(keyword.trim(), currentUserId, pageable)
                    .map(userMapper::toUserResponse);
        }
        return new com.spotify.spotify.dto.response.CustomPageImpl<>(page.getContent(), pageable, page.getTotalElements());
    }

    @Cacheable(value = "my_info", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public UserResponse getMyInfo(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @CacheEvict(value = "admin_users_page", allEntries = true)
    public void toggleUserStatus(String userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(PredefinedRole.ADMIN_ROLE));
        if (isAdmin){
            throw new AppException(ErrorCode.IS_ADMIN);
        }

        boolean currentStatus = user.getEnabled() != null ? user.getEnabled() : true;
        user.setEnabled(!currentStatus);

        userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "user_profile", allEntries = true)
    public Boolean togglePrivacy(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean newStatus = !user.getIsPublicProfile();
        user.setIsPublicProfile(newStatus);
        return newStatus;
    }

    @Cacheable(value = "user_profile", key = "(#userId == 'me' ? T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() : #userId) + '_viewer_' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public UserProfileResponse getUserProfile(String userId){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        User targetUser;

        // 1. Xác định đúng targetUser ngay từ đầu
        if ("me".equals(userId)) {
            targetUser = currentUser;
        } else {
            targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        }

        boolean isOwner = currentUser.getId().equals(targetUser.getId());

        // 2. Kiểm tra chặn (chỉ cần thiết khi xem profile người khác)
        if (!isOwner) {
            boolean isBlocked = userBlockRepository.existsBlockBetweenUsers(currentUser.getId(), targetUser.getId());
            if (isBlocked) {
                throw new AppException(ErrorCode.USER_NOT_EXISTED);
            }
        }

        // 3. Kiểm tra quyền riêng tư
        if (!isOwner && !targetUser.getIsPublicProfile()){
            throw new AppException(ErrorCode.USER_PROFILE_PRIVATE);
        }

        // 4. Query các thông số đếm thực tế
        long playlistCount = isOwner
                ? playlistRepository.countByUser_Id(targetUser.getId())
                : playlistRepository.countByUser_IdAndIsPublicTrue(targetUser.getId());

        long followedArtistsCount = artistFollowRepository.countByUserId(targetUser.getId());

        // ĐẾM ĐỘNG TRỰC TIẾP TỪ BẢNG FOLLOW ĐỂ ĐẢM BẢO CHÍNH XÁC 100%
        long realFollowerCount = userFollowRepository.countByFollowing_Id(targetUser.getId());
        long realFollowingCount = userFollowRepository.countByFollower_Id(targetUser.getId());

        // 5. Build Response
        UserProfileResponse response = userMapper.toUserProfileResponse(targetUser);
        response.setPlaylistCount(playlistCount);
        response.setFollowingArtistCount(followedArtistsCount);
        response.setFollowerCount(realFollowerCount); // Ghi đè lại số liệu chuẩn
        response.setFollowingCount(realFollowingCount); // Ghi đè lại số liệu chuẩn

        if (!isOwner){
            response.setIsFollowedByMe(userFollowRepository.existsByFollower_IdAndFollowing_Id(currentUser.getId(), targetUser.getId()));
        }

        return response;
    }

//    public UserProfileResponse getUserProfile(String userId){
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        User currentUser = userRepository.findByUsername(username)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//
//        User targetUser = userRepository.findByUsername(username)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//
//        if (currentUser != null && !currentUser.getId().equals(userId)) { //Kiểm tra chặn
//            boolean isBlocked = userBlockRepository.existsBlockBetweenUsers(currentUser.getId(), targetUser.getId());
//            if (isBlocked) {
//                // Giả vờ như user này không tồn tại (đúng chuẩn tàng hình)
//                throw new AppException(ErrorCode.USER_NOT_EXISTED);
//            }
//        }
//
////        User targetUser = userRepository.findById(userId)
////                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//
//        if ("me".equals(userId)) {
//            if (currentUser == null) throw new AppException(ErrorCode.UNAUTHENTICATED);
//            targetUser = currentUser;
//        } else {
//            targetUser = userRepository.findById(userId)
//                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//        }
//
//        boolean isOwner = currentUser != null && currentUser.getUsername().equals(targetUser.getUsername());
//        if (!isOwner && !targetUser.getIsPublicProfile()){
//            throw new AppException(ErrorCode.USER_PROFILE_PRIVATE);
//        }
//
//        long playlistCount = isOwner
//                ? playlistRepository.countByUser_Id(userId)
//                : playlistRepository.countByUser_IdAndIsPublicTrue(userId);
//
//        long followedArtistsCount = artistFollowRepository.countByUserId(userId);
//
//        UserProfileResponse response = userMapper.toUserProfileResponse(targetUser);
//        response.setPlaylistCount(playlistCount);
//        response.setFollowingArtistCount(followedArtistsCount);
//
//        if (currentUser != null & !isOwner){
//            response.setIsFollowedByMe(userFollowRepository.existsByFollower_IdAndFollowing_Id(currentUser.getId(), userId));
//        }
//        return response;
//    }

    @Transactional
    @CacheEvict(value = "user_profile", allEntries = true)
    public boolean toggleFollowUser(String targetUserId){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (currentUser.getId().equals(targetUserId)){
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Optional<UserFollow> existingFollow = userFollowRepository
                .findByFollower_IdAndFollowing_Id(currentUser.getId(), targetUser.getId());

        if (existingFollow.isPresent()){
            userFollowRepository.delete(existingFollow.get());
            currentUser.setFollowingCount(Math.max(0, currentUser.getFollowingCount() - 1));
            targetUser.setFollowerCount(Math.max(0, targetUser.getFollowerCount() - 1));

            userRepository.save(currentUser);
            userRepository.save(targetUser);
            return false;
        } else {
            UserFollow newFollow = UserFollow.builder()
                    .follower(currentUser)
                    .following(targetUser)
                    .build();
            userFollowRepository.save(newFollow);

            currentUser.setFollowingCount(currentUser.getFollowingCount() + 1);
            targetUser.setFollowerCount(targetUser.getFollowerCount() + 1);

            NotificationResponse payload = NotificationResponse.builder()
                    .type("NEW_FOLLOWER")
                    .title("New Follower")
                    .message(currentUser.getName() + " started follow you.")
                    .thumbnail(currentUser.getAvatarUrl())
                    .targetUrl("/profile/" + currentUser.getId())
                    .createdAt(LocalDateTime.now())
                    .build();

            SseNotificationEvent event = SseNotificationEvent.builder()
                    .targetType(NotificationTargetType.SPECIFIC_USER)
                    .targetId(targetUser.getUsername())
                    .notificationPayload(payload)
                    .build();

//            kafkaProducerService.sendMessage("sse_topic", event);
            rabbitMQProducerService.sendMessage(RabbitMQConfig.SSE_QUEUE, event);

            userRepository.save(currentUser);
            userRepository.save(targetUser);
            return true;
        }
    }

    @Transactional
    @CacheEvict(value = "user_profile", allEntries = true)
    public boolean toggleBlockUser(String targetUserId){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (currentUser.getId().equals(targetUserId)){
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Optional<UserBlock> existingBlock = userBlockRepository.findByBlocker_IdAndBlocked_Id(currentUser.getId(), targetUser.getId());

        if (existingBlock.isPresent()){
            userBlockRepository.delete(existingBlock.get());
            return  false;
        } else {
            // Chưa chặn -> Thực hiện chặn
            UserBlock newBlock = UserBlock.builder()
                    .blocker(currentUser)
                    .blocked(targetUser)
                    .build();
            userBlockRepository.save(newBlock);

            // Khi chặn, tự động xóa Follow 2 chiều (nếu có)
            userFollowRepository.findByFollower_IdAndFollowing_Id(currentUser.getId(), targetUser.getId())
                    .ifPresent(follow -> {
                        userFollowRepository.delete(follow);
                        currentUser.setFollowingCount(Math.max(0, currentUser.getFollowingCount() - 1));
                        targetUser.setFollowerCount(Math.max(0, targetUser.getFollowerCount() - 1));
                    });

            userFollowRepository.findByFollower_IdAndFollowing_Id(targetUser.getId(), currentUser.getId())
                    .ifPresent(follow -> {
                        userFollowRepository.delete(follow);
                        targetUser.setFollowingCount(Math.max(0, targetUser.getFollowingCount() - 1));
                        currentUser.setFollowerCount(Math.max(0, currentUser.getFollowerCount() - 1));
                    });

            userRepository.save(currentUser);
            userRepository.save(targetUser);

            return true; // Trả về true nghĩa là "Blocked"
        }
    }

    public Page<UserSummaryResponse> getBlockedUser(Pageable pageable){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userBlockRepository.findByBlocker_Id(currentUser.getId(), pageable)
                .map(block -> UserSummaryResponse.builder()
                        .id(block.getBlocked().getId())
                        .username(block.getBlocked().getUsername())
                        .avatarUrl(block.getBlocked().getAvatarUrl())
                        .build());
    }

    @Transactional
    public boolean isCurrentlyPremium(User user){
        if (!user.getIsPremium()){
            return false;
        }

        if (user.getPremiumExpiryDate() != null){
            if (user.getPremiumExpiryDate().isAfter(LocalDateTime.now())){
                return true;
            } else {
                user.setIsPremium(false);
                user.setPremiumExpiryDate(null);
                userRepository.save(user);
                return false;
            }
        }
        return false;
    }

    @Cacheable(value = "premium_status", key = "#username")
    public boolean checkPremiumByUsername(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return isCurrentlyPremium(user);
    }

    @Cacheable(value = "premium_details", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    @Transactional
    public PremiumPlanResponse getPremiumPlan(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!isCurrentlyPremium(user)){
            return PremiumPlanResponse.builder()
                    .premiumExpiryDate(null)
                    .daysRemaining(0L)
                    .build();
        }

        long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), user.getPremiumExpiryDate());
        return PremiumPlanResponse.builder()
                .premiumExpiryDate(user.getPremiumExpiryDate())
                .daysRemaining(Math.max(0, daysLeft))//đảm bảo không âm
                .build();
    }

    private String getPublicIdFromUrl(String url){
        if (url == null || url.isEmpty() || url.contains("ui-avatars")) return null;
        try {
            Pattern pattern = Pattern.compile("upload/(?:v\\d+/)?([^.]+)\\.[a-z0-9]+$");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()){
                return matcher.group(1);
            }
            return null;
        } catch (Exception e){
            log.error("Error parsing Public Id from URL: {}", url);
            return null;
        }
    }

    private void deleteFileCloud(String url, String resourceType){
        String publicId = getPublicIdFromUrl(url);
        if (publicId != null){
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
                log.info("Deleted file on Cloudinary: {} (Type: {})", publicId, resourceType);
            } catch (Exception e){
                log.error("Failed to delete file on Cloudinary: {}", publicId);
            }
        }
    }

    private CloudinaryResponse saveFileCloud(MultipartFile file, String folder){
        if (file == null || file.isEmpty()) return null;
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto"
                    )
            );
            String url = (String) uploadResult.get("secure_url");
            Object durationObj = uploadResult.get("duration");
            Double duration = 0.0;

            if (durationObj != null){
                if (durationObj instanceof Double){
                    duration = (Double) durationObj;
                } else if (durationObj instanceof Integer){
                    duration = ((Integer) durationObj).doubleValue();
                }
            }
            return CloudinaryResponse.builder()
                    .url(url)
                    .duration(duration)
                    .build();
        } catch (Exception e){
            log.error("Cloudinary upload error: ", e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @CacheEvict(value = {"user_profile", "my_info", "admin_users_page"}, allEntries = true)
    @Transactional
    public UserProfileResponse updateMyProfile(UserProfileUpdateRequest request){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getName() != null && !request.getName().trim().isEmpty()){
            user.setName(request.getName().trim());
        }

        if (Boolean.TRUE.equals(request.getIsRemoved())) {
            if (user.getAvatarUrl() != null){
                cloudinaryService.deleteFile(user.getAvatarUrl(), "image");
            }
            user.setAvatarUrl(null);
        }

        else if (request.getAvatar() != null && !request.getAvatar().isEmpty()){
            if (user.getAvatarUrl() != null){
                cloudinaryService.deleteFile(user.getAvatarUrl(), "image");
            }
            CloudinaryResponse cloudRes = cloudinaryService.uploadFile(request.getAvatar(), "spotify_avatars");
            if (cloudRes != null) user.setAvatarUrl(cloudRes.getUrl());
        }

        userRepository.save(user);
        return getUserProfile("me");
    }

    public Page<UserSummaryResponse> getFollowers(String userId, Pageable pageable){
        return userFollowRepository.findByFollowing_Id(userId, pageable)
                .map(follow -> UserSummaryResponse.builder()
                        .id(follow.getFollower().getId())
                        .username(follow.getFollower().getUsername())
                        .avatarUrl(follow.getFollower().getAvatarUrl())
                        .build());
    }

    public Page<UserSummaryResponse> getFollowingUsers(String userId, Pageable pageable){
        return userFollowRepository.findByFollower_Id(userId, pageable)
                .map(follow -> UserSummaryResponse.builder()
                        .id(follow.getFollowing().getId())
                        .username(follow.getFollowing().getUsername())
                        .avatarUrl(follow.getFollowing().getAvatarUrl())
                        .build());
    }

    @Transactional
    @CacheEvict(value = {"user_profile", "my_info"}, allEntries = true)
    public void updatePreferredLanguage(LanguageUpdateRequest request){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setPreferredLanguage(request.getLanguage());
        userRepository.save(user);
    }

    public void sendSupportEmail(SupportRequest request){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Map<String, Object> adminVariables = new HashMap<>();
        adminVariables.put("userName", user.getName());
        adminVariables.put("userEmail", user.getEmail());
        adminVariables.put("requestType", request.getType());
        adminVariables.put("content", request.getContent());

        String adminEmail = "hodanghoang2003@gmail.com";
        emailService.sendHtmlEmail(adminEmail, "New support request: " + request.getType(), "admin-support-template", adminVariables);

        //Email xác nhận tự động
        Map<String, Object> userVariables = new HashMap<>();
        userVariables.put("name", user.getName());
        emailService.sendHtmlEmail(user.getEmail(), "Confirmed support request: ", "user-support-confirm-template", userVariables);
    }
}