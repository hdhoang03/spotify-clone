package com.spotify.spotify.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spotify.spotify.dto.response.CloudinaryResponse;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CloudinaryService {
    Cloudinary cloudinary;

    public CloudinaryResponse uploadFile(MultipartFile file, String folder) {
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

            if (durationObj != null) {
                if (durationObj instanceof Double) {
                    duration = (Double) durationObj;
                } else if (durationObj instanceof Integer) {
                    duration = ((Integer) durationObj).doubleValue();
                }
            }
            return CloudinaryResponse.builder()
                    .url(url)
                    .duration(duration)
                    .build();
        } catch (Exception e) {
            log.error("Cloudinary upload error: ", e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public void deleteFile(String url, String resourceType) {
        String publicId = getPublicIdFromUrl(url);
        if (publicId != null) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
                log.info("Deleted file on Cloudinary: {} (Type: {})", publicId, resourceType);
            } catch (Exception e) {
                log.error("Failed to delete file on Cloudinary: {}", publicId);
            }
        }
    }

    private String getPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty() || url.contains("ui-avatars")) return null;
        try {
            Pattern pattern = Pattern.compile("upload/(?:v\\d+/)?([^.]+)\\.[a-z0-9]+$");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        } catch (Exception e) {
            log.error("Error parsing Public ID from URL: {}", url);
            return null;
        }
    }
}