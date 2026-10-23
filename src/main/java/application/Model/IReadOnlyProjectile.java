package application.Model;
import java.util.List;

import application.Utils.Point;

public interface IReadOnlyProjectile {
    Point getPosition();
    Hitbox getBoundingBox();
    boolean isMovingRight(); // Aggiungi questo getter in Projectile se ti serve flippare lo sprite
}
