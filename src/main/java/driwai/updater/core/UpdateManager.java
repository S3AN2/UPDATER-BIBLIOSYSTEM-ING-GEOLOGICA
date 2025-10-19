package driwai.updater.core;

import driwai.updater.ui.InformationWindow;
import org.update4j.Configuration;
import org.update4j.FileMetadata;
import driwai.updater.ui.UIUpdater;


import java.io.*;
import java.net.URL;
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
        Object[] options = {"Sí", "No", "Más info"};
        InformationWindow informationWindow = new InformationWindow();
        Object selectedValue= informationWindow.InformationUpdate(options);
        if (selectedValue == null) return false;

        if (selectedValue.equals("Más info")) {
            try {
                // Leer JSON desde la URL
                URL url = new URL(AppConfig.INFORMATION_UPDATE);
                ConfigService configService = new ConfigService();
                String info= configService.loadJson(url);
               informationWindow.informationUpdate(info,null);

            } catch (Exception e) {
                informationWindow.informationUpdate("No se pudo obtener la información: ",e);

            }

            // Volver a mostrar la ventana principal de actualización
            return askForUpdate();
        }

        return selectedValue.equals("Sí");
    }

    /**
     * Descarga todos los archivos necesarios con progreso y cancelación.
     */
    public void downloadUpdates(UIUpdater ui) {
        try {
            Path appDir = Paths.get("app");
            if (!Files.exists(appDir)) Files.createDirectories(appDir);
            try {
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    Files.setAttribute(appDir, "dos:hidden", true);
                    System.out.println("✅ Carpeta 'app' oculta correctamente en Windows.");
                }
            } catch (IOException e) {
                System.out.println("❌ No se pudo ocultar la carpeta: " + e.getMessage());
            }
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
