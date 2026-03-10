/*
 * A differenza della classe Settings, questa contiene solo variabili finali
 * statiche, le impostazioni di base del gioco
 */
package application.Utils;

public class GameConfig {
	private GameConfig() {}
	
	public static final int WINDOW_WIDTH = 800;
	public static final int WINDOW_HEIGHT = 600;
	public static final String GAME_TITLE_STRING = "Giochini";
	
	public static final int TARGET_TPS = 200;
	public static final double TIME_PER_TICK = 1_000_000_000.0 / TARGET_TPS;
	
	public static final int pWidth = 50;
	public static final int pHeight = 50;
}
