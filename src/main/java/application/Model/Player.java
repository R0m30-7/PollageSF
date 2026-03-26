/*
 * Classe dedicata ai giocatori, contiene le informazioni dei giocatori, come
 * la posizione (x, y), la salute, la velocità ecc...
 */
package application.Model;

import application.Utils.GameConfig;
import javafx.geometry.Point2D;

public class Player {
	private Point2D position;
    private final double SPEED = 2.0;
    
    // Variabili per la fisica
    private double velocityY = 0.0;
    private boolean isGrounded = false;
    
    // Variabili per il salto
    private final double GRAVITY = 0.2;
    private final double JUMP_STRENGTH = -8.0;	// Negativo perché la y aumenta dal basso verso l'alto
    
    // L'hurtbox del player
    private Hitbox boundingBox;
    
    // --- Nuove variabili per il combattimento ---
    public static final int MAX_HEALTH = 100;
    private int health = MAX_HEALTH;
    private boolean isFacingRight = true; 
    private boolean isPunching = false;
    private int punchTimer = 0;
    private boolean isDefending = false;
    private boolean hasDealtDamage = false; // Memorizza se il pugno ha già fatto danno

    // Costruttore per impostare la posizione iniziale
    public Player(Point2D position) {
        this.position = position;
        
        this.boundingBox = new Hitbox(position, GameConfig.pWidth, GameConfig.pHeight);
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
            System.out.println("PLAYER: isPunching è ora TRUE! Timer impostato a: " + punchTimer);
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
            newX -= SPEED;
            isFacingRight = false;
        } else if (DIR == PlayerState.RIGHT) {
            newX += SPEED;
            isFacingRight = true;
        }
        
        position = new Point2D(newX, position.getY());
        boundingBox.updatePosition(position);
    }

    // 3. IL SALTO (La vera spinta verso l'alto)
    public void jump() {
        // Può saltare solo se non è già in aria
        if (isGrounded) {
            velocityY = JUMP_STRENGTH;
            isGrounded = false;
        }
    }
    
    public void applyPhysics(double groundLevelY, boolean isJumpHeld) {
        // Se il giocatore sta andando verso l'alto (velocityY negativo) 
        // MA ha rilasciato il tasto del salto...
        if (velocityY < 0 && !isJumpHeld) {
            // ...applichiamo una gravità "pesante" per fargli tagliare il salto (scendere in fretta)
            // più è alto il numero e più il taglio è brusco
            velocityY += GRAVITY * 2.5; 
        } else {
            // Gravità normale (quando scende o quando tiene premuto)
            velocityY += GRAVITY; 
        }
        
        double newX = position.getX();
        double newY = position.getY() + velocityY;
        
        // Controlliamo se ha toccato il pavimento
        if (newY + GameConfig.pHeight >= groundLevelY) {
            newY = groundLevelY - GameConfig.pHeight; 
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
}