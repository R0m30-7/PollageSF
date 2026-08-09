package application.Model;

import javafx.geometry.Rectangle2D;
import org.json.JSONObject; //libreria per leggere il JSON
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TextureAtlas {
    private Map<String, Rectangle2D> frames = new HashMap<>();

    public TextureAtlas(String jsonFilePath) {
        try {
            // Lettura del contenuto del file JSON
            String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            // Parsing del JSON
            JSONObject root = new JSONObject(content);
            // Estrazione dei dati dei frame
            JSONObject framesJson = root.getJSONObject("frames");
            // Iterazione sui frame e memorizzazione delle coordinate
            for (String key : framesJson.keySet()) {
                JSONObject frameData = framesJson.getJSONObject(key).getJSONObject("frame");
                double x = frameData.getDouble("x");
                double y = frameData.getDouble("y");
                double w = frameData.getDouble("w");
                double h = frameData.getDouble("h");
                
                frames.put(key, new Rectangle2D(x, y, w, h));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Ricostruisce la chiave del JSON: es. "row00_col00_frame00"
    public Rectangle2D getFrame(int row, int col, int frameIndex) {
        String frameKey = String.format("row%02d_col%02d_frame%02d", row, col, frameIndex);
        return frames.getOrDefault(frameKey, new Rectangle2D(0, 0, 1, 1));
    }
}