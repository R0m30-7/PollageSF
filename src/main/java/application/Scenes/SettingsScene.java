package application.Scenes;

import application.Utils.Settings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button; // Usiamo il bottone standard di JavaFX per semplicità
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingsScene {

    // Passiamo lo stage e la scena precedente per poter usare il tasto "Indietro"
    public Scene getSettingsScene(Stage stage, Scene previousScene) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: cadetblue;"); // Stesso sfondo del MainMenu

        Label title = new Label("IMPOSTAZIONI");
        title.setStyle("-fx-font-size: 36px; -fx-text-fill: white; -fx-font-weight: bold;");

        // --- RECUPERA LE IMPOSTAZIONI ATTUALI ---
        Settings settings = Settings.getInstance();
        
        // --- RISOLUZIONE ---
        Label resLabel = new Label("Risoluzione Finestra:");
        resLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        ComboBox<String> resolutionBox = new ComboBox<>();
        // Aggiungiamo le risoluzioni più famose (16:9)
        resolutionBox.getItems().addAll("720x460", "800x600","1280x720", "1366x768", "1600x900", "1920x1080");
        
        // Selezioniamo di default quella attualmente in uso
        String currentRes = (int)settings.getWindowWidth() + "x" + (int)settings.getWindowHeight();
        if (!resolutionBox.getItems().contains(currentRes)) {
            resolutionBox.getItems().add(currentRes); // Se nel file c'era una risoluzione strana, la aggiunge alla lista
        }
        resolutionBox.setValue(currentRes);

        // 1. Schermo Intero (CheckBox)
        CheckBox fullscreenCheck = new CheckBox("Schermo Intero");
        fullscreenCheck.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        fullscreenCheck.setSelected(settings.isFullscreen());

        // 2. Audio (CheckBox)
        CheckBox audioCheck = new CheckBox("Audio");
        audioCheck.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        audioCheck.setSelected(settings.isAudioOn());

        // 3. Numero di Giocatori (Menu a tendina)
        Label playersLabel = new Label("Numero Giocatori:");
        playersLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        ComboBox<Integer> playersBox = new ComboBox<>();
        playersBox.getItems().addAll(1, 2); // Opzioni disponibili
        playersBox.setValue(settings.getNumberOfPlayers());
        
        // --- IMPOSTAZIONI FPS E HUD ---
        Label fpsSettingLabel = new Label("FPS:");
        fpsSettingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        ComboBox<Integer> fpsBox = new ComboBox<>();
        fpsBox.getItems().addAll(10, 15, 24, 30, 60, 120, 144, 200, 240); // Risoluzioni di aggiornamento classiche
        fpsBox.setValue(settings.getTargetFps());

        CheckBox showFpsCheck = new CheckBox("Mostra FPS");
        showFpsCheck.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        showFpsCheck.setSelected(settings.isShowFps());

        // --- BOTTONI SALVA E INDIETRO ---
        Button saveBackButton = new Button("Salva e Torna Indietro");
        saveBackButton.setStyle("-fx-font-size: 20px; -fx-padding: 10 20; -fx-cursor: hand;");
        
        saveBackButton.setOnAction(e -> {
        	// Aggiorna le impostazioni in memoria
        	// Estraiamo la larghezza e l'altezza dalla stringa (es: da "1280x720" a 1280 e 720)
            String[] resParts = resolutionBox.getValue().split("x");
            settings.setWindowWidth(Double.parseDouble(resParts[0]));
            settings.setWindowHeight(Double.parseDouble(resParts[1]));
            
            settings.setFullscreen(fullscreenCheck.isSelected());
            settings.setIsAudioOn(audioCheck.isSelected());
            settings.setNumberOfPlayers(playersBox.getValue());
            
            settings.setTargetFps(fpsBox.getValue());
            settings.setShowFps(showFpsCheck.isSelected());
            
            // Salva su file config.properties!
            settings.save();
            
            // Torna al menu principale
            stage.setTitle("Main Menu");
            stage.setScene(previousScene);
            
            // Applica la nuova risoluzione e ri-centra la finestra nello schermo
            stage.setWidth(settings.getWindowWidth());
            stage.setHeight(settings.getWindowHeight());
            stage.centerOnScreen(); // Se ingrandisci, serve per non farla uscire fuori dal monitor
            
            // Applica il fullscreen subito allo Stage
            stage.setFullScreen(settings.isFullscreen());
        });

        root.getChildren().addAll(title, resLabel, resolutionBox, fullscreenCheck, audioCheck, playersLabel, playersBox, fpsSettingLabel, fpsBox, showFpsCheck, saveBackButton);

        return new Scene(root, settings.getWindowWidth(), settings.getWindowHeight());
    }
}