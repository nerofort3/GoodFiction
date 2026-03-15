package com.neroforte.goodfiction.DTO;

import java.util.List;

public record RecommendationResponse(
        List<BookRecommendation> recommendations
) {}

record BookRecommendation(
        String title,
        String author,
        String reasoning,
        String estimatedRating
) {}