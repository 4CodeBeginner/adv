import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import repository.PipRepository;
import repository.PipRepositoryImpl;
import ui.MainDashboardFrame;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            PipRepository repository = new PipRepositoryImpl();
            MainDashboardFrame dashboard = new MainDashboardFrame(repository);
            dashboard.setVisible(true);
        });
    }
}