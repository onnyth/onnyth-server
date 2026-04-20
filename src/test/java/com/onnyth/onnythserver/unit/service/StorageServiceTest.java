package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.exceptions.FileUploadException;
import com.onnyth.onnythserver.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for StorageService — focuses on file validation and extension
 * logic which are pure functions requiring no external calls.
 */
class StorageServiceTest {

    private StorageService storageService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        storageService = new StorageService(restTemplate);
    }

    @Nested
    @DisplayName("File Validation")
    class FileValidation {

        @Test
        @DisplayName("throws FileUploadException when file data is null")
        void throwsWhenDataIsNull() {
            assertThatThrownBy(
                    () -> storageService.uploadProfilePicture(UUID.randomUUID(), null, "image/jpeg", "test.jpg"))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("throws FileUploadException when file data is empty")
        void throwsWhenDataIsEmpty() {
            assertThatThrownBy(
                    () -> storageService.uploadProfilePicture(UUID.randomUUID(), new byte[0], "image/jpeg", "test.jpg"))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("throws FileUploadException when file exceeds 5MB")
        void throwsWhenFileTooLarge() {
            byte[] oversizedFile = new byte[6 * 1024 * 1024]; // 6MB

            assertThatThrownBy(() -> storageService.uploadProfilePicture(UUID.randomUUID(), oversizedFile, "image/jpeg",
                    "big.jpg"))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessageContaining("5MB");
        }

        @ParameterizedTest
        @ValueSource(strings = { "image/gif", "image/bmp", "application/pdf", "text/plain", "image/tiff" })
        @DisplayName("throws FileUploadException for disallowed content types")
        void throwsForDisallowedContentTypes(String contentType) {
            byte[] data = new byte[1024];

            assertThatThrownBy(
                    () -> storageService.uploadProfilePicture(UUID.randomUUID(), data, contentType, "test.gif"))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessageContaining("Invalid file type");
        }

        @ParameterizedTest
        @ValueSource(strings = { "image/jpeg", "image/jpg", "image/png", "image/webp" })
        @DisplayName("passes validation and uploads for allowed content types (JPEG, PNG, WebP)")
        void passesForAllowedContentTypes(String contentType) {
            // Mock RestTemplate to return a successful response
            when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.ok("{}"));

            byte[] data = new byte[1024];
            UUID userId = UUID.randomUUID();

            // Should not throw — validation passes and upload succeeds
            String result = storageService.uploadProfilePicture(userId, data, contentType, "test.jpg");
            assertThat(result).isNotNull().contains(userId.toString());
        }

        @Test
        @DisplayName("throws FileUploadException when contentType is null")
        void throwsWhenContentTypeIsNull() {
            byte[] data = new byte[1024];

            assertThatThrownBy(() -> storageService.uploadProfilePicture(UUID.randomUUID(), data, null, "test.jpg"))
                    .isInstanceOf(FileUploadException.class)
                    .hasMessageContaining("Invalid file type");
        }
    }

    @Nested
    @DisplayName("deleteFile()")
    class DeleteFile {

        @Test
        @DisplayName("does nothing when fileUrl is null")
        void doesNothingWhenNull() {
            // Should not throw
            storageService.deleteFile(null);
        }

        @Test
        @DisplayName("does nothing when fileUrl is blank")
        void doesNothingWhenBlank() {
            // Should not throw
            storageService.deleteFile("   ");
        }
    }
}
