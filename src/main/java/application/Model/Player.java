/*
 * Classe dedicata ai giocatori, contiene le informazioni dei giocatori, come
 * la posizione (x, y), la salute, la velocità ecc...
 */
package application.Model;

import application.Utils.GameConfig;
import javafx.geometry.Point2D;

public class Player {
	private Point2D position;
    protected double speed;
    
    // Statistiche personaggio
    protected String atlasPath;
    protected int spriteCols;
    protected int spriteRows;
    protected double frameWidth;	// Larghezza del singolo frame sull'atlas
    protected double frameHeight;	// Altezza del singolo frame sull'atlas
    protected int renderScale;		// Moltiplicatore per la pixelart
    protected double width;
    protected double height;
    
    // Variabili per la fisica
    private double velocityY = 0.0;
    private boolean isGrounded = false;
    
    // Variabili per il salto
    protected double gravity;
    protected double jumpStrength;	// Deve essere negativo perché la y aumenta dal basso verso l'alto
    
    // L'hurtbox del player
    private Hitbox boundingBox;
    
    // --- Nuove variabili per il combattimento ---
    public static int maxHealth;
    protected static int health;
    private boolean isFacingRight = true; 
    private boolean isPunching = false;
    private int punchTimer = 0;
    private boolean isDefending = false;
    private boolean hasDealtDamage = false; // Memorizza se il pugno ha già fatto danno

    // Costruttore per impostare la posizione iniziale
    public Player(Point2D position) {
        this.position = position;
        
        // Inizializzo la hitbox a 0 per il momento
        this.boundingBox = new Hitbox(position, 0, 0);
    }
    
    // --- AGGIORNAMENTO TICK PER LE AZIONI ---
    public void updateTicks() {
        if (isPunching) {
            punchTimer--;
            if (punchTimer <= 0) {
                isPunching = false;
                hasDealtDamage = false;	// Reset alla fine del pugno
            }
        }
    }

    // --- LOGICA AZIONI ---
    public void startPunch() {
        if (!isPunching && !isDefending) {
            isPunching = true;
            hasDealtDamage = false;	// Appena il pugno inizia si azzera il danno
            punchTimer = GameConfig.pPunchDurationTicks;
            //System.out.println("PLAYER: isPunching è ora TRUE! Timer impostato a: " + punchTimer);
        }
    }

    public void setDefending(boolean defending) {
        if (!isPunching) {
            this.isDefending = defending;
        } else {
            this.isDefending = false; 
        }
    }
    
    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }
    
    // IL MOVIMENTO ORIZZONTALE (Sostituisce LEFT e RIGHT)
    public void moveHorizontal(PlayerState DIR) {
        double newX = position.getX();
        
        if (DIR == PlayerState.LEFT) {
            newX -= speed;
            isFacingRight = false;
        } else if (DIR == PlayerState.RIGHT) {
            newX += speed;
            isFacingRight = true;
        }
        
        position = new Point2D(newX, position.getY());
        boundingBox.updatePosition(position);
    }

    // 3. IL SALTO (La vera spinta verso l'alto)
    public void jump() {
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
    
    public Hitbox getBoundingBox() { return boundingBox; }
    public void setPosition(Point2D newPosition) { position = newPosition; }
    public Point2D getPosition() { return position; }
    
    public int getHealth() { return health; }
    public boolean isFacingRight() { return isFacingRight; }
    public void setFacingRight(boolean facingRight) { this.isFacingRight = facingRight; }
    public boolean isPunching() { return isPunching; }
    public boolean isDefending() { return isDefending; }
    public boolean hasDealtDamage() { return hasDealtDamage; }
    public void setHasDealtDamage(boolean dealt) { this.hasDealtDamage = dealt; }
    
    public String getAtlasPath() {return atlasPath;}
    public int getSpriteCols() {return spriteCols;}
    public int getSpriteRows() {return spriteRows;}
    public double getFrameWidth() {return frameWidth;}
    public double getFrameHeight() {return frameHeight;}
    public int getRenderScale() {return renderScale;}
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}