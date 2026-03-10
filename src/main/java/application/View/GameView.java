package application.View;

import application.Model.GameModel;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class GameView {
    
    // Dichiara qui le variabili grafiche
    private Pane root;
    private PlayerRenderer rendererP1;
    private PlayerRenderer rendererP2;

    public GameView() {
        // Inizializza il contenitore principale
        this.root = new Pane();
        
        // Inizializza i renderer dei giocatori
        this.rendererP1 = new PlayerRenderer(Color.BLUE);
        this.rendererP2 = new PlayerRenderer(Color.RED);
        
        // Aggiungi i nodi grafici al root
        this.root.getChildren().addAll(rendererP1.getNode(), rendererP2.getNode());
    }

    public Pane getRoot() {
        return root;
    }

    public void render(GameModel model) {
        // Ora GameView conosce i renderer e può aggiornarli senza errori
        rendererP1.render(model.getPlayer1());
        rendererP2.render(model.getPlayer2());
    }
}