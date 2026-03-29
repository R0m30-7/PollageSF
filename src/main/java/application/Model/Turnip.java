package application.Model;

import javafx.geometry.Point2D;

public class Turnip extends Player {
	public Turnip(Point2D spawn) {
		super(spawn);
		
		// Dati HARDCODED
		this.maxHealth = 100;
		this.health = this.maxHealth;
		
		this.jumpStrength = -11.0;
		this.gravity = 0.2;
		
		this.speed = 2.0;
		
		this.atlasPath = "/Sprites/turnipAtlas.png";
		this.spriteRows = 8;
        this.spriteCols = 8;
        this.frameWidth = 16;
        this.frameHeight = 16;
        this.renderScale = 6;  // Ingrandisce la pixel art esattamente x6 volte
        
        // Dati Fisici (La hitbox si adatta alla grafica ingrandita)
        this.width = this.frameWidth * this.renderScale;
        this.height = this.frameHeight * this.renderScale;
        
        this.getBoundingBox().updateSize(this.width, this.height);
        
        // Mappa delle animazioni
        // (riga, colonnaPartenza, numeroFrame, millisecondiPerFrame, loop)
        
        // Nuovo Idle Stazionario (Ipotizziamo riga 4 per DX, riga 5 per SX, 3 frame slow)
        // !!! MODIFICA IL PRIMO NUMERO (rigaAtlas) QUANDO HAI L'ATLAS CORRETTO !!!
        animations.put(AnimState.IDLE_RIGHT, new AnimData(0, 0, 3, 200, true)); 
        animations.put(AnimState.IDLE_LEFT, new AnimData(1, 0, 3, 200, true));

        // *** MOVIMENTO ***
        // Camminata (Invariata, usa righe originali 1 e 2)
        animations.put(AnimState.WALK_RIGHT, new AnimData(2, 0, 3, 120, true));
        animations.put(AnimState.WALK_LEFT, new AnimData(3, 0, 3, 120, true));
        
        // Giararsi (TURN): Palindroma. Ipotizziamo riga 6, 3 frame veloci.
        // !!! loop è FALSE !!! Gioca una volta sola.
        animations.put(AnimState.TURN, new AnimData(6, 0, 3, 50, false));

        // *** AZIONI ***
        // Parata (BLOCK): Ipotizziamo riga 7 DX, riga 8 SX, 3 frame veloci.
        animations.put(AnimState.BLOCK_RIGHT, new AnimData(4, 0, 3, 50, false));
        animations.put(AnimState.BLOCK_LEFT, new AnimData(5, 0, 3, 50, false));

        // Placeholder futuri (usiamo riga 0 originale per ora)
        animations.put(AnimState.PUNCH_RIGHT, new AnimData(0, 0, 1, 100, false)); 
        animations.put(AnimState.PUNCH_LEFT, new AnimData(1, 0, 1, 100, false));
        animations.put(AnimState.JUMP_RIGHT, new AnimData(0, 0, 1, 150, false));
        animations.put(AnimState.JUMP_LEFT, new AnimData(1, 0, 1, 150, false));
	}
}
