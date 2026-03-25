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

        // 1. Schermo Intero (CheckBox)
        CheckBox fullscreenCheck = new CheckBox("Schermo Intero");
        fullscreenCheck.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        fullscreenCheck.setSelected(settings.isFullscreen());

        // 2. Audio (CheckBox)
        CheckBox audioCheck = new CheckBox("Abilita Audio");
        audioCheck.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        audioCheck.setSelected(settings.isAudioOn());

        // 3. Numero di Giocatori (Menu a tendina)
        Label playersLabel = new Label("Numero Giocatori:");
        playersLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        ComboBox<Integer> playersBox = new ComboBox<>();
        playersBox.getItems().addAll(1, 2); // Opzioni disponibili
        playersBox.setValue(settings.getNumberOfPlayers());

        // --- BOTTONI SALVA E INDIETRO ---
        Button saveBackButton = new Button("Salva e Torna Indietro");
        saveBackButton.setStyle("-fx-font-size: 20px; -fx-padding: 10 20; -fx-cursor: hand;");
        
        saveBackButton.setOnAction(e -> {
            // Aggiorna le impostazioni in memoria
            settings.setFullscreen(fullscreenCheck.isSelected());
            settings.setIsAudioOn(audioCheck.isSelected());
            settings.setNumberOfPlayers(playersBox.getValue());
            
            // Salva su file config.properties!
            settings.save();
            
            // Torna al menu principale
            stage.setTitle("Main Menu");
            stage.setScene(previousScene);
            
            // Applica il fullscreen subito allo Stage
            stage.setFullScreen(settings.isFullscreen());
        });

        root.getChildren().addAll(title, fullscreenCheck, audioCheck, playersLabel, playersBox, saveBackButton);

        return new Scene(root, 1280, 720); // Usa la tua risoluzione base
    }
}