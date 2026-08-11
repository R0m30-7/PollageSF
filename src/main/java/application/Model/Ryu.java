package application.Model;

import javafx.geometry.Point2D;

public class Ryu extends Player {
    

    public Ryu(Point2D spawn) {
        super(spawn);
        
        this.maxHealth = 100;
        this.health = this.maxHealth;
        this.parryStunDuration = 1200;
        
        this.displayName = "Ryu";
        this.pfpPath = "/Sprites/ryuPFP.png";
        
        this.jumpStrength = -13.0;
        this.gravity = 0.2;
        this.speed = 3.5;
        
        this.punchDurationNs = 60 * 1_000_000L;
        this.punchDamage = 10.0;
        
        this.atlasPath = "/Sprites/ryuAtlas.png";
        this.atlas = new TextureAtlas("src/main/resources/Sprites/ryuAtlasManifest.json");
        
        this.renderScale = 5.0; 
        
        this.width = 25.0 * this.renderScale;
        this.height = 54.0 * this.renderScale;
        
        this.getBoundingBox().updateSize(this.width, this.height);
        this.saveBaseStats();

        
        // Mappa delle animazioni basata sulle sezioni del JSON (row e col)
        animations.put(AnimState.IDLE_RIGHT, new AnimData(0, 0, 4, 100, true)); 
        animations.put(AnimState.IDLE_LEFT, new AnimData(0, 0, 4, 100, true));
        animations.put(AnimState.MENU_IDLE, new AnimData(7, 3, 7, 200, true));

        animations.put(AnimState.WALK_RIGHT, new AnimData(0, 1, 5, 100, true));
        animations.put(AnimState.WALK_LEFT, new AnimData(0, 1, 5, 100, true));
        //non ce animazione turn
        animations.put(AnimState.TURN, new AnimData(0, 1, 3, 50, false));

        animations.put(AnimState.BLOCK_RIGHT, new AnimData(0, 5, 1, 50, false));
        animations.put(AnimState.BLOCK_LEFT, new AnimData(0, 5, 1, 50, false));

        animations.put(AnimState.PUNCH_RIGHT, new AnimData(1, 0, 3, 40, false)); 
        animations.put(AnimState.PUNCH_LEFT, new AnimData(1, 1, 5, 200, false));
        animations.put(AnimState.JUMP_RIGHT, new AnimData(0, 2, 7, 100, false));
        animations.put(AnimState.JUMP_LEFT, new AnimData(0, 3, 7, 150, false));
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }
}