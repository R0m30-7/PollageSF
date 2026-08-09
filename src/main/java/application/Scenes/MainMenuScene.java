package application.Scenes;

import application.Controller.GameController;
import application.Utils.Button;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
        root.setStyle(
                "-fx-background-image: url('/MenuBackgrounds/titleScreenMenu.png'); " +
                "-fx-background-size: cover; " +
                "-fx-background-position: center center;"
            );

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
        root.getChildren().add(content);

        Scene mainMenuScene = new Scene(root);

        Scale scale = new Scale(1, 1, BASE_WIDTH / 2, BASE_HEIGHT / 2);
        content.getTransforms().add(scale);

        Runnable updateScale = () -> {
            double factorX = mainMenuScene.getWidth() / BASE_WIDTH;
            double factorY = mainMenuScene.getHeight() / BASE_HEIGHT;
            double factor = Math.min(factorX, factorY);
            scale.setX(factor);
            scale.setY(factor);
        };

        mainMenuScene.widthProperty().addListener((obs, oldVal, newVal) -> updateScale.run());
        mainMenuScene.heightProperty().addListener((obs, oldVal, newVal) -> updateScale.run());
        updateScale.run();

        return mainMenuScene;
    }
}