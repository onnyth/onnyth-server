package com.onnyth.onnythserver.friendship.adapter.in.rest.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record StatComparisonResponse(
        long scoreDifference,
        List<String> higherIn,
        List<String> lowerIn) {
}
