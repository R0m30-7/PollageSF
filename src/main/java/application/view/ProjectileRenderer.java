package application.view;

import application.model.IReadOnlyProjectile;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ProjectileRenderer {
    // Nessun Pane: usiamo direttamente la forma (o ImageView quando avrai lo sprite)
    private Rectangle visualNode;
    private Image atlasImage;
    private ImageView spriteView;
    public ProjectileRenderer(IReadOnlyProjectile projectile) {
        this.visualNode = new Rectangle(
            projectile.getBoundingBox().getWidth(),
            projectile.getBoundingBox().getHeight(),
            Color.CYAN
        );
        try {
            atlasImage = new Image(getClass().getResourceAsStream(projectile.getAtlasPath()));
            
            spriteView = new ImageView(atlasImage);

            spriteView.setSmooth(false); // Non vogliamo che lo sprite sia sfuocato
        } catch (Exception e) {
            System.out.println("Errore caricamento atlas: " + projectile.getAtlasPath());
            System.exit(1);
        }
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