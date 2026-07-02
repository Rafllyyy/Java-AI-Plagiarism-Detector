package com.rafly.service;

import com.rafly.model.HighlightResult;

import java.util.ArrayList;
import java.util.List;

public class SimilarityHighlighter {

    private final TextPreprocessor preprocessor = new TextPreprocessor();
    private static final double THRESHOLD = 0.45;

    public List<HighlightResult> findSimilarSentences(
            String docA, String docB, String algorithm) {

        SimilarityCalculator calculator = createCalculator(algorithm);
        List<HighlightResult> result    = new ArrayList<>();

        List<String> sentencesA = split(docA);
        List<String> sentencesB = split(docB);

        for (String a : sentencesA) {
            for (String b : sentencesB) {
                String cleanA = preprocessor.process(a);
                String cleanB = preprocessor.process(b);

                if (cleanA.isBlank() || cleanB.isBlank()) continue;

                double score = calculator.calculate(cleanA, cleanB);

                if (score >= THRESHOLD) {
                    result.add(new HighlightResult(a.trim(), b.trim(), score));
                }
            }
        }

        return result;
    }

    public List<HighlightResult> findSimilarSentences(String docA, String docB) {
        return findSimilarSentences(docA, docB, "Jaccard");
    }

    /**
     * Split teks menjadi kalimat-kalimat.
     * Pecah per baris dulu, lalu tiap baris dipecah per tanda baca.
     * Kalimat > 200 karakter dipecah lagi per koma.
     */
    private List<String> split(String text) {
        List<String> result = new ArrayList<>();

        String[] lines = text.replace("\r", "").split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("(?<=[.!?])\\s+");

            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty()) continue;

                if (part.length() > 200) {
                    for (String sub : part.split(",\\s+")) {
                        if (!sub.trim().isEmpty()) result.add(sub.trim());
                    }
                } else {
                    result.add(part);
                }
            }
        }

        return result;
    }

    private SimilarityCalculator createCalculator(String algorithm) {
        switch (algorithm) {
            case "Cosine":      return new CosineCalculator();
            case "Levenshtein": return new LevenshteinCalculator();
            case "N-Gram":      return new NgramCalculator();
            default:            return new JaccardCalculator();
        }
    }
}