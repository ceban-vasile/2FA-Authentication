package com.example.demo.service;

import com.example.demo.repository.HistoryRepository;

import com.example.demo.model.History;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository historyRepository;

    public Optional<History> save(History history) {
        return Optional.of(historyRepository.save(history));
    }

    public Optional<List<History>> getAllByUserId(long userId) {
        return historyRepository.getAllByUserId(userId);
    }

    public Optional<History> getByUserIdAndHistoryId(long userId, long historyId) {
        return historyRepository.getByUserIdAndId(userId, historyId);
    }

    public static String generateChatName(String chatHistory) {
        final int MAX_WORDS = 5;
        final int MIN_WORD_LENGTH = 3;
        // Split the chat history into words
        List<String> words = Arrays.asList(chatHistory.split("\\s+"));

        // Filter and process words
        List<String> significantWords = words.stream()
                .filter(word -> word.length() >= MIN_WORD_LENGTH)
                .map(String::toLowerCase)
                .distinct()
                .limit(MAX_WORDS)
                .collect(Collectors.toList());

        // Join the words to create the chat name
        String chatName = String.join("-", significantWords);

        // If the chat name is empty, use a default name
        return chatName.isEmpty() ? "unnamed-chat" : chatName;
    }
}
