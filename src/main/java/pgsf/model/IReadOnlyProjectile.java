package pgsf.model;

import pgsf.utils.Point;

public interface IReadOnlyProjectile {
    Point getPosition();
    Hitbox getBoundingBox();
    boolean isMovingRight();

    // --- Dati per la grafica (letti dalla View) ---
    AnimData getAnimData();     // Riga/colonna/frame dell'animazione in volo
    TextureAtlas getAtlas();    // Atlas del giocatore che ha sparato
    String getAtlasPath();      // Immagine (skin) del giocatore che ha sparato
    double getRenderScale();    // Scala della pixelart, la stessa del giocatore
}
