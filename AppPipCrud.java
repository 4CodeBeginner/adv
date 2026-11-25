import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class AppPipCrud extends JFrame {

    private static final String DB_URL = "jdbc:mysql://mysql-e37d74f-naufalrizqyalfath-4b7e.a.aivencloud.com:28803/defaultdb?useSSL=true&requireSSL=true&serverTimezone=Asia/Jakarta";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASS = "AVNS_HUN5zAyVMLCYDVeigqT";

    private static final NumberFormat NF = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"));

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

    private JTable tableProvinsi;
    private DefaultTableModel modelProvinsi;
    private JTextField txtSearch;
    private JLabel lblTotalRecords;

    private JLabel lblGrandTotalSiswa;
    private JLabel lblGrandTotalDana;
    private final Map<String, JLabel> lblJenjangStats = new HashMap<>();

    public AppPipCrud() {
        setTitle("Sistem Manajemen Data Penyaluran PIP 2026 - Admin Panel");
        setSize(1340, 750);
        setMinimumSize(new Dimension(1120, 620));
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

        refreshAllWithLoading("Menghubungkan & memuat data PIP dari Aiven Cloud...");
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // ==================== AUTO-FIT LEBAR KOLOM TABEL TANPA ELLIPSIS ====================
    private void autoFitTableColumns(JTable table, int actionColumnIndex, int actionColumnWidth) {
        JTableHeader header = table.getTableHeader();
        FontMetrics headerFm = header.getFontMetrics(header.getFont());
        FontMetrics boldCellFm = table.getFontMetrics(new Font("Segoe UI", Font.BOLD, 13));

        TableColumnModel cm = table.getColumnModel();
        int totalRequiredWidth = 0;

        for (int col = 0; col < table.getColumnCount(); col++) {
            TableColumn column = cm.getColumn(col);

            if (col == actionColumnIndex) {
                column.setMinWidth(actionColumnWidth);
                column.setPreferredWidth(actionColumnWidth);
                column.setMaxWidth(actionColumnWidth + 10);
                totalRequiredWidth += actionColumnWidth;
                continue;
            }

            String headerText = String.valueOf(column.getHeaderValue());
            int maxWidth = headerFm.stringWidth(headerText) + 22;

            for (int row = 0; row < table.getRowCount(); row++) {
                Object val = table.getValueAt(row, col);
                if (val != null) {
                    int textWidth = boldCellFm.stringWidth(val.toString()) + 20;
                    if (textWidth > maxWidth) {
                        maxWidth = textWidth;
                    }
                }
            }

            column.setMinWidth(maxWidth);
            column.setPreferredWidth(maxWidth);
            totalRequiredWidth += maxWidth;
        }

        if (table.getParent() != null && table.getParent().getWidth() > totalRequiredWidth) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        } else {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
    }

    // ==================== HELPER ASYNC LOADER DENGAN ANIMASI SPINNER ====================
    private <T> void runWithLoading(RootPaneContainer targetContainer, String message,
                                    Callable<T> backgroundTask, Consumer<T> onSuccess) {
        LoadingGlassPane overlay = new LoadingGlassPane(message);
        Component previousGlass = targetContainer.getGlassPane();

        targetContainer.setGlassPane(overlay);
        overlay.startAnimation();
        overlay.setVisible(true);

        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                return backgroundTask.call();
            }

            @Override
            protected void done() {
                overlay.stopAnimation();
                overlay.setVisible(false);
                if (previousGlass != null) {
                    targetContainer.setGlassPane(previousGlass);
                }

                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    Component parentComp = (targetContainer instanceof Component) ? (Component) targetContainer : AppPipCrud.this;
                    JOptionPane.showMessageDialog(
                            parentComp,
                            "Terjadi kesalahan saat memproses data:\n" + cause.getMessage(),
                            "Error Proses Data",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }

    private void refreshAllWithLoading(String loadingText) {
        String keyword = txtSearch != null ? txtSearch.getText().trim() : "";

        runWithLoading(this, loadingText, () -> {
            List<Object[]> rows = fetchProvinsiRows(keyword);
            Map<String, long[]> summary = fetchSummaryMap();
            return new Object[]{rows, summary};
        }, result -> {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = (List<Object[]>) result[0];
            @SuppressWarnings("unchecked")
            Map<String, long[]> summary = (Map<String, long[]>) result[1];

            applyProvinsiRowsToTable(rows);
            applySummaryToCards(summary);
        });
    }

    // ==================== HELPER MODAL DIALOG DENGAN BACKDROP GELAP ====================
    private JDialog createDimmedDialog(Window owner, String title, int width, int height) {
        JDialog dialog = new JDialog(owner, title, JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(owner);
        dialog.setLayout(new BorderLayout());

        if (owner instanceof RootPaneContainer) {
            RootPaneContainer rpc = (RootPaneContainer) owner;
            Component prevGlass = rpc.getGlassPane();
            DimBackdropGlassPane backdrop = new DimBackdropGlassPane();

            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    rpc.setGlassPane(backdrop);
                    backdrop.setVisible(true);
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    backdrop.setVisible(false);
                    if (prevGlass != null) {
                        rpc.setGlassPane(prevGlass);
                    }
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    backdrop.setVisible(false);
                    if (prevGlass != null) {
                        rpc.setGlassPane(prevGlass);
                    }
                }
            });
        }
        return dialog;
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

        JLabel lblSub = new JLabel("Rekapitulasi per Provinsi, Rincian Jenjang (SD - SMK) & Mutasi Status Siswa  |  Aiven Cloud MySQL");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        titleBox.add(lblTitle);
        titleBox.add(lblSub);
        header.add(titleBox, BorderLayout.WEST);

        JLabel lblBadge = new JLabel("  STATUS: ONLINE (AIVEN)  ");
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

        lblGrandTotalSiswa = new JLabel("Memuat...");
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

        JButton btnCari = createStyledButton("Cari", PRIMARY_DARK);
        btnCari.setPreferredSize(new Dimension(75, 32));
        btnCari.addActionListener(e -> searchProvinsiWithLoading());

        JButton btnRefresh = createStyledButton("Refresh", new Color(100, 116, 139));
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

        JButton btnTambah = createStyledButton("+ Tambah Provinsi (SD - SMA)", ACCENT_BLUE);
        btnTambah.setPreferredSize(new Dimension(225, 34));
        btnTambah.addActionListener(e -> openCreateProvinsiBatchDialog());

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

        autoFitTableColumns(tableProvinsi, 8, 175);

        JScrollPane scrollPane = new JScrollPane(tableProvinsi);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_GRAY, 1));
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

    // ==================== DATABASE QUERY METHODS (BACKGROUND SAFE) ====================
    private List<Object[]> fetchProvinsiRows(String keyword) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
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
                    rows.add(new Object[]{
                            no,
                            rs.getString("provinsi"),
                            rs.getInt("jml_jenjang") + " Jenjang",
                            NF.format(rs.getLong("tot_pemberian")),
                            NF.format(rs.getLong("tot_aktivasi")),
                            NF.format(rs.getLong("tot_relaksasi")),
                            NF.format(rs.getLong("tot_siswa")),
                            "Rp " + NF.format(rs.getLong("tot_dana")),
                            "AKSI"
                    });
                }
            }
        }
        return rows;
    }

    private void applyProvinsiRowsToTable(List<Object[]> rows) {
        modelProvinsi.setRowCount(0);
        for (Object[] r : rows) {
            modelProvinsi.addRow(r);
        }
        autoFitTableColumns(tableProvinsi, 8, 175);
        lblTotalRecords.setText("Menampilkan " + rows.size() + " Provinsi");
    }

    private void searchProvinsiWithLoading() {
        String keyword = txtSearch.getText().trim();
        runWithLoading(this, "Mencari data provinsi...", () -> fetchProvinsiRows(keyword), this::applyProvinsiRowsToTable);
    }

    private Map<String, long[]> fetchSummaryMap() throws SQLException {
        Map<String, long[]> map = new HashMap<>();
        String sql = "SELECT jenjang, SUM(siswa_disalurkan) AS tot_siswa, SUM(dana_disalurkan) AS tot_dana "
                + "FROM penyaluran_pip GROUP BY jenjang";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("jenjang"), new long[]{
                        rs.getLong("tot_siswa"),
                        rs.getLong("tot_dana")
                });
            }
        }
        return map;
    }

    private void applySummaryToCards(Map<String, long[]> summary) {
        long grandSiswa = 0;
        long grandDana = 0;

        for (Map.Entry<String, JLabel> entry : lblJenjangStats.entrySet()) {
            String jenjang = entry.getKey();
            long[] vals = summary.getOrDefault(jenjang, new long[]{0L, 0L});
            grandSiswa += vals[0];
            grandDana += vals[1];

            entry.getValue().setText("<html><b>" + NF.format(vals[0]) + " Siswa</b><br>"
                    + "<span style='color:#475569;font-size:11px'>Rp " + NF.format(vals[1]) + "</span></html>");
        }

        lblGrandTotalSiswa.setText(NF.format(grandSiswa) + " Siswa");
        lblGrandTotalDana.setText("Rp " + NF.format(grandDana));
    }

    // ==================== HELPER DIALOG VALIDASI KETIK ULANG ====================
    private boolean confirmWithTypedText(Component parent, String title, String warningHtml, String expectedPhrase) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 8));
        panel.setPreferredSize(new Dimension(440, 135));

        JLabel lblWarn = new JLabel(warningHtml);
        lblWarn.setForeground(ACCENT_RED);

        JLabel lblInstr = new JLabel("<html>Untuk melanjutkan, ketik persis tulisan berikut:<br>"
                + "<code style='background:#f1f5f9;padding:2px 6px;font-size:12px;'>" + expectedPhrase + "</code></html>");

        JTextField txtConfirm = new JTextField();
        txtConfirm.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panel.add(lblWarn);
        panel.add(lblInstr);
        panel.add(txtConfirm);

        int option = JOptionPane.showConfirmDialog(
                parent,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return false;
        }

        String typed = txtConfirm.getText().trim();
        if (!typed.equals(expectedPhrase)) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Teks validasi tidak sesuai!\nAnda mengetik: \"" + typed + "\"\nSeharusnya: \"" + expectedPhrase + "\"",
                    "Penghapusan Dibatalkan",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        return true;
    }

    // ==================== HAPUS PROVINSI DENGAN VALIDASI KETIK ULANG ====================
    private void deleteProvinsiWithValidation(String provinsi) {
        String expectedPhrase = "hapus " + provinsi;
        String warningHtml = "<html><b>PERINGATAN:</b> Tindakan ini akan menghapus seluruh data jenjang pada <b>"
                + provinsi + "</b> secara permanen.</html>";

        if (!confirmWithTypedText(this, "Validasi Hapus Provinsi", warningHtml, expectedPhrase)) {
            return;
        }

        runWithLoading(this, "Menghapus data provinsi " + provinsi + "...", () -> {
            String sql = "DELETE FROM penyaluran_pip WHERE provinsi = ?";
            int deleted;
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, provinsi);
                deleted = ps.executeUpdate();
            }
            List<Object[]> rows = fetchProvinsiRows(txtSearch.getText().trim());
            Map<String, long[]> summary = fetchSummaryMap();
            return new Object[]{deleted, rows, summary};
        }, res -> {
            int deleted = (int) res[0];
            @SuppressWarnings("unchecked")
            List<Object[]> rows = (List<Object[]>) res[1];
            @SuppressWarnings("unchecked")
            Map<String, long[]> summary = (Map<String, long[]>) res[2];

            applyProvinsiRowsToTable(rows);
            applySummaryToCards(summary);
            JOptionPane.showMessageDialog(
                    this,
                    "Provinsi \"" + provinsi + "\" (" + deleted + " baris jenjang) berhasil dihapus.",
                    "Sukses Hapus Provinsi",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    // ==================== MODAL TAMBAH PROVINSI SEKALIGUS JENJANG SD - SMA (+ OPSIONAL SMK) ====================
    private void openCreateProvinsiBatchDialog() {
        JDialog dialog = createDimmedDialog(this,
                "Tambah Provinsi Baru & Pengaturan Anggaran Awal (SD - SMA)", 980, 440);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
        topPanel.setBackground(new Color(248, 250, 252));
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_GRAY));

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
                "Jenjang",
                "Siswa Pemberian",
                "Siswa Aktivasi",
                "Siswa Relaksasi",
                "Anggaran Pemberian (Rp)",
                "Anggaran Aktivasi (Rp)",
                "Anggaran Relaksasi (Rp)"
        };

        for (String h : headers) {
            JLabel lblH = new JLabel(h, SwingConstants.CENTER);
            lblH.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblH.setForeground(PRIMARY_DARK);
            gridPanel.add(lblH);
        }

        JCheckBox[] chkInclude = new JCheckBox[4];
        JTextField[][] fields = new JTextField[4][6];

        for (int r = 0; r < 4; r++) {
            String j = jenjangList[r];
            chkInclude[r] = new JCheckBox(j, r < 3);
            chkInclude[r].setFont(new Font("Segoe UI", Font.BOLD, 13));
            chkInclude[r].setBackground(Color.WHITE);
            if (r < 3) {
                chkInclude[r].setEnabled(false);
            }
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
                    for (int c = 0; c < 6; c++) {
                        fields[rowIdx][c].setEnabled(active);
                    }
                });
            }
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(new Color(248, 250, 252));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_GRAY));

        JButton btnBatal = createStyledButton("Batal", new Color(100, 116, 139));
        JButton btnSimpan = createStyledButton("Simpan Provinsi & Semua Jenjang", ACCENT_BLUE);
        btnSimpan.setPreferredSize(new Dimension(245, 34));

        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> {
            String provinsi = txtProvinsi.getText().trim();
            if (provinsi.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Nama provinsi wajib diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<Object[]> batchData = new ArrayList<>();
            try {
                for (int r = 0; r < 4; r++) {
                    if (!chkInclude[r].isSelected()) {
                        continue;
                    }
                    String jenjang = jenjangList[r];
                    int sPemb = parseNonNegativeInt(fields[r][0].getText(), jenjang + " - Siswa Pemberian");
                    int sAkt = parseNonNegativeInt(fields[r][1].getText(), jenjang + " - Siswa Aktivasi");
                    int sRel = parseNonNegativeInt(fields[r][2].getText(), jenjang + " - Siswa Relaksasi");
                    long sTot = (long) sPemb + sAkt + sRel;

                    long dPemb = parseNonNegativeLong(fields[r][3].getText(), jenjang + " - Anggaran Pemberian");
                    long dAkt = parseNonNegativeLong(fields[r][4].getText(), jenjang + " - Anggaran Aktivasi");
                    long dRel = parseNonNegativeLong(fields[r][5].getText(), jenjang + " - Anggaran Relaksasi");
                    long dTot = dPemb + dAkt + dRel;

                    batchData.add(new Object[]{provinsi, jenjang, sTot, sPemb, sAkt, sRel, dTot, dPemb, dAkt, dRel});
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
                return;
            }

            runWithLoading(dialog, "Menyimpan data provinsi baru ke cloud...", () -> {
                try (Connection conn = getConnection()) {
                    try (PreparedStatement checkPs = conn.prepareStatement(
                            "SELECT COUNT(*) FROM penyaluran_pip WHERE provinsi = ?")) {
                        checkPs.setString(1, provinsi);
                        try (ResultSet rs = checkPs.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                throw new IllegalStateException("Provinsi \"" + provinsi + "\" sudah terdaftar di database!");
                            }
                        }
                    }

                    conn.setAutoCommit(false);
                    String sql = "INSERT INTO penyaluran_pip (provinsi, jenjang, siswa_disalurkan, "
                            + "siswa_pemberian, siswa_aktivasi_nominasi, siswa_relaksasi, "
                            + "dana_disalurkan, dana_pemberian, dana_aktivasi_nominasi, dana_relaksasi) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        for (Object[] row : batchData) {
                            ps.setString(1, (String) row[0]);
                            ps.setString(2, (String) row[1]);
                            ps.setLong(3, (Long) row[2]);
                            ps.setInt(4, (Integer) row[3]);
                            ps.setInt(5, (Integer) row[4]);
                            ps.setInt(6, (Integer) row[5]);
                            ps.setLong(7, (Long) row[6]);
                            ps.setLong(8, (Long) row[7]);
                            ps.setLong(9, (Long) row[8]);
                            ps.setLong(10, (Long) row[9]);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                        conn.commit();
                    } catch (Exception ex) {
                        conn.rollback();
                        throw ex;
                    } finally {
                        conn.setAutoCommit(true);
                    }
                }
                return batchData.size();
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

    // ==================== MODAL DETAIL PER JENJANG (SD, SMP, SMA, SMK) ====================
    private void openProvinsiDetailDialog(String provinsi) {    
        JDialog detailDialog = createDimmedDialog(this,
                "Rincian Penyaluran per Jenjang - " + provinsi, 1360, 520);
        detailDialog.setLayout(new BorderLayout(0, 10));
        detailDialog.getContentPane().setBackground(BG_MAIN);

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
        btnTambahJenjang.setPreferredSize(new Dimension(190, 34));
        topHeader.add(btnTambahJenjang, BorderLayout.EAST);

        detailDialog.add(topHeader, BorderLayout.NORTH);

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
        cm.getColumn(0).setCellRenderer(centerRenderer);
        cm.getColumn(1).setCellRenderer(centerRenderer);
        cm.getColumn(2).setCellRenderer(rightRenderer);
        cm.getColumn(3).setCellRenderer(rightRenderer);
        cm.getColumn(4).setCellRenderer(rightRenderer);
        cm.getColumn(5).setCellRenderer(boldRightRenderer);
        cm.getColumn(6).setCellRenderer(rightRenderer);
        cm.getColumn(7).setCellRenderer(rightRenderer);
        cm.getColumn(8).setCellRenderer(rightRenderer);
        cm.getColumn(9).setCellRenderer(boldRightRenderer);

        autoFitTableColumns(detailTable, 10, 240);

        Runnable reloadDetail = () -> {
            runWithLoading(detailDialog, "Memuat rincian jenjang " + provinsi + "...", () -> {
                List<Object[]> rows = new ArrayList<>();
                String sql = "SELECT * FROM penyaluran_pip WHERE provinsi = ? "
                        + "ORDER BY FIELD(jenjang, 'SD', 'SMP', 'SMA', 'SMK'), id ASC";
                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, provinsi);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            rows.add(new Object[]{
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
                }
                return rows;
            }, rows -> {
                detailModel.setRowCount(0);
                for (Object[] r : rows) {
                    detailModel.addRow(r);
                }
                autoFitTableColumns(detailTable, 10, 240);
            });
        };

        cm.getColumn(10).setCellRenderer(new DetailRowActionRenderer());
        cm.getColumn(10).setCellEditor(new DetailRowActionEditor(detailDialog, detailModel, provinsi, reloadDetail));

        btnTambahJenjang.addActionListener(e -> {
            openCreateSingleJenjangDialog(detailDialog, provinsi, () -> {
                reloadDetail.run();
                refreshAllWithLoading("Memperbarui rekap nasional...");
            });
        });

        JScrollPane sp = new JScrollPane(detailTable);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_GRAY, 1));
        sp.getViewport().setBackground(Color.WHITE);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        bottomBar.setOpaque(false);
        JButton btnTutup = createStyledButton("Tutup Rincian", new Color(71, 85, 105));
        btnTutup.setPreferredSize(new Dimension(135, 34));
        btnTutup.addActionListener(e -> detailDialog.dispose());
        bottomBar.add(btnTutup);

        bodyCard.add(sp, BorderLayout.CENTER);
        bodyCard.add(bottomBar, BorderLayout.SOUTH);

        detailDialog.add(bodyCard, BorderLayout.CENTER);

        detailDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                reloadDetail.run();
            }
        });

        detailDialog.setVisible(true);
    }

    // ==================== MODAL TAMBAH SATU JENJANG DI DALAM DETAIL PROVINSI ====================
    private void openCreateSingleJenjangDialog(JDialog parentDialog, String provinsi, Runnable onSuccessCallback) {
        JDialog dialog = createDimmedDialog(parentDialog, "Tambah Jenjang Baru - " + provinsi, 490, 480);

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(18, 20, 14, 20));

        JLabel lblProvVal = new JLabel(provinsi);
        lblProvVal.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JComboBox<String> cmbJenjang = new JComboBox<>(new String[]{"SD", "SMP", "SMA", "SMK"});
        JTextField txtSiswaPemberian = new JTextField("0");
        JTextField txtSiswaAktivasi = new JTextField("0");
        JTextField txtSiswaRelaksasi = new JTextField("0");
        JTextField txtDanaPemberian = new JTextField("0");
        JTextField txtDanaAktivasi = new JTextField("0");
        JTextField txtDanaRelaksasi = new JTextField("0");

        formPanel.add(new JLabel("Provinsi:"));
        formPanel.add(lblProvVal);
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
        JButton btnSimpan = createStyledButton("Simpan Jenjang", ACCENT_BLUE);
        btnSimpan.setPreferredSize(new Dimension(150, 32));

        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> {
            String jenjang = (String) cmbJenjang.getSelectedItem();
            int sPemberian, sAktivasi, sRelaksasi;
            long dPemberian, dAktivasi, dRelaksasi;

            try {
                sPemberian = parseNonNegativeInt(txtSiswaPemberian.getText(), "Siswa Pemberian");
                sAktivasi = parseNonNegativeInt(txtSiswaAktivasi.getText(), "Siswa Aktivasi");
                sRelaksasi = parseNonNegativeInt(txtSiswaRelaksasi.getText(), "Siswa Relaksasi");

                dPemberian = parseNonNegativeLong(txtDanaPemberian.getText(), "Anggaran Pemberian");
                dAktivasi = parseNonNegativeLong(txtDanaAktivasi.getText(), "Anggaran Aktivasi");
                dRelaksasi = parseNonNegativeLong(txtDanaRelaksasi.getText(), "Anggaran Relaksasi");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
                return;
            }

            long sTotal = (long) sPemberian + sAktivasi + sRelaksasi;
            long dTotal = dPemberian + dAktivasi + dRelaksasi;

            runWithLoading(dialog, "Menyimpan data jenjang " + jenjang + "...", () -> {
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
                return null;
            }, ignored -> {
                dialog.dispose();
                if (onSuccessCallback != null) {
                    onSuccessCallback.run();
                }
                JOptionPane.showMessageDialog(parentDialog, "Data jenjang " + jenjang + " berhasil ditambahkan!",
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
            });
        });

        btnPanel.add(btnBatal);
        btnPanel.add(btnSimpan);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ==================== MODAL EDIT STATUS SISWA SAJA (TANPA EDIT ANGGARAN) ====================
    private void openEditSiswaOnlyDialog(JDialog parentDialog, int editId, Runnable onSuccessCallback) {
        runWithLoading(parentDialog, "Mengambil data siswa...", () -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM penyaluran_pip WHERE id = ?")) {
                ps.setInt(1, editId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Object[]{
                                rs.getString("provinsi"),
                                rs.getString("jenjang"),
                                rs.getInt("siswa_pemberian"),
                                rs.getInt("siswa_aktivasi_nominasi"),
                                rs.getInt("siswa_relaksasi")
                        };
                    }
                }
            }
            throw new SQLException("Data tidak ditemukan.");
        }, data -> {
            String provinsi = (String) data[0];
            String jenjang = (String) data[1];
            int oldSPemberian = (int) data[2];
            int oldSAktivasi = (int) data[3];
            int oldSRelaksasi = (int) data[4];

            JDialog dialog = createDimmedDialog(parentDialog,
                    "Edit Jumlah Status Siswa (Anggaran Terkunci)", 450, 320);

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
            btnSimpan.setPreferredSize(new Dimension(165, 32));

            btnBatal.addActionListener(e -> dialog.dispose());
            btnSimpan.addActionListener(e -> {
                int sPemberian, sAktivasi, sRelaksasi;
                try {
                    sPemberian = parseNonNegativeInt(txtSPemberian.getText(), "Siswa Pemberian");
                    sAktivasi = parseNonNegativeInt(txtSAktivasi.getText(), "Siswa Aktivasi");
                    sRelaksasi = parseNonNegativeInt(txtSRelaksasi.getText(), "Siswa Relaksasi");
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                long sTotal = (long) sPemberian + sAktivasi + sRelaksasi;

                runWithLoading(dialog, "Memperbarui status siswa...", () -> {
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
                    return null;
                }, ignored -> {
                    dialog.dispose();
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                    refreshAllWithLoading("Menyinkronkan data utama...");
                    JOptionPane.showMessageDialog(parentDialog,
                            "Jumlah status siswa berhasil diperbarui (anggaran tetap terkunci).",
                            "Sukses Update", JOptionPane.INFORMATION_MESSAGE);
                });
            });

            btnPanel.add(btnBatal);
            btnPanel.add(btnSimpan);

            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(btnPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });
    }

    // ==================== MODAL MUTASI STATUS SISWA ====================
    private void openMutasiSiswaDialog(JDialog parentDialog, int id, Runnable onSuccessCallback) {
        runWithLoading(parentDialog, "Memuat data stok siswa & anggaran...", () -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM penyaluran_pip WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Object[]{
                                rs.getString("provinsi"),
                                rs.getString("jenjang"),
                                rs.getLong("siswa_pemberian"),
                                rs.getLong("siswa_aktivasi_nominasi"),
                                rs.getLong("siswa_relaksasi"),
                                rs.getLong("dana_pemberian"),
                                rs.getLong("dana_aktivasi_nominasi"),
                                rs.getLong("dana_relaksasi")
                        };
                    }
                }
            }
            throw new SQLException("Data tidak ditemukan.");
        }, data -> {
            String provinsi = (String) data[0];
            String jenjang = (String) data[1];
            final long curSPemb = (long) data[2], curSAkt = (long) data[3], curSRel = (long) data[4];
            final long curDPemb = (long) data[5], curDAkt = (long) data[6], curDRel = (long) data[7];

            JDialog dialog = createDimmedDialog(parentDialog,
                    "Mutasi Status Siswa - " + provinsi + " (" + jenjang + ")", 520, 410);

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

            String[] kategori = {"Siswa Pemberian", "Siswa Aktivasi Nominasi", "Siswa Relaksasi"};
            JComboBox<String> cmbAsal = new JComboBox<>(kategori);
            cmbAsal.setSelectedIndex(1);
            JComboBox<String> cmbTujuan = new JComboBox<>(kategori);
            cmbTujuan.setSelectedIndex(0);

            JTextField txtJumlahPindah = new JTextField("0");
            JLabel lblPreviewDana = new JLabel("Rp 0");
            lblPreviewDana.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblPreviewDana.setForeground(ACCENT_PURPLE);

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
            btnProses.setPreferredSize(new Dimension(170, 32));

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

                long danaMutasi = (jmlPindah == siswaArr[asalIdx])
                        ? danaArr[asalIdx]
                        : Math.round(((double) danaArr[asalIdx] / siswaArr[asalIdx]) * jmlPindah);

                siswaArr[asalIdx] -= jmlPindah;
                danaArr[asalIdx] -= danaMutasi;

                siswaArr[tujuanIdx] += jmlPindah;
                danaArr[tujuanIdx] += danaMutasi;

                long totalSiswa = siswaArr[0] + siswaArr[1] + siswaArr[2];
                long totalDana = danaArr[0] + danaArr[1] + danaArr[2];

                runWithLoading(dialog, "Memproses mutasi siswa & proporsi anggaran...", () -> {
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
                    }
                    return null;
                }, ignored -> {
                    dialog.dispose();
                    if (onSuccessCallback != null) {
                        onSuccessCallback.run();
                    }
                    refreshAllWithLoading("Menyinkronkan data utama...");
                    JOptionPane.showMessageDialog(parentDialog,
                            "Mutasi berhasil!\n"
                                    + NF.format(jmlPindah) + " siswa dan anggaran Rp " + NF.format(danaMutasi)
                                    + "\ntelah dipindahkan dari " + kategori[asalIdx] + " ke " + kategori[tujuanIdx] + ".",
                            "Sukses Mutasi", JOptionPane.INFORMATION_MESSAGE);
                });
            });

            btnPanel.add(btnBatal);
            btnPanel.add(btnProses);

            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(btnPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });
    }

    // ==================== CUSTOM GLASS PANES (LOADING SPINNER & DIM BACKDROP) ====================
    private static class DimBackdropGlassPane extends JComponent {
        public DimBackdropGlassPane() {
            setOpaque(false);
            addMouseListener(new MouseAdapter() {});
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(15, 23, 42, 135));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private static class LoadingGlassPane extends JComponent {
        private final String message;
        private int angle = 0;
        private final Timer timer;

        public LoadingGlassPane(String message) {
            this.message = message;
            setOpaque(false);
            setLayout(new GridBagLayout());
            addMouseListener(new MouseAdapter() {});

            JPanel card = new JPanel(new BorderLayout(14, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.setColor(new Color(203, 213, 225));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                    g2.dispose();
                }
            };
            card.setOpaque(false);
            card.setBorder(new EmptyBorder(18, 24, 18, 26));

            JComponent spinner = new JComponent() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(32, 32);
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int size = Math.min(getWidth(), getHeight()) - 6;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;

                    g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(new Color(226, 232, 240));
                    g2.drawOval(x, y, size, size);

                    g2.setColor(ACCENT_BLUE);
                    g2.drawArc(x, y, size, size, -angle, 110);
                    g2.dispose();
                }
            };

            JLabel lblMsg = new JLabel(message);
            lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblMsg.setForeground(PRIMARY_DARK);

            card.add(spinner, BorderLayout.WEST);
            card.add(lblMsg, BorderLayout.CENTER);
            add(card);

            timer = new Timer(25, e -> {
                angle = (angle + 12) % 360;
                repaint();
            });
        }

        public void startAnimation() {
            timer.start();
        }

        public void stopAnimation() {
            timer.stop();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(15, 23, 42, 105));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
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
            setBorder(new EmptyBorder(0, 5, 0, 5));

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                c.setForeground(PRIMARY_DARK);
            }
            return c;
        }
    }

    private class MasterActionRenderer extends JPanel implements TableCellRenderer {
        public MasterActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 4));
            setOpaque(true);
            add(createSmallActionBtn("Detail", ACCENT_BLUE, 72));
            add(createSmallActionBtn("Hapus", ACCENT_RED, 72));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : ROW_ALT));
            return this;
        }
    }

    private class MasterActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private int currentRow;

        public MasterActionEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
            panel.setOpaque(true);

            JButton btnDetail = createSmallActionBtn("Detail", ACCENT_BLUE, 72);
            JButton btnHapus = createSmallActionBtn("Hapus", ACCENT_RED, 72);

            btnDetail.addActionListener(e -> {
                fireEditingStopped();
                String provinsi = (String) modelProvinsi.getValueAt(currentRow, 1);
                openProvinsiDetailDialog(provinsi);
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

    private class DetailRowActionRenderer extends JPanel implements TableCellRenderer {
        public DetailRowActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 4));
            setOpaque(true);
            add(createSmallActionBtn("Mutasi", ACCENT_PURPLE, 74));
            add(createSmallActionBtn("Edit", ACCENT_AMBER, 62));
            add(createSmallActionBtn("Hapus", ACCENT_RED, 72));
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
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 4));
            panel.setOpaque(true);

            JButton btnMutasi = createSmallActionBtn("Mutasi", ACCENT_PURPLE, 74);
            JButton btnEdit = createSmallActionBtn("Edit", ACCENT_AMBER, 62);
            JButton btnDel = createSmallActionBtn("Hapus", ACCENT_RED, 72);

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

                String expectedPhrase = "hapus " + jenjang;
                String warningHtml = "<html><b>PERINGATAN:</b> Tindakan ini akan menghapus data jenjang <b>"
                        + jenjang + "</b> pada <b>" + provinsi + "</b> secara permanen.</html>";

                if (confirmWithTypedText(parentDialog, "Validasi Hapus Jenjang", warningHtml, expectedPhrase)) {
                    runWithLoading(parentDialog, "Menghapus data jenjang " + jenjang + "...", () -> {
                        try (Connection conn = getConnection();
                             PreparedStatement ps = conn.prepareStatement("DELETE FROM penyaluran_pip WHERE id = ?")) {
                            ps.setInt(1, id);
                            ps.executeUpdate();
                        }
                        return null;
                    }, ignored -> {
                        reloadDetail.run();
                        refreshAllWithLoading("Memperbarui rekap nasional...");
                        JOptionPane.showMessageDialog(
                                parentDialog,
                                "Data jenjang \"" + jenjang + "\" pada " + provinsi + " berhasil dihapus.",
                                "Sukses Hapus Jenjang",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    });
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

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setMargin(new Insets(0, 8, 0, 8));
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
        btn.setMargin(new Insets(0, 4, 0, 4));
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
            new AppPipCrud().setVisible(true);
        });
    }
}