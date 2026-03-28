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
		
		this.atlasPath = "/Sprites/playerAtlas.png";
        this.spriteCols = 3;
        this.spriteRows = 4;
        this.frameWidth = 16;
        this.frameHeight = 16;
        this.renderScale = 6;  // Ingrandisce la pixel art esattamente x6 volte
        
        // Dati Fisici (La hitbox si adatta alla grafica ingrandita)
        this.width = this.frameWidth * this.renderScale;
        this.height = this.frameHeight * this.renderScale;
        
        this.getBoundingBox().updateSize(this.width, this.height);
        
        // Mappa delle animazioni
        // (riga, numeroFrame, millisecondiPerFrame, loop)
        
        // IDLE (Fermo): Uso il primo frame della camminata, riga 0 (faccia) e riga 1/2 in base alla direzione
        animations.put(AnimState.IDLE_RIGHT, new AnimData(0, 1, 150, true)); // Fermo a destra (Usa la riga 2)
        animations.put(AnimState.IDLE_LEFT, new AnimData(0, 1, 150, true));  // Fermo a sinistra (Usa la riga 1)
        animations.put(AnimState.IDLE_FRONT, new AnimData(0, 1, 150, true)); // Fermo frontale (Riga 0)
        
        // WALK (Camminata): Usa tutti e 3 i frame
        animations.put(AnimState.WALK_RIGHT, new AnimData(1, 3, 120, true)); // Cammina veloce a destra
        animations.put(AnimState.WALK_LEFT, new AnimData(2, 3, 120, true));  // Cammina veloce a sinistra
        
        // PUNCH & JUMP (Placeholder: per ora usano la riga frontale o si fermano su un frame specifico)
        // Nota che loop è FALSE: il pugno si ferma alla fine dell'animazione!
        animations.put(AnimState.PUNCH_RIGHT, new AnimData(1, 1, 100, false)); 
        animations.put(AnimState.PUNCH_LEFT, new AnimData(2, 1, 100, false));
        animations.put(AnimState.DEFEND_RIGHT, new AnimData(0, 1, 200, false));
        animations.put(AnimState.DEFEND_LEFT, new AnimData(0, 1, 200, false));
        animations.put(AnimState.JUMP_RIGHT, new AnimData(0, 1, 150, false));
        animations.put(AnimState.JUMP_LEFT, new AnimData(0, 1, 150, false));
	}
}
