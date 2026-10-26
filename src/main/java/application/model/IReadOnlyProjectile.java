package application.model;
import java.util.List;

import application.utils.Point;

public interface IReadOnlyProjectile {
    Point getPosition();
    Hitbox getBoundingBox();
    boolean isMovingRight();
}
