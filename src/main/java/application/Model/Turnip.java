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
        this.frameWidth = 16;  // Metti i pixel REALI del quadratino del tuo atlas
        this.frameHeight = 16; // Metti i pixel REALI del quadratino del tuo atlas
        this.renderScale = 6;  // Ingrandisce la pixel art esattamente x3 volte (perfettamente nitida!)
        
        // Dati Fisici (La hitbox si adatta alla grafica ingrandita)
        this.width = this.frameWidth * this.renderScale;
        this.height = this.frameHeight * this.renderScale;
        
        this.getBoundingBox().updateSize(this.width, this.height);
	}
}
