package application.model;

import java.util.ArrayList;
import java.util.List;

import application.controller.CharacterFactory;
import application.controller.InputManager;
import application.utils.GameConfig;
import application.utils.Point;

public class GameModel implements IReadOnlyGameModel{
    private Player player1;     // Cervello dei player
    private Player player2;
    
    // L'arena è larga il doppio dello schermo
    private double worldWidth = application.utils.Settings.getInstance().getWindowWidth() * 2;
    
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

    private boolean wasP1HadoukenHeld = false;
    private boolean wasP2HadoukenHeld = false;

    // Variabili per gestire la fine dell'animazione per lo spawn del proiettile
    private boolean p1BulletCreationDelay = false;
    private long p1StartProjectileCountdown = 0;

    private boolean p2BulletCreationDelay = false;
    private long p2StartProjectileCountdown = 0;
    
    // Memorizzazione dimensioni per l'aggiornamento in tempo reale della finestra
    private double currentWindowWidth = 1920;
    private double currentWindowHeight = 1080;
    
    // Definizione della posizione del pavimento
    private double GROUND_LEVEL;
    private double currentGroundRatio = 0.9;    // La posizione precisa del pavimento nelle mappe, e il ratio serve per tenerlo preciso in tutte le risoluzioni
    private double currentGroundLevel;  //! Non so cosa significhi il commento sopra, Paolo è l'autore
    
    // Variabili per la gestione della fine del gioco
    private boolean isGameOver = false;
    private int winner = 0;

    private List<Projectile> projectiles;
    
    
    // Il costruttore richiede larghezza e altezza dello sfondo per il calcolo dei bordi
    public GameModel(double bgWidth, double bgHeight) {
    	// Imposto la larghezza del mondo come quella dell'immagine
    	this.worldWidth = bgWidth;
    	// Imposto il pavimento della scena
    	this.GROUND_LEVEL = currentWindowHeight - 100;      // Cambiare il valore non serve a niente, perché viene ricalcolato al momento del caricamento della mappa
    	
    	// Calcoliamo la y per spawnare i giocatori con i piedi per terra
    	double spawnY = this.GROUND_LEVEL - GameConfig.pHeight;
    	
        // Spawn dei giocatori al centro del mondo (scelgo di base la classe Ryu)
        // Necessario per assegnare a un gamepad un giocatore spcifico
    	player1 = new Ryu(new Point(worldWidth / 2 - 200, spawnY)); 
        player2 = new Ryu(new Point(worldWidth / 2 + 200, spawnY));
        
        // Impostiamo le direzioni iniziali
        player1.setFacingRight(true); 
        player2.setFacingRight(false);
        projectiles = new ArrayList<>();
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public double getCameraX() { return cameraX; }	// Serve alla View

    public void update(InputManager input) {
    	// Aggiornamento tick per la durata dei pugni (se l'animazione è finita, resetta lo stato)
    	player1.updateTicks();
    	player2.updateTicks();

        updatePlayersInput(input);

        updateProjectiles();
        
        // --- LOGICA DI COMBATTIMENTO (attacker, defender) ---
        handleCombat(player1, player2);
        handleCombat(player2, player1);

        // LIMITI DEL MONDO (Muri invisibili)
        // Impediamo ai giocatori di uscire dall'arena totale (WORLD_WIDTH)
        keepPlayerInBounds(player1);
        keepPlayerInBounds(player2);

        // Controllo collisione tra i due giocatori
        collisionDetection(player1, player2);

        updateCamera();

        finalizePlayerFrame(player1);
        finalizePlayerFrame(player2);

        checkGameOver();
    }
    // ==========================================
    //      IL MOTORE DEI DANNI E COLLISIONI
    // ==========================================
    private void handleCombat(Player attacker, Player defender) {
        if (attacker == defender) return;

        Move currentAttack = attacker.getActiveMove();
        if (currentAttack == null) return;

        Hitbox attackHitbox = currentAttack.getActiveHitbox();
        if (attackHitbox != null && attackHitbox.intersects(defender.getBoundingBox())) {
            currentAttack.setHasHit(true);
            double baseDamage = currentAttack.getDamage();

            if (defender.isDefending() && isDefenderFacingThreat(defender, attacker.getPosition().getX())) {
                resolveMeleeBlockOrParry(attacker, defender, baseDamage);
            } else {
                System.out.println("Colpito, danno: " + baseDamage);
                defender.takeDamage((int) baseDamage);
                defender.setHurt(true);
            }
        }
    }

    // Gestisce il calcolo dei frame per distinguere tra Parry perfetto e parata parziale
    private void resolveMeleeBlockOrParry(Player attacker, Player defender, double baseDamage) {
        long blockDuration = System.currentTimeMillis() - defender.getBlockStartTime();

        AnimData blockData = defender.getBlockAnimData();
        long timePerFrameMs = blockData.speedNs / 1_000_000L;
        int totalFrames = blockData.frameCount;

        int currentBlockFrame = (int) (blockDuration / timePerFrameMs);

        if (currentBlockFrame >= totalFrames - 1) {
            System.out.println("PARRY PERFETTO di " + defender.getDisplayName() + "!");
            attacker.stun(defender.getParryStunDuration());
        } else {
            double blockPercentage = (double) (currentBlockFrame + 1) / totalFrames;
            double damageMultiplier = 1.0 - blockPercentage;
            double finalDamage = baseDamage * damageMultiplier;

            System.out.println("Parata parziale (frame " + (currentBlockFrame + 1) + "/" + totalFrames + "). Danno subito: " + finalDamage);
            defender.takeDamage((int) finalDamage);
        }
    }
    
    private void collisionDetection(Player p1, Player p2) {
        // Controllo collisione tra i due giocatori
        if (p1.getBoundingBox().intersects(p2.getBoundingBox())) {
        // Calcoliamo il punto centrale dei due giocatori per capire chi sta a destra e chi a sinistra
            double p1CenterX = p1.getPosition().getX() + (p1.getWidth() / 2.0);
            double p2CenterX = p2.getPosition().getX() + (p2.getWidth() / 2.0);
            
            // Profondità della sovrapposizione orizzontale
            double overlap = (p1.getWidth() / 2.0 + p2.getWidth() / 2.0) - Math.abs(p1CenterX - p2CenterX);
            
            if (overlap > 0) {
                // Spingiamo i giocatori verso l'esterno in egual misura (metà a testa)
                double pushDist = overlap / 2.0;
                
                if (p1CenterX < p2CenterX) {
                    p1.setPosition(new Point(p1.getPosition().getX() - pushDist, p1.getPosition().getY()));
                    p2.setPosition(new Point(p2.getPosition().getX() + pushDist, p2.getPosition().getY()));
                } else {
                    p1.setPosition(new Point(p1.getPosition().getX() + pushDist, p1.getPosition().getY()));
                    p2.setPosition(new Point(p2.getPosition().getX() - pushDist, p2.getPosition().getY()));
                }
                
                // Aggiorniamo le bounding box dopo lo spostamento
                p1.getBoundingBox().updatePosition(p1.getPosition());
                p2.getBoundingBox().updatePosition(p2.getPosition());
            }
        }
    }

    // Metodo di supporto per i muri invisibili dell'arena
    private void keepPlayerInBounds(Player p) {
        double currentX = p.getPosition().getX();
        // Controllo muro sinistro
        if (currentX < 0) {
            p.setPosition(new Point(0, p.getPosition().getY()));
        } 
        // Controllo muro destro (tenendo conto dello spessore del giocatore dalla sua bounding box)
        else if (currentX > worldWidth - p.getWidth()) {
            p.setPosition(new Point(worldWidth - p.getWidth(), p.getPosition().getY()));
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
            p.setPosition(new Point(leftScreenEdge, p.getPosition().getY()));
            p.getBoundingBox().updatePosition(p.getPosition());
        } 
        // Controllo se esce a DESTRA dello schermo
        else if (currentX > rightScreenEdge) {
            p.setPosition(new Point(rightScreenEdge, p.getPosition().getY()));
            p.getBoundingBox().updatePosition(p.getPosition());
        }
    }
    
    // Aggiornare larghezza, altezza e pavimento della finestra
    public void updateWindowSize(double newWidth, double newHeight, double newWorldWidth,double scale) {
        this.currentWindowWidth = newWidth;
        this.currentWindowHeight = newHeight;
        
        // --- SCALING ---
        // Bilanciamento del gioco fatto su uno schermo 1080p.
        player1.updateDynamicScale(scale);
        player2.updateDynamicScale(scale);
        // ------------------------------
        
        // Ricalcolo del pavimento in tempo reale
        this.currentGroundLevel = this.currentWindowHeight * this.currentGroundRatio;   // currentGroundRatio viene impostato a ogni cambio mappa
        
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
    
    // Imposta il Ratio, necessario per trovare il punto corretto quando si cambia dimensione (rispetto a 1080)
    public void setGroundLevelRatio(double ratio) {
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

        this.player1 = f1.create(new Point(spawnX1, spawnY));
        this.player1.setSkinIndex(skin1);
        this.player2 = f2.create(new Point(spawnX2, spawnY));
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
    @Override
    public List<? extends IReadOnlyProjectile> getProjectiles() {
            return projectiles;
    }
    
    public boolean getIsGameOver() { return isGameOver; }
    public int getWinner() { return winner; }

    // --- Metodi helper ---
    private void checkForProjectileSpawn(int playerNum) {
        // --- GESTIONE PROIETTILI PLAYER ---
        // bulletCreationDelay = true significa che non è il primo frame della mossa
        Player currentPlayer = (playerNum == 1) ? player1 : player2;
        boolean delayFlag = (playerNum == 1) ? p1BulletCreationDelay : p2BulletCreationDelay;
        long startCountdown = (playerNum == 1) ? p1StartProjectileCountdown : p2StartProjectileCountdown;

        if (!(currentPlayer.getActiveMove() instanceof RangedMove)) {
            if (playerNum == 1) {
                p1BulletCreationDelay = false;
            } else {
                p2BulletCreationDelay = false;
            }
            return;
        }

        // Se è il primo frame della RangedMove, avviamo il timer
        if (!delayFlag) {
            startCountdown = System.nanoTime();
            if (playerNum == 1) {
                p1BulletCreationDelay = true;
                p1StartProjectileCountdown = startCountdown;
            } else {
                p2BulletCreationDelay = true;
                p2StartProjectileCountdown = startCountdown;
            }
        }

        // 4 sta per il numero di frame mentre 1_000_000 serve per convertire da millisecondi a nanosecondi
        // Controlla se sono passati i 4 frame mentre la mossa è ancora attiva
        if (delayFlag && System.nanoTime() - startCountdown >= currentPlayer.projectileAnimFrameDuration * 4 * 1_000_000L) {
            if (playerNum == 1) {
                p1BulletCreationDelay = false;
            } else {
                p2BulletCreationDelay = false;
            }

            RangedMove ranged = (RangedMove) currentPlayer.getActiveMove();
            Projectile p = ranged.createProjectile(); // Restituisce il proiettile una sola volta grazie a hasFired
            if (p != null) {
                projectiles.add(p);
            }
        }
    }

    // Gestisce la lettura degli input, la fisica e l'aggiornamento dei flag per entrambi i giocatori
    private void updatePlayersInput(InputManager input) {
        // --- Lettura input attuali ---
        boolean isP1JumpHeld = input.isJumpButtonPressed(1);
        boolean isP2JumpHeld = input.isJumpButtonPressed(2);

        boolean isP1PunchHeld = input.isPunchButtonPressed(1);
        boolean isP2PunchHeld = input.isPunchButtonPressed(2);

        boolean isP1KickHeld = input.isKickButtonPressed(1);
        boolean isP2KickHeld = input.isKickButtonPressed(2);

        boolean isP1HookHeld = input.isHookButtonPressed(1);
        boolean isP2HookHeld = input.isHookButtonPressed(2);

        boolean isP1HadoukenHeld = input.isHadoukenButtonPressed(1);
        boolean isP2HadoukenHeld = input.isHadoukenButtonPressed(2);

        // --- Fisica del salto ---
        player1.applyPhysics(this.currentGroundLevel, isP1JumpHeld);
        player2.applyPhysics(this.currentGroundLevel, isP2JumpHeld);

        // --- Azioni Giocatore 1 (passiamo solo i trigger "appena premuto") ---
        handleSinglePlayerActions(
            player1, 1, input,
            isP1JumpHeld && !wasP1JumpHeld,
            isP1PunchHeld && !wasP1PunchHeld,
            isP1HookHeld && !wasP1HookHeld,
            isP1KickHeld && !wasP1KickHeld,
            isP1HadoukenHeld && !wasP1HadoukenHeld
        );

        // --- Azioni Giocatore 2 ---
        handleSinglePlayerActions(
            player2, 2, input,
            isP2JumpHeld && !wasP2JumpHeld,
            isP2PunchHeld && !wasP2PunchHeld,
            isP2HookHeld && !wasP2HookHeld,
            isP2KickHeld && !wasP2KickHeld,
            isP2HadoukenHeld && !wasP2HadoukenHeld
        );

        // --- Salvataggio stato input per il frame successivo ---
        wasP1JumpHeld = isP1JumpHeld;
        wasP2JumpHeld = isP2JumpHeld;
        wasP1PunchHeld = isP1PunchHeld;
        wasP2PunchHeld = isP2PunchHeld;
        wasP1KickHeld = isP1KickHeld;
        wasP2KickHeld = isP2KickHeld;
        wasP1HookHeld = isP1HookHeld;
        wasP2HookHeld = isP2HookHeld;
        wasP1HadoukenHeld = isP1HadoukenHeld;
        wasP2HadoukenHeld = isP2HadoukenHeld;
    }

    // Logica unificata per il singolo giocatore (vale sia per P1 che per P2)
    private void handleSinglePlayerActions(
            Player player, int playerNum, InputManager input,
            boolean jumpJustPressed, boolean punchJustPressed,
            boolean hookJustPressed, boolean kickJustPressed,
            boolean hadoukenJustPressed) {

        if (player.isStunned()) {
            player.setDefending(false);
            return;
        }

        double stickX = input.getLeftStickX(playerNum);
        double stickY = input.getLeftStickY(playerNum);

        // Attiva il crouch se la levetta è spinta verso il basso e il player è a terra
        player.setCrouching(stickY > 0.5 && player.isGrounded());

        if (!player.isCrouching()) {
            handleStandingActions(player, stickX, jumpJustPressed, punchJustPressed, hookJustPressed, kickJustPressed, hadoukenJustPressed);
        } else {
            handleCrouchingActions(player, punchJustPressed);
        }

        // La parata è permessa sia in piedi che da accovacciati
        player.setDefending(input.isDefendButtonPressed(playerNum));
    }

    // Mosse e movimento quando il giocatore è in piedi
    private void handleStandingActions(
            Player player, double stickX,
            boolean jumpJustPressed, boolean punchJustPressed,
            boolean hookJustPressed, boolean kickJustPressed,
            boolean hadoukenJustPressed) {

        if (Math.abs(stickX) > 0.0) {
            player.moveHorizontal(stickX > 0 ? PlayerState.RIGHT : PlayerState.LEFT);
        }
        if (jumpJustPressed) {
            player.jump();
        }
        if (hadoukenJustPressed) {
            AnimState state = player.isFacingRight() ? AnimState.HADOUKEN_RIGHT : AnimState.HADOUKEN_LEFT;
            player.executeMove(new RangedMove(player, state, player.getHadoukenDamage(), player.getHadoukenSpeed()));
        }
        if (hookJustPressed) {
            AnimState state = player.isFacingRight() ? AnimState.HOOK_RIGHT : AnimState.HOOK_LEFT;
            player.executeMove(new MeleeMove(player, state, player.getHookDamage(), player.getHookWidth(), player.getHookHeight()));
        } else if (punchJustPressed) {
            AnimState state = player.isFacingRight() ? AnimState.PUNCH_RIGHT : AnimState.PUNCH_LEFT;
            player.executeMove(new MeleeMove(player, state, player.getPunchDamage(), player.getPunchWidth(), player.getPunchHeight()));
        }
        if (kickJustPressed) {
            AnimState state = player.isFacingRight() ? AnimState.KICK_RIGHT : AnimState.KICK_LEFT;
            player.executeMove(new MeleeMove(player, state, player.getKickDamage(), player.getKickWidth(), player.getKickHeight()));
        }
    }

    // Mosse quando il giocatore è accovacciato
    private void handleCrouchingActions(Player player, boolean punchJustPressed) {
        if (punchJustPressed) {
            AnimState state = player.isFacingRight() ? AnimState.PUNCH_CROUCH_RIGHT : AnimState.PUNCH_CROUCH_LEFT;
            player.executeMove(new MeleeMove(player, state, player.getPunchDamage(), player.getPunchWidth(), player.getPunchHeight()));
        }
    }

    // Controlla spawn, movimento, collisioni e rimozione dei proiettili
    private void updateProjectiles() {
        checkForProjectileSpawn(1);
        checkForProjectileSpawn(2);

        for (Projectile p : projectiles) {
            p.update();

            // Controllo uscita dai muri
            if (p.getPosition().getX() < 0 || p.getPosition().getX() > worldWidth) {
                p.setActive(false);
                continue;
            }

            // Controllo collisione (se appartiene a P1 colpisce P2 e viceversa)
            Player target = (p.getOwner() == player1) ? player2 : player1;
            if (p.getBoundingBox().intersects(target.getBoundingBox())) {
                resolveProjectileHit(p, target);
            }
        }

        // Rimuove i proiettili non più attivi
        projectiles.removeIf(p -> !p.isActive());
    }

    // Gestisce l'applicazione del danno quando un proiettile colpisce un bersaglio
    private void resolveProjectileHit(Projectile p, Player target) {
        boolean isFacingProjectile = isDefenderFacingThreat(target, p.getPosition().getX());

        if (target.isDefending() && isFacingProjectile) {
            int halvedDamage = (int) (p.getDamage() * 0.5);
            target.takeDamage(halvedDamage);
            // Non attiviamo setHurt(true) per mantenere la posa di parata
        } else {
            target.takeDamage((int) p.getDamage());
            target.setHurt(true);
        }

        p.setActive(false); // Il proiettile si distrugge all'impatto
    }

    // Riutilizzabile sia per i proiettili sia per gli attacchi corpo a corpo
    private boolean isDefenderFacingThreat(Player defender, double threatX) {
        boolean isThreatOnRight = threatX > defender.getPosition().getX();
        return (isThreatOnRight && defender.isFacingRight()) 
            || (!isThreatOnRight && !defender.isFacingRight());
    }

    // Calcola la posizione della telecamera con Deadzone e interpolazione lineare (LERP)
    private void updateCamera() {
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
    }

    // Aggiorna le animazioni, resetta il flag di movimento e blocca il giocatore nello schermo visibile
    private void finalizePlayerFrame(Player p) {
        p.updateAnimationState();
        // Reset del movimento
        p.isMoving = false;
        keepPlayerOnScreen(p);
    }

    // Controlla se uno dei due giocatori ha esaurito la salute
    private void checkGameOver() {
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
}