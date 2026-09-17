package application.Model;

public abstract class RangedMove extends Move {
    private AnimState animState;
    protected boolean hasFired = false; // Impedisce di sparare multipli proiettili nello stesso attacco

    public RangedMove(Player user, AnimState animState) {
        super(user);
        this.animState = animState;
    }

    @Override
    public Hitbox getActiveHitbox() {
        // Le mosse a distanza di solito non hanno una hitbox fissa sul corpo del giocatore,
        // il danno è gestito dal Proiettile (Projectile) che viene generato nel mondo.
        
        return null;
    }

    // Metodo specifico che il GameModel chiamerà per creare il proiettile
    public Projectile createProjectile() {
        return null;
    }

    public boolean hasFired() {
        return hasFired;
    }

    public void setHasFired(boolean hasFired) {
        this.hasFired = hasFired;
    }
}