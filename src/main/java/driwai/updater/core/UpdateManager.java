package driwai.updater.core;

import org.update4j.Configuration;
import org.update4j.FileMetadata;
import driwai.updater.ui.UIUpdater;


import javax.swing.*;
import java.io.*;
import java.nio.file.*;

public class UpdateManager {

    private final Configuration config;

    public UpdateManager(Configuration config) {
        this.config = config;
    }

    /**
     * Pregunta si se desea actualizar
     */
    public boolean askForUpdate() {
        int result = JOptionPane.showConfirmDialog(
                null,
                "Se encontró una nueva versión de " + AppConfig.APP_NAME + ".\n¿Desea actualizar ahora?",
                "Actualización disponible",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Descarga todos los archivos necesarios con progreso y cancelación.
     */
    public void downloadUpdates(UIUpdater ui) {
        try {
            Path appDir = Paths.get("app");
            if (!Files.exists(appDir)) Files.createDirectories(appDir);

            for (FileMetadata file : config.getFiles()) {
                if (AppConfig.cancelExecution) {
                    ui.updateStatus("❌ Actualización cancelada por el usuario.");
                    break;
                }

                Path localPath = appDir.resolve(file.getPath().getFileName());
                boolean shouldDownload = !Files.exists(localPath) || Files.size(localPath) != file.getSize();

                if (shouldDownload) {
                    downloadFile(file, localPath, ui);
                } else {
                    ui.updateStatus("✅ Archivo actualizado: " + localPath.getFileName());
                }
            }

            if (!AppConfig.cancelExecution) {
                ui.updateStatus("🎉 Actualización completada correctamente.");
            } else {
                ui.updateStatus("⚠️ Proceso detenido. No se completó la actualización.");
            }

        } catch (Exception e) {
            ui.updateStatus("❌ Error durante la actualización: " + e.getMessage());
        }
    }

    /**
     * Descarga un solo archivo con estadísticas de progreso.
     */
    private void downloadFile(FileMetadata file, Path outputPath, UIUpdater ui) {
        InputStream in = null;

        try {
            Files.createDirectories(outputPath.getParent());
            in = file.getUri().toURL().openStream();

            try (FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
                long startTime = System.currentTimeMillis();
                byte[] buffer = new byte[8192];
                long totalRead = 0;
                long totalSize = file.getSize();
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    // 👇 Verifica cancelación global
                    if (AppConfig.cancelExecution) {
                        ui.updateStatus("⚠️ Descarga cancelada por el usuario.");
                        break;
                    }

                    out.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;

                    // Calcular estadísticas
                    long elapsed = System.currentTimeMillis() - startTime;
                    double speedKBps = totalRead / 1024.0 / (elapsed / 1000.0 + 0.1);
                    long remainingBytes = totalSize - totalRead;
                    int remainingSeconds = (int) (remainingBytes / 1024.0 / (speedKBps + 0.1));
                    int percent = (int) ((totalRead * 100) / totalSize);

                    // Actualizar UI con progreso
                    String status = String.format("⬇️ %s: %d%% (%.1f KB/s, %ds restantes)",
                            outputPath.getFileName(), percent, speedKBps, remainingSeconds);

                    ui.updateProgress(percent, status);
                }

                if (!AppConfig.cancelExecution) {
                    ui.updateProgress(100, "✅ Descargado: " + outputPath.getFileName());
                } else {
                    // ❌ Limpia el archivo incompleto
                    Files.deleteIfExists(outputPath);
                }
            }

        } catch (IOException e) {
            ui.updateStatus("❌ Error al descargar: " + outputPath.getFileName() + " (" + e.getMessage() + ")");
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException ignored) {}
        }
    }
}
