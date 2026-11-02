package pgsf.model;

import pgsf.utils.Point;

public class Projectile implements IReadOnlyProjectile{
    private Point position;
    private boolean movingRight;
    private double speed;
    private double damage;
    private Hitbox boundingBox;
    private Player owner;
    private boolean active = true; // Stato del proiettile, se è ancora in gioco o no

    // --- Impatto ---
    // Dopo aver colpito, il proiettile si ferma e resta in gioco (senza più fare danno)
    // finché non finisce l'animazione d'impatto; poi diventa inattivo e viene rimosso
    private boolean impacting = false;
    private long impactStartNs = 0;

    public Projectile(Point position, boolean movingRight, double speed, double damage, double width, double height, Player owner) {
        this.position = position;
        this.movingRight = movingRight;
        this.speed = speed;
        this.damage = damage;
        this.boundingBox = new Hitbox(position, width, height);
        this.owner = owner;
    }
    
    public Player getOwner() { return owner; }

    public Point getPosition() {return position; }

    public void update() {
        if (impacting) {
            // Fermo sul punto d'impatto: si spegne quando l'animazione è finita
            AnimData impactAnim = owner.getProjectileImpactAnimData();
            long durationNs = (impactAnim != null) ? impactAnim.frameCount * impactAnim.speedNs : 0;
            if (System.nanoTime() - impactStartNs >= durationNs) {
                active = false;
            }
            return;
        }

        double deltaX = movingRight ? speed : -speed;
        position = position.add(deltaX, 0);
        boundingBox.updatePosition(position);
    }

    // Chiamato dal GameModel quando il proiettile colpisce (o viene parato)
    public void startImpact() {
        if (impacting) return;
        impacting = true;
        impactStartNs = System.nanoTime();
    }

    public boolean isImpacting() { return impacting; }

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

    @Override
    public boolean isMovingRight() { return movingRight; }

    // --- Dati grafici: il proiettile usa l'atlas e la skin di chi lo ha sparato ---
    @Override
    public AnimData getAnimData() {
        // In volo o in fase d'impatto: la View disegna semplicemente l'animazione corrente
        return impacting ? owner.getProjectileImpactAnimData() : owner.getProjectileAnimData();
    }

    @Override
    public TextureAtlas getAtlas() { return owner.getAtlas(); }

    @Override
    public String getAtlasPath() { return owner.getAtlasPath(); }

    @Override
    public double getRenderScale() { return owner.getRenderScale(); }
}
