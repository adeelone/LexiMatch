package com.leximatch.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class DictionaryService {
    private final Set<String> allowedWords;
    private final List<String> answerWords;
    private final SecureRandom random = new SecureRandom();

    public DictionaryService(String dictionaryResource, String answerBankResource) {
        this.allowedWords = Set.copyOf(loadWords(dictionaryResource));
        this.answerWords = List.copyOf(loadWords(answerBankResource));
        if (!allowedWords.containsAll(answerWords)) {
            throw new IllegalStateException("Answer bank must be a subset of the dictionary.");
        }
    }

    public boolean isAllowedWord(String word) {
        return allowedWords.contains(normalize(word));
    }

    public String randomAnswer() {
        return answerWords.get(random.nextInt(answerWords.size()));
    }

    public int getDictionarySize() {
        return allowedWords.size();
    }

    public int getAnswerBankSize() {
        return answerWords.size();
    }

    public static String normalize(String word) {
        return word == null ? "" : word.trim().toLowerCase(Locale.ROOT);
    }

    private static List<String> loadWords(String resourcePath) {
        InputStream stream = DictionaryService.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalStateException("Missing resource: " + resourcePath);
        }

        List<String> words = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String normalized = normalize(line);
                if (normalized.length() == 5 && seen.add(normalized)) {
                    words.add(normalized);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load resource: " + resourcePath, ex);
        }

        return words;
    }
}
