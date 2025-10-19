package driwai.updater;

import driwai.updater.core.*;
import driwai.updater.ui.*;
import org.update4j.Configuration;
import org.update4j.FileMetadata;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UpdaterApplication {

	public static void main(String[] args) {


		try {
			AppConfig.setupLaf();
			new LoadingScreen("Verificando actualizaciones...").show(2500);

			URL configUrl = new URL(AppConfig.CONFIG_URL);
			Configuration config;
			try (Reader reader = new InputStreamReader(configUrl.openStream())) {
				config = Configuration.read(reader);
				System.out.println("🧾 Config leído desde: " + configUrl);
			}
			Path appDir = Paths.get("app");
			boolean needsUpdate = false;
			for (FileMetadata file : config.getFiles()) {
				Path localPath = appDir.resolve(file.getPath().getFileName());
				long localSize = Files.exists(localPath) ? Files.size(localPath) : 0;

				System.out.println("🧾 Archivo: " + file.getPath());
				System.out.println("   Tamaño remoto: " + file.getSize() + " bytes");
				System.out.println("   Tamaño local: " + localSize + " bytes");

				if (!Files.exists(localPath) || localSize != file.getSize()) {
					System.out.println("🧾compara q cosa" +needsUpdate);
					needsUpdate = true;
					System.out.println("🧾compara q cos222a" +needsUpdate);
				}
			}


			UpdateManager manager = new UpdateManager(config);
			// Preguntar si desea actualizar
			if (needsUpdate) {
				UpdateAction action = manager.askForUpdate();

				if (action == UpdateAction.UPDATE) {
					UpdateWindow window = new UpdateWindow(AppConfig.APP_NAME, AppConfig.COPYRIGHT);
					manager.downloadUpdates(window);
					window.close();

					if (AppConfig.cancelExecution) {
						System.exit(0);
					}
				}
				else if (action == UpdateAction.SKIP) {
					System.out.println("⏭ Se omitió la actualización. Iniciando programa...");
				}
				else {
					System.out.println("❌ Acción cancelada. Cerrando.");
					System.exit(0);
				}
			}


			// 🚀 Solo se ejecuta si NO canceló
			new ProcessLauncher(AppConfig.APP_NAME_JAR).launchApp();

		} catch (Exception e) {
			e.printStackTrace();
			javax.swing.JOptionPane.showMessageDialog(null, "❌ Error general: " + e.getMessage());
		}
	}
}
