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
	private double p1MaxHealth;
	
	// HUD giocatore 2
	private Rectangle p2HealthBg, p2HealthFill;
	private Rectangle p2PowerBg, p2PowerFill;
	private Text p2Label;
	private double p2MaxHealth;
	
	// Timer
	private Text timerLabel;
	
	// Costanti di BASE (per risoluzione 1080p)
	private static final double BASE_BAR_WIDTH = 400.0;
	private static final double BASE_HEALTH_HEIGHT = 35.0;
	private static final double BASE_POWER_HEIGHT = 15.0;
	private static final double BASE_TOP_MARGIN = 30.0;
	private static final double BASE_SIDE_MARGIN = 50.0;
	
    // Variabile dinamica che servirà per il metodo update() in tempo reale
    private double currentBarWidth = BASE_BAR_WIDTH;
	
	public HUDView(double p1MaxHealth, double p2MaxHealth) {
        // ... (IL TUO COSTRUTTORE RIMANE IDENTICO A QUELLO CHE HAI GIÀ) ...
        // ... (lascia intatto tutto il blocco hudRoot.getChildren().addAll(...) e hudRoot = new Pane(); ecc.)
		this.p1MaxHealth = p1MaxHealth;
		this.p2MaxHealth = p2MaxHealth;
		
		hudRoot = new Pane();
		DropShadow shadow = new DropShadow(5, Color.BLACK);	// Effetto ombra per rendere testi e barre leggibili
		
		// ============== GIOCATORE 1 ==============
		p1Label = new Text("Giocatore 1");
		p1Label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		p1Label.setFill(Color.WHITE);
		p1Label.setEffect(shadow);
        p1Label.setX(BASE_SIDE_MARGIN);
        p1Label.setY(BASE_TOP_MARGIN);
        
        // Vita P1
        p1HealthBg = new Rectangle(BASE_BAR_WIDTH, BASE_HEALTH_HEIGHT, Color.DARKRED);
        p1HealthBg.setX(BASE_SIDE_MARGIN);
        p1HealthBg.setY(BASE_TOP_MARGIN + 10);
        p1HealthBg.setStroke(Color.BLACK);
        p1HealthBg.setStrokeWidth(2);

        p1HealthFill = new Rectangle(BASE_BAR_WIDTH, BASE_HEALTH_HEIGHT, Color.LIMEGREEN);
        p1HealthFill.setX(BASE_SIDE_MARGIN);
        p1HealthFill.setY(BASE_TOP_MARGIN + 10);

        // Potere Speciale P1 (Azzurra)
        p1PowerBg = new Rectangle(BASE_BAR_WIDTH * 0.7, BASE_POWER_HEIGHT, Color.DARKBLUE);
        p1PowerBg.setX(BASE_SIDE_MARGIN);
        p1PowerBg.setY(BASE_TOP_MARGIN + 10 + BASE_HEALTH_HEIGHT + 5);
        p1PowerBg.setStroke(Color.BLACK);

        p1PowerFill = new Rectangle(BASE_BAR_WIDTH * 0.7, BASE_POWER_HEIGHT, Color.CYAN);
        p1PowerFill.setX(BASE_SIDE_MARGIN);
        p1PowerFill.setY(BASE_TOP_MARGIN + 10 + BASE_HEALTH_HEIGHT + 5);
        
        // ============== GIOCATORE 2 ==============
        p2Label = new Text("Giocatore 2");
        p2Label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        p2Label.setFill(Color.WHITE);
        p2Label.setEffect(shadow);
        p2Label.setY(BASE_TOP_MARGIN);

        p2HealthBg = new Rectangle(BASE_BAR_WIDTH, BASE_HEALTH_HEIGHT, Color.DARKRED);
        p2HealthBg.setStroke(Color.BLACK);
        p2HealthBg.setStrokeWidth(2);
        p2HealthBg.setY(BASE_TOP_MARGIN + 10);
        
        p2HealthFill = new Rectangle(BASE_BAR_WIDTH, BASE_HEALTH_HEIGHT, Color.LIMEGREEN);
        p2HealthFill.setY(BASE_TOP_MARGIN + 10);

        p2PowerBg = new Rectangle(BASE_BAR_WIDTH * 0.7, BASE_POWER_HEIGHT, Color.DARKBLUE);
        p2PowerBg.setStroke(Color.BLACK);
        p2PowerBg.setY(BASE_TOP_MARGIN + 10 + BASE_HEALTH_HEIGHT + 5);
        
        p2PowerFill = new Rectangle(BASE_BAR_WIDTH * 0.7, BASE_POWER_HEIGHT, Color.CYAN);
        p2PowerFill.setY(BASE_TOP_MARGIN + 10 + BASE_HEALTH_HEIGHT + 5);

        // ================= TIMER =================
        timerLabel = new Text("99");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        timerLabel.setFill(Color.YELLOW);
        timerLabel.setEffect(shadow);
        timerLabel.setY(BASE_TOP_MARGIN + 40); 
        
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
	
	// --- AGGIORNA LA DISPOSIZIONE E LO ZOOM ---
	// NOTA: Abbiamo aggiunto "scaleFactor" ai parametri!
    public void updateLayout(double screenWidth, double scaleFactor) {
        
        // 1. Ricalcoliamo tutte le dimensioni moltiplicandole per lo zoom!
        currentBarWidth = BASE_BAR_WIDTH * scaleFactor;
        double currentHealthHeight = BASE_HEALTH_HEIGHT * scaleFactor;
        double currentPowerHeight = BASE_POWER_HEIGHT * scaleFactor;
        double currentTopMargin = BASE_TOP_MARGIN * scaleFactor;
        double currentSideMargin = BASE_SIDE_MARGIN * scaleFactor;

        // 2. Aggiorniamo la dimensione dei Testi (Font perfetti a qualsiasi scala!)
        Font nameFont = Font.font("Arial", FontWeight.BOLD, 24 * scaleFactor);
        p1Label.setFont(nameFont);
        p2Label.setFont(nameFont);
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 50 * scaleFactor));

        // 3. Aggiorniamo Altezze
        p1HealthBg.setHeight(currentHealthHeight);
        p1HealthFill.setHeight(currentHealthHeight);
        p2HealthBg.setHeight(currentHealthHeight);
        p2HealthFill.setHeight(currentHealthHeight);

        p1PowerBg.setHeight(currentPowerHeight);
        p1PowerFill.setHeight(currentPowerHeight);
        p2PowerBg.setHeight(currentPowerHeight);
        p2PowerFill.setHeight(currentPowerHeight);

        // 4. Aggiorniamo Larghezze Sfondi
        p1HealthBg.setWidth(currentBarWidth);
        p1PowerBg.setWidth(currentBarWidth * 0.7);
        p2HealthBg.setWidth(currentBarWidth);
        p2PowerBg.setWidth(currentBarWidth * 0.7);

        // 5. Aggiorniamo le Y
        double healthY = currentTopMargin + (10 * scaleFactor);
        double powerY = healthY + currentHealthHeight + (5 * scaleFactor);

        p1Label.setY(currentTopMargin);
        p2Label.setY(currentTopMargin);
        timerLabel.setY(currentTopMargin + (40 * scaleFactor));

        p1HealthBg.setY(healthY);
        p1HealthFill.setY(healthY);
        p2HealthBg.setY(healthY);
        p2HealthFill.setY(healthY);

        p1PowerBg.setY(powerY);
        p1PowerFill.setY(powerY);
        p2PowerBg.setY(powerY);
        p2PowerFill.setY(powerY);

        // 6. Aggiorniamo le X
        p1Label.setX(currentSideMargin);
        p1HealthBg.setX(currentSideMargin);
        p1HealthFill.setX(currentSideMargin);
        p1PowerBg.setX(currentSideMargin);
        p1PowerFill.setX(currentSideMargin);

        // Calcoli per tenere P2 ancorato a destra
        double p2X = screenWidth - currentSideMargin - currentBarWidth;
        double p2PowerX = screenWidth - currentSideMargin - (currentBarWidth * 0.7);

        p2Label.setX(p2X);
        p2Label.setWrappingWidth(currentBarWidth); 
        p2Label.setTextAlignment(TextAlignment.RIGHT);
        
        p2HealthBg.setX(p2X);
        p2HealthFill.setX(p2X);
        p2PowerBg.setX(p2PowerX);
        p2PowerFill.setX(p2PowerX);

        timerLabel.setX((screenWidth / 2.0) - (25 * scaleFactor)); 
    }
    
    // --- AGGIORNA LE BARRE IN TEMPO REALE ---
    public void update(Player p1, Player p2) {
        // --- Aggiorna P1 ---
        double p1HpRatio = Math.max(0, (double) p1.getHealth() / p1MaxHealth);
        p1HealthFill.setWidth(currentBarWidth * p1HpRatio); // Usiamo currentBarWidth!
        
        p1PowerFill.setWidth(0); 

        // --- Aggiorna P2 ---
        double p2HpRatio = Math.max(0, (double) p2.getHealth() / p2MaxHealth);
        double newWidthP2 = currentBarWidth * p2HpRatio; // Usiamo currentBarWidth!
        
        p2HealthFill.setWidth(newWidthP2);
        p2HealthFill.setX(p2HealthBg.getX() + (currentBarWidth - newWidthP2));

        p2PowerFill.setWidth(0);
        p2PowerFill.setX(p2PowerBg.getX() + ((currentBarWidth * 0.7) - 0));
    }
}