package application.View;

import application.Model.IReadOnlyProjectile;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ProjectileRenderer {
    // Nessun Pane: usiamo direttamente la forma (o ImageView quando avrai lo sprite)
    private Rectangle visualNode;

    public ProjectileRenderer(IReadOnlyProjectile projectile) {
        this.visualNode = new Rectangle(
            projectile.getBoundingBox().getWidth(),
            projectile.getBoundingBox().getHeight(),
            Color.CYAN
        );
    }

    public Node getNode() {
        return visualNode;
    }

    public void render(IReadOnlyProjectile projectile, double camX) {
        // Applichiamo posizione assoluta e offset telecamera direttamente sul nodo
        visualNode.setLayoutX(Math.round(projectile.getPosition().getX() - camX));
        visualNode.setLayoutY(Math.round(projectile.getPosition().getY()));
        
        // Aggiorna dimensioni se lo scaling della finestra cambia
        visualNode.setWidth(projectile.getBoundingBox().getWidth());
        visualNode.setHeight(projectile.getBoundingBox().getHeight());
    }
}