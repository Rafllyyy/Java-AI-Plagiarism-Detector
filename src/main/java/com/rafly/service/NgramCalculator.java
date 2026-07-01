package com.rafly.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NgramCalculator implements SimilarityCalculator {

    private final TextPreprocessor preprocessor = new TextPreprocessor();

    private static final int N = 2;

    @Override
    public double calculate(String docA, String docB) {

        List<String> gramsA = generateNGrams(
                preprocessor.preprocessTokens(docA)
        );

        List<String> gramsB = generateNGrams(
                preprocessor.preprocessTokens(docB)
        );

        Set<String> setA = new HashSet<>(gramsA);
        Set<String> setB = new HashSet<>(gramsB);

        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        if (union.isEmpty()) {
            return 0;
        }

        return (double) intersection.size() / union.size();

    }

    private List<String> generateNGrams(List<String> words) {

        List<String> grams = new ArrayList<>();

        for (int i = 0; i <= words.size() - N; i++) {

            StringBuilder sb = new StringBuilder();

            for (int j = 0; j < N; j++) {

                sb.append(words.get(i + j));

                if (j < N - 1)
                    sb.append(" ");

            }

            grams.add(sb.toString());

        }

        return grams;

    }

}