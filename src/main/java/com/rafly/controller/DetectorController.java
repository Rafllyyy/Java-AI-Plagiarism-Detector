package com.rafly.controller;

import com.rafly.service.*;
import com.rafly.model.HighlightResult;
import com.rafly.model.ComparisonResult;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DetectorController {

    private final DocumentLoader        loader      = new DocumentLoader();
    private final FolderScanner         scanner     = new FolderScanner();
    private final SimilarityHighlighter highlighter = new SimilarityHighlighter();
    private final TextPreprocessor      preprocessor= new TextPreprocessor();

    public double compare(String algorithm, String docA, String docB) {
        String processedA = preprocessor.process(docA);
        String processedB = preprocessor.process(docB);
        return createCalculator(algorithm).calculate(processedA, processedB);
    }

    /** Highlight menggunakan algoritma yang sama dengan yang dipilih user */
    public List<HighlightResult> getHighlights(String docA, String docB, String algorithm) {
        return highlighter.findSimilarSentences(docA, docB, algorithm);
    }

    public List<ComparisonResult> compareFolder(
            String algorithm, String sourceDocument, File folder) {

        List<ComparisonResult> results = new ArrayList<>();
        for (File file : scanner.scan(folder)) {
            try {
                String target = loader.load(file);
                results.add(new ComparisonResult(file.getName(),
                        compare(algorithm, sourceDocument, target)));
            } catch (IOException e) {
                System.err.println("Gagal memuat: " + file.getName());
            }
        }
        results.sort(Comparator.comparingDouble(ComparisonResult::getSimilarity).reversed());
        return results;
    }

    private SimilarityCalculator createCalculator(String algorithm) {
        switch (algorithm) {
            case "Jaccard":     return new JaccardCalculator();
            case "Cosine":      return new CosineCalculator();
            case "Levenshtein": return new LevenshteinCalculator();
            case "N-Gram":      return new NgramCalculator();
            default: throw new IllegalArgumentException("Algoritma tidak dikenali: " + algorithm);
        }
    }
}