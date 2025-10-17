package driwai.updater.ui;

import driwai.updater.core.AppConfig;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla de inicio que muestra un spinner animado mientras la aplicación se carga.
 */
public class StartupScreen extends JWindow {

    public StartupScreen() {
        setSize(480, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 249, 255));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel appName = new JLabel(AppConfig.APP_NAME, SwingConstants.CENTER);
        appName.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 28));
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);
        appName.setForeground(new Color(25, 66, 120));

        JLabel loading = new JLabel("Iniciando aplicación...", SwingConstants.CENTER);
        loading.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        loading.setAlignmentX(Component.CENTER_ALIGNMENT);
        loading.setForeground(new Color(90, 90, 90));

        JComponent spinner = new RotatingCircle();
        spinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        spinner.setPreferredSize(new Dimension(50, 50));

        JLabel company = new JLabel(AppConfig.COPYRIGHT, SwingConstants.CENTER);
        company.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        company.setForeground(new Color(100, 100, 100));
        company.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(appName);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(spinner);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(loading);
        panel.add(Box.createRigidArea(new Dimension(0, 35)));
        panel.add(company);

        add(panel);
    }

    /**
     * Muestra la pantalla.
     */
    public void showScreen() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    /**
     * Cierra la pantalla.
     */
    public void closeScreen() {
        SwingUtilities.invokeLater(this::dispose);
    }

    /**
     * Spinner circular animado.
     */
    static class RotatingCircle extends JComponent {
        private float angle = 0f;
        private final Timer timer;

        RotatingCircle() {
            timer = new Timer(16, e -> {
                angle += 5f;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int size = Math.min(getWidth(), getHeight()) - 10;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(getWidth() / 2, getHeight() / 2);
            g2.rotate(Math.toRadians(angle));
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(25, 66, 120));
            g2.drawArc(-size / 2, -size / 2, size, size, 0, 270);
            g2.dispose();
        }
    }
}
