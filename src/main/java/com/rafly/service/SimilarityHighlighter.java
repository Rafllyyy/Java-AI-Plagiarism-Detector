package com.rafly.service;

import com.rafly.model.HighlightResult;

import java.util.ArrayList;
import java.util.List;

public class SimilarityHighlighter {

    private final JaccardCalculator calculator =
            new JaccardCalculator();

    private final TextPreprocessor preprocessor =
            new TextPreprocessor();

    // Lebih realistis
    private static final double THRESHOLD = 0.40;

    public List<HighlightResult> findSimilarSentences(
            String docA,
            String docB
    ) {

        List<HighlightResult> result = new ArrayList<>();

        String[] sentencesA = split(docA);
        String[] sentencesB = split(docB);

        for (String a : sentencesA) {

            for (String b : sentencesB) {

                String cleanA = preprocessor.preprocess(a);
                String cleanB = preprocessor.preprocess(b);

                if (cleanA.isBlank() || cleanB.isBlank())
                    continue;

                double score =
                        calculator.calculate(cleanA, cleanB);

                if (score >= THRESHOLD) {

                    result.add(
                            new HighlightResult(
                                    a,
                                    b,
                                    score
                            )
                    );

                }

            }

        }

        return result;

    }

    private String[] split(String text) {

        return text
                .replace("\r", "")
                .trim()
                .split("[.!?\\n]+");

    }

}