package driwai.updater.core;

import com.formdev.flatlaf.FlatLightLaf;
//configuracion global
public final class AppConfig {
    public static final String APP_NAME = "BiblioSystem-Geológica"; //Nombre de la aplicacion
    public static final String COPYRIGHT = "© 2025 DRIWAI TECHNOLOGIES";
    public static final String APP_NAME_JAR = "BiblioSystem-Geologica.jar";
    public static final String CONFIG_URL = "https://updatesystem.figmmg.shop/update/geologica/config.xml";
    public static final String INFORMATION_UPDATE = "https://updatesystem.figmmg.shop/update/geologica/information.json";

    // ✅ Variable global para cancelar descargas
    public static volatile boolean cancelExecution = false;
    public static void setupLaf() {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception e) {
            System.err.println("No se pudo inicializar FlatLaf: " + e.getMessage());
        }
    }


}
