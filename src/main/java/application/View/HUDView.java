/*
 * Si occupa di disegnare e aggiornare l'interfaccia, come la vita e il timer
 */
package application.View;

import application.Model.Player;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.TextAlignment;

public class HUDView {
	private Pane hudRoot;
	
	// HUD giocatore 1
	private Rectangle p1HealthBg, p1HealthFill;
	private Rectangle p1PowerBg, p1PowerFill;
	private Text p1Label;
	
	// HUD giocatore 2
	private Rectangle p2HealthBg, p2HealthFill;
	private Rectangle p2PowerBg, p2PowerFill;
	private Text p2Label;
	
	// Timer
	private Text timerLabel;
	
	// Costanti per le dimensioni
	private static final double BAR_WIDTH = 400.0;
	private static final double HEALTH_HEIGHT = 35.0;
	private static final double POWER_HEIGHT = 15.0;
	private static final double TOP_MARGIN = 30.0;
	private static final double SIDE_MARGIN = 50.0;
	
	public HUDView() {
		hudRoot = new Pane();
		DropShadow shadow = new DropShadow(5, Color.BLACK);	// Effetto ombra per rendere testi e barre leggibili
		
		// ============== GIOCATORE 1 ==============
		p1Label = new Text("Giocatore 1");
		p1Label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		p1Label.setFill(Color.WHITE);
		p1Label.setEffect(shadow);
        p1Label.setX(SIDE_MARGIN);
        p1Label.setY(TOP_MARGIN);
        
        // Vita P1
        p1HealthBg = new Rectangle(BAR_WIDTH, HEALTH_HEIGHT, Color.DARKRED);
        p1HealthBg.setX(SIDE_MARGIN);
        p1HealthBg.setY(TOP_MARGIN + 10);
        p1HealthBg.setStroke(Color.BLACK);
        p1HealthBg.setStrokeWidth(2);

        p1HealthFill = new Rectangle(BAR_WIDTH, HEALTH_HEIGHT, Color.LIMEGREEN);
        p1HealthFill.setX(SIDE_MARGIN);
        p1HealthFill.setY(TOP_MARGIN + 10);

        // Potere Speciale P1 (Azzurra)
        p1PowerBg = new Rectangle(BAR_WIDTH * 0.7, POWER_HEIGHT, Color.DARKBLUE);
        p1PowerBg.setX(SIDE_MARGIN);
        p1PowerBg.setY(TOP_MARGIN + 10 + HEALTH_HEIGHT + 5);
        p1PowerBg.setStroke(Color.BLACK);

        p1PowerFill = new Rectangle(BAR_WIDTH * 0.7, POWER_HEIGHT, Color.CYAN);
        p1PowerFill.setX(SIDE_MARGIN);
        p1PowerFill.setY(TOP_MARGIN + 10 + HEALTH_HEIGHT + 5);
        
        // ============== GIOCATORE 2 ==============
        // Le coordinate X del P2 verranno impostate dinamicamente in base alla larghezza dello schermo!
        p2Label = new Text("Giocatore 2");
        p2Label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        p2Label.setFill(Color.WHITE);
        p2Label.setEffect(shadow);
        p2Label.setY(TOP_MARGIN);

        p2HealthBg = new Rectangle(BAR_WIDTH, HEALTH_HEIGHT, Color.DARKRED);
        p2HealthBg.setStroke(Color.BLACK);
        p2HealthBg.setStrokeWidth(2);
        p2HealthBg.setY(TOP_MARGIN + 10);
        
        p2HealthFill = new Rectangle(BAR_WIDTH, HEALTH_HEIGHT, Color.LIMEGREEN);
        p2HealthFill.setY(TOP_MARGIN + 10);

        p2PowerBg = new Rectangle(BAR_WIDTH * 0.7, POWER_HEIGHT, Color.DARKBLUE);
        p2PowerBg.setStroke(Color.BLACK);
        p2PowerBg.setY(TOP_MARGIN + 10 + HEALTH_HEIGHT + 5);
        
        p2PowerFill = new Rectangle(BAR_WIDTH * 0.7, POWER_HEIGHT, Color.CYAN);
        p2PowerFill.setY(TOP_MARGIN + 10 + HEALTH_HEIGHT + 5);

        // ================= TIMER =================
        timerLabel = new Text("99");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        timerLabel.setFill(Color.YELLOW);
        timerLabel.setEffect(shadow);
        timerLabel.setY(TOP_MARGIN + 40); // La X verrà centrata in base allo schermo
        
        // Aggiungiamo tutto alla root
        hudRoot.getChildren().addAll(
        	p1Label, p1HealthBg, p1HealthFill, p1PowerBg, p1PowerFill,
            p2Label, p2HealthBg, p2HealthFill, p2PowerBg, p2PowerFill,
            timerLabel
        );
	}
	
	public Pane getNode() {
		return hudRoot;
	}
	
	// --- AGGIORNA LA DISPOSIZIONE SE LA FINESTRA CAMBIA GRANDEZZA ---
    public void updateLayout(double screenWidth) {
        // Calcoliamo dove posizionare le barre del P2 allineandole a destra
        double p2X = screenWidth - SIDE_MARGIN - BAR_WIDTH;
        double p2PowerX = screenWidth - SIDE_MARGIN - (BAR_WIDTH * 0.7);
        
        // --- Allineamento Testo P2 tutto a destra ---
        p2Label.setX(p2X);                           // 1. Lo posizioniamo esattamente sopra l'inizio della barra
        p2Label.setWrappingWidth(BAR_WIDTH);         // 2. Gli diamo come spazio totale l'intera larghezza della barra (400)
        p2Label.setTextAlignment(TextAlignment.RIGHT); // 3. Spingiamo il testo tutto a destra dentro questo spazio!

        // Allineamento P2
        p2Label.setX(p2X); 
        p2HealthBg.setX(p2X);
        p2HealthFill.setX(p2X);
        p2PowerBg.setX(p2PowerX);
        p2PowerFill.setX(p2PowerX);

        // Centriamo il Timer
        timerLabel.setX((screenWidth / 2.0) - 25); 
    }
    
    // --- AGGIORNA LE BARRE IN TEMPO REALE ---
    public void update(Player p1, Player p2) {
        // --- Aggiorna P1 (Si accorcia verso sinistra) ---
        double p1HpRatio = Math.max(0, (double) p1.getHealth() / application.Model.Player.maxHealth);
        p1HealthFill.setWidth(BAR_WIDTH * p1HpRatio);
        
        // Esempio barra speciale P1 (attualmente fissa a 0)
        p1PowerFill.setWidth(0); 

        // --- Aggiorna P2 (Si accorcia verso destra, svuotandosi in modo speculare!) ---
        double p2HpRatio = Math.max(0, (double) p2.getHealth() / application.Model.Player.maxHealth);
        double newWidthP2 = BAR_WIDTH * p2HpRatio;
        
        p2HealthFill.setWidth(newWidthP2);
        // Spostiamo la barra verde in avanti in modo che il vuoto si crei verso il centro dello schermo
        p2HealthFill.setX(p2HealthBg.getX() + (BAR_WIDTH - newWidthP2));

        // Esempio barra speciale P2 (attualmente fissa a 0)
        p2PowerFill.setWidth(0);
        p2PowerFill.setX(p2PowerBg.getX() + ((BAR_WIDTH * 0.7) - 0));
    }
}
