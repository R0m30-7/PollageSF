package application.Model;

public abstract class Move {

    protected Player user;
    protected long startTimeNs;
    protected boolean hasHit = false; // La "sicura" per non colpire due volte

    public Move(Player user) {
        this.user = user;
        this.startTimeNs = System.nanoTime();
    }

    // Il GameModel o il Player chiameranno questo per sapere se l'attacco è finito
    public abstract boolean isFinished();

    // Il GameModel chiamerà questo in handleCombat per farsi dare l'area di danno
    public abstract Hitbox getActiveHitbox();

    public abstract double getDamage();
    
    public abstract AnimState getAnimState();
    
    public boolean hasHit() { return hasHit; }
    public void setHasHit(boolean hasHit) { this.hasHit = hasHit; }
    
    
}
