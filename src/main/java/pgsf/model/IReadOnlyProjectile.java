package pgsf.model;

import pgsf.utils.Point;

public interface IReadOnlyProjectile {
    Point getPosition();
    Hitbox getBoundingBox();
    boolean isMovingRight();
}
