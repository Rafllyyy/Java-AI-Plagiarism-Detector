package com.rafly.controller;

import com.rafly.ai.AIService;
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
    private final AIService             aiService   = new AIService();

    public double compare(String algorithm, String docA, String docB) {
        if ("AI (Semantic)".equals(algorithm)) {
            try {
                AIService.AIResult result = aiService.compare(docA, docB);
                return result.overallSimilarity / 100.0;
            } catch (IOException e) {
                throw new RuntimeException("AI backend tidak bisa dihubungi. " +
                        "Pastikan server Python sudah berjalan di port 8000.\n" +
                        "Detail: " + e.getMessage());
            }
        }
        String processedA = preprocessor.process(docA);
        String processedB = preprocessor.process(docB);
        return createCalculator(algorithm).calculate(processedA, processedB);
    }

    public List<HighlightResult> getHighlights(String docA, String docB, String algorithm) {
        if ("AI (Semantic)".equals(algorithm)) {
            try {
                AIService.AIResult result = aiService.compare(docA, docB);
                return result.matchedSentences;
            } catch (IOException e) {
                return new ArrayList<>();
            }
        }
        return highlighter.findSimilarSentences(docA, docB, algorithm);
    }

    public boolean isAIAvailable() {
        return aiService.isAvailable();
    }

    public List<ComparisonResult> compareFolder(
            String algorithm, String sourceDocument, File folder) {

        List<ComparisonResult> results = new ArrayList<>();
        for (File file : scanner.scan(folder)) {
            try {
                String target = loader.load(file);
                results.add(new ComparisonResult(file.getName(),
                        compare(algorithm, sourceDocument, target)));
            } catch (Exception e) {
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