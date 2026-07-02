package com.rafly.service;

import com.rafly.model.HistoryEntry;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Tanggung jawab tunggal: menyimpan, memuat, dan mengelola
 * riwayat perbandingan dokumen dalam format JSON lokal.
 *
 * File disimpan di: {user.home}/.plagiarismdetector/history.json
 */
public class ComparisonHistoryManager {

    private static final String DIR_NAME  = ".plagiarismdetector";
    private static final String FILE_NAME = "history.json";

    private final Path historyFile;
    private final List<HistoryEntry> entries;

    public ComparisonHistoryManager() {
        Path dir = Paths.get(System.getProperty("user.home"), DIR_NAME);
        this.historyFile = dir.resolve(FILE_NAME);
        this.entries = new ArrayList<>();
        ensureDirectoryExists(dir);
        load();
    }

    // -----------------------------------------------------------------------
    // PUBLIC API
    // -----------------------------------------------------------------------

    /** Tambah satu entri baru dan langsung simpan ke disk. */
    public void add(String docAName, String docBName,
                    String algorithm, double similarity) {
        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
        entries.add(0, new HistoryEntry(docAName, docBName, algorithm, similarity, date));
        save();
    }

    /** Kembalikan semua entri (urutan terbaru di atas). */
    public List<HistoryEntry> getAll() {
        return Collections.unmodifiableList(entries);
    }

    /** Hapus semua riwayat dari memori dan dari disk. */
    public void clearAll() {
        entries.clear();
        save();
    }

    // -----------------------------------------------------------------------
    // SERIALISASI JSON MANUAL (tanpa library eksternal)
    // -----------------------------------------------------------------------

    private void save() {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < entries.size(); i++) {
            sb.append(toJson(entries.get(i)));
            if (i < entries.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");

        try {
            Files.writeString(historyFile, sb.toString());
        } catch (IOException e) {
            System.err.println("Gagal menyimpan history: " + e.getMessage());
        }
    }

    private void load() {
        if (!Files.exists(historyFile)) return;

        try {
            String json = Files.readString(historyFile).trim();
            if (json.equals("[]") || json.isEmpty()) return;

            // Strip [ dan ]
            json = json.substring(1, json.length() - 1).trim();

            // Pecah per objek { ... }
            List<String> objects = splitJsonObjects(json);
            for (String obj : objects) {
                HistoryEntry entry = fromJson(obj.trim());
                if (entry != null) entries.add(entry);
            }
        } catch (IOException e) {
            System.err.println("Gagal memuat history: " + e.getMessage());
        }
    }

    private String toJson(HistoryEntry e) {
        return String.format(
            "  {\"docA\":\"%s\",\"docB\":\"%s\",\"algorithm\":\"%s\"," +
            "\"similarity\":%.4f,\"date\":\"%s\"}",
            escape(e.getDocAName()), escape(e.getDocBName()),
            escape(e.getAlgorithm()), e.getSimilarity(), escape(e.getDate()));
    }

    private HistoryEntry fromJson(String obj) {
        try {
            String docA      = extractString(obj, "docA");
            String docB      = extractString(obj, "docB");
            String algorithm = extractString(obj, "algorithm");
            double similarity = extractDouble(obj, "similarity");
            String date      = extractString(obj, "date");
            return new HistoryEntry(docA, docB, algorithm, similarity, date);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search) + search.length();
        int end   = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private double extractDouble(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search) + search.length();
        int end   = json.indexOf(",", start);
        if (end < 0) end = json.indexOf("}", start);
        return Double.parseDouble(json.substring(start, end).trim());
    }

    private List<String> splitJsonObjects(String json) {
        List<String> result = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}') { if (--depth == 0 && start >= 0) result.add(json.substring(start, i + 1)); }
        }
        return result;
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void ensureDirectoryExists(Path dir) {
        try {
            if (!Files.exists(dir)) Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("Gagal membuat direktori history: " + e.getMessage());
        }
    }
}