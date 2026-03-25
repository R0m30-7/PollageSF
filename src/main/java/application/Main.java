package application;

import java.io.File;

import application.Scenes.MainMenuScene;
import application.Utils.GameConfig;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
    	// Carica delle impostazioni salvate
    	application.Utils.Settings config = application.Utils.Settings.getInstance();
    	
    	// Impostiamo la scena del menu principale
    	primaryStage.setScene(new MainMenuScene().getScenaMenu(primaryStage));
    	
    	// Impostiamo le dimensioni caricate o, altrimenti, di default
    	primaryStage.setWidth(config.getWindowWidth());
        primaryStage.setHeight(config.getWindowHeight());
        
        // Blocco del ridimensionamento manuale
        primaryStage.setResizable(false);
        
        // Applichiamo lo schermo intero se l'utente l'aveva salvato
        primaryStage.setFullScreen(config.isFullscreen());
        
        primaryStage.show();
    }

    // Questo è l'unico main di tutto il tuo progetto!
    public static void main(String[] args) {
    	// 1. DEVE ESSERE LA PRIMA RIGA IN ASSOLUTO DEL PROGRAMMA
        String nativesPath = new File("target/natives").getAbsolutePath();
        System.setProperty("net.java.games.input.librarypath", nativesPath);
        
        // (Opzionale ma utile) Stampa il percorso per debug così vediamo se è corretto
        System.out.println("Cerco le librerie JInput in: " + nativesPath);

        // 2. Solo dopo avvii l'interfaccia grafica
        launch(args);
    }
}