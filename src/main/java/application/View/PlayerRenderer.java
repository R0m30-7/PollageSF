package application.View;

import application.Model.Hitbox;
import application.Model.Player;
import application.Utils.GameConfig; 
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

public class PlayerRenderer {
    // Contenitore libero (Pane) che terrà il corpo, il pugno e la difesa
    private Pane rootNode;
    
    // --- Variabili per lo Sprite Animato ---
    private ImageView spriteView;
    private Image atlas;
    private final int COLUMNS = 3;
    private final int ROWS = 4;
    private double frameWidth;
    private double frameHeight;
    
    // Gestione dell'animazione
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private final long FRAME_DELAY = 150_000_000; // 150 millisecondi per frame
    private double lastPx = 0; // Usata per capire se il personaggio si sta muovendo!
    
    // Forme per le azioni (per ora rimangono i rettangoli)
    private Rectangle punchVisual;
    private Rectangle defenseVisual;
    
    // Per visualizzare l'hitbox del giocatore
    private Rectangle hitboxVisual;

    public PlayerRenderer(Player player) {
        rootNode = new Pane(); 
        
        // --- Caricamento dello sprite ---
        try {
            // Legge tutto dalla classe specifica del player (ad esempio Turnip.java)
            atlas = new Image(getClass().getResourceAsStream(player.getAtlasPath()));
            spriteView = new ImageView(atlas);
            
            frameWidth = player.getFrameWidth();
            frameHeight = player.getFrameHeight();
            
            int scale = player.getRenderScale();

            // Prendi l'immagine, moltiplicala per lo scale impostato senza alterare i pixel, poi disegnala
            spriteView.getTransforms().add(new javafx.scene.transform.Scale(scale, scale, 0, 0));
            spriteView.setSmooth(false);
            
        } catch (Exception e) {
            System.out.println("Errore caricamento atlas: " + player.getAtlasPath());
        }

        // --- 2. IL PUGNO (Giallo) ---
        punchVisual = new Rectangle(GameConfig.pPunchWidth, GameConfig.pPunchHeight, Color.YELLOW);
        punchVisual.setStroke(Color.ORANGE);
        punchVisual.setVisible(false);
        
        // Visualizzazione hitbox
        hitboxVisual = new Rectangle();
        hitboxVisual.setFill(null);       // Niente riempimento!
        hitboxVisual.setStroke(Color.RED); // Bordo rosso
        hitboxVisual.setStrokeWidth(2);    // Spessore del bordo
        // Per ora la lasciamo 0x0, la aggiorneremo nel render

        // --- 3. LA DIFESA (Azzurro semitrasparente) ---
        defenseVisual = new Rectangle(GameConfig.pDefenseWidth, GameConfig.pDefenseHeight, Color.LIGHTBLUE);
        defenseVisual.setOpacity(0.6);
        defenseVisual.setStroke(Color.BLUE);
        defenseVisual.setVisible(false);

        // Aggiungiamo tutto al rootNode (lo sprite prende il posto del bodyContainer)
        if (spriteView != null) rootNode.getChildren().add(spriteView);
        rootNode.getChildren().add(hitboxVisual);	// Visualizzazione hitbox
        rootNode.getChildren().addAll(punchVisual, defenseVisual);
    }

    public Pane getNode() {
        return rootNode;
    }

    public void render(Player player) {
        double px = player.getPosition().getX();
        double py = player.getPosition().getY();
        
        // ==========================================
        // ANIMAZIONE DELLO SPRITE
        // ==========================================
        if (spriteView != null) {
            long now = System.nanoTime();
            // Controlliamo se si è spostato fisicamente rispetto al frame precedente
            boolean isMoving = Math.abs(px - lastPx) > 0.5; 

            // Se è passato abbastanza tempo, facciamo scattare il frame successivo
            if (now - lastFrameTime > FRAME_DELAY) {
                if (isMoving) {
                    currentFrame = (currentFrame + 1) % COLUMNS; // Passa da 0, a 1, a 2 e ricomincia
                } else {
                    currentFrame = 0; // Se è fermo, forza il frame 0
                }
                lastFrameTime = now;
            }

            int rowIndex = 0;
            int colIndex = 0;

            if (isMoving) {
                // È in movimento! Scegliamo la riga in base a dove sta guardando
                if (player.isFacingRight()) {
                    rowIndex = 1; // Riga 3 dell'atlas = Cammina a Destra
                } else {
                    rowIndex = 2; // Riga 2 dell'atlas = Cammina a Sinistra
                }
                colIndex = currentFrame;
            } else {
                // È fermo! Mostra la faccia frontale.
                rowIndex = 0; // Riga 1 dell'atlas
                colIndex = 0; // Prima colonna
            }

            // Spostiamo il Viewport (il mirino) sul frame che abbiamo appena calcolato
            double cropX = colIndex * frameWidth;
            double cropY = rowIndex * frameHeight;
            spriteView.setViewport(new Rectangle2D(cropX, cropY, frameWidth, frameHeight));

            // Aggiorniamo la posizione dello sprite sullo schermo
            spriteView.setLayoutX(Math.round(px));
            spriteView.setLayoutY(Math.round(py));
        }
        
        // Aggiorniamo l'ultima posizione conosciuta per il calcolo del prossimo frame!
        lastPx = px;
        
        // ==========================================
        // Mostra hitbox
        // ==========================================
        // Recuperiamo la Hitbox fisica reale dal Model
        Hitbox physBox = player.getBoundingBox();
        
        // Aggiorniamo il rettangolo rosso per farlo combaciare millimetricamente
        hitboxVisual.setX(Math.round(physBox.getX()));
        hitboxVisual.setY(Math.round(physBox.getY()));
        hitboxVisual.setWidth(physBox.getWidth());
        hitboxVisual.setHeight(physBox.getHeight());
        
        // Lo portiamo in primo piano per vederlo sopra lo sprite
        hitboxVisual.toFront();

        // ==========================================
        // GESTIONE PUGNO (Invariata)
        // ==========================================
        if (player.isPunching()) {
            punchVisual.setVisible(true);
            punchVisual.toFront();
            
            double punchY = py + (player.getHeight() * 0.2); 
            double punchX;
            
            if (player.isFacingRight()) {
                punchX = px + player.getWidth();
            } else {
                punchX = px - GameConfig.pPunchWidth;
            }
            
            punchVisual.setX(punchX);
            punchVisual.setY(punchY);
        } else {
            punchVisual.setVisible(false);
        }
    
        // ==========================================
        // GESTIONE DIFESA (Invariata)
        // ==========================================
        if (player.isDefending()) {
            defenseVisual.setVisible(true);
            double defenseY = py + (player.getHeight() - GameConfig.pDefenseHeight) / 2.0; 
            
            if (player.isFacingRight()) {
                defenseVisual.setX(px + (player.getWidth() * 0.8)); 
            } else {
                defenseVisual.setX(px - GameConfig.pDefenseWidth + (player.getWidth() * 0.2)); 
            }
            defenseVisual.setY(defenseY);
        } else {
            defenseVisual.setVisible(false);
        }
    }
}