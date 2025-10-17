package driwai.updater.core;

import org.update4j.FileMetadata;
import driwai.updater.ui.UIUpdater;


import java.io.*;
import java.nio.file.*;

public class Downloader {

    // Inyección de UIUpdater
    private final UIUpdater ui;

    // Constructor
    public Downloader(UIUpdater ui) {
        this.ui = ui;
    }

    private InputStream in;

    public void download(FileMetadata file, Path outputPath) {
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
                }

            }
        } catch (IOException e) {
            ui.updateStatus("❌ Error al descargar: " + outputPath.getFileName() + " (" + e.getMessage() + ")");
        } finally {
            closeStream();
        }
    }

    private void closeStream() {
        try {
            if (in != null) in.close();
        } catch (IOException ignored) {}
    }
}
