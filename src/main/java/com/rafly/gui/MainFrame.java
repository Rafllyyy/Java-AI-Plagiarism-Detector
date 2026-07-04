package com.rafly.gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.rafly.controller.DetectorController;
import com.rafly.model.ComparisonResult;
import com.rafly.model.HighlightResult;
import com.rafly.model.HistoryEntry;
import com.rafly.service.ComparisonHistoryManager;
import com.rafly.service.DocumentLoader;
import com.rafly.service.ReportExporter;
import com.rafly.service.WordHighlighter;

public class MainFrame extends JFrame {

    // ── Services ─────────────────────────────────────────────────────
    private final DocumentLoader loader = new DocumentLoader();
    private final DetectorController controller = new DetectorController();
    private final WordHighlighter wordHighlighter = new WordHighlighter();
    private final ReportExporter reportExporter = new ReportExporter();
    private final ComparisonHistoryManager historyManager = new ComparisonHistoryManager();

    // ── State ─────────────────────────────────────────────────────────
    private String documentAText = "";
    private String documentBText = "";

    private String lastAlgorithm = "";
    private double lastSimilarity = 0.0;
    private List<HighlightResult> lastHighlights = new ArrayList<>();

    // ── Komponen GUI ──────────────────────────────────────────────────
    private JTextField txtDocumentA;
    private JTextField txtDocumentB;
    private JButton btnBrowseA;
    private JButton btnBrowseB;
    private JButton btnCompare;
    private JButton btnCompareFolder;
    private JButton btnExport;
    private JLabel lblAIStatus;

    private JComboBox<String> cmbAlgorithm;
    private JLabel lblSimilarity;

    private JTextPane txtPreviewA;
    private JTextPane txtPreviewB;

    private JTable tblResult;
    private DefaultTableModel tableModel;

    private JTable tblHistory;
    private DefaultTableModel historyModel;

    private static final int COL_FILENAME = 0;
    private static final int COL_SIMILARITY = 1;
    private static final int COL_STATUS = 2;

    // ── Constructor ───────────────────────────────────────────────────
    public MainFrame() {
        setTitle("Aplikasi Deteksi Plagiarisme Dokumen");
        setSize(950, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.add(buildTopPanel(), BorderLayout.NORTH);
        main.add(buildCenterPanel(), BorderLayout.CENTER);
        add(main);

        btnBrowseA.addActionListener(e -> browseDocument(txtDocumentA, txtPreviewA, true));
        btnBrowseB.addActionListener(e -> browseDocument(txtDocumentB, txtPreviewB, false));
        btnCompare.addActionListener(e -> compareDocuments());
        btnCompareFolder.addActionListener(e -> compareFolder());
        btnExport.addActionListener(e -> exportReport());
        cmbAlgorithm.addActionListener(e -> {
            if ("AI (Semantic)".equals(cmbAlgorithm.getSelectedItem())) {
                checkAIStatus();
            }
        });

        refreshHistoryTable();
        checkAIStatus();
    }

    private void checkAIStatus() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return controller.isAIAvailable();
            }

            @Override
            protected void done() {
                try {
                    boolean available = get();
                    if (available) {
                        lblAIStatus.setText("⬤ AI Online");
                        lblAIStatus.setForeground(new Color(0, 150, 0));
                    } else {
                        lblAIStatus.setText("⬤ AI Offline");
                        lblAIStatus.setForeground(Color.GRAY);
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0;
        g.gridx = 0;
        panel.add(new JLabel("Dokumen A:"), g);
        txtDocumentA = new JTextField(35);
        g.gridx = 1;
        g.weightx = 1.0;
        panel.add(txtDocumentA, g);
        btnBrowseA = new JButton("Browse");
        g.gridx = 2;
        g.weightx = 0;
        panel.add(btnBrowseA, g);

        g.gridy = 1;
        g.gridx = 0;
        panel.add(new JLabel("Dokumen B:"), g);
        txtDocumentB = new JTextField(35);
        g.gridx = 1;
        g.weightx = 1.0;
        panel.add(txtDocumentB, g);
        btnBrowseB = new JButton("Browse");
        g.gridx = 2;
        g.weightx = 0;
        panel.add(btnBrowseB, g);

        g.gridy = 2;
        g.gridx = 0;
        panel.add(new JLabel("Algoritma:"), g);
        cmbAlgorithm = new JComboBox<>(new String[] {
                "Jaccard", "Cosine", "Levenshtein", "N-Gram", "AI (Semantic)"
        });
        g.gridx = 1;
        g.weightx = 1.0;
        panel.add(cmbAlgorithm, g);
        btnCompare = new JButton("Bandingkan");
        g.gridx = 2;
        g.weightx = 0;
        panel.add(btnCompare, g);
        btnCompareFolder = new JButton("Bandingkan Folder");
        g.gridx = 3;
        panel.add(btnCompareFolder, g);
        btnExport = new JButton("Export PDF");
        btnExport.setEnabled(false);
        g.gridx = 4;
        panel.add(btnExport, g);
        lblAIStatus = new JLabel("⬤ AI Offline");
        lblAIStatus.setForeground(Color.GRAY);
        lblAIStatus.setFont(new Font("Arial", Font.PLAIN, 11));
        g.gridx = 5;
        panel.add(lblAIStatus, g);

        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // Label similarity
        lblSimilarity = new JLabel("Similarity : —");
        lblSimilarity.setFont(new Font("Arial", Font.BOLD, 18));
        JPanel resultRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resultRow.add(lblSimilarity);
        panel.add(resultRow, BorderLayout.NORTH);

        // Preview side-by-side
        txtPreviewA = new JTextPane();
        txtPreviewA.setEditable(false);
        txtPreviewB = new JTextPane();
        txtPreviewB.setEditable(false);
        JSplitPane splitH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(txtPreviewA), new JScrollPane(txtPreviewB));
        splitH.setResizeWeight(0.5);
        splitH.setDividerLocation(450);

        // Preview di atas, tabel di bawah — bisa digeser
        JSplitPane splitV = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                splitH, buildBottomPanel());
        splitV.setResizeWeight(0.55);
        splitV.setDividerLocation(280);

        panel.add(splitV, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 4));
        panel.add(buildBatchTablePanel());
        panel.add(buildHistoryPanel());
        return panel;
    }

    private JScrollPane buildBatchTablePanel() {
        tableModel = new DefaultTableModel(
                new String[] { "Nama File", "Similarity (%)", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblResult = new JTable(tableModel);
        tblResult.setRowHeight(26);
        tblResult.getColumnModel().getColumn(COL_FILENAME).setPreferredWidth(380);
        tblResult.getColumnModel().getColumn(COL_SIMILARITY).setPreferredWidth(130);
        tblResult.getColumnModel().getColumn(COL_STATUS).setPreferredWidth(120);
        tblResult.setDefaultRenderer(Object.class, new BatchTableRenderer());

        JScrollPane sp = new JScrollPane(tblResult);
        sp.setPreferredSize(new Dimension(900, 130));
        sp.setBorder(BorderFactory.createTitledBorder("Hasil Batch Comparison"));
        return sp;
    }

    private JPanel buildHistoryPanel() {
        historyModel = new DefaultTableModel(
                new String[] { "Tanggal", "Dokumen A", "Dokumen B", "Algoritma", "Similarity (%)" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblHistory = new JTable(historyModel);
        tblHistory.setRowHeight(24);
        tblHistory.getColumnModel().getColumn(0).setPreferredWidth(150);
        tblHistory.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblHistory.getColumnModel().getColumn(2).setPreferredWidth(180);
        tblHistory.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblHistory.getColumnModel().getColumn(4).setPreferredWidth(110);

        JButton btnClear = new JButton("Hapus Riwayat");
        btnClear.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Hapus semua riwayat?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                historyManager.clearAll();
                refreshHistoryTable();
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.add(btnClear);

        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Riwayat Perbandingan"));
        panel.add(new JScrollPane(tblHistory), BorderLayout.CENTER);
        panel.add(btnRow, BorderLayout.SOUTH);
        return panel;
    }

    // ── Aksi ─────────────────────────────────────────────────────────

    private void browseDocument(JTextField field, JTextPane preview, boolean isDocA) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Dokumen (*.txt, *.docx, *.pdf)", "txt", "docx", "pdf"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile();
        field.setText(file.getAbsolutePath());
        try {
            String text = loader.load(file);
            preview.setText(text);
            if (isDocA)
                documentAText = text;
            else
                documentBText = text;
        } catch (IOException ex) {
            showError("Gagal memuat dokumen: " + ex.getMessage());
        }
    }

    private void compareDocuments() {
        if (documentAText.isEmpty() || documentBText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Silakan pilih kedua dokumen terlebih dahulu.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String algorithm = cmbAlgorithm.getSelectedItem().toString();
        double similarity;
        try {
            similarity = controller.compare(algorithm, documentAText, documentBText);
        } catch (Exception ex) {
            showError(ex.getMessage());
            return;
        }
        lblSimilarity.setText(String.format("Similarity : %.2f%%", similarity * 100));

        List<HighlightResult> highlights = controller.getHighlights(documentAText, documentBText, algorithm);
        highlightText(txtPreviewA, documentAText, highlights, true);
        highlightText(txtPreviewB, documentBText, highlights, false);

        lastAlgorithm = algorithm;
        lastSimilarity = similarity;
        lastHighlights = highlights;
        btnExport.setEnabled(true);

        // Simpan ke riwayat otomatis
        String nameA = new File(txtDocumentA.getText()).getName();
        String nameB = new File(txtDocumentB.getText()).getName();
        historyManager.add(nameA, nameB, algorithm, similarity);
        refreshHistoryTable();
    }

    private void compareFolder() {
        if (documentAText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Pilih Dokumen A sebagai sumber perbandingan terlebih dahulu.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Pilih Folder Dokumen Pembanding");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File folder = chooser.getSelectedFile();
        String algorithm = cmbAlgorithm.getSelectedItem().toString();

        tableModel.setRowCount(0);
        lblSimilarity.setText("Memproses...");
        btnCompareFolder.setEnabled(false);

        new SwingWorker<List<ComparisonResult>, Void>() {
            @Override
            protected List<ComparisonResult> doInBackground() {
                return controller.compareFolder(algorithm, documentAText, folder);
            }

            @Override
            protected void done() {
                try {
                    List<ComparisonResult> results = get();
                    tableModel.setRowCount(0);
                    for (ComparisonResult r : results) {
                        double pct = r.getSimilarity() * 100;
                        tableModel.addRow(new Object[] {
                                r.getFileName(),
                                String.format("%.2f%%", pct),
                                resolveStatus(pct)
                        });
                    }
                    lblSimilarity.setText("Batch selesai — " + results.size() + " file diproses");
                } catch (Exception ex) {
                    showError("Batch comparison gagal: " + ex.getMessage());
                    lblSimilarity.setText("Similarity : —");
                } finally {
                    btnCompareFolder.setEnabled(true);
                }
            }
        }.execute();
    }

    private void exportReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Pilih folder penyimpanan laporan");
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        String docAName = new File(txtDocumentA.getText()).getName();
        String docBName = new File(txtDocumentB.getText()).getName();

        try {
            File result = reportExporter.export(
                    docAName, docBName,
                    lastAlgorithm, lastSimilarity,
                    lastHighlights, chooser.getSelectedFile());

            JOptionPane.showMessageDialog(this,
                    "Laporan berhasil disimpan:\n" + result.getAbsolutePath(),
                    "Export Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            showError("Gagal membuat PDF: " + ex.getMessage());
        }
    }

    // ── History ───────────────────────────────────────────────────────

    private void refreshHistoryTable() {
        historyModel.setRowCount(0);
        for (HistoryEntry e : historyManager.getAll()) {
            historyModel.addRow(new Object[] {
                    e.getDate(),
                    e.getDocAName(),
                    e.getDocBName(),
                    e.getAlgorithm(),
                    String.format("%.2f%%", e.getSimilarity() * 100)
            });
        }
    }

    // ── Highlight ─────────────────────────────────────────────────────

    private void highlightText(JTextPane pane, String originalText,
            List<HighlightResult> highlights, boolean isDocA) {
        pane.setText(originalText);
        StyledDocument doc = pane.getStyledDocument();

        // Reset semua ke putih
        SimpleAttributeSet normal = new SimpleAttributeSet();
        StyleConstants.setBackground(normal, Color.WHITE);
        doc.setCharacterAttributes(0, doc.getLength(), normal, true);

        SimpleAttributeSet yellow = new SimpleAttributeSet();
        StyleConstants.setBackground(yellow, Color.YELLOW);

        // Ambil kata-kata yang sama dari SELURUH kedua dokumen
        String otherText = isDocA ? documentBText : documentAText;
        Set<String> commonWords = wordHighlighter.getCommonWords(originalText, otherText);

        // Highlight setiap kemunculan kata tersebut di seluruh teks
        for (String word : commonWords) {
            int idx = 0;
            while ((idx = originalText.indexOf(word, idx)) >= 0) {
                // Pastikan ini kata utuh (bukan bagian dari kata lain)
                boolean startOk = (idx == 0) || !Character.isLetter(originalText.charAt(idx - 1));
                boolean endOk = (idx + word.length() >= originalText.length())
                        || !Character.isLetter(originalText.charAt(idx + word.length()));
                if (startOk && endOk) {
                    try {
                        doc.setCharacterAttributes(idx, word.length(), yellow, false);
                    } catch (Exception ignored) {
                    }
                }
                idx += word.length();
            }
        }
    }

    // ── Helper ────────────────────────────────────────────────────────

    private String resolveStatus(double pct) {
        if (pct >= 80)
            return "Plagiat";
        if (pct >= 50)
            return "Mencurigakan";
        return "Aman";
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Inner class renderer ──────────────────────────────────────────

    private class BatchTableRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);
            if (!isSelected) {
                String status = (String) tableModel.getValueAt(row, COL_STATUS);
                switch (status) {
                    case "Plagiat":
                        c.setBackground(new Color(255, 180, 180));
                        break;
                    case "Mencurigakan":
                        c.setBackground(new Color(255, 235, 150));
                        break;
                    default:
                        c.setBackground(new Color(200, 240, 200));
                        break;
                }
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }
}