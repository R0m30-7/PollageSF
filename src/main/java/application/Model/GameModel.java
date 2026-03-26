package application.Model;

import javafx.geometry.Point2D;

import application.Controller.InputManager;
import application.Utils.GameConfig;

public class GameModel {
    private Player player1;
    private Player player2;
    
    // Definizione della posizione del pavimento
    private double GROUND_LEVEL;
    
    // L'arena è larga il doppio dello schermo
    private double WORLD_WIDTH = application.Utils.Settings.getInstance().getWindowWidth() * 2;
    
    // Posizione X della telecamera
    private double cameraX = 0;
    
    // Variabili per memorizzare il salto nel frame precedente
    private boolean wasP1JumpHeld = false;
    private boolean wasP2JumpHeld = false;
    
    // Serve per l'aggiornamento in tempo reale della finestra
    private double currentWindowWidth = 1920;
    private double currentWindowHeight = 1080;
    
    // Il costruttore richiede larghezza e altezza dello sfondo per il calcolo dei bordi
    public GameModel(double bgWidth, double bgHeight) {
    	// Imposto la larghezza del mondo come quella dell'immagine
    	this.WORLD_WIDTH = bgWidth;
    	// Imposto il pavimento della scena
    	this.GROUND_LEVEL = currentWindowHeight - 100.0;
    	
    	// Calcoliamo la y per spawnare i giocatori con i piedi per terra
    	double spawnY = this.GROUND_LEVEL - GameConfig.pHeight;
    	
        // Spawn dei giocatori al centro del mondo
    	player1 = new Player(new Point2D(WORLD_WIDTH / 2 - 200, spawnY)); 
        player2 = new Player(new Point2D(WORLD_WIDTH / 2 + 200, spawnY));
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public double getCameraX() { return cameraX; }	// Serve alla View

    public void update(InputManager input) {
        // 1. Leggiamo lo stato dei tasti X di entrambi i giocatori
        boolean isP1JumpHeld = input.isJumpButtonPressed(1);
        boolean isP2JumpHeld = input.isJumpButtonPressed(2);

        // 2. Applichiamo la fisica passando lo stato del tasto!
        player1.applyPhysics(GROUND_LEVEL, isP1JumpHeld);
        player2.applyPhysics(GROUND_LEVEL, isP2JumpHeld);
        
        // 3. Movimento (con LIMITI DELL'ARENA)
        double p1X = input.getLeftStickX(1);
        if (Math.abs(p1X) > 0.0) {
            player1.moveHorizontal(p1X > 0 ? PlayerState.RIGHT : PlayerState.LEFT);
        }
        if (isP1JumpHeld && !wasP1JumpHeld) player1.jump();

        double p2X = input.getLeftStickX(2);
        if (Math.abs(p2X) > 0.0) {
            player2.moveHorizontal(p2X > 0 ? PlayerState.RIGHT : PlayerState.LEFT);
        }
        if (isP2JumpHeld && !wasP2JumpHeld) player2.jump();

        // 4. LIMITI DEL MONDO (Muri invisibili)
        // Impediamo ai giocatori di uscire dall'arena totale (WORLD_WIDTH)
        keepPlayerInBounds(player1);
        keepPlayerInBounds(player2);

        // ==========================================
        //         LOGICA DELLA TELECAMERA
        // ==========================================
        // Calcoliamo il punto centrale tra i due giocatori
        double midpointX = (player1.getPosition().getX() + player2.getPosition().getX()) / 2.0;
        
        // Vogliamo che questo punto medio sia esattamente al centro del nostro schermo
        double targetCameraX = midpointX - (currentWindowWidth / 2.0);
        
        // "Clamp": Impediamo alla telecamera di mostrare il vuoto fuori dall'arena
        if (targetCameraX < 0) {
            targetCameraX = 0; // Blocco a sinistra
        } else if (targetCameraX > (WORLD_WIDTH - currentWindowWidth)) {
            targetCameraX = WORLD_WIDTH - currentWindowWidth; // Blocco a destra
        }
        
        // Aggiorniamo la telecamera
        cameraX = targetCameraX;
        
        // ==========================================
        //         3. LIMITI DELLO SCHERMO
        // ==========================================
        // Ora che la telecamera si è mossa, chiudiamo i giocatori dentro la finestra visibile!
        keepPlayerOnScreen(player1);
        keepPlayerOnScreen(player2);
        
        // Memorizzazione dello stato attuale per il salto
        wasP1JumpHeld = isP1JumpHeld;
        wasP2JumpHeld = isP2JumpHeld;
    }
    
    // Metodo di supporto per i muri invisibili dell'arena
    private void keepPlayerInBounds(Player p) {
        double currentX = p.getPosition().getX();
        // Controllo muro sinistro
        if (currentX < 0) {
            p.setPosition(new Point2D(0, p.getPosition().getY()));
        } 
        // Controllo muro destro (tenendo conto dello spessore del giocatore dalla sua bounding box)
        else if (currentX > WORLD_WIDTH - GameConfig.pWidth) {
            p.setPosition(new Point2D(WORLD_WIDTH - GameConfig.pWidth, p.getPosition().getY()));
        }
        
        // Aggiorniamo la bounding box logica per riflettere la posizione bloccata
        p.getBoundingBox().updatePosition(p.getPosition());
    }
    
    // Metodo per impedire ai giocatori di uscire dall'inquadratura della telecamera
    private void keepPlayerOnScreen(Player p) {
        // Il bordo sinistro dello schermo corrisponde esattamente a dove si trova la telecamera
        double leftScreenEdge = cameraX;
        
        // Il bordo destro dello schermo è la telecamera + la larghezza della finestra,
        // a cui sottraiamo la larghezza del giocatore per non far uscire metà del suo corpo
        double rightScreenEdge = cameraX + currentWindowWidth- GameConfig.pWidth;

        double currentX = p.getPosition().getX(); // Oppure p.getX() a seconda di come l'hai chiamato

        // Controllo se esce a SINISTRA dello schermo
        if (currentX < leftScreenEdge) {
            p.setPosition(new javafx.geometry.Point2D(leftScreenEdge, p.getPosition().getY()));
            p.getBoundingBox().updatePosition(p.getPosition());
        } 
        // Controllo se esce a DESTRA dello schermo
        else if (currentX > rightScreenEdge) {
            p.setPosition(new javafx.geometry.Point2D(rightScreenEdge, p.getPosition().getY()));
            p.getBoundingBox().updatePosition(p.getPosition());
        }
    }
    
    // Aggiornare larghezza, altezza e pavimento della finestra
    public void updateWindowSize(double newWidth, double newHeight, double newWorldWidth) {
        this.currentWindowWidth = newWidth;
        this.currentWindowHeight = newHeight;
        
        // Il mondo di gioco si allarga e restringe in base allo zoom dello sfondo
        this.WORLD_WIDTH = newWorldWidth;

        // Il pavimento è sempre relativo all'altezza della finestra
        this.GROUND_LEVEL = newHeight - 100.0;

        // Sistema di sicurezza: se il giocatore rimpicciolisce la finestra di scatto,
        // i personaggi potrebbero trovarsi "sotto" al pavimento. Li tiriamo su!
        if (player1.getPosition().getY() + GameConfig.pHeight > this.GROUND_LEVEL) {
            player1.setPosition(new javafx.geometry.Point2D(player1.getPosition().getX(), this.GROUND_LEVEL - GameConfig.pHeight));
            player1.getBoundingBox().updatePosition(player1.getPosition());
        }
        if (player2.getPosition().getY() + GameConfig.pHeight > this.GROUND_LEVEL) {
            player2.setPosition(new javafx.geometry.Point2D(player2.getPosition().getX(), this.GROUND_LEVEL - GameConfig.pHeight));
            player2.getBoundingBox().updatePosition(player2.getPosition());
        }
    }
}