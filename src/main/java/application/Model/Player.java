package application.Model;

import javafx.geometry.Point2D;
import java.util.HashMap;
import java.util.Map;

public class Player implements IReadOnlyPlayer{
	private Point2D position;
    protected double speed;
    
    // Statistiche personaggio
    protected String atlasPath;
   
    protected TextureAtlas atlas;
    protected double baseLogicalWidth;  // Larghezza fissa per la fisica
    protected double baseLogicalHeight; // Altezza fissa per la fisica

    protected double renderOffsetX; // Offset per centrare lo sprite rispetto alla hitbox
    protected double renderOffsetY; // Offset per centrare lo sprite rispetto alla hitbox

    protected double renderScale = 1.0;	// Moltiplicatore per la pixelart
    protected double menuRenderScaleMultiplier = 1.0;   // Moltiplicatore dello scaling quando si renderizza la texture nel menu di scelta
    protected double width;
    protected double height;
    
    // --- Variabili per lo Scaling Dinamico ---
    protected double baseSpeed, baseGravity, baseJumpStrength, baseRenderScale = 1.0;
    protected double basePunchWidth = 70.0;		// Sostituisce GameConfig
    protected double basePunchHeight = 30.0; 

    protected double baseKickWidth = 70.0;
    protected double baseKickHeight = 30.0;

    protected double kickWidth;
    protected double kickHeight;
    protected double kickDamage;
    
    protected double punchDamage;
    protected double punchWidth;
    protected double punchHeight;
    
    // --- Variabili per il Menu di Selezione ---
    protected String displayName;
    protected String pfpPath; // Profilo Picture Path
    protected boolean inMenuMode = false;
    
    // Variabili per la fisica
    private double velocityY = 0.0;
    private boolean isGrounded = false;
    private boolean isHurt = false; // Variabile per lo stato di "colpito"
    private long hurtStartTime = 0;
    
    // Variabili per il salto
    protected double gravity;
    protected double jumpStrength;	// Deve essere negativo perché la y aumenta dal basso verso l'alto
    
    // Variabili per il crouch
    protected double normalHeight; // Ti memorizzi l'altezza standard
    
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
    private boolean isDefending = false;
    protected boolean isCrouching = false;

    // --- Variabili per i colori delle skin ---
    protected java.util.List<String> availableSkins = new java.util.ArrayList<>();
    protected int currentSkinIndex = 0;
    
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

    private Move activeMove = null;

    // --- NUOVO AGGIORNAMENTO TEMPO AZIONI ---
    public void updateTicks() {
        // Chiediamo semplicemente alla mossa se ha finito i suoi frame.
        // Se ha finito, la distruggiamo e il giocatore torna libero!
        if (activeMove != null && activeMove.isFinished()) {
            activeMove = null;
        }
    }
    // Lancia l'attacco se possibile
    public void executeMove(Move move) {
        if (this.activeMove == null && !isDefending && !isStunned()) {
            this.activeMove = move;
            this.isMoving = false; // Ferma il giocatore se stava camminando
            }
    }

    public Move getActiveMove() { return activeMove; }

    public void setCrouching(boolean crouching) {
        if (activeMove != null || isDefending) return;        
        // Se lo stato cambia
        if (this.isCrouching != crouching) {
            this.isCrouching = crouching;
            
            // Salviamo l'altezza normale la prima volta
            if (normalHeight == 0) normalHeight = this.height;
            
            double targetHeight = crouching ? normalHeight * 0.6 : normalHeight; // Es: crouch è il 60% dell'altezza
            double heightDifference = normalHeight - targetHeight;
            
            if(!isTurning){
                if (crouching) {
                    // Si abbassa: abbassiamo la Y del pavimento aggiungendo la differenza, 
                    // così la testa scende e i piedi restano piantati a terra!
                    position = new Point2D(position.getX(), position.getY() + heightDifference);
                } else {
                    // Si alza: restituiamo lo spazio sottratto alla Y
                    position = new Point2D(position.getX(), position.getY() - heightDifference);
                }
            }
            
            // Aggiorniamo la dimensione della Hitbox fisica
            this.boundingBox.updateSize(this.width, targetHeight);
            this.boundingBox.updatePosition(position);

            //aggiorniamo l'altezza del player per la grafica
            this.height = targetHeight;
        }
        
        if (crouching) {
            this.isMoving = false;
        }
    }

    public boolean isCrouching() {return isCrouching;}

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
        if (activeMove != null || isDefending) return;
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
        if (activeMove != null || isDefending) return;
        // Può saltare solo se non è già in aria
        if (isGrounded) {
            velocityY = jumpStrength;
            isGrounded = false;
        }
    }

    public void setHurt(boolean hurt) {
        this.isHurt = hurt;
        if (hurt) {
            this.hurtStartTime = System.nanoTime();
            this.activeMove = null; // Interrompe eventuali attacchi in corso
            this.isMoving = false;
        }
    }

    public boolean isHurt() {
        return isHurt;
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

        if (isHurt) {
            AnimState hurtState = null;
            if(isCrouching) {
                hurtState = isFacingRight ? AnimState.HURT_CROUCH_RIGHT : AnimState.HURT_CROUCH_LEFT;
            } else {
                hurtState = isFacingRight ? AnimState.HURT_RIGHT : AnimState.HURT_LEFT;
            }
            
            AnimData hurtAnim = animations.get(hurtState);
            if (hurtAnim != null) {
                long elapsedNs = now - hurtStartTime;
                long totalDurationNs = hurtAnim.frameCount * hurtAnim.speedNs;
                
                if (elapsedNs < totalDurationNs) {
                    currentAnimState = hurtState;
                    return; // Rimane bloccato nello stato Hurt finché l'animazione non finisce
                } else {
                    isHurt = false; // Tempo scaduto, lo stato si spegne da solo!
                }
            } else {
                isHurt = false; // Sicurezza se manca l'animazione nella map
            }
        }


        // --- PRIORITÀ MASSIMA: ANIMAZIONE TURN (UNA TANTUM) ---
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

        // --- LOGICA AZIONI ---
        if (activeMove != null) {
            // Non ci importa se è un pugno, calcio o Hadouken. 
            // La mossa sa già quale stato AnimState restituire!
            currentAnimState = activeMove.getAnimState();
        }
        else if (isDefending) {
            if(isCrouching) {
                currentAnimState = isFacingRight ? AnimState.CROUCH_BLOCK_RIGHT : AnimState.CROUCH_BLOCK_LEFT;
            } else {
                currentAnimState = isFacingRight ? AnimState.BLOCK_RIGHT : AnimState.BLOCK_LEFT;
            }
        }
        else if (isCrouching) { // <-- NUOVA PRIORITÀ
            currentAnimState = isFacingRight ? AnimState.CROUCH_RIGHT : AnimState.CROUCH_LEFT;
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
        	if (inMenuMode) {
                // Se siamo nel menu, si usa l'animazione IDLE del menu
                currentAnimState = AnimState.MENU_IDLE;
            } else {
                // Se siamo in gioco, usa l'idle standard
                currentAnimState = isFacingRight ? AnimState.IDLE_RIGHT : AnimState.IDLE_LEFT;
            }
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

        this.baseLogicalWidth = this.width;
        this.baseLogicalHeight = this.height;
        
        // Inizializza i valori correnti
        this.punchWidth = this.basePunchWidth;
        this.punchHeight = this.basePunchHeight;

        this.kickHeight = this.baseKickHeight;
        this.kickWidth = this.baseKickWidth;

    }
    
    // 2. Moltiplica tutti i valori per la grandezza dello schermo!
    public void updateDynamicScale(double windowScale) {
        this.speed = this.baseSpeed * windowScale;
        this.gravity = this.baseGravity * windowScale;
        this.jumpStrength = this.baseJumpStrength * windowScale;
        this.renderScale = this.baseRenderScale * windowScale;

        this.width = this.baseLogicalWidth * windowScale;
        this.height = this.baseLogicalHeight * windowScale;
        this.boundingBox.updateSize(this.width, this.height);
        
        // Ricalcolo Hitbox degli attacchi
        this.punchWidth = this.basePunchWidth * windowScale;
        this.punchHeight = this.basePunchHeight * windowScale;
        this.kickWidth = this.baseKickWidth * windowScale;
        this.kickHeight = this.baseKickHeight * windowScale;
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
        if (activeMove != null || isDefending) return;
        // 2. Se la direzione sta CAMBIANDO e siamo a terra, attiviamo l'animazione TURN
        if (this.isFacingRight != facingRight && isGrounded) {
            this.isTurning = true;
            this.turnAnimStartTime = System.nanoTime();
        }
        
        // 3. Aggiorniamo la variabile effettiva
        this.isFacingRight = facingRight; 
    }
    public double getPunchDamage() { return punchDamage; }
    public double getKickDamage() { return kickDamage;} // Se il calcio è attivo, restituisce il danno del pugno
    public double getKickWidth() { return kickWidth; }
    public double getKickHeight() { return kickHeight; }

    public double getPunchWidth() { // il pungo a sinistra, definito come il pugno piu lungo ha bisogno di una hitbox piu lunga
        if(!isFacingRight && activeMove != null)
            return punchWidth*1.5;
        return punchWidth;
     }
    public double getPunchHeight() { return punchHeight; }
    public boolean isDefending() { return isDefending; }

    public TextureAtlas getAtlas() { return this.atlas; }
    public String getAtlasPath() {return atlasPath;}

    public double getRenderScale() {
        if(inMenuMode){
            return renderScale * menuRenderScaleMultiplier;
        }
        return renderScale;
    }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public String getDisplayName() { return displayName; }
    public String getPfpPath() { return pfpPath; }
    public long getParryStunDuration() { return parryStunDuration; }
    public boolean isGrounded() { return isGrounded; }
    // Forza lo stato di "Tocca Terra" per l'animazione IDLE nel menu
    public void setGrounded(boolean grounded) { this.isGrounded = grounded; }
    public void setInMenuMode(boolean inMenu) { this.inMenuMode = inMenu; }
    public double getRenderOffsetX() { return renderOffsetX; }
    public double getRenderOffsetY() { return renderOffsetY; }
    public double getHookDamage() {return punchDamage;} // Se il gancio è attivo, restituisce il danno del pugno
    public double getHookWidth() {return punchWidth;} // Se il gancio è attivo, restituisce la larghezza del pugno
    public double getHookHeight() {return punchHeight;} // Se il gancio è attivo, restituisce l'altezza del pugno

    // Metodi per la gestione dei colori delle skin
    public java.util.List<String> getAvailableSkins() { 
        return availableSkins; 
    }
    
    public void setSkinIndex(int index) {
        if (!availableSkins.isEmpty()) {
            this.currentSkinIndex = index % availableSkins.size();
            // Aggiorna il percorso dell'immagine con la skin scelta
            this.atlasPath = availableSkins.get(this.currentSkinIndex);
        }
    }
    public int getSkinIndex() { return currentSkinIndex; }
}