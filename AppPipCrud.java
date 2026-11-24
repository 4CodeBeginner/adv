import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class AppPipCrud extends JFrame {

    // Konfigurasi Koneksi Database MySQL Laragon
    private static final String DB_URL = "jdbc:mysql://localhost:3306/nopl?useSSL=false&serverTimezone=Asia/Jakarta";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private static final NumberFormat NF = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"));

    // Palet Warna Formal Web Admin
    private static final Color BG_MAIN = new Color(243, 244, 246);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color PRIMARY_DARK = new Color(30, 41, 59);
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);
    private static final Color ACCENT_GREEN = new Color(22, 163, 74);
    private static final Color ACCENT_AMBER = new Color(217, 119, 6);
    private static final Color ACCENT_PURPLE = new Color(124, 58, 237);
    private static final Color ACCENT_RED = new Color(220, 38, 38);
    private static final Color BORDER_GRAY = new Color(209, 213, 219);
    private static final Color ROW_ALT = new Color(249, 250, 251);

    // Komponen UI Utama (Master per Provinsi)
    private JTable tableProvinsi;
    private DefaultTableModel modelProvinsi;
    private JTextField txtSearch;
    private JLabel lblTotalRecords;

    // Label Kartu Ringkasan
    private JLabel lblGrandTotalSiswa;
    private JLabel lblGrandTotalDana;
    private final Map<String, JLabel> lblJenjangStats = new HashMap<>();

    public AppPipCrud() {
        setTitle("Sistem Manajemen Data Penyaluran PIP 2026 - Admin Panel");
        setSize(1280, 740);
        setMinimumSize(new Dimension(1100, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout(0, 12));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel mainContainer = new JPanel(new BorderLayout(0, 14));
        mainContainer.setBackground(BG_MAIN);
        mainContainer.setBorder(new EmptyBorder(4, 20, 16, 20));

        mainContainer.add(createSummaryPanel(), BorderLayout.NORTH);
        mainContainer.add(createMasterTableCardPanel(), BorderLayout.CENTER);

        add(mainContainer, BorderLayout.CENTER);

        loadProvinsiData();
        loadSummary();
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // ==================== 1. HEADER PANEL ====================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_DARK);
        header.setBorder(new EmptyBorder(14, 24, 14, 24));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("DASHBOARD PENYALURAN PROGRAM INDONESIA PINTAR (PIP) 2026");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Rekapitulasi per Provinsi, Rincian Jenjang (SD - SMK) & Mutasi Status Siswa  |  MySQL: nopl.penyaluran_pip");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        titleBox.add(lblTitle);
        titleBox.add(lblSub);
        header.add(titleBox, BorderLayout.WEST);

        JLabel lblBadge = new JLabel("  STATUS: ONLINE  ");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setBackground(ACCENT_GREEN);
        lblBadge.setOpaque(true);
        lblBadge.setBorder(new EmptyBorder(6, 10, 6, 10));

        JPanel rightBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        rightBadge.setOpaque(false);
        rightBadge.add(lblBadge);
        header.add(rightBadge, BorderLayout.EAST);

        return header;
    }

    // ==================== 2. SUMMARY CARDS PANEL ====================
    private JPanel createSummaryPanel() {
        JPanel grid = new JPanel(new GridLayout(1, 5, 12, 0));
        grid.setOpaque(false);

        JPanel totalCard = createBaseCard(ACCENT_BLUE);
        JLabel tTitle = new JLabel("TOTAL NASIONAL (SEMUA JENJANG)");
        tTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tTitle.setForeground(new Color(100, 116, 139));

        lblGrandTotalSiswa = new JLabel("0 Siswa");
        lblGrandTotalSiswa.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblGrandTotalSiswa.setForeground(PRIMARY_DARK);

        lblGrandTotalDana = new JLabel("Rp 0");
        lblGrandTotalDana.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGrandTotalDana.setForeground(ACCENT_BLUE);

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
            lblVal.setForeground(PRIMARY_DARK);

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
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 1, 1, 1, new Color(
                        topAccent.getRed(), topAccent.getGreen(), topAccent.getBlue(), 180)),
                new EmptyBorder(10, 14, 10, 14)
        ));
        return card;
    }

    // ==================== 3. MASTER TABLE PER PROVINSI ====================
    private JPanel createMasterTableCardPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY, 1),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTools.setOpaque(false);

        JLabel lblCari = new JLabel("Cari Provinsi:");
        lblCari.setFont(new Font("Segoe UI", Font.BOLD, 12));

        txtSearch = new JTextField(24);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(260, 32));
        txtSearch.setToolTipText("Ketik nama provinsi untuk memfilter...");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadProvinsiData();
            }
        });

        JButton btnRefresh = createStyledButton("Refresh", new Color(100, 116, 139));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadProvinsiData();
            loadSummary();
        });

        leftTools.add(lblCari);
        leftTools.add(txtSearch);
        leftTools.add(btnRefresh);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.setOpaque(false);

        JButton btnTambah = createStyledButton("+ Tambah Data Baru", ACCENT_BLUE);
        btnTambah.setPreferredSize(new Dimension(165, 34));
        btnTambah.addActionListener(e -> openCreateDialog(null, null));

        rightTools.add(btnTambah);

        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);

        String[] columns = {
                "No",
                "Provinsi",
                "Jml Jenjang",
                "Siswa Pemberian",
                "Siswa Aktivasi",
                "Siswa Relaksasi",
                "Total Siswa",
                "Total Dana (Rp)",
                "Aksi"
        };

        modelProvinsi = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8;
            }
        };

        tableProvinsi = new JTable(modelProvinsi);
        styleExcelTable(tableProvinsi);

        ExcelCellRenderer centerRenderer = new ExcelCellRenderer(SwingConstants.CENTER, false);
        ExcelCellRenderer leftRenderer = new ExcelCellRenderer(SwingConstants.LEFT, true);
        ExcelCellRenderer rightRenderer = new ExcelCellRenderer(SwingConstants.RIGHT, false);
        ExcelCellRenderer boldRightRenderer = new ExcelCellRenderer(SwingConstants.RIGHT, true);

        TableColumnModel cm = tableProvinsi.getColumnModel();
        cm.getColumn(0).setPreferredWidth(45);
        cm.getColumn(0).setMaxWidth(55);
        cm.getColumn(0).setCellRenderer(centerRenderer);

        cm.getColumn(1).setPreferredWidth(230);
        cm.getColumn(1).setCellRenderer(leftRenderer);

        cm.getColumn(2).setPreferredWidth(95);
        cm.getColumn(2).setMaxWidth(110);
        cm.getColumn(2).setCellRenderer(centerRenderer);

        cm.getColumn(3).setPreferredWidth(120);
        cm.getColumn(3).setCellRenderer(rightRenderer);

        cm.getColumn(4).setPreferredWidth(115);
        cm.getColumn(4).setCellRenderer(rightRenderer);

        cm.getColumn(5).setPreferredWidth(115);
        cm.getColumn(5).setCellRenderer(rightRenderer);

        cm.getColumn(6).setPreferredWidth(125);
        cm.getColumn(6).setCellRenderer(boldRightRenderer);

        cm.getColumn(7).setPreferredWidth(175);
        cm.getColumn(7).setCellRenderer(boldRightRenderer);

        cm.getColumn(8).setPreferredWidth(120);
        cm.getColumn(8).setMinWidth(115);
        cm.getColumn(8).setMaxWidth(130);
        cm.getColumn(8).setCellRenderer(new DetailButtonRenderer());
        cm.getColumn(8).setCellEditor(new DetailButtonEditor());

        JScrollPane scrollPane = new JScrollPane(tableProvinsi);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_GRAY, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        lblTotalRecords = new JLabel("Menampilkan 0 provinsi");
        lblTotalRecords.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTotalRecords.setForeground(new Color(71, 85, 105));
        footer.add(lblTotalRecords, BorderLayout.WEST);

        JLabel lblHint = new JLabel("Klik 'Detail' untuk mengelola status siswa (Mutasi / Edit Siswa) per jenjang SD - SMK");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(new Color(100, 116, 139));
        footer.add(lblHint, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private void styleExcelTable(JTable tbl) {
        tbl.setRowHeight(36);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setShowGrid(true);
        tbl.setGridColor(new Color(226, 232, 240));
        tbl.setIntercellSpacing(new Dimension(1, 1));
        tbl.setSelectionBackground(new Color(219, 234, 254));
        tbl.setSelectionForeground(PRIMARY_DARK);
        tbl.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = tbl.getTableHeader();
        header.setPreferredSize(new Dimension(0, 38));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(PRIMARY_DARK);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    }

    // ==================== DATABASE OPERATIONS (MASTER) ====================
    private void loadProvinsiData() {
        modelProvinsi.setRowCount(0);
        String keyword = txtSearch != null ? txtSearch.getText().trim() : "";

        StringBuilder sql = new StringBuilder(
                "SELECT provinsi, "
                        + "COUNT(DISTINCT jenjang) AS jml_jenjang, "
                        + "SUM(siswa_pemberian) AS tot_pemberian, "
                        + "SUM(siswa_aktivasi_nominasi) AS tot_aktivasi, "
                        + "SUM(siswa_relaksasi) AS tot_relaksasi, "
                        + "SUM(siswa_disalurkan) AS tot_siswa, "
                        + "SUM(dana_disalurkan) AS tot_dana "
                        + "FROM penyaluran_pip "
        );

        if (!keyword.isEmpty()) {
            sql.append("WHERE provinsi LIKE ? ");
        }
        sql.append("GROUP BY provinsi ORDER BY MIN(id) ASC");

        int no = 0;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (!keyword.isEmpty()) {
                ps.setString(1, "%" + keyword + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    no++;
                    modelProvinsi.addRow(new Object[]{
                            no,
                            rs.getString("provinsi"),
                            rs.getInt("jml_jenjang") + " Jenjang",
                            NF.format(rs.getLong("tot_pemberian")),
                            NF.format(rs.getLong("tot_aktivasi")),
                            NF.format(rs.getLong("tot_relaksasi")),
                            NF.format(rs.getLong("tot_siswa")),
                            "Rp " + NF.format(rs.getLong("tot_dana")),
                            "DETAIL"
                    });
                }
            }
            lblTotalRecords.setText("Menampilkan " + no + " Provinsi");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data provinsi:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSummary() {
        String sql = "SELECT jenjang, SUM(siswa_disalurkan) AS tot_siswa, SUM(dana_disalurkan) AS tot_dana "
                + "FROM penyaluran_pip GROUP BY jenjang";

        long grandSiswa = 0;
        long grandDana = 0;

        for (Map.Entry<String, JLabel> entry : lblJenjangStats.entrySet()) {
            entry.getValue().setText("<html><b>0 Siswa</b><br><span style='color:#475569;font-size:11px'>Rp 0</span></html>");
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String jenjang = rs.getString("jenjang");
                long siswa = rs.getLong("tot_siswa");
                long dana = rs.getLong("tot_dana");

                grandSiswa += siswa;
                grandDana += dana;

                JLabel lbl = lblJenjangStats.get(jenjang);
                if (lbl != null) {
                    lbl.setText("<html><b>" + NF.format(siswa) + " Siswa</b><br>"
                            + "<span style='color:#475569;font-size:11px'>Rp " + NF.format(dana) + "</span></html>");
                }
            }

            lblGrandTotalSiswa.setText(NF.format(grandSiswa) + " Siswa");
            lblGrandTotalDana.setText("Rp " + NF.format(grandDana));
        } catch (SQLException e) {
            lblGrandTotalSiswa.setText("Error");
        }
    }

    // ==================== 4. MODAL DETAIL PER JENJANG (SD, SMP, SMA, SMK) ====================
    private void openProvinsiDetailDialog(String provinsi) {
        JDialog detailDialog = new JDialog(this, "Rincian Penyaluran per Jenjang - " + provinsi, true);
        detailDialog.setSize(1220, 520);
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setLayout(new BorderLayout(0, 10));
        detailDialog.getContentPane().setBackground(BG_MAIN);

        // Header Dialog
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(PRIMARY_DARK);
        topHeader.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel("RINCIAN PER JENJANG: " + provinsi.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Urutan: SD → SMP → SMA → SMK  |  Gunakan 'Mutasi' untuk memindahkan status siswa beserta proporsi anggarannya");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGroup.setOpaque(false);
        titleGroup.add(lblTitle);
        titleGroup.add(lblSub);
        topHeader.add(titleGroup, BorderLayout.WEST);

        JButton btnTambahJenjang = createStyledButton("+ Tambah Jenjang Baru", ACCENT_BLUE);
        btnTambahJenjang.setPreferredSize(new Dimension(180, 32));
        topHeader.add(btnTambahJenjang, BorderLayout.EAST);

        detailDialog.add(topHeader, BorderLayout.NORTH);

        // Body Card Tabel Detail
        JPanel bodyCard = new JPanel(new BorderLayout(0, 10));
        bodyCard.setBackground(Color.WHITE);
        bodyCard.setBorder(new EmptyBorder(14, 18, 14, 18));

        String[] detailCols = {
                "ID",
                "Jenjang",
                "Siswa Pemberian",
                "Siswa Aktivasi",
                "Siswa Relaksasi",
                "Total Siswa",
                "Dana Pemberian",
                "Dana Aktivasi",
                "Dana Relaksasi",
                "Total Dana (Rp)",
                "Aksi"
        };

        DefaultTableModel detailModel = new DefaultTableModel(detailCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 10;
            }
        };

        JTable detailTable = new JTable(detailModel);
        styleExcelTable(detailTable);

        ExcelCellRenderer centerRenderer = new ExcelCellRenderer(SwingConstants.CENTER, true);
        ExcelCellRenderer rightRenderer = new ExcelCellRenderer(SwingConstants.RIGHT, false);
        ExcelCellRenderer boldRightRenderer = new ExcelCellRenderer(SwingConstants.RIGHT, true);

        TableColumnModel cm = detailTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(45);
        cm.getColumn(0).setMaxWidth(55);
        cm.getColumn(0).setCellRenderer(centerRenderer);

        cm.getColumn(1).setPreferredWidth(65);
        cm.getColumn(1).setMaxWidth(75);
        cm.getColumn(1).setCellRenderer(centerRenderer);

        cm.getColumn(2).setPreferredWidth(95);
        cm.getColumn(2).setCellRenderer(rightRenderer);

        cm.getColumn(3).setPreferredWidth(90);
        cm.getColumn(3).setCellRenderer(rightRenderer);

        cm.getColumn(4).setPreferredWidth(90);
        cm.getColumn(4).setCellRenderer(rightRenderer);

        cm.getColumn(5).setPreferredWidth(95);
        cm.getColumn(5).setCellRenderer(boldRightRenderer);

        cm.getColumn(6).setPreferredWidth(115);
        cm.getColumn(6).setCellRenderer(rightRenderer);

        cm.getColumn(7).setPreferredWidth(115);
        cm.getColumn(7).setCellRenderer(rightRenderer);

        cm.getColumn(8).setPreferredWidth(110);
        cm.getColumn(8).setCellRenderer(rightRenderer);

        cm.getColumn(9).setPreferredWidth(130);
        cm.getColumn(9).setCellRenderer(boldRightRenderer);

        // Kolom Aksi di Sisi Paling Kanan: Mutasi | Edit Siswa | Hapus
        cm.getColumn(10).setPreferredWidth(225);
        cm.getColumn(10).setMinWidth(220);
        cm.getColumn(10).setMaxWidth(235);

        Runnable reloadDetail = () -> {
            detailModel.setRowCount(0);
            String sql = "SELECT * FROM penyaluran_pip WHERE provinsi = ? "
                    + "ORDER BY FIELD(jenjang, 'SD', 'SMP', 'SMA', 'SMK'), id ASC";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, provinsi);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        detailModel.addRow(new Object[]{
                                rs.getInt("id"),
                                rs.getString("jenjang"),
                                NF.format(rs.getLong("siswa_pemberian")),
                                NF.format(rs.getLong("siswa_aktivasi_nominasi")),
                                NF.format(rs.getLong("siswa_relaksasi")),
                                NF.format(rs.getLong("siswa_disalurkan")),
                                NF.format(rs.getLong("dana_pemberian")),
                                NF.format(rs.getLong("dana_aktivasi_nominasi")),
                                NF.format(rs.getLong("dana_relaksasi")),
                                "Rp " + NF.format(rs.getLong("dana_disalurkan")),
                                "AKSI"
                        });
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(detailDialog, "Gagal memuat detail jenjang:\n" + e.getMessage());
            }
        };

        cm.getColumn(10).setCellRenderer(new DetailRowActionRenderer());
        cm.getColumn(10).setCellEditor(new DetailRowActionEditor(detailDialog, detailModel, provinsi, reloadDetail));

        btnTambahJenjang.addActionListener(e -> {
            openCreateDialog(provinsi, () -> {
                reloadDetail.run();
                loadProvinsiData();
                loadSummary();
            });
        });

        reloadDetail.run();

        JScrollPane sp = new JScrollPane(detailTable);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_GRAY, 1));
        sp.getViewport().setBackground(Color.WHITE);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        bottomBar.setOpaque(false);
        JButton btnTutup = createStyledButton("Tutup Rincian", new Color(71, 85, 105));
        btnTutup.setPreferredSize(new Dimension(130, 32));
        btnTutup.addActionListener(e -> detailDialog.dispose());
        bottomBar.add(btnTutup);

        bodyCard.add(sp, BorderLayout.CENTER);
        bodyCard.add(bottomBar, BorderLayout.SOUTH);

        detailDialog.add(bodyCard, BorderLayout.CENTER);
        detailDialog.setVisible(true);
    }

    // ==================== 5. MODAL CREATE (ATUR ANGGARAN HANYA DI AWAL) ====================
    private void openCreateDialog(String presetProvinsi, Runnable onSuccessCallback) {
        JDialog dialog = new JDialog(this, "Tambah Data Baru (Pengaturan Siswa & Anggaran Awal)", true);
        dialog.setSize(490, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(18, 20, 14, 20));

        JTextField txtProvinsi = new JTextField();
        if (presetProvinsi != null && !presetProvinsi.isEmpty()) {
            txtProvinsi.setText(presetProvinsi);
        }

        JComboBox<String> cmbJenjang = new JComboBox<>(new String[]{"SD", "SMP", "SMA", "SMK"});
        JTextField txtSiswaPemberian = new JTextField("0");
        JTextField txtSiswaAktivasi = new JTextField("0");
        JTextField txtSiswaRelaksasi = new JTextField("0");
        JTextField txtDanaPemberian = new JTextField("0");
        JTextField txtDanaAktivasi = new JTextField("0");
        JTextField txtDanaRelaksasi = new JTextField("0");

        formPanel.add(new JLabel("Nama Provinsi:"));
        formPanel.add(txtProvinsi);
        formPanel.add(new JLabel("Jenjang Pendidikan:"));
        formPanel.add(cmbJenjang);
        formPanel.add(new JLabel("Siswa Pemberian:"));
        formPanel.add(txtSiswaPemberian);
        formPanel.add(new JLabel("Siswa Aktivasi Nominasi:"));
        formPanel.add(txtSiswaAktivasi);
        formPanel.add(new JLabel("Siswa Relaksasi:"));
        formPanel.add(txtSiswaRelaksasi);
        formPanel.add(new JLabel("Anggaran Pemberian (Rp):"));
        formPanel.add(txtDanaPemberian);
        formPanel.add(new JLabel("Anggaran Aktivasi (Rp):"));
        formPanel.add(txtDanaAktivasi);
        formPanel.add(new JLabel("Anggaran Relaksasi (Rp):"));
        formPanel.add(txtDanaRelaksasi);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(new Color(248, 250, 252));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_GRAY));

        JButton btnBatal = createStyledButton("Batal", new Color(100, 116, 139));
        JButton btnSimpan = createStyledButton("Simpan Data Awal", ACCENT_BLUE);
        btnSimpan.setPreferredSize(new Dimension(145, 32));

        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> {
            try {
                String provinsi = txtProvinsi.getText().trim();
                if (provinsi.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Nama provinsi wajib diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String jenjang = (String) cmbJenjang.getSelectedItem();
                int sPemberian = parseNonNegativeInt(txtSiswaPemberian.getText(), "Siswa Pemberian");
                int sAktivasi = parseNonNegativeInt(txtSiswaAktivasi.getText(), "Siswa Aktivasi");
                int sRelaksasi = parseNonNegativeInt(txtSiswaRelaksasi.getText(), "Siswa Relaksasi");
                long sTotal = (long) sPemberian + sAktivasi + sRelaksasi;

                long dPemberian = parseNonNegativeLong(txtDanaPemberian.getText(), "Anggaran Pemberian");
                long dAktivasi = parseNonNegativeLong(txtDanaAktivasi.getText(), "Anggaran Aktivasi");
                long dRelaksasi = parseNonNegativeLong(txtDanaRelaksasi.getText(), "Anggaran Relaksasi");
                long dTotal = dPemberian + dAktivasi + dRelaksasi;

                String sql = "INSERT INTO penyaluran_pip (provinsi, jenjang, siswa_disalurkan, "
                        + "siswa_pemberian, siswa_aktivasi_nominasi, siswa_relaksasi, "
                        + "dana_disalurkan, dana_pemberian, dana_aktivasi_nominasi, dana_relaksasi) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, provinsi);
                    ps.setString(2, jenjang);
                    ps.setLong(3, sTotal);
                    ps.setInt(4, sPemberian);
                    ps.setInt(5, sAktivasi);
                    ps.setInt(6, sRelaksasi);
                    ps.setLong(7, dTotal);
                    ps.setLong(8, dPemberian);
                    ps.setLong(9, dAktivasi);
                    ps.setLong(10, dRelaksasi);
                    ps.executeUpdate();
                }

                dialog.dispose();
                loadProvinsiData();
                loadSummary();
                if (onSuccessCallback != null) {
                    onSuccessCallback.run();
                }

                JOptionPane.showMessageDialog(this, "Data baru berhasil ditambahkan!",
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);

            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Kesalahan SQL:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnBatal);
        btnPanel.add(btnSimpan);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ==================== 6. MODAL EDIT STATUS SISWA SAJA (TANPA EDIT ANGGARAN) ====================
    private void openEditSiswaOnlyDialog(JDialog parentDialog, int editId, Runnable onSuccessCallback) {
        String provinsi = "";
        String jenjang = "";
        int oldSPemberian = 0, oldSAktivasi = 0, oldSRelaksasi = 0;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM penyaluran_pip WHERE id = ?")) {
            ps.setInt(1, editId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    provinsi = rs.getString("provinsi");
                    jenjang = rs.getString("jenjang");
                    oldSPemberian = rs.getInt("siswa_pemberian");
                    oldSAktivasi = rs.getInt("siswa_aktivasi_nominasi");
                    oldSRelaksasi = rs.getInt("siswa_relaksasi");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parentDialog, "Gagal memuat data: " + e.getMessage());
            return;
        }

        JDialog dialog = new JDialog(parentDialog, "Edit Jumlah Status Siswa (Anggaran Terkunci)", true);
        dialog.setSize(450, 320);
        dialog.setLocationRelativeTo(parentDialog);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 12));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 20, 16, 20));

        JLabel lblInfoVal = new JLabel(provinsi + " (" + jenjang + ")");
        lblInfoVal.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JTextField txtSPemberian = new JTextField(String.valueOf(oldSPemberian));
        JTextField txtSAktivasi = new JTextField(String.valueOf(oldSAktivasi));
        JTextField txtSRelaksasi = new JTextField(String.valueOf(oldSRelaksasi));

        formPanel.add(new JLabel("Wilayah / Jenjang:"));
        formPanel.add(lblInfoVal);
        formPanel.add(new JLabel("Jumlah Siswa Pemberian:"));
        formPanel.add(txtSPemberian);
        formPanel.add(new JLabel("Jumlah Siswa Aktivasi Nominasi:"));
        formPanel.add(txtSAktivasi);
        formPanel.add(new JLabel("Jumlah Siswa Relaksasi:"));
        formPanel.add(txtSRelaksasi);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(new Color(248, 250, 252));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_GRAY));

        JButton btnBatal = createStyledButton("Batal", new Color(100, 116, 139));
        JButton btnSimpan = createStyledButton("Update Status Siswa", ACCENT_AMBER);
        btnSimpan.setPreferredSize(new Dimension(155, 32));

        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> {
            try {
                int sPemberian = parseNonNegativeInt(txtSPemberian.getText(), "Siswa Pemberian");
                int sAktivasi = parseNonNegativeInt(txtSAktivasi.getText(), "Siswa Aktivasi");
                int sRelaksasi = parseNonNegativeInt(txtSRelaksasi.getText(), "Siswa Relaksasi");
                long sTotal = (long) sPemberian + sAktivasi + sRelaksasi;

                // Hanya kolom siswa yang diubah, kolom dana tidak disentuh
                String sql = "UPDATE penyaluran_pip SET siswa_disalurkan=?, siswa_pemberian=?, "
                        + "siswa_aktivasi_nominasi=?, siswa_relaksasi=? WHERE id=?";

                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, sTotal);
                    ps.setInt(2, sPemberian);
                    ps.setInt(3, sAktivasi);
                    ps.setInt(4, sRelaksasi);
                    ps.setInt(5, editId);
                    ps.executeUpdate();
                }

                dialog.dispose();
                loadProvinsiData();
                loadSummary();
                if (onSuccessCallback != null) {
                    onSuccessCallback.run();
                }

                JOptionPane.showMessageDialog(parentDialog,
                        "Jumlah status siswa berhasil diperbarui (anggaran tetap terkunci).",
                        "Sukses Update", JOptionPane.INFORMATION_MESSAGE);

            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Kesalahan SQL:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnBatal);
        btnPanel.add(btnSimpan);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ==================== 7. MODAL MUTASI / PEMINDAHAN STATUS SISWA + PROPORSI ANGGARAN ====================
    private void openMutasiSiswaDialog(JDialog parentDialog, int id, Runnable onSuccessCallback) {
        String provinsi = "";
        String jenjang = "";
        long sPemberian = 0, sAktivasi = 0, sRelaksasi = 0;
        long dPemberian = 0, dAktivasi = 0, dRelaksasi = 0;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM penyaluran_pip WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    provinsi = rs.getString("provinsi");
                    jenjang = rs.getString("jenjang");
                    sPemberian = rs.getLong("siswa_pemberian");
                    sAktivasi = rs.getLong("siswa_aktivasi_nominasi");
                    sRelaksasi = rs.getLong("siswa_relaksasi");
                    dPemberian = rs.getLong("dana_pemberian");
                    dAktivasi = rs.getLong("dana_aktivasi_nominasi");
                    dRelaksasi = rs.getLong("dana_relaksasi");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parentDialog, "Gagal memuat data mutasi: " + e.getMessage());
            return;
        }

        final long curSPemb = sPemberian, curSAkt = sAktivasi, curSRel = sRelaksasi;
        final long curDPemb = dPemberian, curDAkt = dAktivasi, curDRel = dRelaksasi;

        JDialog dialog = new JDialog(parentDialog, "Mutasi Status Siswa - " + provinsi + " (" + jenjang + ")", true);
        dialog.setSize(520, 410);
        dialog.setLocationRelativeTo(parentDialog);
        dialog.setLayout(new BorderLayout());

        // Info Kondisi Saat Ini di Atas
        JPanel infoBox = new JPanel(new GridLayout(3, 1, 0, 3));
        infoBox.setBackground(new Color(241, 245, 249));
        infoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_GRAY),
                new EmptyBorder(12, 18, 12, 18)
        ));

        JLabel l1 = new JLabel("• Siswa Pemberian : " + NF.format(curSPemb) + " siswa  (Dana: Rp " + NF.format(curDPemb) + ")");
        JLabel l2 = new JLabel("• Siswa Aktivasi  : " + NF.format(curSAkt) + " siswa  (Dana: Rp " + NF.format(curDAkt) + ")");
        JLabel l3 = new JLabel("• Siswa Relaksasi : " + NF.format(curSRel) + " siswa  (Dana: Rp " + NF.format(curDRel) + ")");
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoBox.add(l1);
        infoBox.add(l2);
        infoBox.add(l3);

        dialog.add(infoBox, BorderLayout.NORTH);

        // Form Input Mutasi
        String[] kategori = {"Siswa Pemberian", "Siswa Aktivasi Nominasi", "Siswa Relaksasi"};
        JComboBox<String> cmbAsal = new JComboBox<>(kategori);
        cmbAsal.setSelectedIndex(1); // Default asal: Siswa Aktivasi Nominasi
        JComboBox<String> cmbTujuan = new JComboBox<>(kategori);
        cmbTujuan.setSelectedIndex(0); // Default tujuan: Siswa Pemberian

        JTextField txtJumlahPindah = new JTextField("0");
        JLabel lblPreviewDana = new JLabel("Rp 0");
        lblPreviewDana.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPreviewDana.setForeground(ACCENT_PURPLE);

        // Hitung otomatis estimasi dana yang ikut berpindah saat diketik
        Runnable updatePreview = () -> {
            try {
                int asalIdx = cmbAsal.getSelectedIndex();
                long jml = Long.parseLong(txtJumlahPindah.getText().trim());
                if (jml < 0) {
                    lblPreviewDana.setText("Jumlah tidak valid");
                    return;
                }
                long stokSiswa = (asalIdx == 0) ? curSPemb : (asalIdx == 1 ? curSAkt : curSRel);
                long stokDana = (asalIdx == 0) ? curDPemb : (asalIdx == 1 ? curDAkt : curDRel);

                if (stokSiswa <= 0 || jml > stokSiswa) {
                    lblPreviewDana.setText("Melebihi stok siswa asal!");
                    return;
                }
                long danaPindah = (jml == stokSiswa) ? stokDana : Math.round(((double) stokDana / stokSiswa) * jml);
                lblPreviewDana.setText("Rp " + NF.format(danaPindah) + " (otomatis pindah)");
            } catch (NumberFormatException ex) {
                lblPreviewDana.setText("Rp 0");
            }
        };

        txtJumlahPindah.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updatePreview.run();
            }
        });
        cmbAsal.addActionListener(e -> updatePreview.run());

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 14));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(18, 20, 18, 20));

        formPanel.add(new JLabel("Pindahkan Dari (Kategori Asal):"));
        formPanel.add(cmbAsal);
        formPanel.add(new JLabel("Pindahkan Ke (Kategori Tujuan):"));
        formPanel.add(cmbTujuan);
        formPanel.add(new JLabel("Jumlah Siswa Dipindahkan:"));
        formPanel.add(txtJumlahPindah);
        formPanel.add(new JLabel("Anggaran Ikut Berpindah:"));
        formPanel.add(lblPreviewDana);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(new Color(248, 250, 252));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_GRAY));

        JButton btnBatal = createStyledButton("Batal", new Color(100, 116, 139));
        JButton btnProses = createStyledButton("Proses Mutasi Siswa", ACCENT_PURPLE);
        btnProses.setPreferredSize(new Dimension(160, 32));

        btnBatal.addActionListener(e -> dialog.dispose());
        btnProses.addActionListener(e -> {
            int asalIdx = cmbAsal.getSelectedIndex();
            int tujuanIdx = cmbTujuan.getSelectedIndex();

            if (asalIdx == tujuanIdx) {
                JOptionPane.showMessageDialog(dialog, "Kategori asal dan tujuan tidak boleh sama!",
                        "Validasi Mutasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            long jmlPindah;
            try {
                jmlPindah = Long.parseLong(txtJumlahPindah.getText().trim());
                if (jmlPindah <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Masukkan jumlah siswa yang valid (> 0).",
                        "Validasi Mutasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            long[] siswaArr = {curSPemb, curSAkt, curSRel};
            long[] danaArr = {curDPemb, curDAkt, curDRel};

            if (jmlPindah > siswaArr[asalIdx]) {
                JOptionPane.showMessageDialog(dialog,
                        "Jumlah siswa dipindahkan (" + NF.format(jmlPindah)
                                + ") melebihi jumlah siswa pada kategori asal (" + NF.format(siswaArr[asalIdx]) + ").",
                        "Stok Siswa Tidak Cukup", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Hitung dana yang berpindah secara proporsional dari kategori asal ke tujuan
            long danaMutasi = (jmlPindah == siswaArr[asalIdx])
                    ? danaArr[asalIdx]
                    : Math.round(((double) danaArr[asalIdx] / siswaArr[asalIdx]) * jmlPindah);

            // Kurangi di asal, tambahkan di tujuan
            siswaArr[asalIdx] -= jmlPindah;
            danaArr[asalIdx] -= danaMutasi;

            siswaArr[tujuanIdx] += jmlPindah;
            danaArr[tujuanIdx] += danaMutasi;

            long totalSiswa = siswaArr[0] + siswaArr[1] + siswaArr[2];
            long totalDana = danaArr[0] + danaArr[1] + danaArr[2];

            String sql = "UPDATE penyaluran_pip SET "
                    + "siswa_disalurkan=?, siswa_pemberian=?, siswa_aktivasi_nominasi=?, siswa_relaksasi=?, "
                    + "dana_disalurkan=?, dana_pemberian=?, dana_aktivasi_nominasi=?, dana_relaksasi=? "
                    + "WHERE id=?";

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, totalSiswa);
                ps.setLong(2, siswaArr[0]);
                ps.setLong(3, siswaArr[1]);
                ps.setLong(4, siswaArr[2]);
                ps.setLong(5, totalDana);
                ps.setLong(6, danaArr[0]);
                ps.setLong(7, danaArr[1]);
                ps.setLong(8, danaArr[2]);
                ps.setInt(9, id);
                ps.executeUpdate();

                dialog.dispose();
                loadProvinsiData();
                loadSummary();
                if (onSuccessCallback != null) {
                    onSuccessCallback.run();
                }

                JOptionPane.showMessageDialog(parentDialog,
                        "Mutasi berhasil!\n"
                                + NF.format(jmlPindah) + " siswa dan anggaran Rp " + NF.format(danaMutasi)
                                + "\ntelah dipindahkan dari " + kategori[asalIdx] + " ke " + kategori[tujuanIdx] + ".",
                        "Sukses Mutasi", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Gagal memproses mutasi:\n" + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnBatal);
        btnPanel.add(btnProses);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ==================== CUSTOM RENDERERS & BUTTON COLUMNS ====================
    private static class ExcelCellRenderer extends DefaultTableCellRenderer {
        private final int alignment;
        private final boolean isBold;

        public ExcelCellRenderer(int alignment, boolean isBold) {
            this.alignment = alignment;
            this.isBold = isBold;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(alignment);
            setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 8, 0, 8));

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                c.setForeground(PRIMARY_DARK);
            }
            return c;
        }
    }

    private class DetailButtonRenderer extends JPanel implements TableCellRenderer {
        public DetailButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
            setOpaque(true);
            add(createSmallActionBtn("Detail", ACCENT_BLUE, 84));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : ROW_ALT));
            return this;
        }
    }

    private class DetailButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private int currentRow;

        public DetailButtonEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            panel.setOpaque(true);
            JButton btnDetail = createSmallActionBtn("Detail", ACCENT_BLUE, 84);

            btnDetail.addActionListener(e -> {
                fireEditingStopped();
                String provinsi = (String) modelProvinsi.getValueAt(currentRow, 1);
                openProvinsiDetailDialog(provinsi);
            });

            panel.add(btnDetail);
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
            return "DETAIL";
        }
    }

    // Kolom Aksi di Detail Jenjang: [Mutasi] [Edit] [Hapus]
    private class DetailRowActionRenderer extends JPanel implements TableCellRenderer {
        public DetailRowActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
            setOpaque(true);
            add(createSmallActionBtn("Mutasi", ACCENT_PURPLE, 68));
            add(createSmallActionBtn("Edit", ACCENT_AMBER, 54));
            add(createSmallActionBtn("Hapus", ACCENT_RED, 58));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : ROW_ALT));
            return this;
        }
    }

    private class DetailRowActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private int currentRow;

        public DetailRowActionEditor(JDialog parentDialog, DefaultTableModel detailModel,
                                     String provinsi, Runnable reloadDetail) {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            panel.setOpaque(true);

            JButton btnMutasi = createSmallActionBtn("Mutasi", ACCENT_PURPLE, 68);
            JButton btnEdit = createSmallActionBtn("Edit", ACCENT_AMBER, 54);
            JButton btnDel = createSmallActionBtn("Hapus", ACCENT_RED, 58);

            btnMutasi.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) detailModel.getValueAt(currentRow, 0);
                openMutasiSiswaDialog(parentDialog, id, reloadDetail);
            });

            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) detailModel.getValueAt(currentRow, 0);
                openEditSiswaOnlyDialog(parentDialog, id, reloadDetail);
            });

            btnDel.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) detailModel.getValueAt(currentRow, 0);
                String jenjang = (String) detailModel.getValueAt(currentRow, 1);

                int confirm = JOptionPane.showConfirmDialog(
                        parentDialog,
                        "Hapus data jenjang " + jenjang + " untuk " + provinsi + " (ID: " + id + ")?",
                        "Konfirmasi Hapus",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try (Connection conn = getConnection();
                         PreparedStatement ps = conn.prepareStatement("DELETE FROM penyaluran_pip WHERE id = ?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                        reloadDetail.run();
                        loadProvinsiData();
                        loadSummary();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(parentDialog, "Gagal menghapus:\n" + ex.getMessage());
                    }
                }
            });

            panel.add(btnMutasi);
            panel.add(btnEdit);
            panel.add(btnDel);
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

    // ==================== HELPER UI & VALIDATION ====================
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 32));
        return btn;
    }

    private JButton createSmallActionBtn(String text, Color bg, int width) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, 26));
        return btn;
    }

    private int parseNonNegativeInt(String raw, String fieldName) {
        try {
            int val = Integer.parseInt(raw.trim());
            if (val < 0) {
                throw new IllegalArgumentException(fieldName + " tidak boleh negatif.");
            }
            return val;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " harus berupa angka bulat yang valid.");
        }
    }

    private long parseNonNegativeLong(String raw, String fieldName) {
        try {
            long val = Long.parseLong(raw.trim());
            if (val < 0) {
                throw new IllegalArgumentException(fieldName + " tidak boleh negatif.");
            }
            return val;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " harus berupa nominal angka yang valid.");
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            try (Connection ignored = getConnection()) {
                new AppPipCrud().setVisible(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Gagal terhubung ke MySQL Laragon (database: nopl):\n" + e.getMessage()
                                + "\n\nPastikan MySQL di Laragon sudah Start dan mysql-connector-j.jar ada di folder lib.",
                        "Error Koneksi Database",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}