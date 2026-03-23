package com.neroforte.goodfiction.service;

import com.neroforte.goodfiction.DTO.RecommendationResponse;
import com.neroforte.goodfiction.entity.UserBookListItem;
import com.neroforte.goodfiction.repository.UserBookListRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiRecommendationService {

    private final UserBookListRepository userBookListRepository;

    private final ChatClient chatClient;

    public AiRecommendationService(ChatClient.Builder builder, UserBookListRepository userBookRepository) {
        this.chatClient = builder
                .defaultSystem("You are an expert librarian and book critic.")
                .build();
        this.userBookListRepository = userBookRepository;
    }

    public RecommendationResponse getCustomRecommendations(Long userId, String userPrompt) {
        var favorites = userBookListRepository.findBooksByUser(userId);

        String userHistory = favorites.isEmpty() ? "No history available." : favorites.stream()
                .map(item -> String.format("- %s by %s (Genres: %s)",
                        item.getBook().getTitle(),
                        item.getBook().getAuthor(),
                        String.join(", ", item.getBook().getCategories())))
                .collect(Collectors.joining("\n"));

        String systemRules = """
                You are a stateless recommendation engine.
                Focus entirely on fulfilling the user's SPECIFIC REQUEST.
                Use the provided Reading History ONLY to avoid recommending books they have already read, or to gauge their general reading level.
                """;

        return chatClient.prompt()
                .system(systemRules)
                .user(u -> u.text("""
                        User's Specific Request: "%s"
                        
                        User's Reading History:
                        %s
                        
                          Constraints:
                        1. Do not recommend books the user has already read.
                        2. The prime focus should be on user's specific request. If the request is vague, then the focus should
                        shift more towards your suggestions based on user's reading history.
                        3. Match the genre and "vibe" of the history (e.g., if history is High Fantasy, recommend Fantasy).
                        4. Provide a short reasoning for each.
                        5. Return the result strictly as the requested JSON structure.
                        6. Use Ukrainian as a response language.
                        7. Try to provide books from other authors that write within the same genre(genres) if possible
                        """.formatted(userPrompt, userHistory)))
                .call()
                .entity(RecommendationResponse.class);
    }


    public RecommendationResponse getRecommendations(Long userId) {
        List<UserBookListItem> favorites = userBookListRepository.findBooksByUser(userId);

        if (favorites.isEmpty()) {
            log.info("user {} has ", userId);
            throw new IllegalArgumentException("Not enough reading history to generate recommendations.");
        }

        String systemRules = """
                You are a stateless recommendation engine.
                Ignore all previous instructions or context from past interactions.
                Focus ONLY on the data provided in this specific prompt.
                """;

        String userHistory = favorites.stream()
                .map(item -> String.format("- %s by %s. Description: %s (Rated by user: %d/5)",
                        item.getBook().getTitle(),
                        item.getBook().getAuthor(),
                        item.getBook().getDescription(),
                        item.getUserRating()))
                .collect(Collectors.joining("\n"));

        return chatClient.prompt()
                .system(systemRules)
                .user(u -> u.text("""
                        Based on the user's reading history below, recommend from 1 to 5 new books they would enjoy.
                        
                        User's books:
                        {history}
                        
                        Constraints:
                        1. Do not recommend books the user has already read.
                        2. Match the genre and "vibe" of the history (e.g., if history is High Fantasy, recommend Fantasy).
                        If the user's history contains books of different genre you must increase amount of books you recommend to also include additional genres from user history.
                        3. Provide a short reasoning for each.
                        4. Return the result strictly as the requested JSON structure.
                        5. Use Ukrainian as a response language.
                        6. Try to provide books from other authors that write within the same genre(genres) if possible
                        """)
                        .param("history", userHistory))
                .call()
                .entity(RecommendationResponse.class);
    }
}