package application.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Settings {
    // 1. L'unica istanza condivisa di Settings (Singleton)
    private static Settings instance;
    private static final String FILE_PATH = "config.properties";

    private boolean isFullscreen = false;
    private boolean isAudioOn = false;
    private int numberOfPlayers = 1;
    private double windowWidth;
    private double windowHeight;
    
    // Variabili per la gestione degli FPS
    private int targetFps = 60;
    private boolean showFps = false;

    // 2. Costruttore privato! Si può chiamare solo da dentro questa classe
    private Settings() {
        // Valori di default
        this.isFullscreen = false;
        this.isAudioOn = true;
        this.numberOfPlayers = 2;
        
        // Risoluzione di default
        this.windowWidth = 1280.0;
        this.windowHeight = 720.0;
        
        // Default Fps
        
        // Appena viene creato, prova a caricare i salvataggi
        load();
    }

    // 3. Metodo per ottenere le impostazioni da qualsiasi parte del gioco
    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    // --- LOGICA DI SALVATAGGIO SU FILE ---
    public void load() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(fis);

                this.isFullscreen = Boolean.parseBoolean(props.getProperty("isFullscreen", "false"));
                this.isAudioOn = Boolean.parseBoolean(props.getProperty("isAudioOn", "true"));
                this.numberOfPlayers = Integer.parseInt(props.getProperty("numberOfPlayers", "2"));
                
                this.windowWidth = Double.parseDouble(props.getProperty("windowWidth", "1280.0"));
                this.windowHeight = Double.parseDouble(props.getProperty("windowHeight", "720.0"));
                
                // Carico le impostazioni degli FPS
                this.targetFps = Integer.parseInt(props.getProperty("targetFps", "200"));
                this.showFps = Boolean.parseBoolean(props.getProperty("showFps", "false"));
            } catch (Exception e) {
                System.out.println("Errore caricamento impostazioni: " + e.getMessage());
            }
        }
    }

    public void save() {
        Properties props = new Properties();
        props.setProperty("isFullscreen", String.valueOf(isFullscreen));
        props.setProperty("isAudioOn", String.valueOf(isAudioOn));
        props.setProperty("numberOfPlayers", String.valueOf(numberOfPlayers));
        
        props.setProperty("windowWidth", String.valueOf(windowWidth));
        props.setProperty("windowHeight", String.valueOf(windowHeight));
        
        // Salvataggio delle impostazioni degli FPS
        props.setProperty("targetFps", String.valueOf(targetFps));
        props.setProperty("showFps", String.valueOf(showFps));

        try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
            props.store(fos, "Impostazioni di Gioco");
        } catch (Exception e) {
            System.out.println("Errore salvataggio impostazioni: " + e.getMessage());
        }
    }

    // --- GETTER E SETTER ---
    public boolean isFullscreen() { return isFullscreen; }
    public void setFullscreen(boolean fullscreen) { isFullscreen = fullscreen; }
    
    public int getNumberOfPlayers() { return numberOfPlayers; }
    public void setNumberOfPlayers(int number) { numberOfPlayers = number; }
    
    public boolean isAudioOn() { return isAudioOn; }
    public void setIsAudioOn(boolean audioOn) { isAudioOn = audioOn; }
    
    public double getWindowWidth() { return windowWidth; }
    public void setWindowWidth(double width) { this.windowWidth = width; }
    
    public double getWindowHeight() { return windowHeight; }
    public void setWindowHeight(double height) { this.windowHeight = height; }
    
    public int getTargetFps() { return targetFps; }
    public void setTargetFps(int fps) { this.targetFps = fps; }
    
    public boolean isShowFps() { return showFps; }
    public void setShowFps(boolean show) { this.showFps = show; }
}