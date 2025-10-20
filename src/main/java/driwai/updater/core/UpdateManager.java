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


    public UpdateAction askForUpdate() {
        Object[] options = {"Actualizar", "Omitir", "Más info"};

        InformationWindow informationWindow = new InformationWindow();
        Object selectedValue = informationWindow.InformationUpdate(options);
        if (selectedValue == null) return UpdateAction.CANCEL;

        if ("Más info".equals(selectedValue)) {
            try {
                URL url = new URL(AppConfig.INFORMATION_UPDATE);
                ConfigService configService = new ConfigService();
                String info = configService.loadJson(url);
                informationWindow.informationUpdate(info, null);
            } catch (Exception e) {
                informationWindow.informationUpdate("No se pudo obtener la información", e);
            }
            return askForUpdate(); // volver a preguntar
        }

        if ("Actualizar".equals(selectedValue)) {
            return UpdateAction.UPDATE;
        }

        if ("Omitir".equals(selectedValue)) {
            return UpdateAction.SKIP;
        }

        return UpdateAction.CANCEL;
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
        Path tempPath = outputPath.resolveSibling(outputPath.getFileName() + ".tmp");
        try {
            Files.createDirectories(outputPath.getParent());
            in = file.getUri().toURL().openStream();

            // Descargar a archivo temporal primero
            try (FileOutputStream out = new FileOutputStream(tempPath.toFile())) {
                long startTime = System.currentTimeMillis();
                byte[] buffer = new byte[8192];
                long totalRead = 0;
                long totalSize = file.getSize();
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    if (AppConfig.cancelExecution) {
                        ui.updateStatus("⚠️ Descarga cancelada.");
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
//                    ui.updateProgress(percent, "Descargando " + outputPath.getFileName());
                }
            }

            // ✅ Validación del tamaño
            long downloadedSize = Files.size(tempPath);
            if (downloadedSize == file.getSize()) {
                Files.move(tempPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
                ui.updateStatus("✅ Actualizado: " + outputPath.getFileName());
            } else {
                Files.deleteIfExists(tempPath);
                ui.updateStatus("❌ Tamaño incorrecto, descarga fallida para " + outputPath.getFileName());
            }

        } catch (IOException e) {
            ui.updateStatus("❌ Error al descargar: " + e.getMessage());
            try { Files.deleteIfExists(tempPath); } catch (IOException ignored) {}
        } finally {
            try { if (in != null) in.close(); } catch (IOException ignored) {}
        }
    }

}
