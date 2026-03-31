package application.Model;

public enum AnimState {
    IDLE_LEFT,         // Il nuovo idle stazionario/respirazione
    IDLE_RIGHT,
    MENU_IDLE,
    
    // MOVIMENTO
    WALK_LEFT,
    WALK_RIGHT,
    TURN,                         // L'animazione per girarsi (palindroma)
    
    // AZIONI
    BLOCK_LEFT,                   // La parata specifica
    BLOCK_RIGHT,
    
    PUNCH_LEFT,                   // (Manterremo questi come placeholder futuri)
    PUNCH_RIGHT,
    JUMP_LEFT,
    JUMP_RIGHT
}
