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
        System.out.println("🎮 Sistema di input inizializzato. Premi un tasto su un controller per unirti alla partita!");
    }

    public void update() {
        for (Controller c : allControllers) {
            if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
                c.poll(); 
                
                // 1. Controlliamo se QUESTO controller ha un tasto premuto
                boolean isAnyButtonPressed = false;
                for (Component comp : c.getComponents()) {
                    if (comp.getIdentifier() instanceof Component.Identifier.Button) {
                        if (comp.getPollData() != 0.0f) {
                            isAnyButtonPressed = true;
                            break;
                        }
                    }
                }

                // 2. ASSEGNAZIONE AUTOMATICA (Drop-in)
                if (isAnyButtonPressed) {
                    // Se il Player 1 è libero, e il controller NON è già del Player 2
                    if (player1Gamepad == null && c != player2Gamepad) {
                        player1Gamepad = c;
                        System.out.println("✅ Giocatore 1 unito! Controller: " + c.getName());
                    } 
                    // Se il Player 2 è libero, e il controller NON è già del Player 1
                    else if (player2Gamepad == null && c != player1Gamepad) {
                        player2Gamepad = c;
                        System.out.println("✅ Giocatore 2 unito! Controller: " + c.getName());
                    }
                }
            }
        }
    }

    // --- METODI PER LEGGERE LE LEVETTE ---
    // Passiamo 1 o 2 come parametro per decidere di quale giocatore vogliamo leggere l'input

    public double getLeftStickX(int playerNumber) {
        Controller gamepad = (playerNumber == 1) ? player1Gamepad : player2Gamepad;
        if (gamepad != null) {
            Component xAxis = gamepad.getComponent(Component.Identifier.Axis.X);
            if (xAxis != null) {
                double value = xAxis.getPollData();
                if (Math.abs(value) < 0.15) return 0.0; // Deadzone
                return value;
            }
        }
        return 0.0; // Se il giocatore non ha ancora un controller, restituisce 0 (sta fermo)
    }

    public double getLeftStickY(int playerNumber) {
        Controller gamepad = (playerNumber == 1) ? player1Gamepad : player2Gamepad;
        if (gamepad != null) {
            Component yAxis = gamepad.getComponent(Component.Identifier.Axis.Y);
            if (yAxis != null) {
                double value = yAxis.getPollData();
                if (Math.abs(value) < 0.15) return 0.0; // Deadzone
                return value;
            }
        }
        return 0.0;
    }
}