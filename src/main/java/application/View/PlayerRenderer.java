package application.View;

import application.Model.Hitbox;
import application.Model.Player;
//import application.Utils.ImageUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PlayerRenderer {
    private Pane rootNode;
    
    // --- Variabili per lo Sprite Animato ---
    private ImageView spriteView;
    private Image atlasImage; // Cambiato nome per chiarezza
    
    private application.Model.AnimState lastAnimState = null;
    
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    
    private Rectangle hitboxVisual;

    public PlayerRenderer(Player player) {
        rootNode = new Pane(); 
        
        try {
            // 1. Carica l'immagine NATIVA senza applicare dimensioni fittizie
            // NOTA: Se l'immagine ha il background da rimuovere, usa il tuo ImageUtils.
            // Se è già trasparente, usa semplicemente:
            // atlasImage = new Image(getClass().getResourceAsStream(player.getAtlasPath()));
            
            atlasImage = new Image(getClass().getResourceAsStream(player.getAtlasPath()));
            
            spriteView = new ImageView(atlasImage);
            spriteView.setSmooth(false); 
            
            // Non settiamo subito la viewport fissa qui, lo faremo nel render() frame per frame
            
        } catch (Exception e) {
            System.out.println("Errore caricamento atlas: " + player.getAtlasPath());
        }
        
        hitboxVisual = new Rectangle();
        hitboxVisual.setFill(null);       
        hitboxVisual.setStroke(Color.RED); 
        hitboxVisual.setStrokeWidth(2);    

        if (spriteView != null) rootNode.getChildren().add(spriteView);
        rootNode.getChildren().add(hitboxVisual);	
    }

    public Pane getNode() {
        return rootNode;
    }

    public void render(Player player) {
        double px = player.getPosition().getX();
        double py = player.getPosition().getY();
        
        if (spriteView != null) {
            application.Model.AnimState currentState = player.getCurrentAnimState();
            application.Model.AnimData currentData = player.getCurrentAnimData();

            if (currentData != null) {
                
                if (currentState != lastAnimState) {
                    currentFrame = 0;
                    lastFrameTime = System.nanoTime();
                    lastAnimState = currentState;
                }

                long now = System.nanoTime();
                
                if (!player.isStunned()) {
                    if (now - lastFrameTime > currentData.speedNs) {
                        if (currentData.loop) {
                            currentFrame = (currentFrame + 1) % currentData.frameCount;
                        } else {
                            currentFrame = Math.min(currentFrame + 1, currentData.frameCount - 1);
                        }
                        lastFrameTime = now;
                    }
                } else {
                    lastFrameTime = now; 
                }

                // ==========================================
                // IL NUOVO SISTEMA DI RITAGLIO TRAMITE ATLAS
                // ==========================================
                // 1. Chiediamo al TextureAtlas le coordinate del frame esatto
                Rectangle2D frameRect = player.getAtlas().getFrame(currentData.row, currentData.startCol, currentFrame);
                
                // 2. Aggiorniamo la finestra dell'immagine
                spriteView.setViewport(frameRect);

                // 3. Applichiamo lo scaling dinamico, moltiplicando la grandezza NATIVA del frame per lo zoom
                // Non usiamo più la Hitbox per ridimensionare lo sprite, lo sprite scala proporzionalmente a se stesso
                double currentScale = player.getRenderScale();
                spriteView.setFitWidth(frameRect.getWidth() * currentScale);
                spriteView.setFitHeight(frameRect.getHeight() * currentScale);
                
                // ==========================================
                // GESTIONE DIREZIONE (FLIP ORIZZONTALE)
                // ==========================================
                // Poiché il JSON di Ryu contiene solo le animazioni verso destra,
                // dobbiamo flippare l'immagine se il giocatore guarda a sinistra
                if (!player.isFacingRight()) {
                    spriteView.setScaleX(-1);
                    // IMPORTANTE: Quando si flippa con SetScaleX(-1), il punto di origine (X) si inverte.
                    // Bisogna spostare l'immagine verso sinistra di una larghezza pari a se stessa
                    // per compensare il capovolgimento attorno al centro.
                    spriteView.setLayoutX(Math.round(px) + (frameRect.getWidth() * currentScale)/8);
                } else {
                    spriteView.setScaleX(1);
                    spriteView.setLayoutX(Math.round(px));
                }

                // 4. Aggiorniamo la Y (in genere fissa o calcolata rispetto al suolo)
                spriteView.setLayoutY(Math.round(py));
            }
        }
        
        // Render Hitbox (Invariato)
        Hitbox physBox = player.getBoundingBox();
        hitboxVisual.setX(Math.round(physBox.getX())+.2*physBox.getWidth());
        hitboxVisual.setY(Math.round(physBox.getY()));
        hitboxVisual.setWidth(physBox.getWidth());
        hitboxVisual.setHeight(physBox.getHeight());
        hitboxVisual.toFront();
    }
    

    public void setMenuMode() {
        this.hitboxVisual.setVisible(false);
    }
}