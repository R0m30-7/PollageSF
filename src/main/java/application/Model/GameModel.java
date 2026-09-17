package application.Model;

import application.Controller.CharacterFactory;
import application.Controller.InputManager;
import application.Utils.GameConfig;
import javafx.geometry.Point2D;

public class GameModel implements IReadOnlyGameModel{
    private Player player1;     // Cervello dei player
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

    private boolean wasP1HookHeld = false;
    private boolean wasP2HookHeld = false;

    // Variabili per memorizzare il calcio nel frame precedente
    private boolean wasP1KickHeld = false;
    private boolean wasP2KickHeld = false;

    
    // Memorizzazione dimensioni per l'aggiornamento in tempo reale della finestra
    private double currentWindowWidth = 1920;
    private double currentWindowHeight = 1080;
    
    // Definizione della posizione del pavimento
    private double GROUND_LEVEL;
    private double currentGroundRatio = 0.9;    //! Va spiegato meglio
    private double currentGroundLevel;
    
    // Variabili per la gestione della fine del gioco
    private boolean isGameOver = false;
    private int winner = 0;
    
    // Il costruttore richiede larghezza e altezza dello sfondo per il calcolo dei bordi
    public GameModel(double bgWidth, double bgHeight) {
    	// Imposto la larghezza del mondo come quella dell'immagine
    	this.worldWidth = bgWidth;
    	// Imposto il pavimento della scena
    	this.GROUND_LEVEL = currentWindowHeight - 100;      //! Anche questo non cambia un cazzo
    	
    	// Calcoliamo la y per spawnare i giocatori con i piedi per terra
    	double spawnY = this.GROUND_LEVEL - GameConfig.pHeight;
    	
        // Spawn dei giocatori al centro del mondo (scelgo di base la classe Ryu)
        // Necessario per assegnare a un gamepad un giocatore spcifico
    	player1 = new Ryu(new Point2D(worldWidth / 2 - 200, spawnY)); 
        player2 = new Ryu(new Point2D(worldWidth / 2 + 200, spawnY));
        
        // Impostiamo le direzioni iniziali
        player1.setFacingRight(true); 
        player2.setFacingRight(false);
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public double getCameraX() { return cameraX; }	// Serve alla View

    public void update(InputManager input) {
    	// Aggiornamento tick per la durata dei pugni (se l'animazione è finita, resetta lo stato)
    	player1.updateTicks();
    	player2.updateTicks();
    	
        // Leggiamo lo stato dei tasti X di entrambi i giocatori
        boolean isP1JumpHeld = input.isJumpButtonPressed(1);
        boolean isP2JumpHeld = input.isJumpButtonPressed(2);
        
        // Leggiamo lo stato dei pugni
        boolean isP1PunchHeld = input.isPunchButtonPressed(1);
        boolean isP2PunchHeld = input.isPunchButtonPressed(2);

        boolean isP1KickHeld = input.isKickButtonPressed(1);
        boolean isP2KickHeld = input.isKickButtonPressed(2);

        boolean isP1HookHeld = input.isHookButtonPressed(1);
        boolean isP2HookHeld = input.isHookButtonPressed(2);

        // Applichiamo la fisica passando lo stato del tasto (salto)
        player1.applyPhysics(this.currentGroundLevel, isP1JumpHeld);
        player2.applyPhysics(this.currentGroundLevel, isP2JumpHeld);
        
        // Movimento (con limiti dell'arena)
        // --- GIOCATORE 1 ---
        if (!player1.isStunned()) {
            double p1X = input.getLeftStickX(1);
            double p1Y = input.getLeftStickY(1);

            // Se la levetta è spinta decisamente verso il basso, attiva il crouch
            if (p1Y > 0.5 && player1.isGrounded()) {
                player1.setCrouching(true);
            } else {
                player1.setCrouching(false);
            }

            AnimState state;
            if (!player1.isCrouching()) {

                if (Math.abs(p1X) > 0.0) {
                    player1.moveHorizontal(p1X > 0 ? PlayerState.RIGHT : PlayerState.LEFT);
                }
                if (isP1JumpHeld && !wasP1JumpHeld) player1.jump();

                if (isP1HookHeld && !wasP1HookHeld) {
                    state = player1.isFacingRight() ? AnimState.HOOK_RIGHT : AnimState.HOOK_LEFT;
                    player1.executeMove(new MeleeMove(player1, state, player1.getHookDamage(), player1.getHookWidth(), player1.getHookHeight()));
                } else if (isP1PunchHeld && !wasP1PunchHeld) {
                    state = player1.isFacingRight() ? AnimState.PUNCH_RIGHT : AnimState.PUNCH_LEFT;
                    player1.executeMove(new MeleeMove(player1, state, player1.getPunchDamage(), player1.getPunchWidth(), player1.getPunchHeight()));
                }
                if (isP1KickHeld && !wasP1KickHeld) {
                    state = player1.isFacingRight() ? AnimState.KICK_RIGHT : AnimState.KICK_LEFT;
                    player1.executeMove(new MeleeMove(player1, state, player1.getKickDamage(), player1.getKickWidth(), player1.getKickHeight()));
                }
                player1.setDefending(input.isDefendButtonPressed(1));

            }else {
            // --- GIOCATORE ACCOVACCIATO (CROUCHING) ---
            // Controllo per il pugno basso
                if (isP1PunchHeld && !wasP1PunchHeld) {
                    state = player1.isFacingRight() ? AnimState.PUNCH_CROUCH_RIGHT : AnimState.PUNCH_CROUCH_LEFT;
                    player1.executeMove(new MeleeMove(player1, state, player1.getPunchDamage(), player1.getPunchWidth(), player1.getPunchHeight()));
                }
                
                // Permettere la parata bassa
                player1.setDefending(input.isDefendButtonPressed(1));
            }
        } else {
            // Se è stordito, non può difendersi e resta fermo
            player1.setDefending(false);
            //! Aggiungere animazione stordito
        }

        // --- GIOCATORE 2 ---
        if (!player2.isStunned()) {
            double p2X = input.getLeftStickX(2);
            double p2Y = input.getLeftStickY(2);

            if (p2Y > .5 && player2.isGrounded()){
                player2.setCrouching(true);
            }else{
                player2.setCrouching(false);
            }
            
            if(!player2.isCrouching()){
                if (Math.abs(p2X) > 0.0) {
                    player2.moveHorizontal(p2X > 0 ? PlayerState.RIGHT : PlayerState.LEFT);
                }
                if (isP2JumpHeld && !wasP2JumpHeld) player2.jump();

                if (isP2HookHeld && !wasP2HookHeld) {
                    AnimState state = player2.isFacingRight() ? AnimState.HOOK_RIGHT : AnimState.HOOK_LEFT;
                    player2.executeMove(new MeleeMove(player2, state, player2.getHookDamage(), player2.getHookWidth(), player2.getHookHeight()));
                }else if (isP2PunchHeld && !wasP2PunchHeld) {
                    AnimState state = player2.isFacingRight() ? AnimState.PUNCH_RIGHT : AnimState.PUNCH_LEFT;
                    player2.executeMove(new MeleeMove(player2, state, player2.getPunchDamage(), player2.getPunchWidth(), player2.getPunchHeight()));
                }
                if (isP2KickHeld && !wasP2KickHeld) {
                    AnimState state = player2.isFacingRight() ? AnimState.KICK_RIGHT : AnimState.KICK_LEFT;
                    player2.executeMove(new MeleeMove(player2, state, player2.getKickDamage(), player2.getKickWidth(), player2.getKickHeight()));
                }
                player2.setDefending(input.isDefendButtonPressed(2));
            }
            else {

            // --- GIOCATORE ACCOVACCIATO (CROUCHING) ---
            // Controllo per il pugno basso
                if (isP2PunchHeld && !wasP2PunchHeld) {
                    AnimState state = player2.isFacingRight() ? AnimState.PUNCH_CROUCH_RIGHT : AnimState.PUNCH_CROUCH_LEFT;
                    player2.executeMove(new MeleeMove(player2, state, player2.getPunchDamage(), player2.getPunchWidth(), player2.getPunchHeight()));
                }
                
                // Permettere la parata bassa
                player2.setDefending(input.isDefendButtonPressed(2));
            }

        }else {
            // Se è stordito, non può difendersi e resta fermo
            player2.setDefending(false);
        }
        
        // --- LOGICA DI COMBATTIMENTO ---
        handleCombat(player1, player2);
        handleCombat(player2, player1);

        // LIMITI DEL MONDO (Muri invisibili)
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
        //           LIMITI DELLO SCHERMO
        // ==========================================
        // Ora che la telecamera si è mossa, chiudiamo i giocatori dentro la finestra visibile
        keepPlayerOnScreen(player1);
        keepPlayerOnScreen(player2);
        
        // Aggiornamento dello stato attuale per il salto
        wasP1JumpHeld = isP1JumpHeld;
        wasP2JumpHeld = isP2JumpHeld;
        
        wasP1PunchHeld = isP1PunchHeld;
        wasP2PunchHeld = isP2PunchHeld;
        
        wasP1KickHeld = isP1KickHeld;
        wasP2KickHeld = isP2KickHeld;

        wasP1HookHeld = isP1HookHeld;
        wasP2HookHeld = isP2HookHeld;
        
        // ==========================================
        //         CONTROLLO FINE PARTITA
        // ==========================================
        if (!isGameOver) {
            if (player1.getHealth() <= 0) {
                isGameOver = true;
                winner = 2; // Vince P2
            } else if (player2.getHealth() <= 0) {
                isGameOver = true;
                winner = 1; // Vince P1
            }
        }
    }
    // ==========================================
    //      IL MOTORE DEI DANNI E COLLISIONI
    // ==========================================
    private void handleCombat(Player attacker, Player defender) {
        if (attacker == defender) return;   // Controllo di sicurezza

        // Verifichiamo se il giocatore ha una mossa in esecuzione
        Move currentAttack = attacker.getActiveMove();

        if (currentAttack != null) {
            // Chiediamo alla mossa di fornirci la sua Hitbox in questo preciso frame.
            // La mossa restituirà "null" se si sta ancora caricando (Startup) o se ha già colpito
            Hitbox attackHitbox = currentAttack.getActiveHitbox();

            // Se la Hitbox esiste e tocca l'avversario... colpito
            if (attackHitbox != null && attackHitbox.intersects(defender.getBoundingBox())) {
                // Mettiamo sibuto la sicura alla mossa per non fare doppi danni
                currentAttack.setHasHit(true); 
                
                // Il danno viene letto direttamente dall'oggetto Move (player di conseguenza)
                double baseDamage = currentAttack.getDamage();

                // ========================================================
                //        LOGICA DI DIFESA E PARRY
                // ========================================================
                boolean isAttackerOnRight = attacker.getPosition().getX() > defender.getPosition().getX();
                boolean isFacingAttacker = (isAttackerOnRight && defender.isFacingRight()) || (!isAttackerOnRight && !defender.isFacingRight());

                if (defender.isDefending() && isFacingAttacker) {
                    // Percentuale di parata in base al tempo
                    long blockDuration = System.currentTimeMillis() - defender.getBlockStartTime();
                    
                    // Otteniamo dinamicamente i dati dell'animazione di parata del difensore!
                    AnimData blockData = defender.getBlockAnimData(); 
                    long timePerFrameMs = blockData.speedNs / 1_000_000L; 
                    int totalFrames = blockData.frameCount;
                    
                    // Calcoliamo in quale "frame teorico" della parata si trova il difensore
                    int currentBlockFrame = (int) (blockDuration / timePerFrameMs);

                    // Parry perfetto
                    if (currentBlockFrame >= totalFrames - 1) {
                        System.out.println("⭐ PARRY PERFETTO di " + defender.getDisplayName() + "!");
                        attacker.stun(defender.getParryStunDuration());
                        
                    } else {
                        // Parry parziale
                        double blockPercentage = (double) (currentBlockFrame + 1) / totalFrames;
                        double damageMultiplier = 1.0 - blockPercentage;
                        
                        double finalDamage = baseDamage * damageMultiplier;
                        
                        System.out.println("🛡️ Parata parziale (frame " + (currentBlockFrame + 1) + "/" + totalFrames + "). Danno subito: " + finalDamage);
                        defender.takeDamage((int) finalDamage);
                    }
                } else {
                    // Preso in pieno o di spalle
                    System.out.println("💥 Colpito in pieno. Danno: " + baseDamage);
                    defender.takeDamage((int) baseDamage);
                }
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
    public void updateWindowSize(double newWidth, double newHeight, double newWorldWidth,double scale) {
        this.currentWindowWidth = newWidth;
        this.currentWindowHeight = newHeight;
        
        // --- SCALING ---
        // Ipotizziamo che tu abbia bilanciato il gioco su uno schermo 1080p.
        player1.updateDynamicScale(scale);
        player2.updateDynamicScale(scale);
        // ------------------------------
        
        // Ricalcolo del pavimento in tempo reale
        this.currentGroundLevel = this.currentWindowHeight * this.currentGroundRatio;   //! Non fa niente???
        
        // Il mondo di gioco si allarga e restringe in base allo zoom dello sfondo
        this.worldWidth = newWorldWidth;

        /*  Non serve perché la finestra non si può ridimensionare al di fuori delle impostazioni
        // Sistema di sicurezza: se il giocatore rimpicciolisce la finestra di scatto,
        // i personaggi potrebbero trovarsi "sotto" al pavimento. Li tiriamo su.
        if (player1.getPosition().getY() + player1.getHeight() > this.currentGroundLevel) {
            player1.setPosition(new javafx.geometry.Point2D(player1.getPosition().getX(), this.currentGroundLevel - player1.getHeight()));
            player1.getBoundingBox().updatePosition(player1.getPosition());
        }
        if (player2.getPosition().getY() + player2.getHeight() > this.currentGroundLevel) {
            player2.setPosition(new javafx.geometry.Point2D(player2.getPosition().getX(), this.currentGroundLevel - player2.getHeight()));
            player2.getBoundingBox().updatePosition(player2.getPosition());
        }
        */
    }
    
    public void setGroundLevelRatio(double ratio) {     //! Bisogna capire a che serve
    	this.currentGroundRatio = ratio;
        // Calcola subito il pavimento in pixel moltiplicando l'altezza per la percentuale
        this.currentGroundLevel = this.currentWindowHeight * this.currentGroundRatio;
    }
    
    // Metodo universale per lo spawn. Con "skin" si intende il colore 
    public void spawnPlayers(CharacterFactory f1, CharacterFactory f2, int skin1, int skin2) {
        // Le coordinate sono decise solo qui
        // Possiamo usare delle proporzioni rispetto alla larghezza del mondo (worldWidth)
        double spawnX1 = worldWidth * 0.2; 
        double spawnX2 = worldWidth * 0.8;
        double spawnY = 0; // Verranno poi appoggiati al suolo da applyPhysics o setGroundLevel

        this.player1 = f1.create(new Point2D(spawnX1, spawnY));
        this.player1.setSkinIndex(skin1);
        this.player2 = f2.create(new Point2D(spawnX2, spawnY));
        this.player2.setSkinIndex(skin2);

        this.player2.setFacingRight(false); // Il P2 guarda sempre a sinistra all'inizio
    }
    
    // --- INIEZIONE DEI GIOCATORI SELEZIONATI ---
    // Questo serve perché altrimenti, anche se il giocatore scegliesse il personaggio dalla mappa dei
    // personaggi, vedrebbe comunque che il suo giocatore risulta Turnip (o quello inizializato
    // nel costruttore del GameModel())
    public void setPlayers(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        // Se nel GameModel gestisci anche le posizioni di spawn, 
        // puoi riposizionarli qui (es: p1.setPosition(...))
    }
    
    public boolean getIsGameOver() { return isGameOver; }
    public int getWinner() { return winner; }
}