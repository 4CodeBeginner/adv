package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class UiTheme {
    public static final NumberFormat NF = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"));

    public static final Color BG_MAIN = new Color(243, 244, 246);
    public static final Color BG_CARD = Color.WHITE;
    public static final Color PRIMARY_DARK = new Color(30, 41, 59);
    public static final Color ACCENT_BLUE = new Color(37, 99, 235);
    public static final Color ACCENT_GREEN = new Color(22, 163, 74);
    public static final Color ACCENT_AMBER = new Color(217, 119, 6);
    public static final Color ACCENT_PURPLE = new Color(124, 58, 237);
    public static final Color ACCENT_RED = new Color(220, 38, 38);
    public static final Color BORDER_GRAY = new Color(209, 213, 219);
    public static final Color ROW_ALT = new Color(249, 250, 251);

    private UiTheme() {}

    public static JButton createStyledButton(String text, Color bg) {
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

    public static JButton createSmallActionBtn(String text, Color bg, int width) {
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

    public static boolean confirmWithTypedText(Component parent, String title, String warningHtml, String expectedPhrase) {
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
                parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE
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

    public static int parseNonNegativeInt(String raw, String fieldName) {
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

    public static long parseNonNegativeLong(String raw, String fieldName) {
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
}