package application.Controller;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class InputManager {
    private Controller[] allControllers;
    
    // Riferimenti ai due controller separati
    private Controller player1Gamepad; 
    private Controller player2Gamepad;

    public InputManager() {
        allControllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        System.out.println("🎮 Sistema di input inizializzato. Muovi una levetta o premi un tasto per unirti!");
    }

    // --- AGGIORNAMENTO E ASSEGNAZIONE CONTROLLER ---
    public void update() {
        for (Controller c : allControllers) {
            if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
                c.poll(); 
                
                boolean isAnyInputDetected = false;
                for (Component comp : c.getComponents()) {
                    // Controlliamo se preme un pulsante o muove una levetta oltre la deadzone
                    if (Math.abs(comp.getPollData()) > 0.5f) {
                        isAnyInputDetected = true;
                        break;
                    }
                }

                // Assegnazione Automatica (Drop-in)
                if (isAnyInputDetected) {
                    if (player1Gamepad == null && c != player2Gamepad) {
                        player1Gamepad = c;
                        System.out.println("✅ Giocatore 1 unito! Controller: " + c.getName());
                    } else if (player2Gamepad == null && c != player1Gamepad) {
                        player2Gamepad = c;
                        System.out.println("✅ Giocatore 2 unito! Controller: " + c.getName());
                    }
                }
            }
        }
    }

    // --- METODI PER IL MOVIMENTO ---
    public double getLeftStickX(int playerNumber) {
        Controller gamepad = (playerNumber == 1) ? player1Gamepad : player2Gamepad;
        if (gamepad != null) {
            Component xAxis = gamepad.getComponent(Component.Identifier.Axis.X);
            if (xAxis != null) {
                double value = xAxis.getPollData();
                if (Math.abs(value) < 0.15) return 0.0; 
                return value;
            }
        }
        return 0.0; 
    }

    public double getLeftStickY(int playerNumber) {
        Controller gamepad = (playerNumber == 1) ? player1Gamepad : player2Gamepad;
        if (gamepad != null) {
            Component yAxis = gamepad.getComponent(Component.Identifier.Axis.Y);
            if (yAxis != null) {
                double value = yAxis.getPollData();
                if (Math.abs(value) < 0.15) return 0.0; 
                return value;
            }
        }
        return 0.0;
    }

    // --- IL METODO DEL SALTO CORRETTO ---
    public boolean isJumpButtonPressed(int playerNumber) {
        Controller gamepad = (playerNumber == 1) ? player1Gamepad : player2Gamepad;
        
        if (gamepad != null) {
            // Cerchiamo specificamente il tasto "A" (come lo chiama Linux) o i tasti standard (0, 1) per compatibilità futura
            Component buttonA = gamepad.getComponent(Component.Identifier.Button.A);
            Component button0 = gamepad.getComponent(Component.Identifier.Button._0);
            Component button1 = gamepad.getComponent(Component.Identifier.Button._1);
            
            if (buttonA != null && buttonA.getPollData() != 0.0f) return true;
            if (button0 != null && button0.getPollData() != 0.0f) return true;
            if (button1 != null && button1.getPollData() != 0.0f) return true;
        }
        return false;
    }
    
    // Getter necessari per capire se i controller sono connessi
    public boolean isPlayer1Connected() {
    	return player1Gamepad != null;
    }
    public boolean isPlayer2Connected() {
    	return player2Gamepad != null;
    }
}