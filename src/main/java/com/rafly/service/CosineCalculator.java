package com.rafly.service;

import com.rafly.utils.TFIDFVector;

import java.util.*;

public class CosineCalculator implements SimilarityCalculator {

    private final TextPreprocessor preprocessor =
            new TextPreprocessor();

    private final TFIDFVector tfidf =
            new TFIDFVector();

    @Override
    public double calculate(String docA, String docB) {

        List<String> tokensA =
                preprocessor.preprocessTokens(docA);

        List<String> tokensB =
                preprocessor.preprocessTokens(docB);

        List<List<String>> corpus = new ArrayList<>();

        corpus.add(tokensA);
        corpus.add(tokensB);

        Set<String> vocabulary = new HashSet<>();

        vocabulary.addAll(tokensA);
        vocabulary.addAll(tokensB);

        Map<String, Double> vectorA =
                tfidf.createVector(tokensA, corpus, vocabulary);

        Map<String, Double> vectorB =
                tfidf.createVector(tokensB, corpus, vocabulary);

        return cosine(vectorA, vectorB);

    }

    private double cosine(
            Map<String, Double> a,
            Map<String, Double> b
    ) {

        double dot = 0;

        double normA = 0;

        double normB = 0;

        for (String word : a.keySet()) {

            double x = a.get(word);

            double y = b.get(word);

            dot += x * y;

            normA += x * x;

            normB += y * y;

        }

        if (normA == 0 || normB == 0)
            return 0;

        return dot /
                (Math.sqrt(normA) * Math.sqrt(normB));

    }

}