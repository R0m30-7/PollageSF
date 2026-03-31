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
    
    // Per visualizzare l'hitbox del giocatore
    private Rectangle hitboxVisual;

    public PlayerRenderer(Player player) {
        rootNode = new Pane(); 
        
        // --- Caricamento dello sprite ---
        try {
            // 1. Recuperiamo la scala di base (quella scritta nella classe Turnip/RedTurnip)
            double baseScale = player.getRenderScale(); 
            
            // 2. Carichiamo l'atlas GIA' INGRANDITO alla sua dimensione di design
            // Usiamo 'false' nell'ultimo parametro (smooth) per evitare il blur
            double targetW = player.getSpriteCols() * player.getFrameWidth() * baseScale;
            double targetH = player.getSpriteRows() * player.getFrameHeight() * baseScale;
            
            atlas = new Image(getClass().getResourceAsStream(player.getAtlasPath()), targetW, targetH, true, false);
            spriteView = new ImageView(atlas);
            
            // 3. I frame per il ritaglio ora sono quelli "ingranditi" di base
            frameWidth = player.getFrameWidth() * baseScale;
            frameHeight = player.getFrameHeight() * baseScale;
            
            spriteView.setSmooth(false); // Extra sicurezza per il ridimensionamento finestra
            spriteView.setViewport(new Rectangle2D(0, 0, frameWidth, frameHeight));
            
        } catch (Exception e) {
            System.out.println("Errore caricamento atlas: " + player.getAtlasPath());
        }

        // --- IL PUGNO (Giallo) ---
        // Viene creato piccolo poi ingrandito nel render
        punchVisual = new Rectangle(0, 0, Color.YELLOW);
        punchVisual.setStroke(Color.ORANGE);
        punchVisual.setVisible(false);
        
        // Visualizzazione hitbox
        hitboxVisual = new Rectangle();
        hitboxVisual.setFill(null);       // Niente riempimento!
        hitboxVisual.setStroke(Color.RED); // Bordo rosso
        hitboxVisual.setStrokeWidth(2);    // Spessore del bordo

        // Aggiungiamo tutto al rootNode (lo sprite prende il posto del bodyContainer)
        if (spriteView != null) rootNode.getChildren().add(spriteView);
        rootNode.getChildren().add(hitboxVisual);	// Visualizzazione hitbox
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
                
                // --- Congelamento stun ---
                if (!player.isStunned()) {
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
                } else {
                    // Aggiorniamo comunque il timer nascosto. Così quando finisce lo stun, 
                    // l'animazione non "salta" in avanti recuperando il tempo perso!
                    lastFrameTime = now; 
                }

                // 4. Ritaglio del frame (usando la frameWidth già scalata nel costruttore)
                double cropX = (currentData.startCol + currentFrame) * frameWidth;
                double cropY = currentData.row * frameHeight;
                spriteView.setViewport(new Rectangle2D(cropX, cropY, frameWidth, frameHeight));

                // 5. APPLICHIAMO LO SCALING DINAMICO DELLA FINESTRA
                // Questo adatterà il nostro sprite già nitido alla grandezza attuale della finestra
                spriteView.setFitWidth(player.getWidth());
                spriteView.setFitHeight(player.getHeight());	
                
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
        if (player.isPunchActive()) {
            punchVisual.setVisible(true);
            punchVisual.toFront();
            
            // Aggiorniamo le dimensioni del rettangolo!
            punchVisual.setWidth(player.getPunchWidth());
            punchVisual.setHeight(player.getPunchHeight());
            
            double punchY = py + (player.getHeight() * 0.2); 
            double punchX = player.isFacingRight() ? px + player.getWidth() : px - player.getPunchWidth();
            
            punchVisual.setX(punchX);
            punchVisual.setY(punchY);
        } else {
            punchVisual.setVisible(false);
        }
    }
}