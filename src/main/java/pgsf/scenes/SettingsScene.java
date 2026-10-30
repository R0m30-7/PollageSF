package pgsf.scenes;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import pgsf.utils.ScalableBackground;
import pgsf.utils.Settings;

public class SettingsScene {

    // Risoluzione "di design": le dimensioni per cui è stata pensata l'interfaccia.
    // La UI viene scalata rispetto a queste, a prescindere dalla finestra attuale.
    private static final double BASE_WIDTH = 1280;
    private static final double BASE_HEIGHT = 720;
    
    private final Settings settings = Settings.getInstance();

    // --- VARIABILI DI CLASSE PER I CONTROLLI DA SALVARE ---
    // Invece di recuperarli tramite getChildren().get(x) che causa errori se cambia il layout,
    // li memorizziamo qui così il pulsante Salva li trova sempre e in modo sicuro.
    private ComboBox<String> resolutionBox;
    private CheckBox fullscreenCheck;
    private ComboBox<Integer> playersBox;
    private ComboBox<Integer> fpsBox;
    private CheckBox showFpsCheck;

    // --- STILI CSS A TEMA CORNICE DI LEGNO E CIELO NOTTURNO ---
    // -fx-base gestisce in automatico i colori del menu a tendina a comparsa (sfondo e testo)
    private static final String COMBO_STYLE = "-fx-base: #5c3a21; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #8b5a2b; -fx-border-width: 2;";
    // -fx-text-inner-color serve specificatamente per il testo digitabile nei TextField
    private static final String TEXTFIELD_STYLE = "-fx-background-color: rgba(20, 15, 10, 0.8); -fx-text-inner-color: #f1c40f; -fx-font-weight: bold; -fx-border-color: #8b5a2b; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 16px;";
    private static final String LABEL_STYLE = "-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 5, 1.0, 2, 2);";

    public Scene getSettingsScene(Stage stage, Scene previousScene) {
        StackPane root = new StackPane();

        // --- CONTENUTO: questo è il VBox che verrà scalato proporzionalmente ---
        VBox content = createContentContainer();

        Label title = new Label("IMPOSTAZIONI");
        // Titolo dorato con ombra pronunciata per risaltare sul cielo dello sfondo
        title.setStyle("-fx-font-size: 46px; -fx-text-fill: #f1c40f; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 10, 0.7, 3, 3);");

        // --- Inizializzazione delle Sezioni ---
        VBox resolutionSection = createResolutionSection();
        createFullscreenCheck();
        VBox audioSection = createAudioSection();
        VBox otherSettingsSection = createOtherSettingsSection();

        // --- Pulsante di Salvataggio ---
        Button saveBackButton = createSaveButton(stage, previousScene);

        content.getChildren().addAll(title, resolutionSection, fullscreenCheck, audioSection, otherSettingsSection, saveBackButton);

        Scene scene = new Scene(root, settings.getWindowWidth(), settings.getWindowHeight());

        // --- SFONDO: ImageView con scaling "cover" uniforme, sempre centrato ---
        ImageView background = ScalableBackground.create("/MenuBackgrounds/settingsMenu.png", scene);
        root.getChildren().add(background);
        root.getChildren().add(content);

        applyScaling(scene, content);

        return scene;
    }

    private VBox createContentContainer() {
        VBox content = new VBox(22); 
        content.setAlignment(Pos.CENTER);
        
        // Fissiamo il contenuto alla risoluzione di design: verrà "ingrandito/rimpicciolito"
        // tramite transform, non tramite layout, quindi resta sempre proporzionato.
        content.setPrefSize(BASE_WIDTH, BASE_HEIGHT);
        
        // Dimensioniamo il contenuto in modo che stia circa dentro la cornice di legno dipinta
        content.setMaxSize(BASE_WIDTH * 0.70, BASE_HEIGHT * 0.70); 
        
        content.setStyle("-fx-background-color: transparent; -fx-padding: 10;");

        content.setTranslateX(28);
                         
        return content;
    }

    private VBox createResolutionSection() {
        VBox section = new VBox(8);
        section.setAlignment(Pos.CENTER);

        // --- RISOLUZIONE ---
        Label resLabel = new Label("Risoluzione Finestra:");
        resLabel.setStyle(LABEL_STYLE);
        
        // Inizializza la variabile di classe
        resolutionBox = new ComboBox<>();
        resolutionBox.setStyle(COMBO_STYLE);
        resolutionBox.setPrefWidth(200);

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

        section.getChildren().addAll(resLabel, resolutionBox);
        return section;
    }

    private void createFullscreenCheck() {
        fullscreenCheck = new CheckBox("Schermo Intero");
        fullscreenCheck.setStyle(LABEL_STYLE);
        fullscreenCheck.setSelected(settings.isFullscreen());
    }

    private VBox createAudioSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);

        // --- VOLUME MUSICA E SFX ---
        HBox bgmBox = createAudioSliderBox("Volume Musica:", settings.getBgmVolume(), true);
        HBox sfxBox = createAudioSliderBox("Volume Effetti (SFX):", settings.getSfxVolume(), false);

        section.getChildren().addAll(bgmBox, sfxBox);
        return section;
    }

    private HBox createAudioSliderBox(String labelText, double initialVolume, boolean isBgm) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);
        container.setPrefWidth(250);

        Label label = new Label(labelText);
        label.setStyle(LABEL_STYLE);

        javafx.scene.control.Slider slider = new javafx.scene.control.Slider(0, 1, initialVolume);
        slider.setBlockIncrement(0.1);
        slider.setMajorTickUnit(0.1);
        slider.setPrefWidth(230);

        javafx.scene.control.TextField field = new javafx.scene.control.TextField(String.valueOf((int) Math.round(initialVolume * 100)));
        field.setPrefWidth(60);
        field.setAlignment(Pos.CENTER);
        field.setStyle(TEXTFIELD_STYLE); // Usa lo stile specifico con -fx-text-inner-color

        // Aggiorna casella e audio quando muovi lo slider
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isBgm) {
                settings.setBgmVolume(newVal.doubleValue());
                pgsf.utils.SoundManager.getInstance().setBgmVolume(newVal.doubleValue());
            } else {
                settings.setSfxVolume(newVal.doubleValue());
                pgsf.utils.SoundManager.getInstance().setSfxVolume(newVal.doubleValue());
            }
            if (!field.isFocused()) { // Aggiorna il testo solo se non ci stiamo scrivendo dentro
                field.setText(String.valueOf((int) Math.round(newVal.doubleValue() * 100)));
            }
        });

        // Aggiorna lo slider quando scrivi nel testo (premendo Invio o cliccando altrove)
        field.setOnAction(e -> updateSliderFromText(field, slider));
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) updateSliderFromText(field, slider);
        });

        container.getChildren().addAll(label, slider);
        
        // Affianchiamo slider e casella di testo
        HBox hbox = new HBox(15, container, field);
        hbox.setAlignment(Pos.BOTTOM_CENTER);
        return hbox;
    }

    private VBox createOtherSettingsSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);

        HBox combosBox = new HBox(40);
        combosBox.setAlignment(Pos.CENTER);

        // Blocco Giocatori
        VBox playersSection = new VBox(5);
        playersSection.setAlignment(Pos.CENTER);
        Label playersLabel = new Label("Giocatori:");
        playersLabel.setStyle(LABEL_STYLE);
        
        playersBox = new ComboBox<>();
        playersBox.getItems().addAll(1, 2);
        playersBox.setValue(settings.getNumberOfPlayers());
        playersBox.setStyle(COMBO_STYLE);
        playersBox.setPrefWidth(100);
        playersSection.getChildren().addAll(playersLabel, playersBox);

        // Blocco FPS
        VBox fpsSection = new VBox(5);
        fpsSection.setAlignment(Pos.CENTER);
        Label fpsSettingLabel = new Label("Max FPS:");
        fpsSettingLabel.setStyle(LABEL_STYLE);
        
        fpsBox = new ComboBox<>();
        fpsBox.getItems().addAll(10, 15, 24, 30, 60, 120, 144, 200, 240);
        fpsBox.setValue(settings.getTargetFps());
        fpsBox.setStyle(COMBO_STYLE);
        fpsBox.setPrefWidth(120);
        fpsSection.getChildren().addAll(fpsSettingLabel, fpsBox);

        combosBox.getChildren().addAll(playersSection, fpsSection);

        showFpsCheck = new CheckBox("Mostra contatore FPS in gioco");
        showFpsCheck.setStyle(LABEL_STYLE);
        showFpsCheck.setSelected(settings.isShowFps());

        section.getChildren().addAll(combosBox, showFpsCheck);
        return section;
    }

    private Button createSaveButton(Stage stage, Scene previousScene) {
        Button saveBackButton = new Button("SALVA E TORNA AL MENU");
        
        String normalStyle = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 12 30; -fx-cursor: hand; " +
                             "-fx-background-color: linear-gradient(#d35400, #a83200); " + // Arancione/Legno caldo
                             "-fx-text-fill: white; -fx-background-radius: 8; -fx-border-color: #f1c40f; " + // Bordo dorato
                             "-fx-border-radius: 6; -fx-border-width: 2;";
                             
        String hoverStyle = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 12 30; -fx-cursor: hand; " +
                            "-fx-background-color: linear-gradient(#f1c40f, #f39c12); " + // Giallo luminoso al passaggio del mouse
                            "-fx-text-fill: black; -fx-background-radius: 8; -fx-border-color: white; " +
                            "-fx-border-radius: 6; -fx-border-width: 2;";

        saveBackButton.setStyle(normalStyle);
        saveBackButton.setEffect(new DropShadow(8, Color.BLACK));
        
        // Animazione CSS interattiva
        saveBackButton.setOnMouseEntered(e -> saveBackButton.setStyle(hoverStyle));
        saveBackButton.setOnMouseExited(e -> saveBackButton.setStyle(normalStyle));

        saveBackButton.setOnAction(e -> {
            // Adesso legge tutto in totale sicurezza dalle variabili di classe
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

        return saveBackButton;
    }

    private void applyScaling(Scene scene, VBox content) {
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