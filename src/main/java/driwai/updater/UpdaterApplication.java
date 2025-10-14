package driwai.updater;

import org.update4j.Configuration;
import org.update4j.FileMetadata;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.nio.file.*;

public class UpdaterApplication {

	private static final String APP_NAME = "BiblioSystem-Geológica";
	private static final String COPYRIGHT = "© 2025 DRIWAI TECHNOLOGIES";
	private static final String APP_NAME_JAR = "BiblioSystem-Geologica.jar";

	private static JProgressBar progressBar;
	private static JLabel statusLabel;
	private static JFrame frame;
	private static JWindow startupWindow;

	private static volatile boolean cancelExecution = false;

	public static void main(String[] args) {
		try {
			FlatLightLaf.setup();
			showLoadingScreen("Verificando actualizaciones...");

			// Carpeta de la aplicación oculta
			Path appDir = Paths.get("app");
			if (!Files.exists(appDir)) Files.createDirectories(appDir);

			// Hacer carpeta oculta en Windows
			try {
				if (System.getProperty("os.name").toLowerCase().contains("win")) {
					Files.setAttribute(appDir, "dos:hidden", true);
					System.out.println("✅ Carpeta 'app' oculta correctamente en Windows.");
				}
			} catch (IOException e) {
				System.out.println("❌ No se pudo ocultar la carpeta: " + e.getMessage());
			}

			URL configUrl = new URL("https://updatesystem.figmmg.shop/update/geologica/config.xml");
			Configuration config;
			try (Reader reader = new InputStreamReader(configUrl.openStream())) {
				config = Configuration.read(reader);
				System.out.println("🧾 Config leído desde: " + configUrl);
			}

			boolean needsUpdate = false;
			for (FileMetadata file : config.getFiles()) {
				Path localPath = appDir.resolve(file.getPath().getFileName());
				long localSize = Files.exists(localPath) ? Files.size(localPath) : 0;

				System.out.println("🧾 Archivo: " + file.getPath());
				System.out.println("   Tamaño remoto: " + file.getSize() + " bytes");
				System.out.println("   Tamaño local: " + localSize + " bytes");

				if (!Files.exists(localPath) || localSize != file.getSize()) {
					needsUpdate = true;
				}
			}

			if (needsUpdate) {
				int result = JOptionPane.showConfirmDialog(null,
						"Se encontró una nueva versión de " + APP_NAME + ".\n¿Desea actualizar ahora?",
						"Actualización disponible",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);

				if (result == JOptionPane.YES_OPTION) {
					SwingUtilities.invokeLater(() -> showUpdateWindow("Actualizando " + APP_NAME + "..."));

					for (FileMetadata file : config.getFiles()) {
						if (cancelExecution) break;
						Path localPath = appDir.resolve(file.getPath().getFileName());
						boolean shouldDownload = !Files.exists(localPath) || Files.size(localPath) != file.getSize();
						if (shouldDownload) downloadFile(file, localPath);
						else updateStatus("✅ Archivo actualizado: " + localPath.getFileName());
					}

					if (!cancelExecution) {
						updateStatus("🎉 Actualización completada correctamente.");
						Thread.sleep(1200);
						if (frame != null) frame.dispose();
					} else {
						System.out.println("❌ Actualización cancelada por el usuario.");
						return;
					}
				} else {
					System.out.println("🚀 Usuario decidió no actualizar.");
				}
			}

			if (!cancelExecution) {
				showAppStartupScreen();

				System.out.println("🚀 Iniciando aplicación...");
				ProcessBuilder pb = new ProcessBuilder("java", "-jar", appDir.resolve(APP_NAME_JAR).toString());
				Process process = pb.start();

				Thread readerThread = new Thread(() -> {
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
						String line;
						while ((line = reader.readLine()) != null) {
							System.out.println(line);
							if (line.contains("APP_READY")) {
								SwingUtilities.invokeLater(() -> {
									if (startupWindow != null) {
										startupWindow.dispose();
										System.out.println("✅ Ventana de carga cerrada automáticamente.");
									}
								});
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				});
				readerThread.setDaemon(true);
				readerThread.start();
			}

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "❌ Error general: " + e.getMessage());
		} finally {
			if (frame != null) SwingUtilities.invokeLater(() -> frame.dispose());
		}
	}

	// ================== Métodos auxiliares ==================
	private static void showUpdateWindow(String title) {
		frame = new JFrame(title);
		frame.setUndecorated(false);
		frame.setResizable(false);
		frame.setSize(520, 270);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int result = JOptionPane.showConfirmDialog(frame,
						"¿Desea cancelar la actualización y cerrar el programa?",
						"Cancelar actualización",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					cancelExecution = true;
					frame.dispose();
				}
			}
		});

		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

		JLabel appTitle = new JLabel(APP_NAME, SwingConstants.CENTER);
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

		frame.add(panel);
		frame.setVisible(true);
	}

	private static void showAppStartupScreen() {
		startupWindow = new JWindow();
		startupWindow.setSize(480, 300);
		startupWindow.setLocationRelativeTo(null);

		JPanel panel = new JPanel();
		panel.setBackground(new Color(245, 249, 255));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

		JLabel appName = new JLabel(APP_NAME, SwingConstants.CENTER);
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

		JLabel company = new JLabel(COPYRIGHT, SwingConstants.CENTER);
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

		startupWindow.add(panel);
		startupWindow.setVisible(true);
	}

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

	private static void downloadFile(FileMetadata file, Path outputPath) {
		try {
			updateStatus("⬇️ Descargando: " + outputPath.getFileName());
			Files.createDirectories(outputPath.getParent());
			long totalSize = file.getSize();
			long startTime = System.currentTimeMillis();

			try (InputStream in = new BufferedInputStream(file.getUri().toURL().openStream());
				 FileOutputStream out = new FileOutputStream(outputPath.toFile())) {

				byte[] buffer = new byte[8192];
				long totalRead = 0;
				int bytesRead;
				int lastPercent = 0;

				while ((bytesRead = in.read(buffer)) != -1) {
					if (cancelExecution) break;
					out.write(buffer, 0, bytesRead);
					totalRead += bytesRead;

					long elapsed = System.currentTimeMillis() - startTime;
					double speedKBps = totalRead / 1024.0 / (elapsed / 1000.0 + 0.1);
					long remainingBytes = totalSize - totalRead;
					int remainingSeconds = (int) (remainingBytes / 1024.0 / (speedKBps + 0.1));

					int percent = (int) ((totalRead * 100) / totalSize);
					if (percent != lastPercent) {
						lastPercent = percent;
						String status = String.format("⬇️ %s: %d%% (%.1f KB/s, %ds restantes)",
								outputPath.getFileName(), percent, speedKBps, remainingSeconds);
						updateProgress(percent, status);
					}
				}
				if (!cancelExecution) updateProgress(100, "✅ Descargado: " + outputPath.getFileName());
			}
		} catch (Exception e) {
			updateStatus("❌ Error al descargar " + outputPath.getFileName());
		}
	}

	private static void showLoadingScreen(String message) {
		JWindow window = new JWindow();
		window.setSize(420, 230);
		window.setLocationRelativeTo(null);

		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

		JLabel title = new JLabel(APP_NAME, SwingConstants.CENTER);
		title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 24));
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		title.setForeground(new Color(25, 66, 120));

		JLabel loading = new JLabel(message, SwingConstants.CENTER);
		loading.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		loading.setForeground(new Color(90, 90, 90));
		loading.setAlignmentX(Component.CENTER_ALIGNMENT);

		JProgressBar spinner = new JProgressBar();
		spinner.setIndeterminate(true);
		spinner.setAlignmentX(Component.CENTER_ALIGNMENT);
		spinner.setMaximumSize(new Dimension(300, 10));

		JLabel footer = new JLabel(COPYRIGHT, SwingConstants.CENTER);
		footer.setFont(new Font("Segoe UI", Font.ITALIC, 13));
		footer.setForeground(Color.GRAY);
		footer.setAlignmentX(Component.CENTER_ALIGNMENT);

		panel.add(title);
		panel.add(Box.createRigidArea(new Dimension(0, 20)));
		panel.add(spinner);
		panel.add(Box.createRigidArea(new Dimension(0, 20)));
		panel.add(loading);
		panel.add(Box.createRigidArea(new Dimension(0, 30)));
		panel.add(footer);

		window.add(panel);
		window.setVisible(true);

		try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
		window.dispose();
	}

	private static void updateProgress(int percent, String status) {
		SwingUtilities.invokeLater(() -> {
			if (progressBar != null) progressBar.setValue(percent);
			if (statusLabel != null) statusLabel.setText(status);
		});
		System.out.println(status);
	}

	private static void updateStatus(String status) {
		SwingUtilities.invokeLater(() -> {
			if (statusLabel != null) statusLabel.setText(status);
		});
		System.out.println(status);
	}
}
