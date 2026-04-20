package com.onnyth.onnythserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnyth.onnythserver.dto.registration.*;
import com.onnyth.onnythserver.models.RegistrationStep;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.service.RegistrationCommitService;
import com.onnyth.onnythserver.service.RegistrationService;
import com.onnyth.onnythserver.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/registration")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Registration", description = "Multi-step profile registration APIs")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final RegistrationCommitService commitService;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Get registration status for crash recovery")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration status retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/status")
    public ResponseEntity<RegistrationStatusResponse> getStatus(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(registrationService.getStatus(userId));
    }

    @Operation(summary = "Save data for a specific registration step")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Step data saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid step data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    @PutMapping("/step/{stepKey}")
    public ResponseEntity<RegistrationStepResponse> saveStep(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String stepKey,
            @RequestBody Map<String, Object> stepData) {

        UUID userId = UUID.fromString(jwt.getSubject());
        RegistrationStep step = RegistrationStep.fromKey(stepKey);

        if (step == null) {
            return ResponseEntity.badRequest().build();
        }

        RegistrationStepResponse response = registrationService.saveStep(userId, step, stepData);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload profile picture during registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded and step saved"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping(value = "/step/IMAGE/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegistrationStepResponse> uploadImage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file) throws IOException {

        UUID userId = UUID.fromString(jwt.getSubject());

        // Upload to storage
        String imageUrl = storageService.uploadProfilePicture(
                userId,
                file.getBytes(),
                file.getContentType(),
                file.getOriginalFilename()
        );

        // Save in draft
        RegistrationStepResponse response = registrationService.saveImageStep(userId, imageUrl);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Complete registration — atomic commit of all draft data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration completed successfully"),
            @ApiResponse(responseCode = "400", description = "Missing required steps"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/complete")
    public ResponseEntity<RegistrationCompleteResponse> completeRegistration(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        User user = commitService.commitRegistration(userId);

        return ResponseEntity.ok(new RegistrationCompleteResponse(
                true,
                user.getId(),
                user.getUsername()
        ));
    }
}
