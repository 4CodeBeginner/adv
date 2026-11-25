package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class TableHelper {

    private TableHelper() {}

    public static void styleExcelTable(JTable tbl) {
        tbl.setRowHeight(36);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setShowGrid(true);
        tbl.setGridColor(new Color(226, 232, 240));
        tbl.setIntercellSpacing(new Dimension(1, 1));
        tbl.setSelectionBackground(new Color(219, 234, 254));
        tbl.setSelectionForeground(UiTheme.PRIMARY_DARK);
        tbl.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = tbl.getTableHeader();
        header.setPreferredSize(new Dimension(0, 38));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(UiTheme.PRIMARY_DARK);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    }

    public static void autoFitTableColumns(JTable table, int actionColumnIndex, int actionColumnWidth) {
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

    public static class ExcelCellRenderer extends DefaultTableCellRenderer {
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
                c.setBackground(row % 2 == 0 ? Color.WHITE : UiTheme.ROW_ALT);
                c.setForeground(UiTheme.PRIMARY_DARK);
            }
            return c;
        }
    }
}