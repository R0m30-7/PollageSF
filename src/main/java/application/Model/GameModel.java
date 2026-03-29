package application.Model;

import javafx.geometry.Point2D;

import application.Controller.InputManager;
import application.Utils.GameConfig;

public class GameModel {
    private Player player1;
    private Player player2;
    
    // L'arena è larga il doppio dello schermo
    private double WORLD_WIDTH = application.Utils.Settings.getInstance().getWindowWidth() * 2;
    
    // Posizione X della telecamera
    private double cameraX = 0;
    
    // Variabili per memorizzare il salto nel frame precedente
    private boolean wasP1JumpHeld = false;
    private boolean wasP2JumpHeld = false;
    
    // Variabili per memorizzare il pugno nel frame precedente
    private boolean wasP1PunchHeld = false;
    private boolean wasP2PunchHeld = false;
    
    // Serve per l'aggiornamento in tempo reale della finestra
    private double currentWindowWidth = 1920;
    private double currentWindowHeight = 1080;
    
    // Definizione della posizione del pavimento
    private double GROUND_LEVEL;
    private double currentGroundRatio = 0.9;
    private double currentGroundLevel;
    
    // Il costruttore richiede larghezza e altezza dello sfondo per il calcolo dei bordi
    public GameModel(double bgWidth, double bgHeight) {
    	// Imposto la larghezza del mondo come quella dell'immagine
    	this.WORLD_WIDTH = bgWidth;
    	// Imposto il pavimento della scena
    	this.GROUND_LEVEL = currentWindowHeight - 100.0;
    	
    	// Calcoliamo la y per spawnare i giocatori con i piedi per terra
    	double spawnY = this.GROUND_LEVEL - GameConfig.pHeight;
    	
        // Spawn dei giocatori al centro del mondo (scelgo di base la classe Turnip)
    	player1 = new Turnip(new Point2D(WORLD_WIDTH / 2 - 200, spawnY)); 
        player2 = new Turnip(new Point2D(WORLD_WIDTH / 2 + 200, spawnY));
        
        // Impostiamo le direzioni iniziali
        player1.setFacingRight(true); 
        player2.setFacingRight(false);
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public double getCameraX() { return cameraX; }	// Serve alla View

    public void update(InputManager input) {
    	// Aggiornamento tick per la durata dei pugni
    	player1.updateTicks();
    	player2.updateTicks();
    	
        // 1. Leggiamo lo stato dei tasti X di entrambi i giocatori
        boolean isP1JumpHeld = input.isJumpButtonPressed(1);
        boolean isP2JumpHeld = input.isJumpButtonPressed(2);
        
        // Leggiamo lo stato dei pugni
        boolean isP1PunchHeld = input.isPunchButtonPressed(1);
        boolean isP2PunchHeld = input.isPunchButtonPressed(2);

        // 2. Applichiamo la fisica passando lo stato del tasto!
        player1.applyPhysics(this.currentGroundLevel, isP1JumpHeld);
        player2.applyPhysics(this.currentGroundLevel, isP2JumpHeld);
        
        // 3. Movimento (con LIMITI DELL'ARENA)
        double p1X = input.getLeftStickX(1);
        if (Math.abs(p1X) > 0.0) {
            player1.moveHorizontal(p1X > 0 ? PlayerState.RIGHT : PlayerState.LEFT);
        }
        if (isP1JumpHeld && !wasP1JumpHeld) player1.jump();
        
        if (isP1PunchHeld && !wasP1PunchHeld) player1.startPunch();
        
        player1.setDefending(input.isDefendButtonPressed(1));

        double p2X = input.getLeftStickX(2);
        if (Math.abs(p2X) > 0.0) {
            player2.moveHorizontal(p2X > 0 ? PlayerState.RIGHT : PlayerState.LEFT);
        }
        if (isP2JumpHeld && !wasP2JumpHeld) player2.jump();
        if (isP2PunchHeld && !wasP2PunchHeld) player2.startPunch();
        player2.setDefending(input.isDefendButtonPressed(2));
        
        // --- 3. LOGICA DI COMBATTIMENTO ---
        handleCombat(player1, player2);
        handleCombat(player2, player1);

        // 4. LIMITI DEL MONDO (Muri invisibili)
        // Impediamo ai giocatori di uscire dall'arena totale (WORLD_WIDTH)
        keepPlayerInBounds(player1);
        keepPlayerInBounds(player2);

        // ==========================================
        //         LOGICA DELLA TELECAMERA
        // ==========================================
        // Calcoliamo il fulcro (punto centrale) tra i due giocatori nel mondo assoluto
        double midpointX = (player1.getPosition().getX() + player2.getPosition().getX()) / 2.0;
        
        // Definiamo i limiti della "Deadzone" (25% a sinistra, 25% a destra)
        double leftThreshold = currentWindowWidth * 0.25;
        double rightThreshold = currentWindowWidth * 0.75;
        
        // Calcoliamo dove si trova il fulcro RISPETTO all'inquadratura attuale
        double screenMidpointX = midpointX - cameraX;
        
        // Calcolo della posizione target della telecamera (per la morbidezza del movimento)
        double targetCameraX = cameraX;
        
        // Muoviamo la telecamera SOLO se il fulcro esce dalla zona morta centrale
        if (screenMidpointX < leftThreshold) {
            // Il fulcro spinge troppo a sinistra, la telecamera arretra
            targetCameraX = midpointX - leftThreshold;
        } else if (screenMidpointX > rightThreshold) {
            // Il fulcro spinge troppo a destra, la telecamera avanza
            targetCameraX = midpointX - rightThreshold;
        }
        
        // "Clamp": Impediamo alla telecamera di mostrare il vuoto fuori dall'arena
        if (targetCameraX < 0) {
        	targetCameraX = 0; // Blocco al muro sinistro del mondo
        } else if (targetCameraX > (WORLD_WIDTH - currentWindowWidth)) {
        	targetCameraX = WORLD_WIDTH - currentWindowWidth; // Blocco al muro destro del mondo
        }
        
        // LERP (INTERPOLAZIONE LINEARE) - Movimento morbido della telecamera
        // Questo valore decide la morbidezza: 
        // 0.01 è lentissima, 0.1 è fluida ma reattiva, 1.0 è istantanea come prima.
        double cameraSpeed = 0.08;
        
        // La telecamera percorre solo una frazione della distanza verso il bersaglio
        cameraX += (targetCameraX - cameraX) * cameraSpeed;
        
        // Aggiorna lo stato delle animazioni in base a ciò che è successo in questo frame
        player1.updateAnimationState();
        player2.updateAnimationState();
        
        // Resetta il movimento: se nel prossimo frame non premono le levette, saranno fermi
        player1.isMoving = false;
        player2.isMoving = false;
        
        // ==========================================
        //         3. LIMITI DELLO SCHERMO
        // ==========================================
        // Ora che la telecamera si è mossa, chiudiamo i giocatori dentro la finestra visibile!
        keepPlayerOnScreen(player1);
        keepPlayerOnScreen(player2);
        
        // Memorizzazione dello stato attuale per il salto
        wasP1JumpHeld = isP1JumpHeld;
        wasP2JumpHeld = isP2JumpHeld;
        
        wasP1PunchHeld = isP1PunchHeld;
        wasP2PunchHeld = isP2PunchHeld;
    }
    
    // ==========================================
    //      IL MOTORE DEI DANNI E COLLISIONI
    // ==========================================
    private void handleCombat(Player attacker, Player defender) {
    	// 1. Aggiungiamo il controllo: l'attaccante e il difensore NON devono essere la stessa persona!
        if (attacker == defender) return;
        
        // Controlla se sta attaccando e se non ha già fatto danno in questa animazione
        if (attacker.isPunchActive() && !attacker.hasDealtDamage()) {
            
            // 2. Calcoliamo la X del pugno usando la LARGHEZZA REALE del giocatore, non GameConfig!
            double punchX = attacker.isFacingRight() 
                    ? attacker.getPosition().getX() + attacker.getWidth() 
                    : attacker.getPosition().getX() - GameConfig.pPunchWidth;
            
            // 3. Calcoliamo la Y del pugno usando l'ALTEZZA REALE del giocatore
            double punchY = attacker.getPosition().getY() + (attacker.getHeight() * 0.2);
            
            // Crea una Hitbox invisibile per il calcolo matematico
            Hitbox punchHitbox = new Hitbox(new Point2D(punchX, punchY), GameConfig.pPunchWidth, GameConfig.pPunchHeight);
            
            // Controlla se il pugno si sovrappone al corpo del difensore
            if (punchHitbox.intersects(defender.getBoundingBox())) {
                
                // Applica i danni se l'avversario non sta bloccando
                if (defender.isDefending()) {
                    System.out.println("Colpo parato!");
                } else {
                    System.out.println("COLPITO! Danno: " + GameConfig.pPunchDamage);
                    defender.takeDamage(GameConfig.pPunchDamage);
                }
                
                // Segna che il pugno ha colpito
                attacker.setHasDealtDamage(true);
            }
        }
    }
    
    // Metodo di supporto per i muri invisibili dell'arena
    private void keepPlayerInBounds(Player p) {
        double currentX = p.getPosition().getX();
        // Controllo muro sinistro
        if (currentX < 0) {
            p.setPosition(new Point2D(0, p.getPosition().getY()));
        } 
        // Controllo muro destro (tenendo conto dello spessore del giocatore dalla sua bounding box)
        else if (currentX > WORLD_WIDTH - p.getWidth()) {
            p.setPosition(new Point2D(WORLD_WIDTH - p.getWidth(), p.getPosition().getY()));
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
        double rightScreenEdge = cameraX + currentWindowWidth - p.getWidth();

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
        
        // Ricalcola del pavimento in tempo reale
        this.currentGroundLevel = this.currentWindowHeight * this.currentGroundRatio;
        
        // Il mondo di gioco si allarga e restringe in base allo zoom dello sfondo
        this.WORLD_WIDTH = newWorldWidth;

        // Sistema di sicurezza: se il giocatore rimpicciolisce la finestra di scatto,
        // i personaggi potrebbero trovarsi "sotto" al pavimento. Li tiriamo su!
        if (player1.getPosition().getY() + player1.getHeight() > this.currentGroundLevel) {
            player1.setPosition(new javafx.geometry.Point2D(player1.getPosition().getX(), this.currentGroundLevel - player1.getHeight()));
            player1.getBoundingBox().updatePosition(player1.getPosition());
        }
        if (player2.getPosition().getY() + player2.getHeight() > this.currentGroundLevel) {
            player2.setPosition(new javafx.geometry.Point2D(player2.getPosition().getX(), this.currentGroundLevel - player2.getHeight()));
            player2.getBoundingBox().updatePosition(player2.getPosition());
        }
    }
    
    public void setGroundLevelRatio(double ratio) {
    	this.currentGroundRatio = ratio;
        // Calcola subito il pavimento in pixel moltiplicando l'altezza per la percentuale
        this.currentGroundLevel = this.currentWindowHeight * this.currentGroundRatio;
    }
}