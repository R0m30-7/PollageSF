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
    PUNCH_CROUCH_LEFT,
    PUNCH_CROUCH_RIGHT,
    HOOK_LEFT,
    HOOK_RIGHT,

    JUMP_LEFT,
    JUMP_RIGHT,

    CROUCH_LEFT,
    CROUCH_RIGHT,
    KICK_RIGHT,
    KICK_LEFT,
    CROUCH_BLOCK_RIGHT, 
    CROUCH_BLOCK_LEFT,

    HURT_RIGHT,
    HURT_LEFT, HURT_CROUCH_RIGHT, HURT_CROUCH_LEFT, HADOUKEN_RIGHT, HADOUKEN_LEFT
}
