package application.Model;

import javafx.geometry.Point2D;

public class MeleeMove extends Move {
    private AnimState animState;
    private double damage;
    private double width;
    private double height;

    public MeleeMove(Player user, AnimState animState, double damage, double width, double height) {
        super(user);
        this.animState = animState;
        this.damage = damage;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean isFinished() {
        AnimData anim = user.animations.get(animState);
        long elapsedNs = System.nanoTime() - startTimeNs;
        return elapsedNs >= (anim.frameCount * anim.speedNs);
    }

    @Override
    public Hitbox getActiveHitbox() {
        if (hasHit) return null; // Se ha già colpito, non genera più la hitbox!

        AnimData anim = user.animations.get(animState);
        long elapsedNs = System.nanoTime() - startTimeNs;
        long ritardoNs = (anim.frameCount - 1) * anim.speedNs;//-1 perche l'ultima animazione colpisce

        // Se siamo nei frame "attivi" dell'animazione
        if (elapsedNs >= ritardoNs) {
            // Qui generiamo l'hitbox basandoci sulle misure passate
            double x = user.isFacingRight() 
                ? user.getPosition().getX() + user.getWidth() 
                : user.getPosition().getX() - this.width;
                
            double y = user.getPosition().getY() + (user.getHeight() * 0.2);
            
            return new Hitbox(new Point2D(x, y), this.width, this.height);
        }
        return null; // È ancora in fase di caricamento (Startup)
    }

    @Override public double getDamage() { return damage; }
    @Override public AnimState getAnimState() { return animState; }
}