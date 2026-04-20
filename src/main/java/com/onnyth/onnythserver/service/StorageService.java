package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.exceptions.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class StorageService {

    private final RestTemplate restTemplate;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key:${supabase.anon-key}}")
    private String supabaseServiceKey;

    @Value("${supabase.storage.bucket:profile-pics}")
    private String bucketName;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public StorageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Upload a profile picture to Supabase Storage.
     *
     * @param userId          the user's ID
     * @param imageData       the image bytes
     * @param contentType     the content type of the image
     * @param originalFilename the original filename
     * @return the public URL of the uploaded image
     */
    public String uploadProfilePicture(UUID userId, byte[] imageData, String contentType, String originalFilename) {
        validateFile(imageData, contentType);

        String extension = getFileExtension(originalFilename, contentType);
        String fileName = "profile-pics/" + userId + "/" + UUID.randomUUID() + extension;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseServiceKey);
            headers.set("Authorization", "Bearer " + supabaseServiceKey);
            headers.setContentType(MediaType.parseMediaType(contentType));

            HttpEntity<byte[]> entity = new HttpEntity<>(imageData, headers);

            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;

            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;
                log.info("Profile picture uploaded successfully for user {}: {}", userId, publicUrl);
                return publicUrl;
            } else {
                throw new FileUploadException("Failed to upload profile picture");
            }

        } catch (HttpClientErrorException e) {
            log.error("Failed to upload profile picture for user {}: {}", userId, e.getResponseBodyAsString());
            throw new FileUploadException("Failed to upload profile picture: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error uploading profile picture for user {}", userId, e);
            throw new FileUploadException("Failed to upload profile picture");
        }
    }

    /**
     * Delete a file from Supabase Storage.
     *
     * @param fileUrl the public URL of the file to delete
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            // Extract path from URL
            String marker = bucketName + "/";
            int index = fileUrl.indexOf(marker);
            if (index == -1) {
                log.warn("Could not extract file path from URL: {}", fileUrl);
                return;
            }
            String filePath = fileUrl.substring(index + marker.length());

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseServiceKey);
            headers.set("Authorization", "Bearer " + supabaseServiceKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;

            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
            log.info("File deleted successfully: {}", filePath);

        } catch (HttpClientErrorException e) {
            log.warn("Failed to delete file {}: {}", fileUrl, e.getResponseBodyAsString());
        } catch (Exception e) {
            log.warn("Unexpected error deleting file: {}", fileUrl, e);
        }
    }

    /**
     * Validate file size and content type.
     */
    private void validateFile(byte[] data, String contentType) {
        if (data == null || data.length == 0) {
            throw new FileUploadException("File is empty");
        }

        if (data.length > MAX_FILE_SIZE) {
            throw new FileUploadException("File size exceeds maximum allowed size of 5MB");
        }

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new FileUploadException("Invalid file type. Allowed types: JPG, PNG, WebP");
        }
    }

    /**
     * Get file extension from filename or content type.
     */
    private String getFileExtension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }

        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}

