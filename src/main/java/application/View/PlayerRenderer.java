package application.View;

import application.Model.Hitbox;
import application.Model.Player;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PlayerRenderer {
    // Contenitore libero (Pane) che terrà il corpo, il pugno e la difesa
    private Pane rootNode;

    // Mappa dove copiare ogni frame delle animazioni per non renderizzarle sgranate
    private java.util.Map<String, Image> frameCache = new java.util.HashMap<>();
    
    // --- Variabili per lo Sprite Animato ---
    private ImageView spriteView;
    private Image atlasImage; //cambiato nome per chiarezza

    //COMANDI PER I TURNIP
    //private double frameWidth;
    //private double frameHeight;
    
    // Variabile per la State Machine
    private application.Model.AnimState lastAnimState = null;
    
    // Gestione dell'animazione
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    
    // Per visualizzare l'hitbox del giocatore
    private Rectangle hitboxVisual;

    public PlayerRenderer(Player player) {
        rootNode = new Pane(); 
        
        // --- Caricamento dello sprite ---
        try {
            // 1. Recuperiamo la scala di base (quella scritta nella classe Turnip/RedTurnip)
            //double baseScale = player.getRenderScale(); 
            
            // 2. Carichiamo l'atlas GIA' INGRANDITO alla sua dimensione di design
            // Usiamo 'false' nell'ultimo parametro (smooth) per evitare il blur
            //double targetW = player.getSpriteCols() * player.getFrameWidth() * baseScale;
            //double targetH = player.getSpriteRows() * player.getFrameHeight() * baseScale;
            
            //atlasImage = new Image(getClass().getResourceAsStream(player.getAtlasPath()), targetW, targetH, true, false);
            atlasImage = new Image(getClass().getResourceAsStream(player.getAtlasPath()));
            
            spriteView = new ImageView(atlasImage);
            
            // 3. I frame per il ritaglio ora sono quelli "ingranditi" di base
            //frameWidth = player.getFrameWidth() * baseScale;
            //frameHeight = player.getFrameHeight() * baseScale;
            
            spriteView.setSmooth(false); // Extra sicurezza per il ridimensionamento finestra
            //spriteView.setViewport(new Rectangle2D(0, 0, frameWidth, frameHeight));
            
        } catch (Exception e) {
            System.out.println("Errore caricamento atlas: " + player.getAtlasPath());
        }
        
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

                // ==========================================
                // IL NUOVO SISTEMA DI RITAGLIO TRAMITE ATLAS
                // ==========================================
                // 1. Chiediamo al TextureAtlas le coordinate del frame esatto
                Rectangle2D frameRect = player.getAtlas().getFrame(currentData.row, currentData.startCol, currentFrame);
                
                // 2. Aggiorniamo la finestra dell'immagine
                spriteView.setImage(getCroppedFrame(frameRect));

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
                // ==========================================
                // FINE NUOVO SISTEMA DI RITAGLIO TRAMITE ATLAS
                // ==========================================

                // 4. Aggiorniamo la Y (in genere fissa o calcolata rispetto al suolo)
                spriteView.setLayoutY(Math.round(py));
                

                // 4. Ritaglio del frame (usando la frameWidth già scalata nel costruttore)
                //double cropX = (currentData.startCol + currentFrame) * frameWidth;
                //double cropY = currentData.row * frameHeight;
                //spriteView.setViewport(new Rectangle2D(cropX, cropY, frameWidth, frameHeight));

                // 5. APPLICHIAMO LO SCALING DINAMICO DELLA FINESTRA
                // Questo adatterà il nostro sprite già nitido alla grandezza attuale della finestra
                //spriteView.setFitWidth(player.getWidth());
                //spriteView.setFitHeight(player.getHeight());	
                
                // 5. Aggiorniamo la posizione sullo schermo
                //spriteView.setLayoutX(Math.round(px));
                //spriteView.setLayoutY(Math.round(py));
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
    }

    // Metodo helper per renderizzare le texture in HD
    private Image getCroppedFrame(Rectangle2D rect) {
    String key = (int) rect.getMinX() + "_" + (int) rect.getMinY() + "_"
               + (int) rect.getWidth() + "_" + (int) rect.getHeight();
    return frameCache.computeIfAbsent(key, k ->
        new WritableImage(atlasImage.getPixelReader(),
            (int) rect.getMinX(), (int) rect.getMinY(),
            (int) rect.getWidth(), (int) rect.getHeight())
    );
}
    
    // Metodo speciale da chiamare quando renderizziamo il personaggio nel Menu
    public void setMenuMode() {
        this.hitboxVisual.setVisible(false);
    }
}