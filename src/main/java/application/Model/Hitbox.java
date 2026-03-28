/*
 * Una classe che rappresenta l'hitbox dei giocatori.
 * Ogni giocatore ne avrà due: l'"Hurtbox" dove viene colpito e l'"Hitbox"
 * che compare solo durante l'attacco, serve per capire dove è stato sferrato
 * l'attacco
 */
package application.Model;

import application.Utils.GameConfig;
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
        return (this.x < other.x + GameConfig.pWidth &&
                this.x + GameConfig.pWidth > other.x &&
                this.y < other.y + GameConfig.pHeight &&
                this.y + GameConfig.pHeight > other.y);
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}