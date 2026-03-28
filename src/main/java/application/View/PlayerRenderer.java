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
    private double frameWidth;
    private double frameHeight;
    
    // Variabile per la State Machine
    private application.Model.AnimState lastAnimState = null;
    
    // Gestione dell'animazione
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    
    // Forme per le azioni (per ora rimangono i rettangoli)
    private Rectangle punchVisual;
    private Rectangle defenseVisual;
    
    // Per visualizzare l'hitbox del giocatore
    private Rectangle hitboxVisual;

    public PlayerRenderer(Player player) {
        rootNode = new Pane(); 
        
        // --- Caricamento dello sprite ---
        try {
            int scale = player.getRenderScale();
            
            // Calcoliamo quanto sarà grande l'INTERO atlas una volta ingrandito
            double targetWidth = player.getSpriteCols() * player.getFrameWidth() * scale;
            double targetHeight = player.getSpriteRows() * player.getFrameHeight() * scale;
            
            // Carichiamo l'immagine dicendo a Java di ingrandirla subito.
            // I parametri sono: (percorso, larghezza, altezza, mantieniProporzioni, SMOOTH)
            // Mettendo l'ultimo parametro a FALSE, JavaFX usa il Nearest-Neighbor perfetto in memoria
            atlas = new Image(getClass().getResourceAsStream(player.getAtlasPath()), targetWidth, targetHeight, true, false);
            spriteView = new ImageView(atlas);
            
            // Ora i "quadratini" di ritaglio sono direttamente quelli ingranditi
            frameWidth = player.getFrameWidth() * scale;
            frameHeight = player.getFrameHeight() * scale;
            
            // Impostiamo il viewport
            spriteView.setViewport(new Rectangle2D(0, 0, frameWidth, frameHeight));
            
        } catch (Exception e) {
            System.out.println("Errore caricamento atlas: " + player.getAtlasPath());
        }

        // --- IL PUGNO (Giallo) ---
        punchVisual = new Rectangle(GameConfig.pPunchWidth, GameConfig.pPunchHeight, Color.YELLOW);
        punchVisual.setStroke(Color.ORANGE);
        punchVisual.setVisible(false);
        
        // Visualizzazione hitbox
        hitboxVisual = new Rectangle();
        hitboxVisual.setFill(null);       // Niente riempimento!
        hitboxVisual.setStroke(Color.RED); // Bordo rosso
        hitboxVisual.setStrokeWidth(2);    // Spessore del bordo
        // Per ora la lasciamo 0x0, la aggiorneremo nel render

        // --- LA DIFESA (Azzurro semitrasparente) ---
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
            // 1. Chiediamo al giocatore quale "cartuccia" usare in questo momento
            application.Model.AnimState currentState = player.getCurrentAnimState();
            application.Model.AnimData currentData = player.getCurrentAnimData();

            // Sicurezza: se per qualche motivo mancano i dati, non facciamo nulla
            if (currentData != null) {
                
                // 2. Se ha cambiato animazione (es. da Camminata a Salto), azzeriamo il frame!
                if (currentState != lastAnimState) {
                    currentFrame = 0;
                    lastFrameTime = System.nanoTime();
                    lastAnimState = currentState;
                }

                // 3. Calcolo del tempo per scorrere i frame
                long now = System.nanoTime();
                if (now - lastFrameTime > currentData.speedNs) {
                    if (currentData.loop) {
                        // Ciclo continuo (es. camminata): 0, 1, 2, 0, 1, 2...
                        currentFrame = (currentFrame + 1) % currentData.frameCount;
                    } else {
                        // Animazione singola (es. pugno): si ferma all'ultimo frame
                        currentFrame = Math.min(currentFrame + 1, currentData.frameCount - 1);
                    }
                    lastFrameTime = now;
                }

                // 4. Spostiamo il mirino (Viewport) leggendo le coordinate esatte dalla cartuccia!
                double cropX = currentFrame * frameWidth;
                double cropY = currentData.row * frameHeight;
                spriteView.setViewport(new Rectangle2D(cropX, cropY, frameWidth, frameHeight));

                // 5. Aggiorniamo la posizione sullo schermo
                spriteView.setLayoutX(Math.round(px));
                spriteView.setLayoutY(Math.round(py));
            }
        }
        
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