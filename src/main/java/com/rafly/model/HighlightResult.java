package com.rafly.model;

public class HighlightResult {

    private final String sentenceA;
    private final String sentenceB;
    private final double similarity;

    public HighlightResult(
            String sentenceA,
            String sentenceB,
            double similarity
    ) {

        this.sentenceA = sentenceA;
        this.sentenceB = sentenceB;
        this.similarity = similarity;

    }

    public String getSentenceA() {
        return sentenceA;
    }

    public String getSentenceB() {
        return sentenceB;
    }

    public double getSimilarity() {
        return similarity;
    }

}