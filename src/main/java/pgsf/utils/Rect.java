package pgsf.utils;

/**
 * Rettangolo immutabile (origine in alto a sinistra + dimensioni).
 * Sostituisce javafx.geometry.Rectangle2D nel Model, così che il Model
 * non dipenda da JavaFX. I nomi dei getter ricalcano quelli di Rectangle2D.
 */
public class Rect {
    private final double minX, minY, width, height;

    public Rect(double minX, double minY, double width, double height) {
        this.minX = minX;
        this.minY = minY;
        this.width = width;
        this.height = height;
    }

    public double getMinX() {
        return this.minX;
    }

    public double getMinY() {
        return this.minY;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }
}
