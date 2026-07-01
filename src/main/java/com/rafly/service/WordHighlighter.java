package com.rafly.service;

import java.util.HashSet;
import java.util.Set;

public class WordHighlighter {

    private final TextPreprocessor preprocessor = new TextPreprocessor();

    public Set<String> getCommonWords(String sentenceA, String sentenceB) {

        String cleanA = preprocessor.preprocess(sentenceA);
        String cleanB = preprocessor.preprocess(sentenceB);

        Set<String> wordsA = new HashSet<>();
        Set<String> common = new HashSet<>();

        for (String word : cleanA.split("\\s+")) {

            if (!word.isBlank()) {
                wordsA.add(word);
            }

        }

        for (String word : cleanB.split("\\s+")) {

            if (wordsA.contains(word)) {
                common.add(word);
            }

        }

        return common;

    }

}