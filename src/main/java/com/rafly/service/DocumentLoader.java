package com.rafly.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class DocumentLoader {

    public String load(File file) throws IOException {

        String name = file.getName().toLowerCase();

        if (name.endsWith(".txt")) {

            return readTxt(file);

        }

        if (name.endsWith(".docx")) {

            return readDocx(file);

        }

        if (name.endsWith(".pdf")) {

            return readPdf(file);

        }

        throw new IOException("Format file tidak didukung.");

    }

    private String readTxt(File file) throws IOException {

        return Files.readString(file.toPath());

    }

    private String readDocx(File file) throws IOException {

        StringBuilder sb = new StringBuilder();

        try (XWPFDocument document =
                     new XWPFDocument(Files.newInputStream(file.toPath()))) {

            for (XWPFParagraph p : document.getParagraphs()) {

                sb.append(p.getText()).append("\n");

            }

        }

        return sb.toString();

    }

    private String readPdf(File file) throws IOException {

        try (PDDocument document = Loader.loadPDF(file)) {

            PDFTextStripper stripper = new PDFTextStripper();

            return stripper.getText(document);

        }

    }

}