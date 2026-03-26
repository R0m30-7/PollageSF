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
            Component buttonA = gamepad.getComponent(Component.Identifier.Button.A);
            if (buttonA != null && buttonA.getPollData() != 0.0f) return true;
        }
        return false;
    }
    
    // --- METODO PER IL TASTO PAUSA ---
    public boolean isPauseButtonPressed(int playerNumber) {
        Controller gamepad = (playerNumber == 1) ? player1Gamepad : player2Gamepad;
        
        if (gamepad != null) {
            Component startBtn = gamepad.getComponent(Component.Identifier.Button.START);
            Component selectBtn = gamepad.getComponent(Component.Identifier.Button.SELECT);
            Component btn7 = gamepad.getComponent(Component.Identifier.Button._7);
            Component btn8 = gamepad.getComponent(Component.Identifier.Button._8);
            Component btn9 = gamepad.getComponent(Component.Identifier.Button._9);
            
            if (startBtn != null && startBtn.getPollData() > 0.5f) return true;
            if (selectBtn != null && selectBtn.getPollData() > 0.5f) return true;
            if (btn7 != null && btn7.getPollData() > 0.5f) return true;
            if (btn8 != null && btn8.getPollData() > 0.5f) return true;
            if (btn9 != null && btn9.getPollData() > 0.5f) return true;
        }
        return false;
    }
    
    // --- METODI PER AZIONI DI COMBATTIMENTO ---
    
    public boolean isPunchButtonPressed(int playerNumber) {
        Controller gamepad = (playerNumber == 1) ? player1Gamepad : player2Gamepad;
        if (gamepad != null) {
            for (Component comp : gamepad.getComponents()) {
                // IL FILTRO CRUCIALE: Se non è un bottone, lo ignoriamo! 
                // Questo impedisce alla levetta X di far partire i pugni
                if (comp.getIdentifier() instanceof Component.Identifier.Button) {
                    String btnName = comp.getIdentifier().getName(); 
                    if ((btnName.equals("X") || btnName.equals("Square") || btnName.equals("2")) && comp.getPollData() > 0.5f) {
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
                // IL FILTRO CRUCIALE: Solo bottoni!
                if (comp.getIdentifier() instanceof Component.Identifier.Button) {
                    String btnName = comp.getIdentifier().getName();
                    if ((btnName.equals("B") || btnName.equals("Circle") || btnName.equals("1")) && comp.getPollData() > 0.5f) {
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