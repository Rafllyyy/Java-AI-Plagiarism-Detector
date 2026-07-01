package com.rafly.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextPreprocessor {

    private static final Pattern NON_ALPHA = Pattern.compile("[^a-z\\s]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    // ── Pipeline utama ───────────────────────────────────────────────

    public String process(String rawText) {
        List<String> tokens = tokenize(rawText);
        tokens = removeStopwords(tokens);
        tokens = stem(tokens);
        return String.join(" ", tokens);
    }

    public List<String> processToTokens(String rawText) {
        List<String> tokens = tokenize(rawText);
        tokens = removeStopwords(tokens);
        tokens = stem(tokens);
        return tokens;
    }

    // ── Alias untuk kompatibilitas file lama ─────────────────────────

    /** @deprecated gunakan process() */
    public String preprocess(String text) {
        return process(text);
    }

    /** @deprecated gunakan processToTokens() */
    public List<String> preprocessTokens(String text) {
        return processToTokens(text);
    }

    // ── Langkah-langkah pipeline ─────────────────────────────────────

    public String clean(String text) {
        if (text == null) return "";
        String lower   = text.toLowerCase();
        String cleaned = NON_ALPHA.matcher(lower).replaceAll(" ");
        return WHITESPACE.matcher(cleaned).replaceAll(" ").trim();
    }

    public List<String> tokenize(String text) {
        String cleaned = clean(text);
        List<String> tokens = new ArrayList<>();
        if (cleaned.isEmpty()) return tokens;
        for (String word : cleaned.split("\\s+")) {
            if (!word.isEmpty()) tokens.add(word);
        }
        return tokens;
    }

    public List<String> removeStopwords(List<String> tokens) {
        List<String> result = new ArrayList<>();
        for (String t : tokens) {
            if (!IndonesianStopwords.isStopword(t)) result.add(t);
        }
        return result;
    }

    public List<String> stem(List<String> tokens) {
        List<String> result = new ArrayList<>();
        for (String t : tokens) result.add(IndonesianStemmer.stem(t));
        return result;
    }
}