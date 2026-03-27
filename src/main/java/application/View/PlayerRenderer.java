package application.View;

import application.Model.Player;
import application.Utils.GameConfig; // Assicurati di avere questo import per le costanti di dimensione
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane; // Importiamo StackPane per impilare forme e testo
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font; // Importiamo i font
import javafx.scene.text.FontWeight; // Importiamo i pesi dei font
import javafx.scene.text.Text; // Importiamo il nodo di testo

public class PlayerRenderer {
	// Contenitore libero (Pane) che terrà il corpo, il pugno, la difesa e la barra HP
    private Pane rootNode;
    // Il tuo fantastico contenitore per il numero centrato sul corpo
    private StackPane bodyContainer;
    
    // Forme per le azioni
    private Rectangle punchVisual;
    private Rectangle defenseVisual;

    public PlayerRenderer(String playerNumberStr) {
    	rootNode = new Pane(); 
        
        // --- 1. IL CORPO (Usiamo il tuo codice originale per lo StackPane) ---
        bodyContainer = new StackPane();
        bodyContainer.setPrefSize(GameConfig.pWidth, GameConfig.pHeight);
        
        Rectangle playerShape = new Rectangle(GameConfig.pWidth, GameConfig.pHeight);
        playerShape.setFill(Color.GRAY);
        playerShape.setStroke(Color.BLACK);
        playerShape.setStrokeWidth(3.0); 

        Text playerNumberText = new Text(playerNumberStr);
        playerNumberText.setFont(Font.font("Arial", FontWeight.BOLD, 40.0));
        playerNumberText.setFill(Color.BLACK);
        
        bodyContainer.getChildren().addAll(playerShape, playerNumberText);

        // --- 2. IL PUGNO (Giallo) ---
        punchVisual = new Rectangle(GameConfig.pPunchWidth, GameConfig.pPunchHeight, Color.YELLOW);
        punchVisual.setStroke(Color.ORANGE);
        punchVisual.setVisible(false);

        // --- 3. LA DIFESA (Azzurro semitrasparente) ---
        defenseVisual = new Rectangle(GameConfig.pDefenseWidth, GameConfig.pDefenseHeight, Color.LIGHTBLUE);
        defenseVisual.setOpacity(0.6);
        defenseVisual.setStroke(Color.BLUE);
        defenseVisual.setVisible(false);

        // Aggiungiamo tutto al rootNode
        rootNode.getChildren().addAll(bodyContainer, punchVisual, defenseVisual);
    }

    // Restituisce il nodo grafico principale (lo StackPane)
    public Pane getNode() {
        return rootNode;
    }

    // Aggiorna la posizione dell'intero container sullo schermo
    public void render(Player player) {
        double px = player.getPosition().getX();
        double py = player.getPosition().getY();
        
        // 1. Muovi il contenitore del corpo
        bodyContainer.setLayoutX(px);
        bodyContainer.setLayoutY(py);

        // 3. Gestione visiva Pugno
        if (player.isPunching()) {
        	//System.out.println("RENDERER: Sto disegnando il pugno");
            punchVisual.setVisible(true);
            punchVisual.toFront();
            
            double punchY = py + (GameConfig.pHeight * 0.2); // Altezza faccia/spalle
            double punchX;
            
            if (player.isFacingRight()) {
            	punchX = px + GameConfig.pWidth;
            } else {
            	punchX = px - GameConfig.pPunchWidth;
            }
            
            punchVisual.setX(punchX);
            punchVisual.setY(punchY);
        } else {
            punchVisual.setVisible(false);
        }
    
	    // 4. Gestione visiva Difesa
	    if (player.isDefending()) {
	        defenseVisual.setVisible(true);
	        double defenseY = py + (GameConfig.pHeight - GameConfig.pDefenseHeight) / 2.0; // Centrata
	        
	        if (player.isFacingRight()) {
	            defenseVisual.setX(px + (GameConfig.pWidth * 0.8)); // Spostata verso destra
	        } else {
	            defenseVisual.setX(px - GameConfig.pDefenseWidth + (GameConfig.pWidth * 0.2)); // Verso sinistra
	        }
	        defenseVisual.setY(defenseY);
	    } else {
	        defenseVisual.setVisible(false);
	    }
    }
}