/*
 * A differenza della classe Settings, questa contiene solo variabili finali
 * statiche, le impostazioni di base del gioco
 */
package application.Utils;

public class GameConfig {
	private GameConfig() {}
	
	public static final String GAME_TITLE_STRING = "Giochini";
	
	public static final int TARGET_TPS = 200;
	public static final double TIME_PER_TICK = 1_000_000_000.0 / TARGET_TPS;
	
	public static final int pWidth = 100;
	public static final int pHeight = 200;
	
	// --- Dimensioni delle azioni ---
	public static final double pPunchWidth = 70.0;   // Orizzontale
    public static final double pPunchHeight = 30.0;
    public static final double pDefenseWidth = 40.0; // Verticale
    public static final double pDefenseHeight = 180.0; // Quasi quanto il giocatore
    
    // --- Combattimento (NUOVO) ---
    public static final int pPunchDurationTicks = 10;
    public static final int pPunchDamage = 10; // Quanta vita toglie un singolo pugno
}
