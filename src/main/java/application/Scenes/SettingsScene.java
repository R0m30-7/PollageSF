package application.Scenes;

import application.Utils.Settings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import application.Utils.ScalableBackground;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

public class SettingsScene {

    // Risoluzione "di design": le dimensioni per cui è stata pensata l'interfaccia.
    // La UI viene scalata rispetto a queste, a prescindere dalla finestra attuale.
    private static final double BASE_WIDTH = 1280;
    private static final double BASE_HEIGHT = 720;

    public Scene getSettingsScene(Stage stage, Scene previousScene) {

        StackPane root = new StackPane();

        // --- CONTENUTO: questo è il VBox che verrà scalato proporzionalmente ---
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        // Fissiamo il contenuto alla risoluzione di design: verrà "ingrandito/rimpicciolito"
        // tramite transform, non tramite layout, quindi resta sempre proporzionato.
        content.setPrefSize(BASE_WIDTH, BASE_HEIGHT);
        content.setMaxSize(BASE_WIDTH, BASE_HEIGHT);

        Label title = new Label("IMPOSTAZIONI");
        title.setStyle("-fx-font-size: 36px; -fx-text-fill: white; -fx-font-weight: bold;");

        Settings settings = Settings.getInstance();

        // --- RISOLUZIONE ---
        Label resLabel = new Label("Risoluzione Finestra:");
        resLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        ComboBox<String> resolutionBox = new ComboBox<>();
        resolutionBox.getItems().addAll("670x670", "720x460", "800x600", "1280x720", "1366x768", "1600x900", "1920x1080");

        String currentRes = (int) settings.getWindowWidth() + "x" + (int) settings.getWindowHeight();
        if (!resolutionBox.getItems().contains(currentRes)) {
            resolutionBox.getItems().add(currentRes);
        }
        resolutionBox.setValue(currentRes);

        CheckBox fullscreenCheck = new CheckBox("Schermo Intero");
        fullscreenCheck.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        fullscreenCheck.setSelected(settings.isFullscreen());

        CheckBox audioCheck = new CheckBox("Audio");
        audioCheck.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        audioCheck.setSelected(settings.isAudioOn());

        Label playersLabel = new Label("Numero Giocatori:");
        playersLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        ComboBox<Integer> playersBox = new ComboBox<>();
        playersBox.getItems().addAll(1, 2);
        playersBox.setValue(settings.getNumberOfPlayers());

        Label fpsSettingLabel = new Label("FPS:");
        fpsSettingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        ComboBox<Integer> fpsBox = new ComboBox<>();
        fpsBox.getItems().addAll(10, 15, 24, 30, 60, 120, 144, 200, 240);
        fpsBox.setValue(settings.getTargetFps());

        CheckBox showFpsCheck = new CheckBox("Mostra FPS");
        showFpsCheck.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        showFpsCheck.setSelected(settings.isShowFps());

        Button saveBackButton = new Button("Salva e Torna Indietro");
        saveBackButton.setStyle("-fx-font-size: 20px; -fx-padding: 10 20; -fx-cursor: hand;");

        saveBackButton.setOnAction(e -> {
            String[] resParts = resolutionBox.getValue().split("x");
            settings.setWindowWidth(Double.parseDouble(resParts[0]));
            settings.setWindowHeight(Double.parseDouble(resParts[1]));

            settings.setFullscreen(fullscreenCheck.isSelected());
            settings.setIsAudioOn(audioCheck.isSelected());
            settings.setNumberOfPlayers(playersBox.getValue());

            settings.setTargetFps(fpsBox.getValue());
            settings.setShowFps(showFpsCheck.isSelected());

            settings.save();

            stage.setTitle("Main Menu");
            stage.setScene(previousScene);

            stage.setWidth(settings.getWindowWidth());
            stage.setHeight(settings.getWindowHeight());
            stage.centerOnScreen();

            stage.setFullScreen(settings.isFullscreen());
        });

        content.getChildren().addAll(title, resLabel, resolutionBox, fullscreenCheck, audioCheck,
                playersLabel, playersBox, fpsSettingLabel, fpsBox, showFpsCheck, saveBackButton);

        Scene scene = new Scene(root, settings.getWindowWidth(), settings.getWindowHeight());

        // --- SFONDO: ImageView con scaling "cover" uniforme, sempre centrato ---
        ImageView background = ScalableBackground.create("/MenuBackgrounds/settingsMenu.png", scene);
        root.getChildren().add(background);
        root.getChildren().add(content);

        // --- SCALING DEL CONTENUTO ---
        // Il contenuto viene "clippato" alla dimensione di design (BASE_WIDTH x BASE_HEIGHT)
        // e poi scalato con una Scale transform in base al rapporto tra la dimensione
        // reale della finestra e quella di design.
        Scale scale = new Scale(1, 1, BASE_WIDTH / 2, BASE_HEIGHT / 2);
        content.getTransforms().add(scale);

        // Fattore UNICO per X e Y, e soprattutto lo STESSO tipo di calcolo usato per
        // lo sfondo (Math.max = "cover"). Se usassimo Math.min ("contain") qui, il
        // contenuto scalerebbe con un fattore diverso da quello dello sfondo e i due
        // finirebbero per disallinearsi (i bottoni sembrerebbero "scentrati" rispetto
        // agli elementi dello sfondo, anche se in realtà sono centrati nella finestra).
        Runnable updateScale = () -> {
            double factorX = scene.getWidth() / BASE_WIDTH;
            double factorY = scene.getHeight() / BASE_HEIGHT;
            double factor = Math.max(factorX, factorY);
            scale.setX(factor);
            scale.setY(factor);
        };

        scene.widthProperty().addListener((obs, oldVal, newVal) -> updateScale.run());
        scene.heightProperty().addListener((obs, oldVal, newVal) -> updateScale.run());
        updateScale.run();

        return scene;
    }
}