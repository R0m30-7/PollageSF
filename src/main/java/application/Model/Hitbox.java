/*
 * Una classe che rappresenta l'hitbox dei giocatori.
 * Ogni giocatore ne avrà due: l'"Hurtbox" dove viene colpito e l'"Hitbox"
 * che compare solo durante l'attacco, serve per capire dove è stato sferrato
 * l'attacco
 */
package application.Model;

import javafx.geometry.Point2D;

public class Hitbox {
    private double x, y;
    private double width, height;	// Dimensioni per le hitbox dinamiche

    public Hitbox(Point2D position, double width, double height) {
        this.x = position.getX();
        this.y = position.getY();
        this.width = width;
        this.height = height;
    }
    
    public void updateSize(double width, double height) {
    	this.width = width;
        this.height = height;
    }

    // Aggiorna la posizione della hitbox per farla "seguire" il giocatore
    public void updatePosition(Point2D position) {
        this.x = position.getX();
        this.y = position.getY();
    }

    // IL CUORE DELLE COLLISIONI (La formula AABB)
    public boolean intersects(Hitbox other) {
    	// Controllo asse X: Il mio lato destro supera il suo sinistro E il mio sinistro non supera il suo destro?
        boolean xOverlap = (this.x + this.width >= other.x) && 
                           (this.x <= other.x + other.getWidth());
        
        // Controllo asse Y: Il mio lato basso supera il suo alto E il mio alto non supera il suo basso?
        boolean yOverlap = (this.y + this.height >= other.y) && 
                           (this.y <= other.y + other.getHeight());
                           
        // C'è collisione SOLO se si sovrappongono su entrambi gli assi contemporaneamente
        return xOverlap && yOverlap;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}