package application.Scenes;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class PlayScene {
    
    // Costruisce e restituisce la Scene partendo dal root della GameView
    public Scene getScene(Pane gameRoot) {
        // Qui potresti aggiungere sfondi, stili CSS specifici per la scena, ecc.
        
        return new Scene(gameRoot, application.Utils.Settings.getInstance().getWindowWidth(), application.Utils.Settings.getInstance().getWindowHeight());
    }
}