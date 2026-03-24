package application.Model;

import javafx.geometry.Point2D;

import application.Controller.InputManager;
import application.Utils.GameConfig;

public class GameModel {
    private Player player1;
    private Player player2;
    
    // Definizione della posizione del pavimento (100 pixel sopra il fondo della finestra)
    private final double GROUND_LEVEL = GameConfig.WINDOW_HEIGHT - 100;

    public GameModel() {
        // Presumo tu li inizializzi con coordinate iniziali, aggiusta se necessario
    	player1 = new Player(new Point2D(200, 200)); 
        player2 = new Player(new Point2D(400, 200));
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }

    public void update(InputManager input) {
        // 1. Leggiamo lo stato dei tasti X di entrambi i giocatori
        boolean isP1JumpHeld = input.isJumpButtonPressed(1);
        boolean isP2JumpHeld = input.isJumpButtonPressed(2);

        // 2. Applichiamo la fisica passando lo stato del tasto!
        player1.applyPhysics(GROUND_LEVEL, isP1JumpHeld);
        player2.applyPhysics(GROUND_LEVEL, isP2JumpHeld);
        
        // ==========================================
        //         INPUT GIOCATORE 1
        // ==========================================
        double p1X = input.getLeftStickX(1);
        if (Math.abs(p1X) > 0.0) {
            // (Adatta questa riga in base a come avevi implementato il tuo moveHorizontal)
            player1.moveHorizontal(p1X > 0 ? PlayerState.RIGHT : PlayerState.LEFT);
        }
        
        // Il metodo jump() scatterà solo se il giocatore ha i piedi per terra,
        // quindi va bene chiamarlo a ogni tick in cui il tasto è premuto.
        if (isP1JumpHeld) {
            player1.jump();
        }

        // ==========================================
        //         INPUT GIOCATORE 2
        // ==========================================
        double p2X = input.getLeftStickX(2);
        if (Math.abs(p2X) > 0.0) {
            player2.moveHorizontal(p2X > 0 ? PlayerState.RIGHT : PlayerState.LEFT);
        }
        
        if (isP2JumpHeld) {
            player2.jump();
        }
    }
}