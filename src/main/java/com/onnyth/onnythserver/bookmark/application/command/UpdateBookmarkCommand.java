package com.onnyth.onnythserver.bookmark.application.command;

import java.util.Set;

public record UpdateBookmarkCommand(
        String url,
        String title,
        Set<String> tags
) {}

