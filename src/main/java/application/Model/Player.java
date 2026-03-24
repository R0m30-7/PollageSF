/*
 * Classe dedicata ai giocatori, contiene le informazioni dei giocatori, come
 * la posizione (x, y), la salute, la velocità ecc...
 */
package application.Model;

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
    
    private Hitbox boundingBox;

    // Costruttore per impostare la posizione iniziale
    public Player(Point2D position) {
        this.position = position;
        
        this.boundingBox = new Hitbox(position);
    }
    
    // IL MOVIMENTO ORIZZONTALE (Sostituisce LEFT e RIGHT)
    public void moveHorizontal(PlayerState DIR) {
        double newX = position.getX();
        
        if (DIR == PlayerState.LEFT) {
            newX -= SPEED;
        } else if (DIR == PlayerState.RIGHT) {
            newX += SPEED;
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
        if (newY >= groundLevelY) {
            newY = groundLevelY; 
            velocityY = 0.0;     
            isGrounded = true;   
        } else {
            isGrounded = false;  
        }
        
        position = new Point2D(newX, newY);
        boundingBox.updatePosition(position);
    }
    
    //TODO In teoria questo metodo si dovrebbe poter eliminare
    public void keepInBounds(double x, double y) {
    	double currentX = position.getX();
        double currentY = position.getY();
        double playerSize = 50.0; // Sostituisci con la vera larghezza/altezza del tuo quadrato

        // Controllo asse X (Sinistra e Destra)
        if (currentX < 0) {
            currentX = 0; // Muro sinistro
        } else if (currentX > x - playerSize) {
            currentX = x - playerSize; // Muro destro (tiene conto della larghezza!)
        }

        // Controllo asse Y (Alto e Basso)
        if (currentY < 0) {
            currentY = 0; // Tetto
        } else if (currentY > y - (playerSize* 3 / 2)) {
            currentY = y - (playerSize* 3 / 2); // Pavimento
        }

        // Aggiorniamo la posizione corretta e la hitbox
        position = new Point2D(currentX, currentY);
        boundingBox.updatePosition(position);
    }
    
    public Hitbox getBoundingBox() {
    	return boundingBox;
    }
    
    public void setPosition(Point2D newPosition) {
    	position = newPosition;
    }
    
    public Point2D getPosition() {
    	return position;
    }
}