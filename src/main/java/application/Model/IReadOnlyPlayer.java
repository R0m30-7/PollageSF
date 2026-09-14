package application.Model;

import javafx.geometry.Point2D;

public interface IReadOnlyPlayer {
    Point2D getPosition();
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
}