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

//			ConfigService configService = new ConfigService();
//			var config = configService.loadRemoteConfig(AppConfig.CONFIG_URL);
//			System.out.print("ESTO ES LA LECTURA"+ config);
//
//			if(){}
//



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
					needsUpdate = true;
				}
			}


			UpdateManager manager = new UpdateManager(config);
			// Preguntar si desea actualizar
			if(needsUpdate){
				if (manager.askForUpdate()) {
					UpdateWindow window = new UpdateWindow(AppConfig.APP_NAME, AppConfig.COPYRIGHT);
					manager.downloadUpdates(window);
					window.close();

					// 🚫 Si canceló, salir sin ejecutar el programa
					if (AppConfig.cancelExecution) {
						System.out.println("❌ Actualización cancelada por el usuario. Cerrando aplicación...");
						System.exit(0);
					}

				} else {
					System.out.println("⏹️ Actualización cancelada por el usuario.");
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
