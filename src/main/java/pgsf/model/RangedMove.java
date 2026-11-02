package pgsf.model;

import pgsf.utils.Point;

public class RangedMove extends Move {
    private AnimState animState;
    private double damage;
    private double projectileSpeed;
    private long durationNs;
    private boolean hasFired = false; // Impedisce di sparare più volte nello stesso attacco

    public RangedMove(Player user, AnimState animState, double damage, double projectileSpeed) {
        super(user);
        this.animState = animState;
        this.damage = damage;
        this.projectileSpeed = projectileSpeed;

        // Calcoliamo la durata totale in base ai dati dell'animazione del player
        AnimData anim = user.getCurrentAnimData();

        if (anim != null) {
            this.durationNs = (long) anim.frameCount * anim.speedNs;
        } else {
            this.durationNs = 500_000_000L; // Fallback di sicurezza (0.5 secondi)
        }
    }

    @Override
    public boolean isFinished() {
        return (System.nanoTime() - startTimeNs) >= durationNs;
    }

    @Override
    public Hitbox getActiveHitbox() {
        // Le mosse ranged non hanno una hitbox fisica sul corpo del giocatore, 
        // il danno è delegato interamente al Proiettile.
        return null;
    }

    @Override
    public double getDamage() {
        return damage;
    }

    @Override
    public AnimState getAnimState() {
        return animState;
    }

    // Metodo chiamato dal GameModel per generare il proiettile nel momento esatto
    public Projectile createProjectile() {
        if (!hasFired) {
            hasFired = true; // Si attiva una sola volta per mossa

            // La hitbox si adatta alla grafica come per i giocatori: primo frame
            // dell'animazione del proiettile per la scala di render, ristretta al 70%
            double projW = 40, projH = 30; // Fallback se manca l'animazione
            AnimData projAnim = user.getProjectileAnimData();
            if (projAnim != null) {
                javafx.geometry.Rectangle2D frame = user.getAtlas().getFrame(projAnim.row, projAnim.startCol, 0);
                projW = frame.getWidth() * user.getRenderScale() * 0.70;
                projH = frame.getHeight() * user.getRenderScale() * 0.70;
            }

            // Posizione di spawn davanti alle mani/faccia del player
            double spawnX = user.isFacingRight()
                ? user.getPosition().getX() + user.getWidth()
                : user.getPosition().getX() - projW;
            // Centro verticale del proiettile all'altezza delle mani (35% dell'altezza del player)
            double spawnY = user.getPosition().getY() + (user.getHeight() * 0.35) - (projH / 2.0);

            // Creiamo il proiettile passandogli anche il proprietario (user) così sapremo chi ha sparato
            return new Projectile(new Point(spawnX, spawnY), user.isFacingRight(), projectileSpeed, damage, projW, projH, user);
        }
        return null;
    }
}