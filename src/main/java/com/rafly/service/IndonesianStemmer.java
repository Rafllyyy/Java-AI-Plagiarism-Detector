package com.rafly.service;

/**
 * Stemmer sederhana Bahasa Indonesia menggunakan pendekatan rule-based
 * (penyederhanaan dari algoritma Nazief-Adriani).
 *
 * Tanggung jawab tunggal: mengubah kata berimbuhan menjadi kata dasar
 * agar token dari "berlari" dan "lari" diperlakukan sebagai kata yang sama.
 *
 * Contoh:
 *   "membaca"   → "baca"
 *   "berlari"   → "lari"
 *   "dimakan"   → "makan"
 *   "makanan"   → "makan"
 *   "pelajaran" → "ajar"
 */
public class IndonesianStemmer {

    private IndonesianStemmer() {}

    /**
     * Proses stemming satu kata.
     * Urutan: hapus sufiks dulu, lalu hapus prefiks.
     */
    public static String stem(String word) {
        if (word == null || word.length() <= 3) return word;

        String result = removeSuffix(word);
        result = removePrefix(result);

        // Jika hasil terlalu pendek setelah stripping, kembalikan yang lebih panjang
        if (result.length() < 2) return word;
        return result;
    }

    // -----------------------------------------------------------------------
    // SUFIKS
    // -----------------------------------------------------------------------

    private static String removeSuffix(String word) {
        // Urutan penting: cek yang lebih panjang dulu
        if (word.endsWith("kan")) {
            String stem = word.substring(0, word.length() - 3);
            if (stem.length() >= 2) return stem;
        }
        if (word.endsWith("an")) {
            String stem = word.substring(0, word.length() - 2);
            if (stem.length() >= 2) return stem;
        }
        if (word.endsWith("nya")) {
            String stem = word.substring(0, word.length() - 3);
            if (stem.length() >= 2) return stem;
        }
        if (word.endsWith("i")) {
            String stem = word.substring(0, word.length() - 1);
            if (stem.length() >= 2) return stem;
        }
        return word;
    }

    // -----------------------------------------------------------------------
    // PREFIKS
    // -----------------------------------------------------------------------

    private static String removePrefix(String word) {
        // me- / mem- / men- / meng- / meny-
        if (word.startsWith("meny") && word.length() > 5) return word.substring(3);   // meny → s
        if (word.startsWith("meng") && word.length() > 5) return word.substring(4);
        if (word.startsWith("men")  && word.length() > 4) return word.substring(3);
        if (word.startsWith("mem")  && word.length() > 4) return word.substring(3);
        if (word.startsWith("me")   && word.length() > 3) return word.substring(2);

        // pe- / pem- / pen- / peng- / peny-
        if (word.startsWith("peny") && word.length() > 5) return word.substring(3);
        if (word.startsWith("peng") && word.length() > 5) return word.substring(4);
        if (word.startsWith("pen")  && word.length() > 4) return word.substring(3);
        if (word.startsWith("pem")  && word.length() > 4) return word.substring(3);
        if (word.startsWith("pe")   && word.length() > 3) return word.substring(2);

        // ber- / be-
        if (word.startsWith("ber") && word.length() > 4) return word.substring(3);
        if (word.startsWith("be")  && word.length() > 3) return word.substring(2);

        // ter-
        if (word.startsWith("ter") && word.length() > 4) return word.substring(3);

        // di-
        if (word.startsWith("di") && word.length() > 3) return word.substring(2);

        // ke-
        if (word.startsWith("ke") && word.length() > 3) return word.substring(2);

        // se-
        if (word.startsWith("se") && word.length() > 3) return word.substring(2);

        return word;
    }
}