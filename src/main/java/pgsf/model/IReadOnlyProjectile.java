package pgsf.model;
import java.util.List;

import pgsf.utils.Point;

public interface IReadOnlyProjectile {
    Point getPosition();
    Hitbox getBoundingBox();
    boolean isMovingRight();
}
