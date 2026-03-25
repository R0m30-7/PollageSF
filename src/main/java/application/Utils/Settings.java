package application.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Settings {
    // 1. L'unica istanza condivisa di Settings (Singleton)
    private static Settings instance;
    private static final String FILE_PATH = "config.properties";

    private boolean isFullscreen;
    private boolean isAudioOn;
    private int numberOfPlayers;
    private double windowWidth;
    private double windowHeight;

    // 2. Costruttore privato! Si può chiamare solo da dentro questa classe
    private Settings() {
        // Valori di default
        this.isFullscreen = false;
        this.isAudioOn = true;
        this.numberOfPlayers = 2;
        
        // Risoluzione di default
        this.windowWidth = 1280.0;
        this.windowHeight = 720.0;
        
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
}