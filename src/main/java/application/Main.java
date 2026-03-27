package application;

// --- IMPORT ESPLICITI (Così Eclipse non si confonde) ---
import application.Scenes.MainMenuScene;
import application.Utils.Settings;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Carica delle impostazioni salvate usando l'import pulito
        Settings config = Settings.getInstance();
        
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

    // Questo è l'avvio di JavaFX
    public static void main(String[] args) {
        launch(args);
    }
}