/*
 * Classe dedicata ai giocatori, contiene le informazioni dei giocatori, come
 * la posizione (x, y), la salute, la velocità ecc...
 */
package application.Model;

import javafx.geometry.Point2D;
import java.util.HashMap;
import java.util.Map;

public class Player {
	private Point2D position;
    protected double speed;
    
    // Statistiche personaggio
    protected String atlasPath;
    protected int spriteCols;
    protected int spriteRows;
    protected double frameWidth;	// Larghezza del singolo frame sull'atlas
    protected double frameHeight;	// Altezza del singolo frame sull'atlas
    protected double renderScale;	// Moltiplicatore per la pixelart
    protected double width;
    protected double height;
    
    // --- Variabili per lo Scaling Dinamico ---
    protected double baseSpeed, baseGravity, baseJumpStrength, baseRenderScale;
    protected double basePunchWidth = 70.0;		// Sostituisce GameConfig
    protected double basePunchHeight = 30.0; 
    
    protected double punchWidth;
    protected double punchHeight;
    
    // --- Variabili per il Menu di Selezione ---
    protected String displayName;
    protected String pfpPath; // Profilo Picture Path
    
    // Variabili per la fisica
    private double velocityY = 0.0;
    private boolean isGrounded = false;
    
    // Variabili per il salto
    protected double gravity;
    protected double jumpStrength;	// Deve essere negativo perché la y aumenta dal basso verso l'alto
    
    // --- Meccanica di parata e stordimento ---
    protected long blockStartTime = 0;
    protected boolean wasDefending = false;
    protected long stunEndTime = 0;
    protected long parryStunDuration; 	// Quanto tempo rimane stordito il nemico se esegui un parry
    
    // L'hurtbox del player
    private Hitbox boundingBox;
    
    // --- Nuove variabili per il combattimento ---
    public int maxHealth;
    protected int health;
    private boolean isFacingRight = true; 
    private boolean isPunching = false;
    protected double punchDamage;
    private long punchStartTime = 0; // Memorizza il nanosecondo esatto in cui parte il pugno
    protected long punchDurationNs; // Quanto dura l'impatto del pugno
    private boolean isDefending = false;
    private boolean hasCheckedHit = false; // Memorizza se il gioco ha già calcolato il pugno o no
    
    // --- Gestione delle animazioni ---
    protected Map<AnimState, AnimData> animations = new HashMap<>();
    private AnimState currentAnimState = AnimState.IDLE_RIGHT; // Stato di default
    public boolean isMoving = false; // Ci servirà per capire se sta camminando
    // --- GESTIONE ANIMAZIONI AVANZATA ---
    // Placeholder per la durata stimata dell'animazione TURN (in nanosecondi)
    // Es: 3 frame a 50ms = 150ms = 150,000,000ns
    private final long TURN_DURATION_NS = 150_000_000L; 
    private long turnAnimStartTime = 0;
    private boolean isTurning = false;

    // Costruttore per impostare la posizione iniziale
    public Player(Point2D position) {
        this.position = position;
        
        // Inizializzo la hitbox a 0 per il momento
        this.boundingBox = new Hitbox(position, 0, 0);
    }
    
 // --- AGGIORNAMENTO TEMPO AZIONI ---
    public void updateTicks() {
        if (isPunching) {
            AnimData anim = getCurrentAnimData();
            if (anim != null) {
            	long elapsedNs = System.nanoTime() - punchStartTime;
                
                // 1. Quando finisce l'animazione visiva?
                long animDurationNs = anim.frameCount * anim.speedNs;
                
                // Il pugno finisce ESATTAMENTE quando scade il tempo totale dell'animazione
                if (elapsedNs >= animDurationNs) {
                    isPunching = false;
                    hasCheckedHit = false;
                }
            }
        }
    }

    // --- LOGICA AZIONI ---
    public void startPunch() {
        if (!isPunching && !isDefending) {
            isPunching = true;
            hasCheckedHit = false;	
            this.punchStartTime = System.nanoTime(); // Registra il momento esatto!
        }
    }
    
    // Ritorna TRUE solo se l'animazione si trova nella "finestra di impatto" (Active Frames)
    public boolean isPunchActive() {
        if (!isPunching) return false;
        
        AnimData anim = getCurrentAnimData();
        if (anim == null) return false;
        
        long elapsedNs = System.nanoTime() - punchStartTime;
        
        // 1. IL TUO RITARDO: (numeroFrame - 1) * tempoPerFrame
        long ritardoNs = (anim.frameCount - 1) * anim.speedNs; 
        
        // 2. L'hitbox è attiva solo DOPO il ritardo, per la durata che decidi tu!
        return elapsedNs >= ritardoNs;
    }

    public void setDefending(boolean defending) {
        // Se ha appena iniziato a parare in questo frame
        if (defending && !this.wasDefending) {
            this.blockStartTime = System.currentTimeMillis();
        }
        this.isDefending = defending;
        this.wasDefending = defending;
    }
    
    public long getBlockStartTime() { 
        return blockStartTime; 
    }
    
    public void stun(long durationMs) { 
        this.stunEndTime = System.currentTimeMillis() + durationMs; 
    }

    public boolean isStunned() { 
        return System.currentTimeMillis() < stunEndTime; 
    }
    
    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }
    
    // IL MOVIMENTO ORIZZONTALE (Sostituisce LEFT e RIGHT)
    public void moveHorizontal(PlayerState DIR) {
    	// --- Blocco azione: movimento bloccato se si attacca o difende ---
        if (isPunching || isDefending) return;
        
        this.isMoving = true;
        
        double newX = position.getX();
        
        if (DIR == PlayerState.LEFT) {
            newX -= speed;
            setFacingRight(false);
        } else if (DIR == PlayerState.RIGHT) {
            newX += speed;
            setFacingRight(true);
        }
        
        position = new Point2D(newX, position.getY());
        boundingBox.updatePosition(position);
    }

    // 3. IL SALTO (La vera spinta verso l'alto)
    public void jump() {
    	// --- Blocco azione: movimento bloccato se si attacca o difende ---
        if (isPunching || isDefending) return;
        
        // Può saltare solo se non è già in aria
        if (isGrounded) {
            velocityY = jumpStrength;
            isGrounded = false;
        }
    }
    
    public void applyPhysics(double groundLevelY, boolean isJumpHeld) {
        // Se il giocatore sta andando verso l'alto (velocityY negativo) 
        // MA ha rilasciato il tasto del salto...
        if (velocityY < 0 && !isJumpHeld) {
            // ...applichiamo una gravità "pesante" per fargli tagliare il salto (scendere in fretta)
            // più è alto il numero e più il taglio è brusco
            velocityY += gravity * 2.5; 
        } else {
            // Gravità normale (quando scende o quando tiene premuto)
            velocityY += gravity; 
        }
        
        double newX = position.getX();
        double newY = position.getY() + velocityY;
        
        // Controlliamo se ha toccato il pavimento
        if (newY + this.height >= groundLevelY) {
            newY = groundLevelY - this.height; 
            velocityY = 0.0;     
            isGrounded = true;
        } else {
            isGrounded = false;  
        }
        
        position = new Point2D(newX, newY);
        boundingBox.updatePosition(position);
    }
    
    // Cervello delle animazioni
    public void updateAnimationState() {
        // Se il giocatore è stordito, NON cambiamo l'animazione.
        // Rimarrà bloccato nell'ultimo stato (es. se stava tirando un pugno, rimarrà col braccio teso)
        if (isStunned()) {
            return; 
        }
        
        long now = System.nanoTime();

        // --- 1. PRIORITÀ MASSIMA: ANIMAZIONE TURN (UNA TANTUM) ---
        if (isTurning) {
            // Controlliamo se è passato abbastanza tempo dall'inizio della svolta
            if (now - turnAnimStartTime < TURN_DURATION_NS) {
                currentAnimState = AnimState.TURN;
                return; // Blocchiamo qui la logica per questo frame
            } else {
                // Tempo scaduto, l'animazione TURN è finita
                isTurning = false; 
            }
        }

        // --- 2. LOGICA AZIONI ---
        if (isPunching) {
            currentAnimState = isFacingRight ? AnimState.PUNCH_RIGHT : AnimState.PUNCH_LEFT;
        } 
        else if (isDefending) {
            // Usiamo i nuovi stati BLOCK specifici
            currentAnimState = isFacingRight ? AnimState.BLOCK_RIGHT : AnimState.BLOCK_LEFT;
        } 
        else if (!isGrounded) {
            currentAnimState = isFacingRight ? AnimState.JUMP_RIGHT : AnimState.JUMP_LEFT;
        } 
        // --- 3. LOGICA MOVIMENTO Orizzontale ---
        else if (isMoving) {
            currentAnimState = isFacingRight ? AnimState.WALK_RIGHT : AnimState.WALK_LEFT;
        } 
        // --- 4. LOGICA IDLE (Fermo) ---
        else {
        	// Appena si ferma, scatta subito l'idle completo!
            currentAnimState = isFacingRight ? AnimState.IDLE_RIGHT : AnimState.IDLE_LEFT;
        }
    }
    
    // Restituisce i dati dell'animazione di parata 
    // per permettere al Model di calcolarne la durata e i frame senza hardcodare nulla
    public application.Model.AnimData getBlockAnimData() {
        return animations.get(isFacingRight() ? AnimState.BLOCK_RIGHT : AnimState.BLOCK_LEFT);
    }
    
    // ==========================================
    // SISTEMA DI SCALING DINAMICO UNIVERSALE
    // ==========================================
    
    // 1. Salva i valori "originali" scritti nella classe (es. RedTurnip)
    public void saveBaseStats() {
        this.baseSpeed = this.speed;
        this.baseGravity = this.gravity;
        this.baseJumpStrength = this.jumpStrength;
        this.baseRenderScale = this.renderScale;
        
        // Inizializza i valori correnti
        this.punchWidth = this.basePunchWidth;
        this.punchHeight = this.basePunchHeight;
    }
    
    // 2. Moltiplica tutti i valori per la grandezza dello schermo!
    public void updateDynamicScale(double windowScale) {
        this.speed = this.baseSpeed * windowScale;
        this.gravity = this.baseGravity * windowScale;
        this.jumpStrength = this.baseJumpStrength * windowScale;
        this.renderScale = this.baseRenderScale * windowScale;

        // Ricalcolo Hitbox del corpo
        this.width = this.frameWidth * this.renderScale;
        this.height = this.frameHeight * this.renderScale;
        this.boundingBox.updateSize(this.width, this.height);
        
        // Ricalcolo Hitbox degli attacchi
        this.punchWidth = this.basePunchWidth * windowScale;
        this.punchHeight = this.basePunchHeight * windowScale;
    }
    
    public AnimState getCurrentAnimState() { return currentAnimState; }
    public AnimData getCurrentAnimData() { return animations.get(currentAnimState); }
    
    public Hitbox getBoundingBox() { return boundingBox; }
    public void setPosition(Point2D newPosition) { position = newPosition; }
    public Point2D getPosition() { return position; }
    
    public int getMaxHealth() { return maxHealth; }
    public int getHealth() { return health; }
    public boolean isFacingRight() { return isFacingRight; }
    // ==========================================
    // GESTIONE DIREZIONE E ANIMAZIONE "TURN"
    // ==========================================
    public void setFacingRight(boolean facingRight) {
    	// --- Blocco azione: movimento bloccato se si attacca o difende ---
        if (isPunching || isDefending) return;
        
        // 2. Se la direzione sta CAMBIANDO e siamo a terra, attiviamo l'animazione TURN
        if (this.isFacingRight != facingRight && isGrounded) {
            this.isTurning = true;
            this.turnAnimStartTime = System.nanoTime();
        }
        
        // 3. Aggiorniamo la variabile effettiva
        this.isFacingRight = facingRight; 
    }
    public double getPunchDamage() { return punchDamage; }
    public double getPunchWidth() { return punchWidth; }
    public double getPunchHeight() { return punchHeight; }
    public boolean isPunching() { return isPunching; }
    public boolean isDefending() { return isDefending; }
    public boolean hasCheckedHit() { return hasCheckedHit; }
    public void setHasCheckedHit(boolean dealt) { this.hasCheckedHit = dealt; }
    
    public String getAtlasPath() {return atlasPath;}
    public int getSpriteCols() {return spriteCols;}
    public int getSpriteRows() {return spriteRows;}
    public double getFrameWidth() {return frameWidth;}
    public double getFrameHeight() {return frameHeight;}
    public double getRenderScale() {return renderScale;}
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public String getDisplayName() { return displayName; }
    public String getPfpPath() { return pfpPath; }
    public long getParryStunDuration() { return parryStunDuration; }
}