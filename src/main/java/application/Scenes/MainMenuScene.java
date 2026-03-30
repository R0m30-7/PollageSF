package application.Scenes;

import application.Controller.GameController;
import application.Utils.Button;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenuScene {
	// Questo metodo costruisce e restituisce l'intera scena del MainMenu
	public Scene getScenaMenu(Stage stage) {
		// Rappresenta il contenitore principale della scena
		StackPane root = new StackPane();
		root.setStyle(
			    "-fx-background-image: url('/MenuBackgrounds/titleScreenMenu.png'); " +
			    "-fx-background-size: cover; " +
			    "-fx-background-position: center center;"
			);

		VBox MenuButContainer = new VBox(20); 	// Il 20 indica 20 pixel di spazio tra un bottone e l'altro
		MenuButContainer.setAlignment(Pos.CENTER);
		MenuButContainer.setTranslateY(150);	// Per evitare che i pulsanti si sovrappongano con lo sfondo
		
		Button[] MenuButList = new Button[3];	// Voglio 3 pulsanti nel menu

		// 1. IL PERCORSO CORRETTO PER MAVEN (La cartella resources è diventata la radice "/")
		java.net.URL path = getClass().getResource("/Buttons/MainMenuAtlas.png");

		// 2. Controllo se esiste
		if (path == null) {
		    System.out.println("Caricamento dell'atlas fallito. Controlla il nome o la cartella!");
		} else {
		    // 3. Se lo trova, procedo a ritagliare i pulsanti. 
		    // Il tuo uso di toExternalForm() qui è perfetto!
		    MenuButList[0] = new Button(path.toExternalForm(), 0, 0, 140, 56);
		    MenuButList[1] = new Button(path.toExternalForm(), 0, 56, 140, 56);
		    MenuButList[2] = new Button(path.toExternalForm(), 0, 112, 140, 56);
		}
		
		MenuButList[0].setAction(() -> {
			stage.setTitle("Play Game");
			
			// Creiamo il controller passandogli la finestra principale
	        GameController controller = new GameController(stage);
	        // Diciamo al controller di preparare e avviare il gioco
	        controller.startGame();
			stage.centerOnScreen();
			stage.setFullScreen(application.Utils.Settings.getInstance().isFullscreen());
		});
		MenuButList[1].setAction(() -> {
			stage.setTitle("Settings Menu");
			
			SettingsScene settingsScene = new SettingsScene();
			Scene currentMenuScene = root.getScene();
			
			// Passiamo allo stage la scena ATTUALE (mainMenuScene)
			stage.setScene(settingsScene.getSettingsScene(stage, currentMenuScene));
			
			// Forziamo il fullscreen se era attivo
			stage.setFullScreen(application.Utils.Settings.getInstance().isFullscreen());
		});
		MenuButList[2].setAction(() -> {
			System.exit(0);
		});
		
		MenuButContainer.getChildren().addAll(MenuButList);
		root.getChildren().add(MenuButContainer);
		
		Scene mainMenuScene = new Scene(root);
		
		return mainMenuScene;
	}
}
