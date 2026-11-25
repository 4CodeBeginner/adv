package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import model.PenyaluranPip;
import model.ProvinsiSummary;
import repository.PipRepository;

public class MainDashboardFrame extends JFrame {

    private final PipRepository repository;

    private JTable tableProvinsi;
    private DefaultTableModel modelProvinsi;
    private JTextField txtSearch;
    private JLabel lblTotalRecords;

    private JLabel lblGrandTotalSiswa;
    private JLabel lblGrandTotalDana;
    private final Map<String, JLabel> lblJenjangStats = new HashMap<>();

    public MainDashboardFrame(PipRepository repository) {
        this.repository = repository;

        setTitle("Sistem Manajemen Data Penyaluran PIP 2026 - Admin Panel");
        setSize(1340, 750);
        setMinimumSize(new Dimension(1120, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UiTheme.BG_MAIN);
        setLayout(new BorderLayout(0, 12));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel mainContainer = new JPanel(new BorderLayout(0, 14));
        mainContainer.setBackground(UiTheme.BG_MAIN);
        mainContainer.setBorder(new EmptyBorder(4, 20, 16, 20));

        mainContainer.add(createSummaryPanel(), BorderLayout.NORTH);
        mainContainer.add(createMasterTableCardPanel(), BorderLayout.CENTER);

        add(mainContainer, BorderLayout.CENTER);

        refreshAllWithLoading("Menghubungkan & memuat data PIP dari Aiven Cloud...");
    }

    private void refreshAllWithLoading(String loadingText) {
        String keyword = txtSearch != null ? txtSearch.getText().trim() : "";

        LoadingGlassPane.runWithLoading(this, loadingText, () -> {
            List<ProvinsiSummary> summaries = repository.findProvinsiSummaries(keyword);
            Map<String, long[]> stats = repository.getNasionalSummaryByJenjang();
            return new Object[]{summaries, stats};
        }, result -> {
            @SuppressWarnings("unchecked")
            List<ProvinsiSummary> summaries = (List<ProvinsiSummary>) result[0];
            @SuppressWarnings("unchecked")
            Map<String, long[]> stats = (Map<String, long[]>) result[1];

            applyProvinsiRowsToTable(summaries);
            applySummaryToCards(stats);
        });
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiTheme.PRIMARY_DARK);
        header.setBorder(new EmptyBorder(14, 24, 14, 24));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("DASHBOARD PENYALURAN PROGRAM INDONESIA PINTAR (PIP) 2026");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Rekapitulasi per Provinsi, Rincian Jenjang (SD - SMK) & Mutasi Status Siswa  |  Aiven Cloud MySQL");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        titleBox.add(lblTitle);
        titleBox.add(lblSub);
        header.add(titleBox, BorderLayout.WEST);

        JLabel lblBadge = new JLabel("  STATUS: ONLINE (AIVEN)  ");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setBackground(UiTheme.ACCENT_GREEN);
        lblBadge.setOpaque(true);
        lblBadge.setBorder(new EmptyBorder(6, 10, 6, 10));

        JPanel rightBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        rightBadge.setOpaque(false);
        rightBadge.add(lblBadge);
        header.add(rightBadge, BorderLayout.EAST);

        return header;
    }

    private JPanel createSummaryPanel() {
        JPanel grid = new JPanel(new GridLayout(1, 5, 12, 0));
        grid.setOpaque(false);

        JPanel totalCard = createBaseCard(UiTheme.ACCENT_BLUE);
        JLabel tTitle = new JLabel("TOTAL NASIONAL (SEMUA JENJANG)");
        tTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tTitle.setForeground(new Color(100, 116, 139));

        lblGrandTotalSiswa = new JLabel("Memuat...");
        lblGrandTotalSiswa.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblGrandTotalSiswa.setForeground(UiTheme.PRIMARY_DARK);

        lblGrandTotalDana = new JLabel("Rp 0");
        lblGrandTotalDana.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGrandTotalDana.setForeground(UiTheme.ACCENT_BLUE);

        totalCard.add(tTitle);
        totalCard.add(lblGrandTotalSiswa);
        totalCard.add(lblGrandTotalDana);
        grid.add(totalCard);

        String[] jenjangList = {"SD", "SMP", "SMA", "SMK"};
        Color[] colors = {
                new Color(14, 165, 233),
                new Color(16, 185, 129),
                new Color(245, 158, 11),
                new Color(139, 92, 246)
        };

        for (int i = 0; i < jenjangList.length; i++) {
            String j = jenjangList[i];
            JPanel card = createBaseCard(colors[i]);

            JLabel lblHead = new JLabel("JENJANG " + j);
            lblHead.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblHead.setForeground(new Color(100, 116, 139));

            JLabel lblVal = new JLabel("<html><b>0 Siswa</b><br><span style='color:#475569;font-size:11px'>Rp 0</span></html>");
            lblVal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblVal.setForeground(UiTheme.PRIMARY_DARK);

            lblJenjangStats.put(j, lblVal);

            card.add(lblHead);
            card.add(lblVal);
            grid.add(card);
        }

        return grid;
    }

    private JPanel createBaseCard(Color topAccent) {
        JPanel card = new JPanel();
        card.setLayout(new GridLayout(0, 1, 0, 3));
        card.setBackground(UiTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 1, 1, 1, new Color(
                        topAccent.getRed(), topAccent.getGreen(), topAccent.getBlue(), 180)),
                new EmptyBorder(10, 14, 10, 14)
        ));
        return card;
    }

    private JPanel createMasterTableCardPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(UiTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_GRAY, 1),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTools.setOpaque(false);

        JLabel lblCari = new JLabel("Cari Provinsi:");
        lblCari.setFont(new Font("Segoe UI", Font.BOLD, 12));

        txtSearch = new JTextField(22);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(240, 32));
        txtSearch.setToolTipText("Ketik nama provinsi lalu tekan Enter atau klik Cari...");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchProvinsiWithLoading();
                }
            }
        });

        JButton btnCari = UiTheme.createStyledButton("Cari", UiTheme.PRIMARY_DARK);
        btnCari.setPreferredSize(new Dimension(75, 32));
        btnCari.addActionListener(e -> searchProvinsiWithLoading());

        JButton btnRefresh = UiTheme.createStyledButton("Refresh", new Color(100, 116, 139));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            refreshAllWithLoading("Menyegarkan data dari Aiven Cloud...");
        });

        leftTools.add(lblCari);
        leftTools.add(txtSearch);
        leftTools.add(btnCari);
        leftTools.add(btnRefresh);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.setOpaque(false);

        JButton btnTambah = UiTheme.createStyledButton("+ Tambah Provinsi (SD - SMA)", UiTheme.ACCENT_BLUE);
        btnTambah.setPreferredSize(new Dimension(225, 34));
        btnTambah.addActionListener(e -> openCreateProvinsiBatchDialog());

        rightTools.add(btnTambah);

        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);

        String[] columns = {
                "No", "Provinsi", "Jml Jenjang", "Siswa Pemberian", "Siswa Aktivasi",
                "Siswa Relaksasi", "Total Siswa", "Total Dana (Rp)", "Aksi"
        };

        modelProvinsi = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8;
            }
        };

        tableProvinsi = new JTable(modelProvinsi);
        TableHelper.styleExcelTable(tableProvinsi);

        TableHelper.ExcelCellRenderer centerRenderer = new TableHelper.ExcelCellRenderer(SwingConstants.CENTER, false);
        TableHelper.ExcelCellRenderer leftRenderer = new TableHelper.ExcelCellRenderer(SwingConstants.LEFT, true);
        TableHelper.ExcelCellRenderer rightRenderer = new TableHelper.ExcelCellRenderer(SwingConstants.RIGHT, false);
        TableHelper.ExcelCellRenderer boldRightRenderer = new TableHelper.ExcelCellRenderer(SwingConstants.RIGHT, true);

        TableColumnModel cm = tableProvinsi.getColumnModel();
        cm.getColumn(0).setCellRenderer(centerRenderer);
        cm.getColumn(1).setCellRenderer(leftRenderer);
        cm.getColumn(2).setCellRenderer(centerRenderer);
        cm.getColumn(3).setCellRenderer(rightRenderer);
        cm.getColumn(4).setCellRenderer(rightRenderer);
        cm.getColumn(5).setCellRenderer(rightRenderer);
        cm.getColumn(6).setCellRenderer(boldRightRenderer);
        cm.getColumn(7).setCellRenderer(boldRightRenderer);
        cm.getColumn(8).setCellRenderer(new MasterActionRenderer());
        cm.getColumn(8).setCellEditor(new MasterActionEditor());

        TableHelper.autoFitTableColumns(tableProvinsi, 8, 175);

        JScrollPane scrollPane = new JScrollPane(tableProvinsi);
        scrollPane.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_GRAY, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        lblTotalRecords = new JLabel("Menampilkan 0 provinsi");
        lblTotalRecords.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTotalRecords.setForeground(new Color(71, 85, 105));
        footer.add(lblTotalRecords, BorderLayout.WEST);

        JLabel lblHint = new JLabel("Klik 'Detail' untuk rincian SD - SMK, atau 'Hapus' untuk menghapus provinsi beserta seluruh datanya");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(new Color(100, 116, 139));
        footer.add(lblHint, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private void applyProvinsiRowsToTable(List<ProvinsiSummary> list) {
        modelProvinsi.setRowCount(0);
        int no = 0;
        for (ProvinsiSummary s : list) {
            no++;
            modelProvinsi.addRow(new Object[]{
                    no,
                    s.getProvinsi(),
                    s.getJumlahJenjang() + " Jenjang",
                    UiTheme.NF.format(s.getTotalPemberian()),
                    UiTheme.NF.format(s.getTotalAktivasi()),
                    UiTheme.NF.format(s.getTotalRelaksasi()),
                    UiTheme.NF.format(s.getTotalSiswa()),
                    "Rp " + UiTheme.NF.format(s.getTotalDana()),
                    "AKSI"
            });
        }
        TableHelper.autoFitTableColumns(tableProvinsi, 8, 175);
        lblTotalRecords.setText("Menampilkan " + list.size() + " Provinsi");
    }

    private void searchProvinsiWithLoading() {
        String keyword = txtSearch.getText().trim();
        LoadingGlassPane.runWithLoading(this, "Mencari data provinsi...", () ->
                repository.findProvinsiSummaries(keyword),
                this::applyProvinsiRowsToTable
        );
    }

    private void applySummaryToCards(Map<String, long[]> summary) {
        long grandSiswa = 0;
        long grandDana = 0;

        for (Map.Entry<String, JLabel> entry : lblJenjangStats.entrySet()) {
            String jenjang = entry.getKey();
            long[] vals = summary.getOrDefault(jenjang, new long[]{0L, 0L});
            grandSiswa += vals[0];
            grandDana += vals[1];

            entry.getValue().setText("<html><b>" + UiTheme.NF.format(vals[0]) + " Siswa</b><br>"
                    + "<span style='color:#475569;font-size:11px'>Rp " + UiTheme.NF.format(vals[1]) + "</span></html>");
        }

        lblGrandTotalSiswa.setText(UiTheme.NF.format(grandSiswa) + " Siswa");
        lblGrandTotalDana.setText("Rp " + UiTheme.NF.format(grandDana));
    }

    private void deleteProvinsiWithValidation(String provinsi) {
        String expectedPhrase = "hapus " + provinsi;
        String warningHtml = "<html><b>PERINGATAN:</b> Tindakan ini akan menghapus seluruh data jenjang pada <b>"
                + provinsi + "</b> secara permanen.</html>";

        if (!UiTheme.confirmWithTypedText(this, "Validasi Hapus Provinsi", warningHtml, expectedPhrase)) {
            return;
        }

        LoadingGlassPane.runWithLoading(this, "Menghapus data provinsi " + provinsi + "...", () -> {
            int deleted = repository.deleteByProvinsi(provinsi);
            List<ProvinsiSummary> summaries = repository.findProvinsiSummaries(txtSearch.getText().trim());
            Map<String, long[]> stats = repository.getNasionalSummaryByJenjang();
            return new Object[]{deleted, summaries, stats};
        }, res -> {
            int deleted = (int) res[0];
            @SuppressWarnings("unchecked")
            List<ProvinsiSummary> summaries = (List<ProvinsiSummary>) res[1];
            @SuppressWarnings("unchecked")
            Map<String, long[]> stats = (Map<String, long[]>) res[2];

            applyProvinsiRowsToTable(summaries);
            applySummaryToCards(stats);
            JOptionPane.showMessageDialog(
                    this,
                    "Provinsi \"" + provinsi + "\" (" + deleted + " baris jenjang) berhasil dihapus.",
                    "Sukses Hapus Provinsi",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    private void openCreateProvinsiBatchDialog() {
        BaseModalDialog dialog = new BaseModalDialog(this,
                "Tambah Provinsi Baru & Pengaturan Anggaran Awal (SD - SMA)", 980, 440);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
        topPanel.setBackground(new Color(248, 250, 252));
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER_GRAY));

        JLabel lblProv = new JLabel("Nama Provinsi Baru:");
        lblProv.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JTextField txtProvinsi = new JTextField(28);
        txtProvinsi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtProvinsi.setPreferredSize(new Dimension(280, 32));
        txtProvinsi.setToolTipText("Contoh: Prov. Kalimantan Utara");

        JLabel lblNote = new JLabel("  (Jenjang SD, SMP, dan SMA wajib diisi; centang SMK jika ingin sekalian ditambahkan)");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblNote.setForeground(new Color(100, 116, 139));

        topPanel.add(lblProv);
        topPanel.add(txtProvinsi);
        topPanel.add(lblNote);

        String[] jenjangList = {"SD", "SMP", "SMA", "SMK"};
        JPanel gridPanel = new JPanel(new GridLayout(5, 7, 8, 10));
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(new EmptyBorder(16, 18, 16, 18));

        String[] headers = {
                "Jenjang", "Siswa Pemberian", "Siswa Aktivasi", "Siswa Relaksasi",
                "Anggaran Pemberian (Rp)", "Anggaran Aktivasi (Rp)", "Anggaran Relaksasi (Rp)"
        };

        for (String h : headers) {
            JLabel lblH = new JLabel(h, SwingConstants.CENTER);
            lblH.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblH.setForeground(UiTheme.PRIMARY_DARK);
            gridPanel.add(lblH);
        }

        JCheckBox[] chkInclude = new JCheckBox[4];
        JTextField[][] fields = new JTextField[4][6];

        for (int r = 0; r < 4; r++) {
            String j = jenjangList[r];
            chkInclude[r] = new JCheckBox(j, r < 3);
            chkInclude[r].setFont(new Font("Segoe UI", Font.BOLD, 13));
            chkInclude[r].setBackground(Color.WHITE);
            if (r < 3) chkInclude[r].setEnabled(false);
            gridPanel.add(chkInclude[r]);

            final int rowIdx = r;
            for (int c = 0; c < 6; c++) {
                JTextField tf = new JTextField("0");
                tf.setHorizontalAlignment(SwingConstants.RIGHT);
                tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                tf.setEnabled(r < 3);
                fields[r][c] = tf;
                gridPanel.add(tf);
            }

            if (r == 3) {
                chkInclude[r].addActionListener(e -> {
                    boolean active = chkInclude[rowIdx].isSelected();
                    for (int c = 0; c < 6; c++) fields[rowIdx][c].setEnabled(active);
                });
            }
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(new Color(248, 250, 252));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER_GRAY));

        JButton btnBatal = UiTheme.createStyledButton("Batal", new Color(100, 116, 139));
        JButton btnSimpan = UiTheme.createStyledButton("Simpan Provinsi & Semua Jenjang", UiTheme.ACCENT_BLUE);
        btnSimpan.setPreferredSize(new Dimension(245, 34));

        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> {
            String provinsi = txtProvinsi.getText().trim();
            if (provinsi.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Nama provinsi wajib diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<PenyaluranPip> batchList = new ArrayList<>();
            try {
                for (int r = 0; r < 4; r++) {
                    if (!chkInclude[r].isSelected()) continue;
                    String jenjang = jenjangList[r];
                    batchList.add(new PenyaluranPip(
                            provinsi, jenjang,
                            UiTheme.parseNonNegativeInt(fields[r][0].getText(), jenjang + " - Siswa Pemberian"),
                            UiTheme.parseNonNegativeInt(fields[r][1].getText(), jenjang + " - Siswa Aktivasi"),
                            UiTheme.parseNonNegativeInt(fields[r][2].getText(), jenjang + " - Siswa Relaksasi"),
                            UiTheme.parseNonNegativeLong(fields[r][3].getText(), jenjang + " - Anggaran Pemberian"),
                            UiTheme.parseNonNegativeLong(fields[r][4].getText(), jenjang + " - Anggaran Aktivasi"),
                            UiTheme.parseNonNegativeLong(fields[r][5].getText(), jenjang + " - Anggaran Relaksasi")
                    ));
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LoadingGlassPane.runWithLoading(dialog, "Menyimpan data provinsi baru ke cloud...", () -> {
                repository.insertBatchProvinsi(provinsi, batchList);
                return batchList.size();
            }, insertedCount -> {
                dialog.dispose();
                refreshAllWithLoading("Memperbarui tabel utama...");
                JOptionPane.showMessageDialog(this,
                        "Provinsi \"" + provinsi + "\" berhasil ditambahkan beserta " + insertedCount + " jenjang!",
                        "Sukses Simpan Provinsi", JOptionPane.INFORMATION_MESSAGE);
            });
        });

        btnPanel.add(btnBatal);
        btnPanel.add(btnSimpan);

        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(gridPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private static class MasterActionRenderer extends JPanel implements TableCellRenderer {
        public MasterActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 4));
            setOpaque(true);
            add(UiTheme.createSmallActionBtn("Detail", UiTheme.ACCENT_BLUE, 72));
            add(UiTheme.createSmallActionBtn("Hapus", UiTheme.ACCENT_RED, 72));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : UiTheme.ROW_ALT));
            return this;
        }
    }

    private class MasterActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private int currentRow;

        public MasterActionEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
            panel.setOpaque(true);

            JButton btnDetail = UiTheme.createSmallActionBtn("Detail", UiTheme.ACCENT_BLUE, 72);
            JButton btnHapus = UiTheme.createSmallActionBtn("Hapus", UiTheme.ACCENT_RED, 72);

            btnDetail.addActionListener(e -> {
                fireEditingStopped();
                String provinsi = (String) modelProvinsi.getValueAt(currentRow, 1);
                new ProvinsiDetailDialog(
                        MainDashboardFrame.this,
                        provinsi,
                        repository,
                        () -> refreshAllWithLoading("Memperbarui rekap nasional...")
                ).setVisible(true);
            });

            btnHapus.addActionListener(e -> {
                fireEditingStopped();
                String provinsi = (String) modelProvinsi.getValueAt(currentRow, 1);
                deleteProvinsiWithValidation(provinsi);
            });

            panel.add(btnDetail);
            panel.add(btnHapus);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.currentRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "AKSI";
        }
    }
}