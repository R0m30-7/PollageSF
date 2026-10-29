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

    // Cache dei MediaPlayer per ricordare il secondo esatto di ogni OST
    private final Map<String, MediaPlayer> bgmCache = new HashMap<>();

    private MediaPlayer bgmPlayer;
    private String currentBgmPath = "";    // Deve partire vuoto
    
    private double bgmVolume = 0.8;
    private double sfxVolume = 0.75;

    private SoundManager() {
        loadSfx(SoundEvent.JUMP, "/Sounds/voices/2A_claw_yodel.wav");
        loadSfx(SoundEvent.PUNCH_SWING, "/Sounds/voices/2A_claw_yodel.wav");
        loadSfx(SoundEvent.HOOK_SWING, "/Sounds/voices/2A_claw_yodel.wav");
        loadSfx(SoundEvent.KICK_SWING, "/Sounds/voices/2A_claw_yodel.wav");
        loadSfx(SoundEvent.HADOUKEN, "/Sounds/voices/2A_claw_yodel.wav");
        loadSfx(SoundEvent.HIT, "/Sounds/voices/2A_claw_yodel.wav");
        loadSfx(SoundEvent.BLOCK, "/Sounds/voices/2A_claw_yodel.wav");
        loadSfx(SoundEvent.PARRY, "/Sounds/voices/2A_claw_yodel.wav");
        loadSfx(SoundEvent.KO, "/Sounds/voices/2A_claw_yodel.wav");

        loadUiSfx("cursor", "/Sounds/UI/swipe.wav");
        loadUiSfx("confirm", "/Sounds/UI/confirm.wav");
        loadUiSfx("cancel", "/Sounds/voices/2A_claw_yodel.wav");
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

    private void loadUiSfx(String key, String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                System.out.println("[Audio OK] Caricato UI SFX (" + key + "): " + url.toExternalForm());
                uiSfxCache.put(key, new AudioClip(url.toExternalForm()));
            } else {
                System.err.println("[Audio ERRORE] UI SFX NON trovato (" + key + "): " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("[Audio ECCEZIONE UI SFX] " + resourcePath + " -> " + e.getMessage());
        }
    }

    public void playBgm(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            return;
        }

        // Se è già la traccia attiva, ci assicuriamo solo che stia suonando (es. se era in pausa)
        if (resourcePath.equals(currentBgmPath) && bgmPlayer != null) {
            if (bgmPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                bgmPlayer.play();
            }
            return;
        }

        // Mettiamo in PAUSA la traccia precedente (senza distruggerla, così ricorda il secondo)
        if (bgmPlayer != null) {
            bgmPlayer.pause();
        }

        try {
            // Cerchiamo se abbiamo già caricato questa OST in precedenza
            MediaPlayer nextPlayer = bgmCache.get(resourcePath);

            if (nextPlayer == null) {
                URL url = getClass().getResource(resourcePath);
                if (url == null) {
                    System.err.println("[Audio ERRORE] BGM NON trovata nel classpath: " + resourcePath);
                    return;
                }
                System.out.println("[Audio OK] Creazione nuova BGM: " + url.toExternalForm());
                Media media = new Media(url.toExternalForm());
                media.setOnError(() -> System.err.println("[Media ERRORE] " + media.getError()));

                final MediaPlayer newPlayer = new MediaPlayer(media);
                newPlayer.setOnError(() -> System.err.println("[MediaPlayer ERRORE] " + newPlayer.getError()));
                // Quando arriva alla fine della canzone, riparte da capo in automatico
                newPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                newPlayer.setVolume(bgmVolume);

                bgmCache.put(resourcePath, nextPlayer);
                nextPlayer = newPlayer;
            }

            bgmPlayer = nextPlayer;
            currentBgmPath = resourcePath;
            bgmPlayer.setVolume(bgmVolume);
            bgmPlayer.play(); // Riprende esattamente da dove era rimasta!

        } catch (Exception e) {
            System.err.println("[Audio ECCEZIONE BGM] " + resourcePath + " -> " + e.getMessage());
            e.printStackTrace();
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
        if (bgmPlayer != null) {
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
        }
    }
}