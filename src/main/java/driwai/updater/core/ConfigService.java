package driwai.updater.core;

import org.update4j.Configuration;
import java.io.InputStreamReader;
import java.net.URL;
//lectura de configuracion remota y lectura
public class ConfigService {
    public Configuration loadRemoteConfig(String url) throws Exception {
        try (var reader = new InputStreamReader(new URL(url).openStream())) {
            System.out.println("🧾 Leyendo configuración desde " + url);
            return Configuration.read(reader);
        }
    }
}
