package application.Model;


import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

public class Ken extends Player {
    
    public Ken(Point2D spawn) {
        super(spawn);
        
        // Offset grafici (da ricalibrare se Ken è disegnato più a destra/sinistra nel frame)
        this.renderOffsetX = 20.0; 
        this.renderOffsetY = 0.0;
        
        this.maxHealth = 100;
        this.health = this.maxHealth;
        this.parryStunDuration = 1200;
        
        // --- IDENTIKIT ---
        this.displayName = "Ken";
        this.pfpPath = "/Sprites/kenPFP.png"; // L'icona per il menu di selezione
        
        // --- STATISTICHE UNICHE (Esempio: Ken è più veloce di Ryu) ---
        this.jumpStrength = -10.0;
        this.gravity = 0.2;
        this.speed = 2.5; // Ryu aveva 3.5, facciamo Ken un po' più agile!
        
        this.punchDamage = 9.0; // ...ma fa 1 danno in meno

        this.kickDamage = 12.0; // Danno del calcio
        this.kickWidth = 30.0; // Larghezza della hitbox del
        this.kickHeight = 20.0; // Altezza della hitbox del calcio
        
        // --- COLLEGAMENTO ASSET GRAFICI ---
        this.atlasPath = "/Sprites/paletteDiverseKen/ken_purple_chromakey.png";
        this.atlas = new TextureAtlas("/Sprites/kenAtlasManifest.json");
        
        this.baseRenderScale = 4;
        this.renderScale = this.baseRenderScale;
       
        Rectangle2D defaultFrame = this.atlas.getFrame(0, 0, 0);

        this.width = defaultFrame.getWidth() * 0.70 * this.renderScale;
        this.height = defaultFrame.getHeight() * this.renderScale;
        this.getBoundingBox().updateSize(this.width, this.height);
    
        this.saveBaseStats();

        // ==========================================
        // MAPPATURA ANIMAZIONI DI KEN
        // ==========================================
        // ATTENZIONE: Qui devi cambiare i numeri (riga, colonna, frameTotali)
        // leggendo il file kenAtlasManifest.json. Se Ken ha le mosse su righe
        // diverse rispetto a Ryu, modificale qui!
        // Mappa delle animazioni basata sulle sezioni del JSON (row e col)
        animations.put(AnimState.IDLE_RIGHT, new AnimData(0, 0, 4, 100, true)); 
        animations.put(AnimState.IDLE_LEFT, new AnimData(0, 0, 4, 100, true));
        animations.put(AnimState.MENU_IDLE, new AnimData(0, 0, 4, 150, true));

        animations.put(AnimState.WALK_RIGHT, new AnimData(0, 1, 5, 75, true));
        animations.put(AnimState.WALK_LEFT, new AnimData(0, 1, 5, 75, true));
        //non ce animazione turn
        animations.put(AnimState.TURN, new AnimData(7, 2, 1, 5, false));

        animations.put(AnimState.BLOCK_RIGHT, new AnimData(0, 5, 1, 50, false));
        animations.put(AnimState.BLOCK_LEFT, new AnimData(0, 5, 1, 50, false));

        animations.put(AnimState.CROUCH_BLOCK_RIGHT, new AnimData(0, 5, 2, 0, false));
        animations.put(AnimState.CROUCH_BLOCK_LEFT, new AnimData(0, 5, 2, 0, false));

        animations.put(AnimState.PUNCH_RIGHT, new AnimData(1, 0, 3, 80, false)); 
        animations.put(AnimState.PUNCH_LEFT, new AnimData(1, 1, 5, 80, false));
        animations.put(AnimState.HOOK_RIGHT, new AnimData(1, 2, 3, 80, false));
        animations.put(AnimState.HOOK_LEFT, new AnimData(1, 3, 7, 80, false));

        animations.put(AnimState.JUMP_RIGHT, new AnimData(0, 2, 7, 80, false));
        animations.put(AnimState.JUMP_LEFT, new AnimData(0, 3, 7, 80, false));
        animations.put(AnimState.CROUCH_RIGHT, new AnimData(0, 4, 2, 400, false));
        animations.put(AnimState.CROUCH_LEFT, new AnimData(0, 4, 2, 400, false));
        animations.put(AnimState.PUNCH_CROUCH_RIGHT, new AnimData(3, 0, 3, 40, false));
        animations.put(AnimState.PUNCH_CROUCH_LEFT, new AnimData(3, 0, 3, 40, false));
        animations.put(AnimState.KICK_LEFT, new AnimData(2, 0, 3, 80, false));
        animations.put(AnimState.KICK_RIGHT, new AnimData(2, 1, 5, 75, false));

        animations.put(AnimState.HURT_RIGHT, new AnimData(6, 1, 3, 100, false));
        animations.put(AnimState.HURT_LEFT, new AnimData(6, 1, 3, 100, false));
        animations.put(AnimState.HURT_CROUCH_RIGHT, new AnimData(6, 2, 1, 100, false));
        animations.put(AnimState.HURT_CROUCH_LEFT, new AnimData(6, 2, 1, 100, false));
        animations.put(AnimState.HADOUKEN_RIGHT, new AnimData(4, 5, 4, 80, false));
        animations.put(AnimState.HADOUKEN_LEFT, new AnimData(4, 5, 4, 80, false));
        
        // ... (Copia qui il resto delle animazioni e aggiusta i numeri) ...
        // --- INIZIO CODICE COLORI DELLE SKIN ---
        this.availableSkins.add("/Sprites/paletteDiverseKen/ken_brown_chromakey.png");      // 0: Marrone
        this.availableSkins.add("/Sprites/paletteDiverseKen/ken_green_chromakey.png");      // 1: Verde
        this.availableSkins.add("/Sprites/paletteDiverseKen/ken_navy_chromakey.png");       // 2: Blu navy
        this.availableSkins.add("/Sprites/paletteDiverseKen/ken_paleyellow_chromakey.png"); // 3: Giallo
        this.availableSkins.add("/Sprites/paletteDiverseKen/ken_purple_chromakey.png");     // 4: Viola
        this.availableSkins.add("/Sprites/paletteDiverseKen/ken_teal_chromakey.png");       // 5: Verde acqua
        this.availableSkins.add("/Sprites/paletteDiverseKen/ken_orange_chromakey.png");     // 6: Arancione
        // --- FINE CODICE COLORI DELLE SKIN ---
    }
    public TextureAtlas getAtlas() {
        return atlas;
    }
}