package com.rafly.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TFIDFVector {

    public Map<String, Double> createVector(
            List<String> document,
            List<List<String>> corpus,
            Set<String> vocabulary
    ) {

        Map<String, Double> vector = new HashMap<>();

        for (String word : vocabulary) {

            double tf = termFrequency(word, document);

            double idf = inverseDocumentFrequency(word, corpus);

            vector.put(word, tf * idf);

        }

        return vector;

    }

    private double termFrequency(String word, List<String> document) {

        int count = 0;

        for (String token : document) {

            if (token.equals(word))
                count++;

        }

        return (double) count / document.size();

    }

    private double inverseDocumentFrequency(
            String word,
            List<List<String>> corpus
    ) {

        int docsContaining = 0;

        for (List<String> doc : corpus) {

            if (doc.contains(word))
                docsContaining++;

        }

        return Math.log(
                (double) corpus.size() /
                        (1 + docsContaining)
        ) + 1;

    }

}