package application.View;

import application.Model.GameModel;
import application.Utils.GameConfig;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class GameView {
    
    // Dichiarazione delle variabili grafiche
    private Pane root;
    private PlayerRenderer rendererP1;
    private PlayerRenderer rendererP2;
    
    // Immagine di sfondo
    private ImageView backgroundView;
    private double bgWidth;
    private double bgHeight;
    
    public GameView() {
        // Inizializza il contenitore principale
        this.root = new Pane();
        
        // --- PREPARAZIONE DELLO SFONDO ---
        // L'immagine dovrebbe essere idealmente larga quanto WORLD_WIDTH (es. 1600px o più)
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/Backgrounds/twinTowers.png"));
            backgroundView = new ImageView(bgImage);
            
            // 1. Salviamo le dimensioni reali dell'immagine per passarle al Model
            this.bgWidth = bgImage.getWidth();
            this.bgHeight = bgImage.getHeight();
            
            // 2. ALLINEAMENTO IN BASSO A SINISTRA
            // Spingiamo l'immagine in alto di (AltezzaFinestra - AltezzaImmagine)
            // Se l'immagine è più alta della finestra, offsetY sarà negativo e la taglierà in alto!
            double offsetY = GameConfig.WINDOW_HEIGHT - this.bgHeight;
            backgroundView.setY(offsetY);
            
        } catch (Exception e) {
            System.out.println("Sfondo non trovato, imposto un colore neutro o lascio vuoto.");
            backgroundView = new ImageView(); // Fallback in caso di errore
            
            // Valori di emergenza
            this.bgWidth = 1600; 
            this.bgHeight = GameConfig.WINDOW_HEIGHT;
        }
        
        // Inizializza i renderer dei giocatori
        this.rendererP1 = new PlayerRenderer("1");
        this.rendererP2 = new PlayerRenderer("2");
        
        // Aggiungi i nodi grafici al root
        this.root.getChildren().addAll(backgroundView, rendererP1.getNode(), rendererP2.getNode());
    }

    public Pane getRoot() {
        return root;
    }
    
    // Getter per far leggere le dimensioni al GameController/GameModel
    public double getBgWidth() { return bgWidth; }
    public double getBgHeight() { return bgHeight; }

    public void render(GameModel model) {
        // Prendiamo la posizione della telecamera
    	double camX = model.getCameraX();
    	
    	// Spostiamo lo sfondo all'indietro rispetto alla telecamera
    	backgroundView.setLayoutX(-camX);
    	
    	// Spostamento dei giocatori sottraendo la telecamera
    	double p1ScreenX = model.getPlayer1().getPosition().getX() - camX;
    	double p1ScreenY = model.getPlayer1().getPosition().getY();		// La y non ha telecamera
    	rendererP1.getNode().setLayoutX(p1ScreenX);
    	rendererP1.getNode().setLayoutY(p1ScreenY);
    	
    	double p2ScreenX = model.getPlayer2().getPosition().getX() - camX;
    	double p2ScreenY = model.getPlayer2().getPosition().getY();		// La y non ha telecamera
    	rendererP2.getNode().setLayoutX(p2ScreenX);
    	rendererP2.getNode().setLayoutY(p2ScreenY);
    }
}