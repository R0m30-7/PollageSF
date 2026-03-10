package application.Scenes;

import application.Utils.GameConfig;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class PlayScene {
    
    // Costruisce e restituisce la Scene partendo dal root della GameView
    public Scene getScene(Pane gameRoot) {
        // Qui potresti aggiungere sfondi, stili CSS specifici per la scena, ecc.
        
        return new Scene(gameRoot, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
    }
}