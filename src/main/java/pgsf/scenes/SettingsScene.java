package pgsf.scenes;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import pgsf.utils.ScalableBackground;
import pgsf.utils.Settings;

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
        content.setMinSize(BASE_WIDTH, BASE_HEIGHT);

        Label title = new Label("IMPOSTAZIONI");
        title.setStyle("-fx-font-size: 36px; -fx-text-fill: white; -fx-font-weight: bold;");

        Settings settings = Settings.getInstance();

        // --- RISOLUZIONE ---
        Label resLabel = new Label("Risoluzione Finestra:");
        resLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        ComboBox<String> resolutionBox = new ComboBox<>();

        // Mostriamo SEMPRE tutte le risoluzioni: quelle che non entrano nello schermo
        // disponibile (visualBounds esclude taskbar/barre di sistema) restano visibili
        // ma disabilitate/scolorite, non selezionabili.
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        String[] allResolutions = { "670x670", "720x460", "800x600", "1280x720", "1366x768", "1600x900", "1920x1080" };
        resolutionBox.getItems().addAll(allResolutions);

        String currentRes = (int) settings.getWindowWidth() + "x" + (int) settings.getWindowHeight();
        if (!resolutionBox.getItems().contains(currentRes)) {
            resolutionBox.getItems().add(currentRes);
        }

        // Helper: la risoluzione "res" (formato "WxH") entra nello schermo disponibile?
        java.util.function.Predicate<String> fitsScreen = res -> {
            String[] parts = res.split("x");
            double w = Double.parseDouble(parts[0]);
            double h = Double.parseDouble(parts[1]);
            return w <= screenBounds.getWidth() && h <= screenBounds.getHeight();
        };

        // Disabilita e scolorisce visivamente le voci troppo grandi nella lista a tendina.
        resolutionBox.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                    setOpacity(1.0);
                } else {
                    setText(item);
                    boolean fits = fitsScreen.test(item);
                    setDisable(!fits);
                    setOpacity(fits ? 1.0 : 0.4);
                }
            }
        });

        // Rete di sicurezza: disabilitare la cella blocca il click del mouse, ma non
        // sempre la selezione da tastiera. Se per caso finisse selezionata una
        // risoluzione troppo grande, si torna automaticamente all'ultima valida.
        resolutionBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !fitsScreen.test(newVal)) {
                resolutionBox.setValue(oldVal != null && fitsScreen.test(oldVal) ? oldVal : currentRes);
            }
        });

        resolutionBox.setValue(fitsScreen.test(currentRes) ? currentRes
                : allResolutions[0]); // fallback: la più piccola prevista, se anche quella salvata non entra più

        CheckBox fullscreenCheck = new CheckBox("Schermo Intero");
        fullscreenCheck.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        fullscreenCheck.setSelected(settings.isFullscreen());

        // --- VOLUME MUSICA (BGM) ---
        Label bgmLabel = new Label("Volume Musica:");
        bgmLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        
        javafx.scene.control.Slider bgmSlider = new javafx.scene.control.Slider(0, 1, settings.getBgmVolume());
        bgmSlider.setBlockIncrement(0.1);
        bgmSlider.setMajorTickUnit(0.1);
        bgmSlider.setMaxWidth(250);
        
        javafx.scene.control.TextField bgmField = new javafx.scene.control.TextField(String.valueOf((int) Math.round(settings.getBgmVolume() * 100)));
        bgmField.setPrefWidth(60);
        bgmField.setAlignment(Pos.CENTER);
        
        // Affianchiamo slider e casella di testo
        javafx.scene.layout.HBox bgmBox = new javafx.scene.layout.HBox(15, bgmSlider, bgmField);
        bgmBox.setAlignment(Pos.CENTER);
        
        // Aggiorna casella e audio quando muovi lo slider
        bgmSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            settings.setBgmVolume(newVal.doubleValue());
            pgsf.utils.SoundManager.getInstance().setBgmVolume(newVal.doubleValue());
            if (!bgmField.isFocused()) { // Aggiorna il testo solo se non ci stiamo scrivendo dentro
                bgmField.setText(String.valueOf((int) Math.round(newVal.doubleValue() * 100)));
            }
        });
        
        // Aggiorna lo slider quando scrivi nel testo (premendo Invio o cliccando altrove)
        bgmField.setOnAction(e -> updateSliderFromText(bgmField, bgmSlider));
        bgmField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) updateSliderFromText(bgmField, bgmSlider); 
        });

        // --- VOLUME EFFETTI (SFX) ---
        Label sfxLabel = new Label("Volume Effetti (SFX):");
        sfxLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        
        javafx.scene.control.Slider sfxSlider = new javafx.scene.control.Slider(0, 1, settings.getSfxVolume());
        sfxSlider.setBlockIncrement(0.1);
        sfxSlider.setMajorTickUnit(0.1);
        sfxSlider.setMaxWidth(250);
        
        javafx.scene.control.TextField sfxField = new javafx.scene.control.TextField(String.valueOf((int) Math.round(settings.getSfxVolume() * 100)));
        sfxField.setPrefWidth(60);
        sfxField.setAlignment(Pos.CENTER);
        
        // Affianchiamo slider e casella di testo
        javafx.scene.layout.HBox sfxBox = new javafx.scene.layout.HBox(15, sfxSlider, sfxField);
        sfxBox.setAlignment(Pos.CENTER);
        
        sfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            settings.setSfxVolume(newVal.doubleValue());
            pgsf.utils.SoundManager.getInstance().setSfxVolume(newVal.doubleValue());
            if (!sfxField.isFocused()) {
                sfxField.setText(String.valueOf((int) Math.round(newVal.doubleValue() * 100)));
            }
        });
        
        sfxField.setOnAction(e -> updateSliderFromText(sfxField, sfxSlider));
        sfxField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) updateSliderFromText(sfxField, sfxSlider);
        });

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
            double chosenWidth = Double.parseDouble(resParts[0]);
            double chosenHeight = Double.parseDouble(resParts[1]);

            // Misura di sicurezza finale: anche se per qualche motivo arrivasse qui un
            // valore troppo grande, non viene mai applicato oltre lo spazio disponibile.
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            chosenWidth = Math.min(chosenWidth, bounds.getWidth());
            chosenHeight = Math.min(chosenHeight, bounds.getHeight());

            settings.setWindowWidth(chosenWidth);
            settings.setWindowHeight(chosenHeight);

            settings.setFullscreen(fullscreenCheck.isSelected());

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

        content.getChildren().addAll(title, resLabel, resolutionBox, fullscreenCheck,
                bgmLabel, bgmBox, sfxLabel, sfxBox,
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

    // Metodo helper per aggiornare lo slider leggendo il testo inserito
    private void updateSliderFromText(javafx.scene.control.TextField field, javafx.scene.control.Slider slider) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            value = Math.max(0, Math.min(100, value)); // Blocca il valore tra 0 e 100
            slider.setValue(value / 100.0);
            field.setText(String.valueOf(value));
        } catch (NumberFormatException ex) {
            // Se l'utente inserisce lettere o caratteri non validi, ripristina il valore attuale
            field.setText(String.valueOf((int) Math.round(slider.getValue() * 100)));
        }
    }
}