package pgsf.model;

import pgsf.utils.Point;

public interface IReadOnlyPlayer {
    Point getPosition();
    double getRenderOffsetX();
    double getRenderOffsetY();
    double getRenderScale();
    boolean isFacingRight();
    boolean isStunned();
    AnimState getCurrentAnimState();
    AnimData getCurrentAnimData();
    TextureAtlas getAtlas();
    String getAtlasPath();      // usato solo nel costruttore
    Hitbox getBoundingBox();
    int getHealth();
    int getMaxHealth();         // usato da GameView.initPlayers per l'HUD
    // Metodi aggiunti per permettere al Renderer di calcolare il Pivot Point
    double getWidth();
    double getHeight();
}