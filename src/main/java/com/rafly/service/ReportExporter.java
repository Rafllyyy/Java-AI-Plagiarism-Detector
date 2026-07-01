package com.rafly.service;

import com.rafly.model.HighlightResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportExporter {

    private static final float MARGIN     = 50f;
    private static final float PAGE_W     = PDRectangle.A4.getWidth();
    private static final float PAGE_H     = PDRectangle.A4.getHeight();
    private static final float USABLE_W   = PAGE_W - 2 * MARGIN;

    // PDFBox 3.x: font dibuat via Standard14Fonts.FontName
    private static PDType1Font bold(PDDocument doc) {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    }
    private static PDType1Font normal(PDDocument doc) {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }
    private static PDType1Font oblique(PDDocument doc) {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
    }

    public File export(
            String docAName,
            String docBName,
            String algorithm,
            double similarity,
            List<HighlightResult> highlights,
            File outputDir) throws IOException {

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String dateStr   = new SimpleDateFormat("dd MMMM yyyy, HH:mm").format(new Date());
        File   outFile   = new File(outputDir, "plagiarism_report_" + timestamp + ".pdf");

        try (PDDocument doc = new PDDocument()) {
            PageWriter w = new PageWriter(doc);

            // Judul
            w.line("LAPORAN DETEKSI PLAGIARISME", bold(doc), 16, true);
            w.space(4);
            w.line("Tanggal   : " + dateStr, normal(doc), 10, false);
            w.line("Algoritma : " + algorithm, normal(doc), 10, false);
            w.space(6);  w.hr();  w.space(8);

            // Info dokumen
            w.line("INFORMASI DOKUMEN", bold(doc), 12, false);
            w.space(4);
            w.line("Dokumen A : " + docAName, normal(doc), 10, false);
            w.line("Dokumen B : " + docBName, normal(doc), 10, false);
            w.space(10);

            // Skor
            double pct = similarity * 100;
            w.line("HASIL SIMILARITY", bold(doc), 12, false);
            w.space(4);
            w.line(String.format("Skor   : %.2f%%", pct), bold(doc), 14, false);
            w.line("Status : " + resolveStatus(pct), bold(doc), 12, false);
            w.space(4);
            w.line(resolveDesc(pct), oblique(doc), 9, false);
            w.space(10);  w.hr();  w.space(8);

            // Kalimat mirip
            w.line("KALIMAT YANG TERDETEKSI MIRIP", bold(doc), 12, false);
            w.space(4);
            if (highlights == null || highlights.isEmpty()) {
                w.line("Tidak ada kalimat yang terdeteksi mirip.", oblique(doc), 10, false);
            } else {
                int i = 1;
                for (HighlightResult h : highlights) {
                    w.space(4);
                    w.line("[" + i++ + "]  Similarity: "
                            + String.format("%.2f%%", h.getSimilarity() * 100),
                            bold(doc), 10, false);
                    w.line("  Dok A: " + h.getSentenceA(), normal(doc), 9, false);
                    w.line("  Dok B: " + h.getSentenceB(), normal(doc), 9, false);
                }
            }

            // Footer
            w.space(16);  w.hr();  w.space(4);
            w.line("Dibuat oleh: Aplikasi Deteksi Plagiarisme — " + dateStr,
                    oblique(doc), 8, false);

            w.close();
            doc.save(outFile);
        }

        return outFile;
    }

    private String resolveStatus(double pct) {
        if (pct >= 80) return "PLAGIAT";
        if (pct >= 50) return "MENCURIGAKAN";
        return "AMAN";
    }

    private String resolveDesc(double pct) {
        if (pct >= 80) return "Tingkat kemiripan sangat tinggi. Indikasi kuat adanya plagiarisme.";
        if (pct >= 50) return "Kemiripan cukup signifikan. Disarankan diperiksa lebih lanjut.";
        return "Tidak menunjukkan indikasi plagiarisme yang berarti.";
    }

    // ── Inner class: cursor & auto page-break ────────────────────────

    private static class PageWriter {
        private final PDDocument doc;
        private PDPage page;
        private PDPageContentStream cs;
        private float y;

        PageWriter(PDDocument doc) throws IOException {
            this.doc = doc;
            newPage();
        }

        private void newPage() throws IOException {
            if (cs != null) cs.close();
            page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y  = PAGE_H - MARGIN;
        }

        void line(String text, PDType1Font font, float size, boolean center)
                throws IOException {
            for (String ln : wrap(text, font, size, USABLE_W)) {
                if (y < MARGIN + 20) newPage();
                float x = center ? (PAGE_W - width(ln, font, size)) / 2 : MARGIN;
                cs.beginText();
                cs.setFont(font, size);
                cs.newLineAtOffset(x, y);
                cs.showText(safe(ln));
                cs.endText();
                y -= size * 1.4f;
            }
        }

        void space(float pt) throws IOException {
            if (y < MARGIN + 20) newPage();
            y -= pt;
        }

        void hr() throws IOException {
            if (y < MARGIN + 20) newPage();
            cs.setLineWidth(0.5f);
            cs.moveTo(MARGIN, y);
            cs.lineTo(PAGE_W - MARGIN, y);
            cs.stroke();
            y -= 6;
        }

        void close() throws IOException { if (cs != null) cs.close(); }

        private String safe(String s) { return s.replaceAll("[^\\x20-\\x7E]", "?"); }

        private float width(String s, PDType1Font f, float sz) throws IOException {
            return f.getStringWidth(safe(s)) / 1000 * sz;
        }

        private List<String> wrap(String text, PDType1Font f, float sz, float max)
                throws IOException {
            List<String> lines = new ArrayList<>();
            StringBuilder cur  = new StringBuilder();
            for (String w : text.split(" ")) {
                String cand = cur.length() == 0 ? w : cur + " " + w;
                if (width(cand, f, sz) > max) {
                    if (cur.length() > 0) lines.add(cur.toString());
                    cur = new StringBuilder(w);
                } else {
                    cur = new StringBuilder(cand);
                }
            }
            if (cur.length() > 0) lines.add(cur.toString());
            return lines;
        }
    }
}