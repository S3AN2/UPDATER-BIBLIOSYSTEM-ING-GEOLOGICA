package driwai.updater;

import driwai.updater.core.*;
import driwai.updater.ui.*;

public class UpdaterApplication {

	public static void main(String[] args) {
		try {
			AppConfig.setupLaf();
			new LoadingScreen("Verificando actualizaciones...").show(1500);

			ConfigService configService = new ConfigService();
			var config = configService.loadRemoteConfig(AppConfig.CONFIG_URL);

			UpdateManager manager = new UpdateManager(config);

			// Preguntar si desea actualizar
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

			// 🚀 Solo se ejecuta si NO canceló
			new ProcessLauncher(AppConfig.APP_NAME_JAR).launchApp();

		} catch (Exception e) {
			e.printStackTrace();
			javax.swing.JOptionPane.showMessageDialog(null, "❌ Error general: " + e.getMessage());
		}
	}
}
