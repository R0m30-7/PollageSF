/*
 * Una classe che riceve come oggetto un Player. Controlla il suo
 * PlayerState e decide cosa disegnare.
 */
package application.View;

import application.Model.Player;
import application.Utils.GameConfig;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PlayerRenderer {
    private Rectangle node;

    // Quando creiamo un renderer, scegliamo di che colore sarà
    public PlayerRenderer(Color color) {
        node = new Rectangle(GameConfig.pWidth, GameConfig.pHeight, color);
    }

    public Rectangle getNode() { return node; }

    public void render(Player player) {
        node.setX(player.getPosition().getX());
        node.setY(player.getPosition().getY());
    }
}