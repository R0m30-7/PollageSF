package application.Model;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

public class Ryu extends Player {
    

    public Ryu(Point2D spawn) {
        super(spawn);
        this.renderOffsetX = 20.0; 
        this.renderOffsetY = 0.0;
        
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
        
        this.baseRenderScale = 2.75; // Il tuo valore base isolato

        this.renderScale = this.baseRenderScale;
       
        // Chiedi all'atlas le dimensioni del primo frame (es. riga 0, colonna 0)
        Rectangle2D defaultFrame = this.atlas.getFrame(0, 0, 0);

        // La Hitbox si adatta matematicamente alla grafica usando la stessa scala
        //quel .70 stringe un po la hitbox
        this.width = defaultFrame.getWidth()*.70 * this.renderScale;
        this.height = defaultFrame.getHeight() * this.renderScale;

        this.getBoundingBox().updateSize(this.width, this.height);
    
        System.out.println("baserenderscale then" + this.baseRenderScale);
        // SALVI TUTTO NEI VALORI BASE (inclusi width, height e baseRenderScale)
        this.saveBaseStats();
        System.out.println("baserenderscale after" + this.baseRenderScale);


        
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
        animations.put(AnimState.CROUCH_RIGHT, new AnimData(0, 4, 2, 400, false));
        animations.put(AnimState.CROUCH_LEFT, new AnimData(0, 4, 2, 400, false));
    
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }
}