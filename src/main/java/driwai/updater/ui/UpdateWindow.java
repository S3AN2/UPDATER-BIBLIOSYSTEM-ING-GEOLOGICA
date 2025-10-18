package driwai.updater.ui;

import driwai.updater.core.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Ventana principal del actualizador.
 * Muestra progreso, estado y maneja el cierre (cancelación de actualización).
 */
public class UpdateWindow extends JFrame implements UIUpdater {

    private final JProgressBar progressBar;
    private final JLabel statusLabel;

    public UpdateWindow(String title, String COPYRIGHT) {
        super(title);
        setSize(520, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        // ✅ Captura el evento de cierre (❌)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                        UpdateWindow.this,
                        "¿Desea cancelar la actualización y cerrar el programa?",
                        "Cancelar actualización",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (result == JOptionPane.YES_OPTION) {
                    AppConfig.cancelExecution = true; // Marca cancelación global
                    dispose();
                }
            }
        });

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel appTitle = new JLabel(AppConfig.APP_NAME, SwingConstants.CENTER);
        appTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 26));
        appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        appTitle.setForeground(new Color(25, 66, 120));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setPreferredSize(new Dimension(400, 25));
        progressBar.setMaximumSize(new Dimension(400, 25));

        statusLabel = new JLabel("Iniciando actualización...");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(80, 80, 80));

        JLabel footer = new JLabel(COPYRIGHT, SwingConstants.CENTER);
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        footer.setForeground(Color.GRAY);

        panel.add(appTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(progressBar);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(statusLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(footer);

        add(panel);
        setVisible(true);
    }

    // Actualiza el progreso de descarga
    @Override
    public void updateProgress(int percent, String status) {
        SwingUtilities.invokeLater(() -> {
            if (progressBar != null) progressBar.setValue(percent);
            if (statusLabel != null) statusLabel.setText(status);
        });
    }

    // Actualiza el estado
    @Override
    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    // Cierra la ventana
    @Override
    public void close() {
        SwingUtilities.invokeLater(this::dispose);
    }
}
