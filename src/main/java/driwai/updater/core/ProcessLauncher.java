package driwai.updater.core;

import driwai.updater.ui.StartupScreen;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;


public class ProcessLauncher {

    private final String jarName;

    public ProcessLauncher(String jarName) {
        this.jarName = jarName;
    }

    public void launchApp() throws IOException {
        System.out.println("🚀 Iniciando aplicación...");

        // 🔹 Mostrar la pantalla de inicio
        StartupScreen startup = new StartupScreen();
        startup.showScreen();

        // 🔹 Ejecutar el JAR principal
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "app/" + jarName);
        Process process = pb.start();

        // 🔹 Escuchar la salida del proceso
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);

                    // 🔹 Cuando la app principal esté lista, cerrar la pantalla
                    if (line.contains("APP_READY")) {
                        System.out.println("✅ Aplicación iniciada correctamente.");
                        startup.closeScreen();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
