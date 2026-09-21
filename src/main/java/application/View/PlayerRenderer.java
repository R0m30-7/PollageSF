package application.View;

import application.Model.Hitbox;
import application.Model.IReadOnlyPlayer;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PlayerRenderer {
    // Contenitore (Pane) che terrà il corpo, il pugno e la difesa
    private Pane rootNode;

    // Mappa dove copiare ogni frame delle animazioni per non renderizzarle sgranate
    private java.util.Map<String, Image> frameCache = new java.util.HashMap<>();
    
    // --- Variabili per lo Sprite Animato ---
    private ImageView spriteView;
    private Image atlasImage;
    
    // Variabile per i vari stati per le animazioni
    private application.Model.AnimState lastAnimState = null;   //! PERICOLO MVC
    
    // Gestione dell'animazione
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    
    //! Per visualizzare l'hitbox del giocatore
    private Rectangle hitboxVisual;

    public PlayerRenderer(IReadOnlyPlayer player) {
        rootNode = new Pane(); 
        
        // --- Caricamento dello sprite ---
        try {
            atlasImage = new Image(getClass().getResourceAsStream(player.getAtlasPath()));
            
            spriteView = new ImageView(atlasImage);

            spriteView.setSmooth(false); // Non vogliamo che lo sprite sia sfuocato
        } catch (Exception e) {
            System.out.println("Errore caricamento atlas: " + player.getAtlasPath());
            System.exit(1);
        }
        
        //! Visualizzazione hitbox
        hitboxVisual = new Rectangle();
        hitboxVisual.setFill(null);           // Niente riempimento
        hitboxVisual.setStroke(Color.RED);          // Bordo rosso
        hitboxVisual.setStrokeWidth(2);      // Spessore del bordo

        // Aggiungiamo tutto al rootNode (lo sprite prende il posto del bodyContainer)
        if (spriteView != null){
            rootNode.getChildren().add(spriteView);
        }
        rootNode.getChildren().add(hitboxVisual);	//! Visualizzazione hitbox
    }

    public Pane getNode() {
        return rootNode;
    }

    public void render(IReadOnlyPlayer player) {    //! Modificare qui per aggiungere il PIVOT point
        double px = player.getPosition().getX() - player.getRenderOffsetX();
        double py = player.getPosition().getY() - player.getRenderOffsetY();
        
        // ==========================================
        // ANIMAZIONE DELLO SPRITE
        // ==========================================
        if (spriteView != null) {
            // Chiediamo al giocatore quale frame usare in questo momento
            application.Model.AnimState currentState = player.getCurrentAnimState();
            application.Model.AnimData currentData = player.getCurrentAnimData();

            if (currentData != null) {
                // Se ha cambiato animazione (es. da camminata a salto), azzeriamo il frame
                if (currentState != lastAnimState) {
                    currentFrame = 0;
                    lastFrameTime = System.nanoTime();
                    lastAnimState = currentState;
                }

                // Aggiornamento del tempo per scorrere i frame
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
                    // l'animazione non "salta" in avanti recuperando il tempo perso
                    //! Modificare qui per aggiungere animazione stun
                    lastFrameTime = now; 
                }

                // Prendiamo dal TextureAtlas le coordinate del frame esatto
                Rectangle2D frameRect = player.getAtlas().getFrame(currentData.row, currentData.startCol, currentFrame);
                
                // Prendiamo la scala per ingrandire il player
                double currentScale = player.getRenderScale();

                // Aggiorniamo la finestra
                spriteView.setImage(getCroppedFrame(frameRect, currentScale));
                
                // ==========================================
                // GESTIONE DIREZIONE (FLIP ORIZZONTALE)
                // ==========================================
                // Poiché il JSON di Ryu contiene solo le animazioni verso destra,
                // dobbiamo flippare l'immagine se il giocatore guarda a sinistra
                
                if (!player.isFacingRight()) {
                    spriteView.setScaleX(-1);

                    // Soluzione momentanea per ricentrare il personaggio durante il terzo frame del pugno a sinsitra,
                    // siccome è largo
                    //! Quando verrà inserito il pivot point, modificare questa funzione per renderla generica
                    if(currentState == application.Model.AnimState.PUNCH_LEFT) {
                        if (currentFrame == 2){
                            double offsetCompensazione = 25.0 * currentScale; 
                            px -= offsetCompensazione;
                        }       
                    
                    }else if(currentState == application.Model.AnimState.PUNCH_CROUCH_LEFT){
                        if (currentFrame == 0){
                            double offsetCompensazione = 5.0 * currentScale; 
                            px -= offsetCompensazione;
                        }else if (currentFrame == 1){
                            double offsetCompensazione = 23.0 * currentScale; 
                            px -= offsetCompensazione;
                        }     

                    }
                } else {
                    spriteView.setScaleX(1);
                }
                // Approssimazione delle coordinate per consentire il disegno sullo schermo
                spriteView.setLayoutX(Math.round(px));
                spriteView.setLayoutY(Math.round(py));
            }
        }
        
        //! ==========================================
        //! Mostra hitbox
        //! ==========================================
        //! Recuperiamo la Hitbox fisica reale dal Model
        Hitbox physBox = player.getBoundingBox();
        
        //! Aggiorniamo il rettangolo rosso per farlo combaciare millimetricamente
        hitboxVisual.setX(Math.round(physBox.getX()));
        hitboxVisual.setY(Math.round(physBox.getY()));
        hitboxVisual.setWidth(physBox.getWidth());
        hitboxVisual.setHeight(physBox.getHeight());
        
        //! Lo portiamo in primo piano per vederlo sopra lo sprite
        hitboxVisual.toFront();
    }

    // Metodo helper per renderizzare le texture in HD (Nearest-Neighbor via Software)
    private Image getCroppedFrame(Rectangle2D rect, double scale) {
        // Generiamo chiave univoca (dipendente da posizione di partenza, dimensione del ritaglio e la scala di
        // ingradimento/riduzione) in modo da non dover ricalcolare più volte lo stesso frame, dato che lo salviamo in cache
        String key = (int) rect.getMinX() + "_" + (int) rect.getMinY() + "_"
                   + (int) rect.getWidth() + "_" + (int) rect.getHeight() + "_" + scale;
        
        // Se l'immagine esiste in cache viene restituita, altrimenti viene generata
        return frameCache.computeIfAbsent(key, k -> {
            // Salvataggio delle dimensioni originali
            int srcW = (int) rect.getWidth();
            int srcH = (int) rect.getHeight();
            
            // Calcolo delle dimensioni finali utilizzando la scala e le dimensioni iniziali
            int dstW = (int) Math.round(srcW * scale);
            int dstH = (int) Math.round(srcH * scale);
            
            WritableImage scaledImg = new WritableImage(dstW, dstH);    // Allocazione di un buffer grafico vuoto
            javafx.scene.image.PixelReader reader = atlasImage.getPixelReader();
            javafx.scene.image.PixelWriter writer = scaledImg.getPixelWriter();
            
            // Scaliamo copiando lo stesso pixel più volte (Nearest-Neighbor). Questo itera su ogni pixel dell'immagine finale
            // e fa combaciare la coordinata originale con quella finale dividendo per la scala.
            for (int y = 0; y < dstH; y++) {
                for (int x = 0; x < dstW; x++) {
                    int srcX = (int) (rect.getMinX() + (x / scale));
                    int srcY = (int) (rect.getMinY() + (y / scale));
                    writer.setArgb(x, y, reader.getArgb(srcX, srcY));   // Copia del pixel
                }
            }
            return scaledImg;
        });
    }
    
    // Metodo speciale da chiamare quando renderizziamo il personaggio nel Menu
    public void setMenuMode() {
        this.hitboxVisual.setVisible(false);
    }
}