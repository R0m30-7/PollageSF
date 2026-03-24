package application.View;

import application.Model.GameModel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class GameView {
    
    private Pane root;
    private PlayerRenderer rendererP1;
    private PlayerRenderer rendererP2;
    
    // Variabili per lo sfondo
    private ImageView backgroundView;
    private Pane backgroundContainer; // Contenitore che ci permette di tagliare l'immagine
    private Rectangle clip;           // La maschera "invisibile" che taglia l'eccesso
    
    // Dimensioni reali dell'immagine
    private double bgWidth;
    private double bgHeight;
    
    public GameView() {
        this.root = new Pane();
        
        // --- 1. CARICAMENTO IMMAGINE ---
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/Backgrounds/twinTowers.png"));
            this.backgroundView = new ImageView(bgImage);
            
            this.bgWidth = bgImage.getWidth();
            this.bgHeight = bgImage.getHeight();
        } catch (Exception e) {
            System.out.println("Sfondo non trovato, uso valori di emergenza.");
            this.backgroundView = new ImageView(); 
            this.bgWidth = 1600; 
            this.bgHeight = 720;
        }
        
        // --- 2. CONTENITORE E TAGLIO ---
        // Mettiamo l'immagine dentro un contenitore separato
        this.backgroundContainer = new Pane(this.backgroundView);
        
        // Creiamo la maschera iniziale (es. 720 di altezza di default)
        this.clip = new Rectangle(this.bgWidth, 720);
        this.backgroundContainer.setClip(this.clip); // Applichiamo il taglio!
        
        // Allineamento iniziale in basso a sinistra
        double offsetY = 720 - this.bgHeight;
        this.backgroundView.setY(offsetY);
        
        // --- 3. GIOCATORI ---
        this.rendererP1 = new PlayerRenderer("1");
        this.rendererP2 = new PlayerRenderer("2");
        
        // Aggiungiamo il contenitore (non l'immagine diretta!) e i lottatori
        this.root.getChildren().addAll(this.backgroundContainer, rendererP1.getNode(), rendererP2.getNode());
    }

    public Pane getRoot() { return root; }
    public double getBgWidth() { return bgWidth; }
    public double getBgHeight() { return bgHeight; }

    public void render(GameModel model) {
        double camX = model.getCameraX();
    	
    	// Spostiamo l'intero CONTENITORE all'indietro rispetto alla telecamera
    	this.backgroundContainer.setLayoutX(-camX);
    	
    	// Spostamento dei giocatori
    	double p1ScreenX = model.getPlayer1().getPosition().getX() - camX;
    	double p1ScreenY = model.getPlayer1().getPosition().getY();
    	rendererP1.getNode().setLayoutX(p1ScreenX);
    	rendererP1.getNode().setLayoutY(p1ScreenY);
    	
    	double p2ScreenX = model.getPlayer2().getPosition().getX() - camX;
    	double p2ScreenY = model.getPlayer2().getPosition().getY();
    	rendererP2.getNode().setLayoutX(p2ScreenX);
    	rendererP2.getNode().setLayoutY(p2ScreenY);
    }

    // ==========================================
    // NUOVO: METODO CHIAMATO DAL CONTROLLER QUANDO RIDIMENSIONI LA FINESTRA
    // ==========================================
    public void updateWindowSize(double newWidth, double newHeight) {
        
        // 1. Aggiorniamo la maschera: taglierà l'immagine alla nuova altezza della finestra!
        if (this.clip != null) {
            this.clip.setHeight(newHeight);
        }
        
        // 2. Aggiorniamo le dimensioni del contenitore
        if (this.backgroundContainer != null) {
            this.backgroundContainer.setPrefSize(this.bgWidth, newHeight);
        }
        
        // 3. Il cuore della logica: ricalcoliamo la coordinata Y per tenere l'immagine ancorata in basso!
        if (this.backgroundView != null) {
            double offsetY = newHeight - this.bgHeight;
            this.backgroundView.setY(offsetY);
        }
    }
}