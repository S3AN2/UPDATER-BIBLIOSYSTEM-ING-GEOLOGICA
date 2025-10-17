package driwai.updater.ui;

import driwai.updater.core.AppConfig;
import javax.swing.*;
import java.awt.*;

/**
 * Pantalla de carga inicial que muestra un indicador mientras se verifica la configuración o actualizaciones.
 */
public class LoadingScreen extends JWindow {

    private final JLabel messageLabel;

    public LoadingScreen(String message) {
        setSize(420, 230);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel(AppConfig.APP_NAME, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(new Color(25, 66, 120));

        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        progress.setMaximumSize(new Dimension(300, 10));
        progress.setAlignmentX(Component.CENTER_ALIGNMENT);

        messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(80, 80, 80));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel footer = new JLabel(AppConfig.COPYRIGHT, SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footer.setForeground(Color.GRAY);
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(progress);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(messageLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(footer);

        add(panel);
    }

    /**
     * Muestra la pantalla durante el tiempo indicado (en milisegundos).
     */
    public void show(int milliseconds) {
        SwingUtilities.invokeLater(() -> setVisible(true));
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {}
        SwingUtilities.invokeLater(this::dispose);
    }



}
