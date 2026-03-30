package application.Controller;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

public class InputManager {
    private static InputManager instance;
    private ControllerManager controllerManager;
    
    // Memorizziamo gli indici interni dei controller assegnati ai giocatori (-1 = non assegnato)
    private int p1Index = -1;
    private int p2Index = -1;

    // Costruttore privato per il Singleton
    private InputManager() {
        controllerManager = new ControllerManager();
        controllerManager.initSDLGamepad();
        System.out.println("🎮 Motore Jamepad (SDL2) inizializzato. Muovi una levetta o premi un tasto per unirti!");
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
        // Fondamentale: Jamepad gestisce le periferiche USB in automatico qui!
        controllerManager.update(); 
        
        // 1. Controllo disconnessioni fisiche
        if (p1Index != -1 && !controllerManager.getState(p1Index).isConnected) {
            System.out.println("❌ Giocatore 1 Disconnesso!");
            p1Index = -1;
        }
        if (p2Index != -1 && !controllerManager.getState(p2Index).isConnected) {
            System.out.println("❌ Giocatore 2 Disconnesso!");
            p2Index = -1;
        }

        // 2. Assegnazione Automatica (Drop-in)
        for (int i = 0; i < controllerManager.getNumControllers(); i++) {
            ControllerState state = controllerManager.getState(i);
            
            if (state.isConnected) {
                // Se preme un tasto d'azione qualsiasi per confermare la presenza
                if (state.a || state.b || state.x || state.y || state.start) {
                    
                    if (p1Index == -1 && i != p2Index) {
                        p1Index = i;
                        System.out.println("✅ Giocatore 1 unito! (Pad index: " + i + ")");
                    } else if (p2Index == -1 && i != p1Index) {
                        p2Index = i;
                        System.out.println("✅ Giocatore 2 unito! (Pad index: " + i + ")");
                    }
                }
            }
        }
    }

    // --- METODO SUPPORTO PER OTTENERE LO STATO ---
    private ControllerState getState(int playerNumber) {
        int index = (playerNumber == 1) ? p1Index : p2Index;
        if (index != -1) {
            return controllerManager.getState(index);
        }
        return null; // Ritorna null se il giocatore non ha un pad assegnato
    }

    // --- METODI PER IL MOVIMENTO ---
    public double getLeftStickX(int playerNumber) {
        ControllerState state = getState(playerNumber);
        if (state != null && Math.abs(state.leftStickX) > 0.15) {
            return state.leftStickX;
        }
        return 0.0; 
    }

    public double getLeftStickY(int playerNumber) {
        ControllerState state = getState(playerNumber);
        if (state != null && Math.abs(state.leftStickY) > 0.15) {
            // Nota: Jamepad restituisce Y positivo verso l'alto.
            // Se nel menu le mappe scorrono al contrario, basta togliere il meno (-) qui!
            return state.leftStickY; 
        }
        return 0.0;
    }

    // --- IL METODO DEL SALTO ---
    public boolean isJumpButtonPressed(int playerNumber) {
        ControllerState state = getState(playerNumber);
        // Tasto A universale (Croce su PlayStation, A su Xbox/Nintendo)
        return state != null && state.a; 
    }
    
    // --- METODO PER IL TASTO PAUSA ---
    public boolean isPauseButtonPressed(int playerNumber) {
        ControllerState state = getState(playerNumber);
        // Tasto Start/Options universale
        return state != null && state.start; 
    }
    
    // --- METODI PER AZIONI DI COMBATTIMENTO ---
    public boolean isPunchButtonPressed(int playerNumber) {
        ControllerState state = getState(playerNumber);
        // Tasto X o Y (Quadrato o Triangolo su PS)
        return state != null && (state.x || state.y); 
    }

    public boolean isDefendButtonPressed(int playerNumber) {
        ControllerState state = getState(playerNumber);
        // Tasto B universale (Cerchio su PS)
        return state != null && state.b; 
    }
    
    // --- GESTIONE EMERGENZA E STATO ---
    public boolean isPlayer1Connected() { return p1Index != -1; }
    public boolean isPlayer2Connected() { return p2Index != -1; }

    public boolean hasLostControllers(int requiredPlayers) {
        if (requiredPlayers == 1) return !isPlayer1Connected();
        if (requiredPlayers == 2) return !isPlayer1Connected() || !isPlayer2Connected();
        return false;
    }

    // Questo metodo ora spegne e riaccende brutalmente (ma in sicurezza) la libreria SDL
    public void rescanControllers() {
        System.out.println("🔄 Riavvio forzato del driver USB (Jamepad)...");
        controllerManager.quitSDLGamepad();
        controllerManager.initSDLGamepad();
        // Resettiamo gli indici per forzare i giocatori a ri-premere un tasto
        p1Index = -1;
        p2Index = -1; 
    }
    
    // Buona pratica: chiamarlo quando si chiude il gioco intero (System.exit)
    public void chiudiTutto() {
        controllerManager.quitSDLGamepad();
    }
}