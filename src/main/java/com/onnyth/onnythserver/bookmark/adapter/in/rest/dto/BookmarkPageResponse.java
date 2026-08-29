package com.onnyth.onnythserver.bookmark.adapter.in.rest.dto;

import com.onnyth.onnythserver.bookmark.domain.model.Bookmark;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record BookmarkPageResponse(
        List<BookmarkResponse> content,
        long totalElements,
        int totalPages,
        int currentPage,
        boolean hasNext
) {
    public static BookmarkPageResponse fromPage(Page<Bookmark> page) {
        return BookmarkPageResponse.builder()
                .content(page.getContent().stream().map(BookmarkResponse::fromDomain).toList())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .hasNext(page.hasNext())
                .build();
    }
}

