package com.rafly.model;

/**
 * Data class untuk satu entri riwayat perbandingan.
 */
public class HistoryEntry {

    private final String docAName;
    private final String docBName;
    private final String algorithm;
    private final double similarity;
    private final String date;

    public HistoryEntry(String docAName, String docBName,
                        String algorithm, double similarity, String date) {
        this.docAName   = docAName;
        this.docBName   = docBName;
        this.algorithm  = algorithm;
        this.similarity = similarity;
        this.date       = date;
    }

    public String getDocAName()   { return docAName; }
    public String getDocBName()   { return docBName; }
    public String getAlgorithm()  { return algorithm; }
    public double getSimilarity() { return similarity; }
    public String getDate()       { return date; }
}