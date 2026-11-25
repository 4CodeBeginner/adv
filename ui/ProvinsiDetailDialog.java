package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import repository.PipRepository;

public class ProvinsiDetailDialog extends BaseModalDialog {

    private final String provinsi;
    private final PipRepository repository;
    private final Runnable onDataChangedCallback;
    private final DefaultTableModel detailModel;
    private final JTable detailTable;

    public ProvinsiDetailDialog(Window owner, String provinsi, PipRepository repository, Runnable onDataChangedCallback) {
        super(owner, "Rincian Penyaluran per Jenjang - " + provinsi, 1360, 520);
        this.provinsi = provinsi;
        this.repository = repository;
        this.onDataChangedCallback = onDataChangedCallback;

        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(UiTheme.BG_MAIN);

        add(createTopHeader(), BorderLayout.NORTH);

        String[] detailCols = {
                "ID", "Jenjang", "Siswa Pemberian", "Siswa Aktivasi", "Siswa Relaksasi",
                "Total Siswa", "Dana Pemberian", "Dana Aktivasi", "Dana Relaksasi",
                "Total Dana (Rp)", "Aksi"
        };

        detailModel = new DefaultTableModel(detailCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 10;
            }
        };

        detailTable = new JTable(detailModel);
        TableHelper.styleExcelTable(detailTable);

        TableHelper.ExcelCellRenderer centerRenderer = new TableHelper.ExcelCellRenderer(SwingConstants.CENTER, true);
        TableHelper.ExcelCellRenderer rightRenderer = new TableHelper.ExcelCellRenderer(SwingConstants.RIGHT, false);
        TableHelper.ExcelCellRenderer boldRightRenderer = new TableHelper.ExcelCellRenderer(SwingConstants.RIGHT, true);

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
        cm.getColumn(10).setCellRenderer(new DetailRowActionRenderer());
        cm.getColumn(10).setCellEditor(new DetailRowActionEditor());

        TableHelper.autoFitTableColumns(detailTable, 10, 240);

        JPanel bodyCard = new JPanel(new BorderLayout(0, 10));
        bodyCard.setBackground(Color.WHITE);
        bodyCard.setBorder(new EmptyBorder(14, 18, 14, 18));

        JScrollPane sp = new JScrollPane(detailTable);
        sp.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_GRAY, 1));
        sp.getViewport().setBackground(Color.WHITE);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        bottomBar.setOpaque(false);
        JButton btnTutup = UiTheme.createStyledButton("Tutup Rincian", new Color(71, 85, 105));
        btnTutup.setPreferredSize(new Dimension(135, 34));
        btnTutup.addActionListener(e -> dispose());
        bottomBar.add(btnTutup);

        bodyCard.add(sp, BorderLayout.CENTER);
        bodyCard.add(bottomBar, BorderLayout.SOUTH);
        add(bodyCard, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                reloadDetail();
            }
        });
    }

    private JPanel createTopHeader() {
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UiTheme.PRIMARY_DARK);
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

        JButton btnTambahJenjang = UiTheme.createStyledButton("+ Tambah Jenjang Baru", UiTheme.ACCENT_BLUE);
        btnTambahJenjang.setPreferredSize(new Dimension(190, 34));
        btnTambahJenjang.addActionListener(e -> openCreateSingleJenjangDialog());
        topHeader.add(btnTambahJenjang, BorderLayout.EAST);

        return topHeader;
    }

    private void reloadDetail() {
        LoadingGlassPane.runWithLoading(this, "Memuat rincian jenjang " + provinsi + "...", () ->
                repository.findByProvinsi(provinsi),
                list -> {
                    detailModel.setRowCount(0);
                    for (PenyaluranPip p : list) {
                        detailModel.addRow(new Object[]{
                                p.getId(),
                                p.getJenjang(),
                                UiTheme.NF.format(p.getSiswaPemberian()),
                                UiTheme.NF.format(p.getSiswaAktivasiNominasi()),
                                UiTheme.NF.format(p.getSiswaRelaksasi()),
                                UiTheme.NF.format(p.getSiswaDisalurkan()),
                                UiTheme.NF.format(p.getDanaPemberian()),
                                UiTheme.NF.format(p.getDanaAktivasiNominasi()),
                                UiTheme.NF.format(p.getDanaRelaksasi()),
                                "Rp " + UiTheme.NF.format(p.getDanaDisalurkan()),
                                "AKSI"
                        });
                    }
                    TableHelper.autoFitTableColumns(detailTable, 10, 240);
                }
        );
    }

    private void openCreateSingleJenjangDialog() {
        BaseModalDialog dialog = new BaseModalDialog(this, "Tambah Jenjang Baru - " + provinsi, 490, 480);

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

        formPanel.add(new JLabel("Provinsi:")); formPanel.add(lblProvVal);
        formPanel.add(new JLabel("Jenjang Pendidikan:")); formPanel.add(cmbJenjang);
        formPanel.add(new JLabel("Siswa Pemberian:")); formPanel.add(txtSiswaPemberian);
        formPanel.add(new JLabel("Siswa Aktivasi Nominasi:")); formPanel.add(txtSiswaAktivasi);
        formPanel.add(new JLabel("Siswa Relaksasi:")); formPanel.add(txtSiswaRelaksasi);
        formPanel.add(new JLabel("Anggaran Pemberian (Rp):")); formPanel.add(txtDanaPemberian);
        formPanel.add(new JLabel("Anggaran Aktivasi (Rp):")); formPanel.add(txtDanaAktivasi);
        formPanel.add(new JLabel("Anggaran Relaksasi (Rp):")); formPanel.add(txtDanaRelaksasi);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(new Color(248, 250, 252));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER_GRAY));

        JButton btnBatal = UiTheme.createStyledButton("Batal", new Color(100, 116, 139));
        JButton btnSimpan = UiTheme.createStyledButton("Simpan Jenjang", UiTheme.ACCENT_BLUE);
        btnSimpan.setPreferredSize(new Dimension(150, 32));

        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> {
            String jenjang = (String) cmbJenjang.getSelectedItem();
            PenyaluranPip baru;
            try {
                baru = new PenyaluranPip(
                        provinsi, jenjang,
                        UiTheme.parseNonNegativeInt(txtSiswaPemberian.getText(), "Siswa Pemberian"),
                        UiTheme.parseNonNegativeInt(txtSiswaAktivasi.getText(), "Siswa Aktivasi"),
                        UiTheme.parseNonNegativeInt(txtSiswaRelaksasi.getText(), "Siswa Relaksasi"),
                        UiTheme.parseNonNegativeLong(txtDanaPemberian.getText(), "Anggaran Pemberian"),
                        UiTheme.parseNonNegativeLong(txtDanaAktivasi.getText(), "Anggaran Aktivasi"),
                        UiTheme.parseNonNegativeLong(txtDanaRelaksasi.getText(), "Anggaran Relaksasi")
                );
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LoadingGlassPane.runWithLoading(dialog, "Menyimpan data jenjang " + jenjang + "...", () -> {
                repository.insertSingleJenjang(baru);
                return null;
            }, ignored -> {
                dialog.dispose();
                reloadDetail();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
                JOptionPane.showMessageDialog(this, "Data jenjang " + jenjang + " berhasil ditambahkan!",
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
            });
        });

        btnPanel.add(btnBatal);
        btnPanel.add(btnSimpan);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void openEditSiswaOnlyDialog(int editId) {
        LoadingGlassPane.runWithLoading(this, "Mengambil data siswa...", () -> repository.findById(editId), item -> {
            BaseModalDialog dialog = new BaseModalDialog(this,
                    "Edit Jumlah Status Siswa (Anggaran Terkunci)", 450, 320);

            JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 12));
            formPanel.setBackground(Color.WHITE);
            formPanel.setBorder(new EmptyBorder(20, 20, 16, 20));

            JLabel lblInfoVal = new JLabel(item.getProvinsi() + " (" + item.getJenjang() + ")");
            lblInfoVal.setFont(new Font("Segoe UI", Font.BOLD, 13));

            JTextField txtSPemberian = new JTextField(String.valueOf(item.getSiswaPemberian()));
            JTextField txtSAktivasi = new JTextField(String.valueOf(item.getSiswaAktivasiNominasi()));
            JTextField txtSRelaksasi = new JTextField(String.valueOf(item.getSiswaRelaksasi()));

            formPanel.add(new JLabel("Wilayah / Jenjang:")); formPanel.add(lblInfoVal);
            formPanel.add(new JLabel("Jumlah Siswa Pemberian:")); formPanel.add(txtSPemberian);
            formPanel.add(new JLabel("Jumlah Siswa Aktivasi Nominasi:")); formPanel.add(txtSAktivasi);
            formPanel.add(new JLabel("Jumlah Siswa Relaksasi:")); formPanel.add(txtSRelaksasi);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
            btnPanel.setBackground(new Color(248, 250, 252));
            btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER_GRAY));

            JButton btnBatal = UiTheme.createStyledButton("Batal", new Color(100, 116, 139));
            JButton btnSimpan = UiTheme.createStyledButton("Update Status Siswa", UiTheme.ACCENT_AMBER);
            btnSimpan.setPreferredSize(new Dimension(165, 32));

            btnBatal.addActionListener(e -> dialog.dispose());
            btnSimpan.addActionListener(e -> {
                int sPemb, sAkt, sRel;
                try {
                    sPemb = UiTheme.parseNonNegativeInt(txtSPemberian.getText(), "Siswa Pemberian");
                    sAkt = UiTheme.parseNonNegativeInt(txtSAktivasi.getText(), "Siswa Aktivasi");
                    sRel = UiTheme.parseNonNegativeInt(txtSRelaksasi.getText(), "Siswa Relaksasi");
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                LoadingGlassPane.runWithLoading(dialog, "Memperbarui status siswa...", () -> {
                    repository.updateSiswaOnly(editId, sPemb, sAkt, sRel);
                    return null;
                }, ignored -> {
                    dialog.dispose();
                    reloadDetail();
                    if (onDataChangedCallback != null) onDataChangedCallback.run();
                    JOptionPane.showMessageDialog(this,
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

    private void openMutasiSiswaDialog(int id) {
        LoadingGlassPane.runWithLoading(this, "Memuat data stok siswa & anggaran...", () -> repository.findById(id), item -> {
            BaseModalDialog dialog = new BaseModalDialog(this,
                    "Mutasi Status Siswa - " + item.getProvinsi() + " (" + item.getJenjang() + ")", 520, 410);

            JPanel infoBox = new JPanel(new GridLayout(3, 1, 0, 3));
            infoBox.setBackground(new Color(241, 245, 249));
            infoBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER_GRAY),
                    new EmptyBorder(12, 18, 12, 18)
            ));

            JLabel l1 = new JLabel("• Siswa Pemberian : " + UiTheme.NF.format(item.getSiswaPemberian()) + " siswa  (Dana: Rp " + UiTheme.NF.format(item.getDanaPemberian()) + ")");
            JLabel l2 = new JLabel("• Siswa Aktivasi  : " + UiTheme.NF.format(item.getSiswaAktivasiNominasi()) + " siswa  (Dana: Rp " + UiTheme.NF.format(item.getDanaAktivasiNominasi()) + ")");
            JLabel l3 = new JLabel("• Siswa Relaksasi : " + UiTheme.NF.format(item.getSiswaRelaksasi()) + " siswa  (Dana: Rp " + UiTheme.NF.format(item.getDanaRelaksasi()) + ")");
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
            lblPreviewDana.setForeground(UiTheme.ACCENT_PURPLE);

            Runnable updatePreview = () -> {
                try {
                    int asalIdx = cmbAsal.getSelectedIndex();
                    long jml = Long.parseLong(txtJumlahPindah.getText().trim());
                    if (jml < 0) {
                        lblPreviewDana.setText("Jumlah tidak valid");
                        return;
                    }
                    long stokSiswa = (asalIdx == 0) ? item.getSiswaPemberian() : (asalIdx == 1 ? item.getSiswaAktivasiNominasi() : item.getSiswaRelaksasi());
                    long stokDana = (asalIdx == 0) ? item.getDanaPemberian() : (asalIdx == 1 ? item.getDanaAktivasiNominasi() : item.getDanaRelaksasi());

                    if (stokSiswa <= 0 || jml > stokSiswa) {
                        lblPreviewDana.setText("Melebihi stok siswa asal!");
                        return;
                    }
                    long danaPindah = (jml == stokSiswa) ? stokDana : Math.round(((double) stokDana / stokSiswa) * jml);
                    lblPreviewDana.setText("Rp " + UiTheme.NF.format(danaPindah) + " (otomatis pindah)");
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

            formPanel.add(new JLabel("Pindahkan Dari (Kategori Asal):")); formPanel.add(cmbAsal);
            formPanel.add(new JLabel("Pindahkan Ke (Kategori Tujuan):")); formPanel.add(cmbTujuan);
            formPanel.add(new JLabel("Jumlah Siswa Dipindahkan:")); formPanel.add(txtJumlahPindah);
            formPanel.add(new JLabel("Anggaran Ikut Berpindah:")); formPanel.add(lblPreviewDana);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
            btnPanel.setBackground(new Color(248, 250, 252));
            btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER_GRAY));

            JButton btnBatal = UiTheme.createStyledButton("Batal", new Color(100, 116, 139));
            JButton btnProses = UiTheme.createStyledButton("Proses Mutasi Siswa", UiTheme.ACCENT_PURPLE);
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
                    if (jmlPindah <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Masukkan jumlah siswa yang valid (> 0).",
                            "Validasi Mutasi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                LoadingGlassPane.runWithLoading(dialog, "Memproses mutasi siswa & proporsi anggaran...", () ->
                        repository.mutasiStatusSiswa(id, asalIdx, tujuanIdx, jmlPindah),
                        danaMutasi -> {
                            dialog.dispose();
                            reloadDetail();
                            if (onDataChangedCallback != null) onDataChangedCallback.run();
                            JOptionPane.showMessageDialog(this,
                                    "Mutasi berhasil!\n"
                                            + UiTheme.NF.format(jmlPindah) + " siswa dan anggaran Rp " + UiTheme.NF.format(danaMutasi)
                                            + "\ntelah dipindahkan dari " + kategori[asalIdx] + " ke " + kategori[tujuanIdx] + ".",
                                    "Sukses Mutasi", JOptionPane.INFORMATION_MESSAGE);
                        }
                );
            });

            btnPanel.add(btnBatal);
            btnPanel.add(btnProses);

            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(btnPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });
    }

    private static class DetailRowActionRenderer extends JPanel implements TableCellRenderer {
        public DetailRowActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 4));
            setOpaque(true);
            add(UiTheme.createSmallActionBtn("Mutasi", UiTheme.ACCENT_PURPLE, 74));
            add(UiTheme.createSmallActionBtn("Edit", UiTheme.ACCENT_AMBER, 62));
            add(UiTheme.createSmallActionBtn("Hapus", UiTheme.ACCENT_RED, 72));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : UiTheme.ROW_ALT));
            return this;
        }
    }

    private class DetailRowActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private int currentRow;

        public DetailRowActionEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 4));
            panel.setOpaque(true);

            JButton btnMutasi = UiTheme.createSmallActionBtn("Mutasi", UiTheme.ACCENT_PURPLE, 74);
            JButton btnEdit = UiTheme.createSmallActionBtn("Edit", UiTheme.ACCENT_AMBER, 62);
            JButton btnDel = UiTheme.createSmallActionBtn("Hapus", UiTheme.ACCENT_RED, 72);

            btnMutasi.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) detailModel.getValueAt(currentRow, 0);
                openMutasiSiswaDialog(id);
            });

            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) detailModel.getValueAt(currentRow, 0);
                openEditSiswaOnlyDialog(id);
            });

            btnDel.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) detailModel.getValueAt(currentRow, 0);
                String jenjang = (String) detailModel.getValueAt(currentRow, 1);

                String expectedPhrase = "hapus " + jenjang;
                String warningHtml = "<html><b>PERINGATAN:</b> Tindakan ini akan menghapus data jenjang <b>"
                        + jenjang + "</b> pada <b>" + provinsi + "</b> secara permanen.</html>";

                if (UiTheme.confirmWithTypedText(ProvinsiDetailDialog.this, "Validasi Hapus Jenjang", warningHtml, expectedPhrase)) {
                    LoadingGlassPane.runWithLoading(ProvinsiDetailDialog.this, "Menghapus data jenjang " + jenjang + "...", () -> {
                        repository.deleteById(id);
                        return null;
                    }, ignored -> {
                        reloadDetail();
                        if (onDataChangedCallback != null) onDataChangedCallback.run();
                        JOptionPane.showMessageDialog(
                                ProvinsiDetailDialog.this,
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
}