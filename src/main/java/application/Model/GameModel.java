package application.Model;

import javafx.geometry.Point2D;

import application.Controller.InputManager;

public class GameModel {
    private Player player1;
    private Player player2;

    public GameModel() {
        // Presumo tu li inizializzi con coordinate iniziali, aggiusta se necessario
    	player1 = new Player(new Point2D(200, 200)); 
        player2 = new Player(new Point2D(400, 200));
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }

    public void update(InputManager input) {
        
        // ==========================================
        //         MOVIMENTO GIOCATORE 1
        // ==========================================
        double p1X = input.getLeftStickX(1);
        double p1Y = input.getLeftStickY(1);

        // Se la levetta è fuori dalla deadzone (cioè si sta muovendo)
        if (Math.abs(p1X) > 0.0 || Math.abs(p1Y) > 0.0) {
            
            // Quale asse sta spingendo di più? X (orizzontale) o Y (verticale)?
            if (Math.abs(p1X) > Math.abs(p1Y)) {
                // Movimento Orizzontale Dominante
                if (p1X > 0) {
                    player1.Move(PlayerState.RIGHT);
                } else {
                    player1.Move(PlayerState.LEFT);
                }
            } else {
                // Movimento Verticale Dominante
                if (p1Y > 0) {
                    player1.Move(PlayerState.DOWN); // Y positivo in JavaFX va verso il basso
                } else {
                    player1.Move(PlayerState.UP);   // Y negativo va verso l'alto
                }
            }
        }

        // ==========================================
        //         MOVIMENTO GIOCATORE 2
        // ==========================================
        double p2X = input.getLeftStickX(2);
        double p2Y = input.getLeftStickY(2);

        if (Math.abs(p2X) > 0.0 || Math.abs(p2Y) > 0.0) {
            if (Math.abs(p2X) > Math.abs(p2Y)) {
                if (p2X > 0) {
                    player2.Move(PlayerState.RIGHT);
                } else {
                    player2.Move(PlayerState.LEFT);
                }
            } else {
                if (p2Y > 0) {
                    player2.Move(PlayerState.DOWN);
                } else {
                    player2.Move(PlayerState.UP);
                }
            }
        }
    }
}