package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;

public class BaseModalDialog extends JDialog {

    public BaseModalDialog(Window owner, String title, int width, int height) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setSize(width, height);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        if (owner instanceof RootPaneContainer) {
            RootPaneContainer rpc = (RootPaneContainer) owner;
            DimBackdropGlassPane backdrop = new DimBackdropGlassPane();

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    rpc.setGlassPane(backdrop);
                    backdrop.setVisible(true);
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    clearBackdrop(rpc, backdrop);
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    clearBackdrop(rpc, backdrop);
                }
            });
        }
    }

    private void clearBackdrop(RootPaneContainer rpc, DimBackdropGlassPane backdrop) {
        backdrop.setVisible(false);
        if (rpc.getGlassPane() == backdrop) {
            JPanel emptyGlass = new JPanel();
            emptyGlass.setOpaque(false);
            emptyGlass.setVisible(false);
            rpc.setGlassPane(emptyGlass);
        }
    }

    public static class DimBackdropGlassPane extends JComponent {
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
}