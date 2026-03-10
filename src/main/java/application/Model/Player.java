/*
 * Classe dedicata ai giocatori, contiene le informazioni dei giocatori, come
 * la posizione (x, y), la salute, la velocità ecc...
 */
package application.Model;

import javafx.geometry.Point2D;

public class Player {
	private Point2D position;
    private final double SPEED = 1.5;
    
    private Hitbox boudingBox;

    // Costruttore per impostare la posizione iniziale
    public Player(Point2D position) {
        this.position = position;
        
        this.boudingBox = new Hitbox(position);
    }
    
    public void Move(PlayerState DIR) {
    	double newX, newY;
    	newX = position.getX();
    	newY = position.getY();
    	
    	if(DIR == PlayerState.UP) {
    		newX = position.getX();
        	newY = position.getY() - SPEED;
    	} else if(DIR == PlayerState.DOWN) {
    		newX = position.getX();
        	newY = position.getY() + SPEED;
    	} else if(DIR == PlayerState.LEFT) {
    		newX = position.getX() - SPEED;
    		newY = position.getY();
    	} else if(DIR == PlayerState.RIGHT) {
    		newX = position.getX() + SPEED;
        	newY = position.getY();
    	}
    	position = new Point2D(newX, newY);
    	boudingBox.updatePosition(position);
    }
    
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
        boudingBox.updatePosition(position);
    }
    
    public Hitbox getBoudingBox() {
    	return boudingBox;
    }
    
    public Point2D getPosition() {
    	return position;
    }
}