package application.Model;

import javafx.geometry.Point2D;

public class Projectile {
    private Point2D position;
    private boolean movingRight;
    private double speed;
    private double damage;
    private Hitbox boundingBox;
    private Player owner;
    private boolean active = true; // Stato del proiettile, se è ancora in gioco o no

    public Projectile(Point2D position, boolean movingRight, double speed, double damage, double width, double height, Player owner) {
        this.position = position;
        this.movingRight = movingRight;
        this.speed = speed;
        this.damage = damage;
        this.boundingBox = new Hitbox(position, width, height);
        this.owner = owner;
    }
    
    public Player getOwner() { return owner; }

    public Point2D getPosition() {return position; }

    public void update() {
        double deltaX = movingRight ? speed : -speed;
        position = position.add(deltaX, 0);
        boundingBox.updatePosition(position);
    }

    public Hitbox getBoundingBox() {
        return boundingBox;
    }

    public double getDamage() {
        return damage;
    }

    public void setActive(boolean b) {
        this.active = b;
    }

    public boolean isActive() {return active; } // Placeholder, implementa la logica per determinare se il proiettile è ancora attivo
}
