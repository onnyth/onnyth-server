package com.onnyth.onnythserver.bookmark.adapter.in.rest.dto;

import com.onnyth.onnythserver.shared.validation.ValidUri;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record BookmarkUpdateRequest(
        @NotBlank(message = "URL is required")
        @ValidUri(message = "must be a valid URI")
        String url,

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "must not exceed 255 characters")
        String title,

        Set<@NotBlank(message = "Tag cannot be blank")
            @Size(max = 50, message = "must not exceed 50 characters") String> tags
) {}

