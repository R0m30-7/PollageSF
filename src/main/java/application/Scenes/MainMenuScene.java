package application.Scenes;

import application.Controller.GameController;
import application.Utils.Button;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import application.Utils.ScalableBackground;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

public class MainMenuScene {

    // Stessa risoluzione di design usata in SettingsScene, per coerenza tra le schermate
    private static final double BASE_WIDTH = 1280;
    private static final double BASE_HEIGHT = 720;

    public Scene getScenaMenu(Stage stage) {
        StackPane root = new StackPane();

        // Il contenuto (bottoni) viene "congelato" alla dimensione di design e poi
        // scalato con una Scale transform uniforme, esattamente come in SettingsScene.
        StackPane content = new StackPane();
        content.setPrefSize(BASE_WIDTH, BASE_HEIGHT);
        content.setMaxSize(BASE_WIDTH, BASE_HEIGHT);

        VBox MenuButContainer = new VBox(20);
        MenuButContainer.setAlignment(Pos.CENTER);
        MenuButContainer.setTranslateY(150);

        Button[] MenuButList = new Button[3];

        java.net.URL path = getClass().getResource("/Buttons/MainMenuAtlas.png");

        if (path == null) {
            System.out.println("Caricamento dell'atlas fallito. Controlla il nome o la cartella!");
        } else {
            MenuButList[0] = new Button(path.toExternalForm(), 0, 0, 140, 56);
            MenuButList[1] = new Button(path.toExternalForm(), 0, 56, 140, 56);
            MenuButList[2] = new Button(path.toExternalForm(), 0, 112, 140, 56);
        }

        MenuButList[0].setAction(() -> {
            stage.setTitle("Play Game");

            GameController controller = new GameController(stage);
            controller.startGame();
            stage.centerOnScreen();
            stage.setFullScreen(application.Utils.Settings.getInstance().isFullscreen());
        });
        MenuButList[1].setAction(() -> {
            stage.setTitle("Settings Menu");

            SettingsScene settingsScene = new SettingsScene();
            Scene currentMenuScene = root.getScene();

            stage.setScene(settingsScene.getSettingsScene(stage, currentMenuScene));
            stage.setFullScreen(application.Utils.Settings.getInstance().isFullscreen());
        });
        MenuButList[2].setAction(() -> {
            System.exit(0);
        });

        MenuButContainer.getChildren().addAll(MenuButList);
        content.getChildren().add(MenuButContainer);

        Scene mainMenuScene = new Scene(root);

        // --- SFONDO: ImageView con scaling "cover" uniforme, sempre centrato ---
        ImageView background = ScalableBackground.create("/MenuBackgrounds/titleScreenMenu.png", mainMenuScene);
        root.getChildren().add(background);
        root.getChildren().add(content);

        Scale scale = new Scale(1, 1, BASE_WIDTH / 2, BASE_HEIGHT / 2);
        content.getTransforms().add(scale);

        // Stesso fattore (Math.max, "cover") usato per lo sfondo: contenuto e sfondo
        // devono scalare insieme allo stesso ritmo, altrimenti si disallineano.
        Runnable updateScale = () -> {
            double factorX = mainMenuScene.getWidth() / BASE_WIDTH;
            double factorY = mainMenuScene.getHeight() / BASE_HEIGHT;
            double factor = Math.max(factorX, factorY);
            scale.setX(factor);
            scale.setY(factor);
        };

        mainMenuScene.widthProperty().addListener((obs, oldVal, newVal) -> updateScale.run());
        mainMenuScene.heightProperty().addListener((obs, oldVal, newVal) -> updateScale.run());
        updateScale.run();

        return mainMenuScene;
    }
}