package driwai.updater.core;

import org.json.JSONArray;
import org.json.JSONObject;
import org.update4j.Configuration;

import java.io.BufferedReader;
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

    public String  loadJson(URL url) throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder jsonContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonContent.append(line);
        }
        reader.close();

        // Parsear JSON
        JSONObject obj = new JSONObject(jsonContent.toString());
        String version = obj.getString("version");
        String releaseDate = obj.getString("releaseDate");
        String releaseName = obj.getString("releaseName");
        String author = obj.getString("author");
        JSONArray changes = obj.getJSONArray("changes");

        // Construir texto con formato
        StringBuilder info = new StringBuilder();
        info.append("Nombre de la versión: ").append(releaseName).append("\n");
        info.append("Versión: ").append(version).append("\n");
        info.append("Fecha de lanzamiento: ").append(releaseDate).append("\n");
        info.append("Autor: ").append(author).append("\n\n");
        info.append("Cambios:\n");
        for (int i = 0; i < changes.length(); i++) {
            info.append(" - ").append(changes.getString(i)).append("\n");
        }
        return info.toString();
    }

}
