package ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class LoadingGlassPane extends JComponent {
    private int angle = 0;
    private final Timer timer;

    public LoadingGlassPane(String message) {
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

                g2.setColor(UiTheme.ACCENT_BLUE);
                g2.drawArc(x, y, size, size, -angle, 110);
                g2.dispose();
            }
        };

        JLabel lblMsg = new JLabel(message);
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMsg.setForeground(UiTheme.PRIMARY_DARK);

        card.add(spinner, BorderLayout.WEST);
        card.add(lblMsg, BorderLayout.CENTER);
        add(card);

        timer = new Timer(25, e -> {
            angle = (angle + 12) % 360;
            repaint();
        });
    }

    public void startAnimation() { timer.start(); }
    public void stopAnimation() { timer.stop(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(15, 23, 42, 105));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }

    public static <T> void runWithLoading(RootPaneContainer targetContainer, String message,
                                          Callable<T> backgroundTask, Consumer<T> onSuccess) {
        LoadingGlassPane overlay = new LoadingGlassPane(message);

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

                // Selalu bersihkan GlassPane pada targetContainer setelah loading selesai
                JPanel cleanGlass = new JPanel();
                cleanGlass.setOpaque(false);
                cleanGlass.setVisible(false);
                targetContainer.setGlassPane(cleanGlass);

                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    Component parentComp = (targetContainer instanceof Component) ? (Component) targetContainer : null;
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
}