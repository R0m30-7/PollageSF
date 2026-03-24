package application.View;

import application.Model.Player;
import application.Utils.GameConfig; // Assicurati di avere questo import per le costanti di dimensione
import javafx.scene.layout.StackPane; // Importiamo StackPane per impilare forme e testo
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font; // Importiamo i font
import javafx.scene.text.FontWeight; // Importiamo i pesi dei font
import javafx.scene.text.Text; // Importiamo il nodo di testo

public class PlayerRenderer {
    // Il nostro nodo grafico ora è uno StackPane
    private StackPane node;

    public PlayerRenderer(String playerNumberStr) {
        // --- 1. IL CONTENITORE PRINCIPALE ---
        // StackPane centra automaticamente i suoi figli uno sopra l'altro
        this.node = new StackPane();
        
        // Impostiamo la dimensione preferita del contenitore per matchare le costanti del gioco
        this.node.setPrefSize(GameConfig.pWidth, GameConfig.pHeight);
        
        // --- 2. IL BORDO E L'INTERNO (BOUNDING BOX) ---
        // Creiamo un singolo rettangolo che fa da entrambi
        Rectangle playerShape = new Rectangle(GameConfig.pWidth, GameConfig.pHeight);
        
        // L'interno grigio (che rappresenta la bounding box)
        playerShape.setFill(Color.GRAY);
        
        // Il bordo nero (che rappresenta il giocatore)
        playerShape.setStroke(Color.BLACK);
        playerShape.setStrokeWidth(3.0); // Spessore del bordo visibile

        // --- 3. IL NUMERO DEL GIOCATORE ---
        // Creiamo il nodo di testo con il numero fornito
        Text playerNumberText = new Text(playerNumberStr);
        
        // Configuriamo il font: lo rendiamo grande, grassetto e nero
        playerNumberText.setFont(Font.font("Arial", FontWeight.BOLD, 40.0));
        playerNumberText.setFill(Color.BLACK);
        
        // --- 4. ASSEMBLIAMO IL GIOCATORE ---
        // Aggiungiamo i figli allo StackPane nell'ordine di "disegno": 
        // prima il quadrato, poi il testo sopra!
        this.node.getChildren().addAll(playerShape, playerNumberText);
    }

    // Restituisce il nodo grafico principale (lo StackPane)
    public StackPane getNode() {
        return node;
    }

    // Aggiorna la posizione dell'intero container sullo schermo
    public void render(Player player) {
        // Ora muoviamo lo StackPane, non il singolo rettangolo
        node.setLayoutX(player.getPosition().getX());
        node.setLayoutY(player.getPosition().getY());
    }
}