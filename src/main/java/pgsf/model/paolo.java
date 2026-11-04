package pgsf.model;

import pgsf.utils.Rect;
import pgsf.utils.Point;

public class paolo extends Player {
    

    public paolo(Point spawn) {
        super(spawn);
        this.renderOffsetX = 0.0; 
        this.renderOffsetY = 0.0;
        
        this.maxHealth = 100;
        this.health = this.maxHealth;
        this.parryStunDuration = 1200;
        
        this.displayName = "Paolo";
        this.pfpPath = "/Sprites/ryuPFP.png";
        
        this.jumpStrength = -10.0;
        this.gravity = 0.2;
        this.speed = 2.5;
        
        this.punchDamage = 10.0;
        
        this.kickDamage = 12.0; // Danno del calcio
        this.kickWidth = 30.0; // Larghezza della hitbox del calcio
        this.kickHeight = 20.0; // Altezza della hitbox del calcio

        this.projectileAnimFrameDuration = 80;
        
        this.atlasPath = "/Sprites/paoloAtlas.png";
        this.atlas = new TextureAtlas("/Sprites/paolo_atlas_manifest.json");
        
        this.baseRenderScale = 4; // Il tuo valore base isolato

        this.renderScale = this.baseRenderScale;
       
        // Chiedi all'atlas le dimensioni del primo frame (es. riga 0, colonna 0)
        Rect defaultFrame = this.atlas.getFrame(0, 0, 0);

        // La Hitbox si adatta matematicamente alla grafica usando la stessa scala
        // Quel .70 stringe un po la hitbox
        this.width = defaultFrame.getWidth()*.70 * this.renderScale;
        this.height = defaultFrame.getHeight() * this.renderScale;

        this.getBoundingBox().updateSize(this.width, this.height);
    
        // SALVI TUTTO NEI VALORI BASE (inclusi width, height e baseRenderScale)
        this.saveBaseStats();

        // Mappa delle animazioni basata sulle sezioni del JSON (row e col)
        animations.put(AnimState.IDLE_RIGHT, new AnimData(0, 0, 4, 100, true)); 
        animations.put(AnimState.IDLE_LEFT, new AnimData(0, 0, 4, 100, true));
        
        // --- INIZIO CODICE COLORI DELLE SKIN ---

    }

    public TextureAtlas getAtlas() {
        return atlas;
    }
}