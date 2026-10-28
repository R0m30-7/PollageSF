package pgsf.utils;

import java.net.URL;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import pgsf.model.SoundEvent;

public class SoundManager {
    private static SoundManager instance;

    private final Map<SoundEvent, AudioClip> sfxCache = new EnumMap<>(SoundEvent.class);
    private final Map<String, AudioClip> uiSfxCache = new HashMap<>();

    private MediaPlayer bgmPlayer;
    private String currentBgmPath = "";

    private double bgmVolume = 0.35;
    private double sfxVolume = 0.75;

    private SoundManager() {
        loadSfx(SoundEvent.JUMP, "/home/paolo/VS_CODE_WORKSPACE/JavaWorkSpace/PollageSF/target/classes/Sounds/voices/2A_claw_yodel.wav");
        loadSfx(SoundEvent.PUNCH_SWING, "/home/paolo/VS_CODE_WORKSPACE/JavaWorkSpace/PollageSF/target/classes/Sounds/voices/2A_claw_yodel.wav");
        loadSfx(SoundEvent.HOOK_SWING, "/home/paolo/VS_CODE_WORKSPACE/JavaWorkSpace/PollageSF/target/classes/Sounds/2A_claw_yodel.wav");
        loadSfx(SoundEvent.KICK_SWING, "/home/paolo/VS_CODE_WORKSPACE/JavaWorkSpace/PollageSF/target/classes/Sounds/voices/56_chunli_yatta.wav");
        loadSfx(SoundEvent.HADOUKEN, "/home/paolo/VS_CODE_WORKSPACE/JavaWorkSpace/PollageSF/target/classes/Sounds/hadouken.wav");
        loadSfx(SoundEvent.HIT, "/home/paolo/VS_CODE_WORKSPACE/JavaWorkSpace/PollageSF/target/classes/Sounds/hit.wav");
        loadSfx(SoundEvent.BLOCK, "/home/paolo/VS_CODE_WORKSPACE/JavaWorkSpace/PollageSF/target/classes/Sounds/block.wav");
        loadSfx(SoundEvent.PARRY, "/home/paolo/VS_CODE_WORKSPACE/JavaWorkSpace/PollageSF/target/classes/Sounds/parry.wav");
        loadSfx(SoundEvent.KO, "/home/paolo/VS_CODE_WORKSPACE/JavaWorkSpace/PollageSF/target/classes/Sounds/ko.wav");

        loadUiSfx("cursor", "/home/paolo/VS_CODE_WORKSPACE/JavaWorkSpace/PollageSF/target/classes/Sounds/voices/56_chunli_yatta.wav");
        loadUiSfx("confirm", "/home/paolo/VS_CODE_WORKSPACE/JavaWorkSpace/PollageSF/target/classes/Sounds/voices/56_chunli_yatta.wav");
        loadUiSfx("cancel", "/home/paolo/VS_CODE_WORKSPACE/JavaWorkSpace/PollageSF/target/classes/Sounds/voices/56_chunli_yatta.wav");
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void loadSfx(SoundEvent event, String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                System.out.println("[Audio OK] Caricato SFX: " + url.toExternalForm());
                sfxCache.put(event, new AudioClip(url.toExternalForm()));
            } else {
                System.err.println("[Audio ERRORE] File NON trovato nel classpath: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("[Audio ECCEZIONE] " + resourcePath + " -> " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void playBgm(String resourcePath) {
        if (resourcePath == null || resourcePath.equals(currentBgmPath)) {
            return;
        }
        stopBgm();
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                System.out.println("[Audio OK] Avvio BGM: " + url.toExternalForm());
                Media media = new Media(url.toExternalForm());
                media.setOnError(() -> System.err.println("[Media ERRORE] " + media.getError()));
                
                bgmPlayer = new MediaPlayer(media);
                bgmPlayer.setOnError(() -> System.err.println("[MediaPlayer ERRORE] " + bgmPlayer.getError()));
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                bgmPlayer.setVolume(bgmVolume);
                bgmPlayer.play();
                currentBgmPath = resourcePath;
            } else {
                System.err.println("[Audio ERRORE] BGM NON trovata nel classpath: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("[Audio ECCEZIONE BGM] " + resourcePath + " -> " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUiSfx(String key, String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                uiSfxCache.put(key, new AudioClip(url.toExternalForm()));
            }
        } catch (Exception e) {
            System.out.println("[Audio] Errore caricamento UI SFX: " + resourcePath);
        }
    }

    public void playSfx(SoundEvent event) {
        AudioClip clip = sfxCache.get(event);
        if (clip != null) {
            clip.play(sfxVolume);
        }
    }

    public void playUiSfx(String key) {
        AudioClip clip = uiSfxCache.get(key);
        if (clip != null) {
            clip.play(sfxVolume);
        }
    }

    
    public void pauseBgm() {
        if (bgmPlayer != null && bgmPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            bgmPlayer.pause();
        }
    }

    public void resumeBgm() {
        if (bgmPlayer != null && bgmPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
            bgmPlayer.play();
        }
    }

    public void stopBgm() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
            currentBgmPath = "";
        }
    }
}