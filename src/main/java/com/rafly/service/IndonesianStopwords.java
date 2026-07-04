package com.rafly.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Stopword Bahasa Indonesia.
 * Sumber: PySastrawi (https://github.com/har07/PySastrawi) — MIT License.
 * Di-load dari GitHub saat startup; fallback ke daftar minimal jika offline.
 */
public class IndonesianStopwords {

    private static final String SOURCE_URL = "https://raw.githubusercontent.com/har07/PySastrawi/master/" +
            "src/Sastrawi/StopWordRemover/StopWordRemoverFactory.py";

    private static Set<String> STOPWORDS = null;

    // ── Fallback jika tidak ada internet ─────────────────────────────
    private static final Set<String> FALLBACK = new HashSet<>();
    static {
        String[] words = {
                "yang", "dan", "di", "ke", "dari", "ini", "itu", "dengan", "untuk", "pada",
                "adalah", "atau", "juga", "tidak", "dalam", "oleh", "sebagai", "karena",
                "akan", "ada", "sudah", "saya", "anda", "kita", "mereka", "dia", "kami",
                "tetapi", "namun", "sehingga", "agar", "jika", "kalau", "bila", "ketika",
                "sejak", "sampai", "hingga", "setelah", "sebelum", "selama", "meskipun",
                "bahwa", "tentang", "terhadap", "antara", "bagi", "demi", "tanpa", "per",
                "telah", "sedang", "belum", "bukan", "juga", "pun", "hanya", "saja",
                "sangat", "cukup", "terlalu", "agak", "hampir", "sekitar", "mungkin",
                "selalu", "kadang", "sering", "jarang", "pasti", "tentu", "memang", "masih"
        };
        for (String w : words)
            FALLBACK.add(w);
    }

    private IndonesianStopwords() {
    }

    // ── Load stopword dari PySastrawi ─────────────────────────────────

    private static synchronized Set<String> getStopwords() {
        if (STOPWORDS != null)
            return STOPWORDS;

        STOPWORDS = new HashSet<>();
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(SOURCE_URL).toURL().openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (conn.getResponseCode() == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()))) {

                    StringBuilder sb = new StringBuilder();
                    String line;
                    boolean inList = false;
                    while ((line = br.readLine()) != null) {
                        if (line.contains("return ["))
                            inList = true;
                        if (inList) {
                            sb.append(line);
                            if (line.contains("]"))
                                break;
                        }
                    }

                    // Parse kata dari string Python list
                    String raw = sb.toString();
                    int i = 0;
                    while (i < raw.length()) {
                        int start = raw.indexOf('\'', i);
                        if (start < 0)
                            break;
                        int end = raw.indexOf('\'', start + 1);
                        if (end < 0)
                            break;
                        String word = raw.substring(start + 1, end).trim();
                        if (!word.isEmpty() && word.matches("[a-zA-Z\\-]+")) {
                            STOPWORDS.add(word.toLowerCase());
                        }
                        i = end + 1;
                    }
                }
            }

            if (STOPWORDS.isEmpty()) {
                System.out.println("[Stopwords] Gagal load dari GitHub, pakai fallback.");
                STOPWORDS = new HashSet<>(FALLBACK);
            } else {
                System.out.println("[Stopwords] Loaded " + STOPWORDS.size()
                        + " kata dari PySastrawi.");
            }

        } catch (Exception e) {
            System.out.println("[Stopwords] Offline, pakai fallback: " + e.getMessage());
            STOPWORDS = new HashSet<>(FALLBACK);
        }

        return STOPWORDS;
    }

    // ── Public API ────────────────────────────────────────────────────

    public static boolean isStopword(String word) {
        return getStopwords().contains(word.toLowerCase());
    }

    public static Set<String> getAll() {
        return new HashSet<>(getStopwords());
    }
}