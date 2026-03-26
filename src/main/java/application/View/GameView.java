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
    
    // Dimensioni dell'immagine di sfondo
    private double bgWidth;
    private double bgHeight;
    private double originalBgWidth;
    private double originalBgHeight;
    
    public GameView() {
        this.root = new Pane();
        
        // --- CARICAMENTO DELL'IMMAGINE DI SFONDO ---
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/Backgrounds/culoLand.jpeg"));
            this.backgroundView = new ImageView(bgImage);
            
            this.originalBgWidth = bgImage.getWidth();
            this.originalBgHeight = bgImage.getHeight();
        } catch (Exception e) {
            System.out.println("Sfondo non trovato, uso valori di emergenza.");
            this.backgroundView = new ImageView(); 
            this.bgWidth = 1600; 
            this.bgHeight = 720;
        }
        
        // Dice all'ImageView di non deformare le proporzioni dell'immagine
        this.backgroundView.setPreserveRatio(true);
        
        // --- CONTENITORE E TAGLIO ---
        // Mettiamo l'immagine dentro un contenitore separato
        this.backgroundContainer = new Pane(this.backgroundView);
        
        // Creiamo la maschera iniziale (es. 720 di altezza di default)
        this.clip = new Rectangle(this.originalBgWidth, 720);
        this.backgroundContainer.setClip(this.clip); // Applichiamo il taglio!
        
        // Allineamento iniziale in basso a sinistra
        double offsetY = 720 - this.originalBgHeight;
        this.backgroundView.setY(offsetY);
        
        // --- 3. GIOCATORI ---
        this.rendererP1 = new PlayerRenderer("1");
        this.rendererP2 = new PlayerRenderer("2");
        
        // Aggiungiamo il contenitore (non l'immagine diretta!) e i lottatori
        this.root.getChildren().addAll(this.backgroundContainer, rendererP1.getNode(), rendererP2.getNode());
        
        // Inizializzazione dei valori fittizi per l'avvio
        this.bgWidth = this.originalBgWidth;
        this.bgHeight = this.originalBgHeight;
    }

    public Pane getRoot() { return root; }
    public double getBgWidth() { return bgWidth; }
    public double getBgHeight() { return bgHeight; }

    public void render(GameModel model) {
        double camX = model.getCameraX();
    	
    	// Spostiamo l'intero SFONDO all'indietro rispetto alla telecamera
    	this.backgroundContainer.setLayoutX(-camX);
    	
    	// --- LA MAGIA CHE MANCAVA ---
    	// Chiamiamo finalmente i renderer per fargli ricalcolare vita, pugni e difese!
    	rendererP1.render(model.getPlayer1());
    	rendererP2.render(model.getPlayer2());
    	
    	// --- SPOSTAMENTO TELECAMERA SUI GIOCATORI ---
    	// Visto che PlayerRenderer posiziona già il corpo e i pugni alle coordinate 
        // assolute (px, py), qui dobbiamo solo far scorrere il loro "livello" all'indietro 
        // in base a dove si trova la telecamera.
    	rendererP1.getNode().setLayoutX(-camX);
    	rendererP1.getNode().setLayoutY(0); // La Y è gestita dal PlayerRenderer
    	
    	rendererP2.getNode().setLayoutX(-camX);
    	rendererP2.getNode().setLayoutY(0);
    }

    // ==========================================
    // METODO CHIAMATO DAL CONTROLLER QUANDO VIENE RIDIMENSIONATA LA FINESTRA
    // ==========================================
    public void updateWindowSize(double newWidth, double newHeight) {
    	// Calcoliamo di quanto dovremmo zoomMare l'immagine per coprire la finestra
        double scaleX = newWidth / this.originalBgWidth;
        double scaleY = newHeight / this.originalBgHeight;
        
        // Prendiamo la scala MAGGIORE. Così siamo sicuri al 100% che non ci siano spazi bianchi
        double scale = Math.max(scaleX, scaleY);
        
        // Non rimpicciolire mai l'immagine sotto la sua misura originale
        scale = Math.max(1.0, scale);

        // Calcoliamo le nuove dimensioni "Zoommate" dell'arena
        this.bgWidth = this.originalBgWidth * scale;
        this.bgHeight = this.originalBgHeight * scale;

        // Applichiamo le nuove dimensioni all'immagine grafica
        if (this.backgroundView != null) {
            this.backgroundView.setFitWidth(this.bgWidth);
            this.backgroundView.setFitHeight(this.bgHeight);
            
            // L'ancoraggio in basso a sinistra
            double offsetY = newHeight - this.bgHeight;
            this.backgroundView.setY(offsetY);
        }

        // Aggiorniamo la maschera che taglia lo schermo
        if (this.clip != null) {
            this.clip.setWidth(this.bgWidth);
            this.clip.setHeight(newHeight);
        }
        
        // Aggiorniamo il contenitore
        if (this.backgroundContainer != null) {
            this.backgroundContainer.setPrefSize(this.bgWidth, newHeight);
        }
    }
}