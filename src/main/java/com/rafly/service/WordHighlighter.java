package com.rafly.service;

import java.util.HashSet;
import java.util.Set;

public class WordHighlighter {

    private static final int MIN_WORD_LENGTH = 5;

    /**
     * Kembalikan kata-kata bermakna yang muncul di KEDUA dokumen penuh.
     * Digunakan untuk highlight seluruh dokumen, bukan per kalimat.
     */
    public Set<String> getCommonWords(String docA, String docB) {
        Set<String> wordsA = extractWords(docA);
        Set<String> wordsB = extractWords(docB);
        wordsA.retainAll(wordsB);
        return wordsA;
    }

    private Set<String> extractWords(String text) {
        Set<String> result = new HashSet<>();
        for (String raw : text.split("\\s+")) {
            String word = raw.replaceAll("^[^a-zA-Z]+|[^a-zA-Z]+$", "");
            if (word.length() >= MIN_WORD_LENGTH
                    && !IndonesianStopwords.isStopword(word.toLowerCase())) {
                result.add(word);
            }
        }
        return result;
    }
}