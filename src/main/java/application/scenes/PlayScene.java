package application.scenes;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class PlayScene {
    
    // Costruisce e restituisce la Scene partendo dal root della GameView
    public Scene getScene(Pane gameRoot) {
        // Qui si possono aggiungere sfondi, stili CSS specifici per la scena, ecc.
        
        return new Scene(gameRoot, application.utils.Settings.getInstance().getWindowWidth(), application.utils.Settings.getInstance().getWindowHeight());
    }
}