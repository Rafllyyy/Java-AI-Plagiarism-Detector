package com.rafly.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JaccardCalculator implements SimilarityCalculator {

    private TextPreprocessor preprocessor = new TextPreprocessor();

    @Override
    public double calculate(String docA, String docB) {

        List<String> tokensA = preprocessor.processToTokens(docA);
        List<String> tokensB = preprocessor.processToTokens(docB);

        Set<String> setA = new HashSet<>(tokensA);
        Set<String> setB = new HashSet<>(tokensB);

        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        if(union.isEmpty()){
            return 0;
        }

        return (double) intersection.size() / union.size();

    }

}