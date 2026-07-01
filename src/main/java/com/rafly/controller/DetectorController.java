package com.rafly.controller;

import com.rafly.service.CosineCalculator;
import com.rafly.service.JaccardCalculator;
import com.rafly.service.LevenshteinCalculator;
import com.rafly.service.NgramCalculator;
import com.rafly.service.SimilarityCalculator;
import com.rafly.service.TextPreprocessor;
import com.rafly.service.SimilarityHighlighter;
import com.rafly.service.FolderScanner;
import com.rafly.service.DocumentLoader;
import com.rafly.model.HighlightResult;
import com.rafly.model.ComparisonResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DetectorController {

    private final DocumentLoader loader         = new DocumentLoader();
    private final FolderScanner scanner         = new FolderScanner();
    private final SimilarityHighlighter highlighter = new SimilarityHighlighter();
    private final TextPreprocessor preprocessor  = new TextPreprocessor();

    // ------------------------------------------------------------------
    // COMPARE: dua dokumen
    // ------------------------------------------------------------------

    /**
     * Bandingkan dua teks dokumen menggunakan algoritma yang dipilih.
     * Teks dipreproses terlebih dahulu sebelum dikirim ke kalkulator.
     *
     * @param algorithm nama algoritma: "Jaccard", "Cosine", "Levenshtein", "N-Gram"
     * @param docA      isi teks dokumen A (raw)
     * @param docB      isi teks dokumen B (raw)
     * @return skor similarity dalam rentang 0.0 – 1.0
     */
    public double compare(String algorithm, String docA, String docB) {
        String processedA = preprocessor.process(docA);
        String processedB = preprocessor.process(docB);

        SimilarityCalculator calculator = createCalculator(algorithm);
        return calculator.calculate(processedA, processedB);
    }

    // ------------------------------------------------------------------
    // HIGHLIGHTS
    // ------------------------------------------------------------------

    /**
     * Kembalikan daftar pasangan kalimat yang mirip untuk keperluan highlight.
     * Highlight menggunakan teks ASLI (bukan preprocessed) agar tampilan GUI
     * tetap terbaca oleh pengguna.
     */
    public List<HighlightResult> getHighlights(String docA, String docB) {
        return highlighter.findSimilarSentences(docA, docB);
    }

    // ------------------------------------------------------------------
    // COMPARE FOLDER (Batch Comparison)
    // ------------------------------------------------------------------

    /**
     * Bandingkan satu dokumen sumber terhadap semua dokumen di dalam folder.
     * Hasil diurutkan dari similarity tertinggi ke terendah.
     *
     * @param algorithm      nama algoritma
     * @param sourceDocument isi teks dokumen sumber (raw)
     * @param folder         folder yang berisi dokumen-dokumen pembanding
     * @return List ComparisonResult, sudah diurutkan descending by similarity
     */
    public List<ComparisonResult> compareFolder(
            String algorithm,
            String sourceDocument,
            File folder) {

        List<ComparisonResult> results = new ArrayList<>();
        List<File> files = scanner.scan(folder);

        for (File file : files) {
            try {
                String target = loader.load(file);
                double similarity = compare(algorithm, sourceDocument, target);
                results.add(new ComparisonResult(file.getName(), similarity));
            } catch (IOException e) {
                // Catat error tapi lanjut ke file berikutnya
                System.err.println("Gagal memuat file: " + file.getName() + " — " + e.getMessage());
            }
        }

        results.sort(Comparator.comparingDouble(ComparisonResult::getSimilarity).reversed());
        return results;
    }

    // ------------------------------------------------------------------
    // PRIVATE HELPER
    // ------------------------------------------------------------------

    private SimilarityCalculator createCalculator(String algorithm) {
        switch (algorithm) {
            case "Jaccard":     return new JaccardCalculator();
            case "Cosine":      return new CosineCalculator();
            case "Levenshtein": return new LevenshteinCalculator();
            case "N-Gram":      return new NgramCalculator();
            default:
                throw new IllegalArgumentException("Algoritma tidak dikenali: " + algorithm);
        }
    }
}