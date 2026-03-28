/*
 * MAPPATURA DEI TASTI DEI CONTROLLER
 * WINDOWS
 * DualShock (Ps5): quadrato=0, x=1, o=2, triangolo=3, L1=4, R1=5,
 * L2=6, R2=7, Share=8, Options=9, L3=10, R3=11, PS=12, trackpad=13
 * 
 * LINUX
 * DualShock: quad=Y, x=A, o=B, trian=X, L1=Left Thumb, R1=Right Thumb, L2=Left Thumb 2,
 * R2=Right Thumb 2, Share=Select, Options=Start, L3=Left Thumb 3, R3=Right Thumb 3, PS=Mode
 */

package application.Controller;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class InputManager {
    // Rendiamo l'InputManager un Singleton in modo che il Menu Principale
    // e il Gioco leggano gli stessi controller!
    private static InputManager instance;
    
    private Controller[] allControllers;
    
    // Riferimenti ai due controller separati
    private Controller player1Gamepad;
    private Controller player2Gamepad;

    // Costruttore privato per il Singleton
    private InputManager() {
        allControllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        System.out.println("🎮 Sistema di input inizializzato. Muovi una levetta o premi un tasto per unirti!");
    }

    // Metodo per ottenere l'istanza globale
    public static InputManager getInstance() {
        if (instance == null) {
            instance = new InputManager();
        }
        return instance;
    }

    // --- AGGIORNAMENTO E ASSEGNAZIONE CONTROLLER ---
    public void update() {
        for (Controller c : allControllers) {
            if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
                c.poll(); 
                
                boolean isButtonPressed = false;
                for (Component comp : c.getComponents()) {
                    // Controlliamo che l'input sia effettivamente un bottone, e non un asse
                    if (comp.getIdentifier() instanceof Component.Identifier.Button) {
                    	if(comp.getPollData() > 0.5f) {
                    		
                    		//System.out.println("TASTO RILEVATO: " + comp.getIdentifier().getName());
                    		
                    		isButtonPressed = true;
                    		break;
                    	}
                    }
                }

                // Assegnazione Automatica (Drop-in) SOLO se è stato premuto un tasto vero
                if (isButtonPressed) {
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

    // --- IL METODO DEL SALTO ---
    public boolean isJumpButtonPressed(int playerNumber) {
    	Controller gamepad = (playerNumber == 1) ? player1Gamepad : player2Gamepad;
        if (gamepad != null) {
            for (Component comp : gamepad.getComponents()) {
                if (comp.getIdentifier() instanceof Component.Identifier.Button) {
                    String btnName = comp.getIdentifier().getName();
                    // "A" o "Cross" per Linux/Xbox | "1" (Tasto X) per PS5 su Windows
                    if ((btnName.equals("A") || btnName.equals("1")) && comp.getPollData() > 0.5f) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    // --- METODO PER IL TASTO PAUSA ---
    public boolean isPauseButtonPressed(int playerNumber) {
    	Controller gamepad = (playerNumber == 1) ? player1Gamepad : player2Gamepad;
        if (gamepad != null) {
            for (Component comp : gamepad.getComponents()) {
                if (comp.getIdentifier() instanceof Component.Identifier.Button) {
                    String btnName = comp.getIdentifier().getName();
                    // START/SELECT generici | "8" (Share) o "9" (Options) per PS5 su Windows
                    if ((btnName.equals("Start") || btnName.equals("9")) && comp.getPollData() > 0.5f) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    // --- METODI PER AZIONI DI COMBATTIMENTO ---
    
    public boolean isPunchButtonPressed(int playerNumber) {
    	Controller gamepad = (playerNumber == 1) ? player1Gamepad : player2Gamepad;
        if (gamepad != null) {
            for (Component comp : gamepad.getComponents()) {
                if (comp.getIdentifier() instanceof Component.Identifier.Button) {
                    String btnName = comp.getIdentifier().getName(); 
                    // "X", "Y", "Square", "Triangle" per Linux/Xbox | "0" (Quadrato) o "3" (Triangolo) per PS5 su Windows
                    if ((btnName.equals("Y") || btnName.equals("0")) && comp.getPollData() > 0.5f) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isDefendButtonPressed(int playerNumber) {
    	Controller gamepad = (playerNumber == 1) ? player1Gamepad : player2Gamepad;
        if (gamepad != null) {
            for (Component comp : gamepad.getComponents()) {
                if (comp.getIdentifier() instanceof Component.Identifier.Button) {
                    String btnName = comp.getIdentifier().getName();
                    // "B", "Circle" per Linux/Xbox | "2" (Cerchio) per PS5 su Windows
                    if ((btnName.equals("B") || btnName.equals("2")) && comp.getPollData() > 0.5f) {
                        return true;
                    }
                }
            }
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