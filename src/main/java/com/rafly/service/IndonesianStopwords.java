package com.rafly.service;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Menyimpan daftar stopword Bahasa Indonesia.
 * Tanggung jawab tunggal: hanya menyediakan kumpulan kata yang tidak bermakna
 * secara semantik dan sebaiknya diabaikan saat analisis teks.
 */
public class IndonesianStopwords {

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        // Kata ganti
        "saya", "aku", "kamu", "anda", "kita", "kami", "mereka", "dia", "ia",
        "nya", "ku", "mu",
        // Kata penunjuk
        "ini", "itu", "sini", "sana", "situ",
        // Kata sambung
        "dan", "atau", "tetapi", "namun", "sedangkan", "melainkan", "bahwa",
        "karena", "sebab", "sehingga", "agar", "supaya", "jika", "kalau",
        "bila", "ketika", "saat", "sejak", "sampai", "hingga", "setelah",
        "sebelum", "selama", "meskipun", "walaupun", "meski", "walau",
        // Kata depan / preposisi
        "di", "ke", "dari", "pada", "untuk", "dengan", "oleh", "tentang",
        "terhadap", "dalam", "antara", "bagi", "demi", "per", "tanpa",
        // Kata keterangan umum
        "sudah", "telah", "sedang", "akan", "belum", "tidak", "bukan",
        "juga", "pun", "hanya", "saja", "bahkan", "justru", "malah",
        "selalu", "kadang", "sering", "jarang", "mungkin", "pasti",
        "tentu", "memang", "masih", "lagi", "lebih", "sangat", "cukup",
        "terlalu", "agak", "hampir", "sekitar", "kira", "kiranya",
        // Kata tanya
        "apa", "siapa", "kapan", "dimana", "bagaimana", "mengapa", "kenapa",
        "berapa", "mana",
        // Kata bilangan & urutan
        "satu", "dua", "tiga", "empat", "lima", "enam", "tujuh", "delapan",
        "sembilan", "sepuluh", "pertama", "kedua", "ketiga",
        // Lain-lain
        "ada", "adalah", "merupakan", "yaitu", "yakni", "seperti", "sebagai",
        "tersebut", "hal", "cara", "jenis", "semua", "setiap", "berbagai",
        "beberapa", "banyak", "sedikit", "seluruh", "masing", "antara",
        "lain", "lainnya", "dll", "dsb", "dst", "vs", "yang", "yg"
    ));

    private IndonesianStopwords() {
        // utility class — tidak perlu diinstansiasi
    }

    /** Mengembalikan true jika kata termasuk stopword. */
    public static boolean isStopword(String word) {
        return STOPWORDS.contains(word.toLowerCase());
    }

    /** Mengembalikan salinan Set stopword (read-only intent). */
    public static Set<String> getAll() {
        return new HashSet<>(STOPWORDS);
    }
}