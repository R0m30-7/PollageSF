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
    
    // HUD
    private HUDView hud;
    
    public GameView() {
        this.root = new Pane();
        
        // Creiamo l'ImageView vuoto, l'immagine verrà caricata dal menu in seguito
        this.backgroundView = new ImageView();
        this.backgroundView.setPreserveRatio(true);
        this.backgroundContainer = new Pane(this.backgroundView);
        
        // Valori di emergenza/default finché non scegliamo la mappa
        this.originalBgWidth = 1920;
        this.originalBgHeight = 1080;
        this.bgWidth = 1920;
        this.bgHeight = 1080;
        
        this.clip = new Rectangle(this.originalBgWidth, 720);
        this.backgroundContainer.setClip(this.clip);
        
        // Offset temporaneo
        this.backgroundView.setY(0);
        
        // --- GIOCATORI ---
        this.rendererP1 = new PlayerRenderer("1");
        this.rendererP2 = new PlayerRenderer("2");
        
        // --- HUD ---
        this.hud = new HUDView();
        
        this.root.getChildren().addAll(this.backgroundContainer, rendererP1.getNode(), rendererP2.getNode());
        this.root.getChildren().add(this.hud.getNode());	// Aggiunta dell'HUD per ultimo
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
    	
    	// Aggiorniamo le barre della vita dei giocatori
    	this.hud.update(model.getPlayer1(), model.getPlayer2());
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
        
        // Aggiorniamo anche l'HUD
        if(this.hud != null) {
        	this.hud.updateLayout(newWidth);
        }
    }
    
    // --- METODO PER CAMBIARE SFONDO DINAMICAMENTE ---
    public void changeBackground(String imagePath, double windowWidth, double windowHeight) {
        try {
            Image bgImage = new Image(getClass().getResourceAsStream(imagePath));
            if (bgImage.isError()) {
                System.out.println("Immagine non trovata: " + imagePath);
                return;
            }
            this.backgroundView.setImage(bgImage);
            this.originalBgWidth = bgImage.getWidth();
            this.originalBgHeight = bgImage.getHeight();
            
            // Ricalcoliamo lo zoom e la maschera con le nuove dimensioni!
            updateWindowSize(windowWidth, windowHeight);
        } catch (Exception e) {
            System.out.println("Errore nel cambio sfondo: " + e.getMessage());
        }
    }
}