package application.Model;

import javafx.geometry.Point2D;
import application.Controller.CharacterFactory;
import application.Controller.InputManager;
import application.Utils.GameConfig;

public class GameModel {
    private Player player1;
    private Player player2;
    
    // L'arena è larga il doppio dello schermo
    private double worldWidth = application.Utils.Settings.getInstance().getWindowWidth() * 2;
    
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
    	this.worldWidth = bgWidth;
    	// Imposto il pavimento della scena
    	this.GROUND_LEVEL = currentWindowHeight - 100.0;
    	
    	// Calcoliamo la y per spawnare i giocatori con i piedi per terra
    	double spawnY = this.GROUND_LEVEL - GameConfig.pHeight;
    	
        // Spawn dei giocatori al centro del mondo (scelgo di base la classe Turnip)
    	player1 = new Turnip(new Point2D(worldWidth / 2 - 200, spawnY)); 
        player2 = new Turnip(new Point2D(worldWidth / 2 + 200, spawnY));
        
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
        // --- GIOCATORE 1 ---
        if (!player1.isStunned()) {
            double p1X = input.getLeftStickX(1);
            if (Math.abs(p1X) > 0.0) {
                player1.moveHorizontal(p1X > 0 ? PlayerState.RIGHT : PlayerState.LEFT);
            }
            if (isP1JumpHeld && !wasP1JumpHeld) player1.jump();
            if (isP1PunchHeld && !wasP1PunchHeld) player1.startPunch();
            player1.setDefending(input.isDefendButtonPressed(1));
        } else {
            // Se è stordito, abbassa le difese e si ferma!
            player1.setDefending(false);
        }

        // --- GIOCATORE 2 ---
        if (!player2.isStunned()) {
            double p2X = input.getLeftStickX(2);
            if (Math.abs(p2X) > 0.0) {
                player2.moveHorizontal(p2X > 0 ? PlayerState.RIGHT : PlayerState.LEFT);
            }
            if (isP2JumpHeld && !wasP2JumpHeld) player2.jump();
            if (isP2PunchHeld && !wasP2PunchHeld) player2.startPunch();
            player2.setDefending(input.isDefendButtonPressed(2));
        } else {
            player2.setDefending(false);
        }
        
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
        } else if (targetCameraX > (worldWidth - currentWindowWidth)) {
        	targetCameraX = worldWidth - currentWindowWidth; // Blocco al muro destro del mondo
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
        if (attacker == defender) return;
        
        if (attacker.isPunchActive() && !attacker.hasDealtDamage()) {
            
        	// ORA USIAMO LE DIMENSIONI DEL GIOCATORE, NON PIÙ GAMECONFIG!
            double punchX = attacker.isFacingRight() 
                    ? attacker.getPosition().getX() + attacker.getWidth() 
                    : attacker.getPosition().getX() - attacker.getPunchWidth();
            double punchY = attacker.getPosition().getY() + (attacker.getHeight() * 0.2);
            
            Hitbox punchHitbox = new Hitbox(new Point2D(punchX, punchY), attacker.getPunchWidth(), attacker.getPunchHeight());
            
            if (punchHitbox.intersects(defender.getBoundingBox())) {
                
                // IL DANNO ORA È DINAMICO!
                double baseDamage = attacker.getPunchDamage();

                boolean isAttackerOnRight = attacker.getPosition().getX() > defender.getPosition().getX();
                boolean isFacingAttacker = (isAttackerOnRight && defender.isFacingRight()) || (!isAttackerOnRight && !defender.isFacingRight());

                if (defender.isDefending() && isFacingAttacker) {
                    long blockDuration = System.currentTimeMillis() - defender.getBlockStartTime();
                    
                    // Otteniamo dinamicamente i dati dell'animazione di parata del difensore!
                    AnimData blockData = defender.getBlockAnimData(); 
                    long timePerFrameMs = blockData.speedNs / 1_000_000L; // Convertiamo nanosecondi in millisecondi
                    int totalFrames = blockData.frameCount;
                    
                    // Calcoliamo in quale "frame teorico" della parata si trova il difensore
                    int currentBlockFrame = (int) (blockDuration / timePerFrameMs);

                    // Se l'animazione è arrivata all'ultimo frame (o lo ha superato tenendo premuto) -> PARRY PERFETTO!
                    if (currentBlockFrame >= totalFrames - 1) {
                        System.out.println("⭐ PARRY PERFETTO di " + defender.getDisplayName() + "!");
                        attacker.stun(defender.getParryStunDuration());
                        
                    } else {
                        // Formula Dinamica: (FrameAttuale + 1) / FrameTotali.
                        // Es: 3 frame totali. Frame 0 -> 33% bloccato. Frame 1 -> 66% bloccato.
                        double blockPercentage = (double) (currentBlockFrame + 1) / totalFrames;
                        double damageMultiplier = 1.0 - blockPercentage;
                        
                        double finalDamage = baseDamage * damageMultiplier;
                        
                        System.out.println("🛡️ Parata Parziale (Frame " + (currentBlockFrame + 1) + "/" + totalFrames + ")! Danno subito: " + finalDamage);
                        defender.takeDamage((int) finalDamage);
                    }
                } else {
                    // Preso in pieno o di spalle!
                    System.out.println("💥 COLPITO IN PIENO! Danno: " + baseDamage);
                    defender.takeDamage((int) baseDamage);
                }
                
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
        else if (currentX > worldWidth - p.getWidth()) {
            p.setPosition(new Point2D(worldWidth - p.getWidth(), p.getPosition().getY()));
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
        
        // --- SCALING ---
        // Ipotizziamo che tu abbia bilanciato il gioco su uno schermo 1080p.
        double scaleFactor = newHeight / 1080.0; 
        player1.updateDynamicScale(scaleFactor);
        player2.updateDynamicScale(scaleFactor);
        // ------------------------------
        
        // Ricalcola del pavimento in tempo reale
        this.currentGroundLevel = this.currentWindowHeight * this.currentGroundRatio;
        
        // Il mondo di gioco si allarga e restringe in base allo zoom dello sfondo
        this.worldWidth = newWorldWidth;

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
    
    // Metodo universale per lo spawn
    public void spawnPlayers(CharacterFactory f1, CharacterFactory f2) {
        // Le coordinate sono decise solo qui! 
        // Possiamo usare delle proporzioni rispetto alla larghezza del mondo (worldWidth)
        double spawnX1 = worldWidth * 0.2; 
        double spawnX2 = worldWidth * 0.8;
        double spawnY = 0; // Verranno poi appoggiati al suolo da applyPhysics o setGroundLevel

        this.player1 = f1.create(new Point2D(spawnX1, spawnY));
        this.player2 = f2.create(new Point2D(spawnX2, spawnY));
        
        this.player2.setFacingRight(false); // Il P2 guarda sempre a sinistra all'inizio
    }
    
    // --- INIEZIONE DEI GIOCATORI SELEZIONATI ---
    // Questo serve perché altrimenti, anche se il giocatore scegliesse il personaggio dalla mappa dei
    // personaggi, vedrebbe comunque che il suo giocatore risulta Turnip (ovvero quello inizializato
    // nel costruttore del GameModel()
    public void setPlayers(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        // Se nel tuo GameModel gestisci anche le posizioni di spawn, 
        // puoi riposizionarli qui! (es: p1.setPosition(...))
    }
}