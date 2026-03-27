/*
 * Contiene il game loop e chiama gli update
 */
package application.Controller;

import java.util.ArrayList;
import java.util.List;

import application.Model.GameModel;
import application.Scenes.PlayScene;
import application.Utils.GameConfig;
import application.View.GameView;
import javafx.scene.control.Label;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.control.ScrollPane;

public class GameController {
    private GameModel model;
    private GameView view;
    private InputManager inputManager;
    private Stage stage;
    private Scene scene;
    
    // --- Stati di avvio del gioco ---
    private boolean waitingForControllers = true;	// Finchè è true il gioco rimane in pausa
    private boolean waitingForMapSelection = false;
    
    // --- Menu connessione ---
    private VBox connectionMenu;
    private Label p1Label;
    private Label p2Label;
    
    // --- Scelta della mappa ---
    private VBox mapSelectionMenu;
    private TilePane mapsContainer; // Contenitore orizzontale per le mappe
    private ScrollPane mapScrollPane;	// Telecamera scorrevole per mostrare tutte le mappe
    private List<VBox> mapNodes = new ArrayList<>(); // Lista dei quadretti visivi
    private int currentMapIndex = 0;
    private List<MapData> availableMaps = new ArrayList<>();
    
    // Struttura dati per le mappe
    public static class MapData {
        public String displayName;
        public String imagePath;
        public double groundLevel;
        
        public MapData(String displayName, String imagePath, double groundLevel) {
            this.displayName = displayName;
            this.imagePath = imagePath;
            this.groundLevel = groundLevel;
        }
    }
    
    // --- Menu pausa ---
    private boolean isPaused = false;
    private VBox pauseMenu;
    private  boolean wasP1Pause = false;
    private boolean wasP2Pause = false;
    
    // Variabili per la navigazione dei menu tramite controller
    private List<Button> pauseButtons = new ArrayList<>();
    private int currentPauseIndex = 0;
    private long lastMenuInputTime = 0; // Serve per non scorrere i bottoni a 200 all'ora!
    private boolean wasConfirmPressed = false;
    
    private AnimationTimer gameLoop;
    private Label fpsLabel;

    public GameController(Stage stage) {
        this.stage = stage;
        this.view = new GameView();
        this.model = new GameModel(view.getBgWidth(), view.getBgHeight());
        this.inputManager = InputManager.getInstance();
        
        // Caricamento delle mappe in memoria
        availableMaps.add(new MapData("Rifugio dell'amicizia", "/Backgrounds/broBase.jpeg", 100));
        availableMaps.add(new MapData("Villaggio incantato", "/Backgrounds/cherryVillage.jpeg", 100));
        availableMaps.add(new MapData("CuloLand", "/Backgrounds/culoLand.jpeg", 100));
        availableMaps.add(new MapData("Paradise & Hell", "/Backgrounds/doubleSide.jpeg", 200));
        availableMaps.add(new MapData("Fight Club", "/Backgrounds/fightClub.jpeg", 310));
        availableMaps.add(new MapData("Smordor", "/Backgrounds/smordor.jpeg", 100));
        availableMaps.add(new MapData("9/11", "/Backgrounds/twinTowers.jpeg", 100));
        availableMaps.add(new MapData("UniPG", "/Backgrounds/uni.jpeg", 100));
        availableMaps.add(new MapData("Mini Rifugio", "/Backgrounds/villaggioPiccolo.jpeg", 100));
        availableMaps.add(new MapData("Koloxtol", "/Backgrounds/villaggioRurale.jpeg", 100));
    }

    public void startGame() {
    	// Creazione del contenitore a strati per contenere anche il menu
    	StackPane mainRoot = new StackPane();
    	
    	// Aggiunta del gioco in sfondo e lo sfuochiamo
    	mainRoot.getChildren().add(view.getRoot());
    	view.getRoot().setEffect(new GaussianBlur(25));
    	
    	// Creazione del menu da aggiungere sopra al gioco
    	createConnectionMenu();
    	mainRoot.getChildren().add(connectionMenu);
    	
    	// Creiamo e aggiungiamo il menu delle mappe (nascosto)
    	createMapSelectionMenu();
    	mainRoot.getChildren().add(mapSelectionMenu);
    	
    	// AGGIUNGIAMO ANCHE IL MENU DI PAUSA AL PANINO (Nascosto)
        createPauseMenu();
        mainRoot.getChildren().add(pauseMenu);
    	
    	// Creazione dell'etichetta degli FPS in alto a destra
        fpsLabel = new Label("FPS: 0");
        fpsLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: limegreen; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);");
        StackPane.setAlignment(fpsLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(fpsLabel, new javafx.geometry.Insets(10, 20, 10, 10)); 
        
        // Lo mostriamo solo se il giocatore ha messo la spunta!
        fpsLabel.setVisible(application.Utils.Settings.getInstance().isShowFps());
        mainRoot.getChildren().add(fpsLabel);
    	
    	// Passiamo a PlayScene la root con i livelli
        PlayScene playScene = new PlayScene();
        // Chiediamo a PlayScene di creare la scena passandole il root della nostra View
        scene = playScene.getScene(mainRoot);
        
        // Diciamo allo ScrollPane di essere sempre alto esattamente il 65% (0.65) dell'altezza della finestra!
        mapScrollPane.prefHeightProperty().bind(scene.heightProperty().multiply(0.65));
        
        // --- ASCOLTATORI DI RIDIMENSIONAMENTO ---
        // Se l'utente allarga o stringe la finestra...
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            double newW = newVal.doubleValue();
            double currentH = scene.getHeight(); // Prendiamo l'altezza attuale
            
            // Prima aggiorniamo la view così calcola lo zoom
            view.updateWindowSize(newW, currentH);
            
            // Diciamo poi al Model di aggiornare i limiti passando il nuovo bgWidth della View
            model.updateWindowSize(newW, currentH, view.getBgWidth());
        });

        // Se l'utente alza o abbassa la finestra...
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            double currentW = scene.getWidth(); // Prendiamo la larghezza attuale
            double newH = newVal.doubleValue();
            
            // Prima aggiorniamo la view così calcola lo zoom
            view.updateWindowSize(currentW, newH);
            
            // Diciamo poi al Model di aggiornare i limiti passando il nuovo bgWidth della View
            model.updateWindowSize(currentW, newH, view.getBgWidth());	
        });

        // 3. Impostiamo la finestra e avviamo il gioco
        stage.setTitle(GameConfig.GAME_TITLE_STRING);
        stage.setScene(scene);
        stage.show();

        startGameLoop();
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastTime = System.nanoTime();
            
            private double physicsAccumulator = 0.0;
            private double renderAccumulator = 0.0;
            
            // Variabili per il contatore FPS a schermo
            private long lastFpsTime = 0;
            private int framesRendered = 0;
            
            @Override
            public void handle(long now) {
                long frameTime = now - lastTime;
                lastTime = now;
                
                // --- Contatore FPS a schermo ---
                if (now - lastFpsTime >= 1_000_000_000) { // È passato 1 secondo
                    fpsLabel.setText("FPS: " + framesRendered);
                    framesRendered = 0;
                    lastFpsTime = now;
                }
                
                if (waitingForControllers) {
                    // --- MODALITÀ MENU IN PAUSA ---
                    inputManager.update(); 
                    
                    // Leggiamo quanti giocatori devono giocare
                    int numPlayers = application.Utils.Settings.getInstance().getNumberOfPlayers();
                    
                    if (inputManager.isPlayer1Connected()) {
                        p1Label.setText("Giocatore 1: CONNESSO! 🎮");
                        p1Label.setStyle("-fx-font-size: 24px; -fx-text-fill: #00FF00; -fx-font-weight: bold;");
                    }
                    
                    // Aggiorniamo l'etichetta 2 solo se stiamo effettivamente aspettando un secondo giocatore
                    if (numPlayers == 2 && inputManager.isPlayer2Connected()) {
                        p2Label.setText("Giocatore 2: CONNESSO! 🎮");
                        p2Label.setStyle("-fx-font-size: 24px; -fx-text-fill: #00FF00; -fx-font-weight: bold;");
                    }

                    // --- LOGICA DI AVVIO INTELLIGENTE ---
                    boolean canStartGame = false;
                    
                    if (numPlayers == 1 && inputManager.isPlayer1Connected()) {
                        canStartGame = true; // Basta un controller!
                    } else if (numPlayers == 2 && inputManager.isPlayer1Connected() && inputManager.isPlayer2Connected()) {
                        canStartGame = true; // Servono entrambi i controller!
                    }

                    // Se i requisiti sono soddisfatti, via col gioco!
                    if (canStartGame) {
                        closeConnectionMenu();
                    }
                    
                    physicsAccumulator = 0;
                    renderAccumulator = 0;
                } else if (waitingForMapSelection) {
                    inputManager.update();
                    
                    long currentTimeMs = System.currentTimeMillis();
                    double xInput = inputManager.getLeftStickX(1); 
                    double yInput = inputManager.getLeftStickY(1); // Leggiamo anche l'asse Y
                    
                    if (currentTimeMs - lastMenuInputTime > 250) { 
                        
                        // Calcoliamo quante colonne ci sono fisicamente a schermo in questo momento
                        // (300 di larghezza immagine + 40 di gap = 340)
                        int cols = Math.max(1, (int) (mapsContainer.getWidth() / 340));

                        if (xInput < -0.5) { // SINISTRA
                            currentMapIndex--;
                            if (currentMapIndex < 0) currentMapIndex = availableMaps.size() - 1;
                            updateMapSelectionUI(); 
                            lastMenuInputTime = currentTimeMs;
                            
                        } else if (xInput > 0.5) { // DESTRA
                            currentMapIndex++;
                            if (currentMapIndex >= availableMaps.size()) currentMapIndex = 0;
                            updateMapSelectionUI(); 
                            lastMenuInputTime = currentTimeMs;
                            
                        } else if (yInput < -0.5) { // SU (Salta alla riga sopra)
                            if (currentMapIndex - cols >= 0) {
                                currentMapIndex -= cols;
                                updateMapSelectionUI();
                                lastMenuInputTime = currentTimeMs;
                            }
                            
                        } else if (yInput > 0.5) { // GIÙ (Salta alla riga sotto)
                            if (currentMapIndex + cols < availableMaps.size()) {
                                currentMapIndex += cols;
                                updateMapSelectionUI();
                                lastMenuInputTime = currentTimeMs;
                            }
                        }
                    }
                    
                    boolean isConfirm = inputManager.isJumpButtonPressed(1);
                    if (isConfirm && !wasConfirmPressed) {
                        confirmMapSelection(); 
                    }
                    wasConfirmPressed = isConfirm;
                    
                    physicsAccumulator = 0;
                    renderAccumulator = 0;
                    
                } else {
                	// --- Modalità gioco attivo ---
                	// --- LETTURA TASTO PAUSA ---
                    // Aggiorniamo i controller per leggere il tasto Pause
                    inputManager.update(); 
                    
                    boolean isP1Pause = inputManager.isPauseButtonPressed(1);
                    boolean isP2Pause = inputManager.isPauseButtonPressed(2);

                    // Se il tasto viene premuto in questo preciso frame (e non lo era prima)
                    if ((isP1Pause && !wasP1Pause) || (isP2Pause && !wasP2Pause)) {
                        togglePause();
                    }
                    // Salviamo in memoria lo stato per il prossimo frame
                    wasP1Pause = isP1Pause;
                    wasP2Pause = isP2Pause;

                    // --- GESTIONE DELLA FISICA ---
                    if (isPaused) {
                        // Se siamo in pausa, azzeriamo l'accumulatore fisico.
                        // Questo evita che, togliendo la pausa, il gioco cerchi di "recuperare"
                        // tutti i secondi persi sparando i giocatori nello spazio!
                        physicsAccumulator = 0; 
                        
                        // ==========================================
                        //  NAVIGAZIONE MENU DI PAUSA CON CONTROLLER
                        // ==========================================
                        long currentTimeMs = System.currentTimeMillis();
                        double yInput = inputManager.getLeftStickY(1); // Usiamo il P1 per scorrere
                        
                        // Il delay (200ms) serve per permettere all'utente di scorrere un bottone alla volta
                        if (currentTimeMs - lastMenuInputTime > 200) {
                            if (yInput < -0.5) { // Levetta verso l'ALTO
                                currentPauseIndex--;
                                if (currentPauseIndex < 0) currentPauseIndex = pauseButtons.size() - 1; // Ritorna all'ultimo
                                updatePauseMenuSelection();
                                lastMenuInputTime = currentTimeMs;
                            } else if (yInput > 0.5) { // Levetta verso il BASSO
                                currentPauseIndex++;
                                if (currentPauseIndex >= pauseButtons.size()) currentPauseIndex = 0; // Ritorna al primo
                                updatePauseMenuSelection();
                                lastMenuInputTime = currentTimeMs;
                            }
                        }
                        
                        // Selezione con il tasto del Salto (X / A)
                        boolean isConfirm = inputManager.isJumpButtonPressed(1);
                        if (isConfirm && !wasConfirmPressed) {
                            // .fire() simula esattamente il click del mouse su quel bottone!
                            pauseButtons.get(currentPauseIndex).fire();
                        }
                        wasConfirmPressed = isConfirm;
                        
                    } else {
                        // GIOCO ATTIVO! Facciamo muovere i giocatori.
                        physicsAccumulator += frameTime;
                        while (physicsAccumulator >= application.Utils.GameConfig.TIME_PER_TICK) {
                            inputManager.update(); // Mantiene fluido il movimento     
                            model.update(inputManager); 
                            physicsAccumulator -= application.Utils.GameConfig.TIME_PER_TICK;
                        }
                    }

                    // --- RENDER GRAFICO (Gira sempre, sia in gioco che in pausa) ---
                    renderAccumulator += frameTime;
                    double targetFrameTime = 1_000_000_000.0 / application.Utils.Settings.getInstance().getTargetFps();

                    if (renderAccumulator >= targetFrameTime) {
                        view.render(model);
                        framesRendered++;
                        renderAccumulator %= targetFrameTime;
                    }
	            }
            }
        };
        gameLoop.start();
    }

    // --- Menu che chiede di connettere i controller, quello prima di entrare in game ---
    private void createConnectionMenu() {
        connectionMenu = new VBox(20); 
        connectionMenu.setAlignment(Pos.CENTER);
        connectionMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        Label title = new Label("CONNETTI I CONTROLLER");
        title.setStyle("-fx-font-size: 40px; -fx-text-fill: white; -fx-font-weight: bold;");

        p1Label = new Label("Giocatore 1: IN ATTESA (Premi un tasto)");
        p1Label.setStyle("-fx-font-size: 24px; -fx-text-fill: yellow;");

        p2Label = new Label("Giocatore 2: IN ATTESA (Premi un tasto)");
        p2Label.setStyle("-fx-font-size: 24px; -fx-text-fill: yellow;");

        // --- LEGGIAMO LE IMPOSTAZIONI ---
        int numPlayers = application.Utils.Settings.getInstance().getNumberOfPlayers();
        
        // Se c'è un solo giocatore, il Giocatore 2 diventa la CPU (o comunque non serve il controller)
        if (numPlayers == 1) {
            p2Label.setText("Giocatore 2: CPU (Nessun controller richiesto)");
            p2Label.setStyle("-fx-font-size: 24px; -fx-text-fill: gray;"); // Lo facciamo grigio per far capire che è disabilitato
        }
        
        // --- Bottone torna al menu ---
        Button backToMenuBtn = new Button("Menu Principale");
        backToMenuBtn.setStyle("-fx-font-size: 18px; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-color: darkred; -fx-text-fill: white;");
        
        backToMenuBtn.setOnAction(e -> {
            // 1. Fermiamo il loop per evitare che il gioco continui a girare in background
            if (gameLoop != null) {
                gameLoop.stop();
            }
            
            // 2. Creiamo una nuova istanza del MainMenuScene
            application.Scenes.MainMenuScene mainMenu = new application.Scenes.MainMenuScene();
            
            // 3. Impostiamo il titolo e ricarichiamo la scena
            stage.setTitle("Main Menu");
            stage.setScene(mainMenu.getScenaMenu(stage));
            
            // 4. Manteniamo le impostazioni di fullscreen
            stage.setFullScreen(application.Utils.Settings.getInstance().isFullscreen());
        });

        connectionMenu.getChildren().addAll(title, p1Label, p2Label, backToMenuBtn);
    }
    
    // --- CHIUSURA DEL MENU CHE CHIEDE DI CONNETTERE I CONTROLLER ---
    private void closeConnectionMenu() {
        waitingForControllers = false;		
        connectionMenu.setVisible(false);	
        
        // --- IL FIX ANTI-SKIP ---
        // Diciamo al gioco di memorizzare che stiamo già premendo il tasto, 
        // così aspetterà che lo rilasciamo prima di cliccare la mappa!
        wasConfirmPressed = inputManager.isJumpButtonPressed(1);
        
        // Passiamo allo stato Scelta Mappa
        waitingForMapSelection = true;
        mapSelectionMenu.setVisible(true);
        currentMapIndex = 0;
        updateMapSelectionUI();
    }
    
    // --- Costruzione del menu per la scelta della mappa ---
    private void createMapSelectionMenu() {
        mapSelectionMenu = new VBox(40);
        mapSelectionMenu.setAlignment(Pos.CENTER);
        mapSelectionMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);"); 
        mapSelectionMenu.setVisible(false);

        Label title = new Label("SELEZIONA L'ARENA");
        title.setStyle("-fx-font-size: 50px; -fx-text-fill: white; -fx-font-weight: bold;");

        // --- LA GRIGLIA ---
        mapsContainer = new TilePane();
        mapsContainer.setAlignment(Pos.CENTER);
        mapsContainer.setHgap(40); 
        mapsContainer.setVgap(40); 
        mapsContainer.setPrefColumns(3); 
        mapsContainer.setMaxWidth(1200); 
        
        mapNodes.clear(); 

        for (MapData map : availableMaps) {
            VBox singleMapBox = new VBox(15); 
            singleMapBox.setAlignment(Pos.CENTER);

            ImageView thumb = new ImageView();
            thumb.setFitWidth(300); 
            thumb.setFitHeight(170);
            thumb.setPreserveRatio(false); 
            
            try {
                Image img = new Image(getClass().getResourceAsStream(map.imagePath));
                if (!img.isError()) thumb.setImage(img);
            } catch (Exception e) {
                System.out.println("Nessuna foto trovata: " + map.imagePath);
            }

            Label nameLabel = new Label(map.displayName);
            nameLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

            singleMapBox.getChildren().addAll(thumb, nameLabel);
            
            mapNodes.add(singleMapBox); 
            mapsContainer.getChildren().add(singleMapBox); 
        }
        
        // Creiamo un contenitore elastico che centra automaticamente la griglia al suo interno
        StackPane gridCenterer = new StackPane(mapsContainer);
        gridCenterer.setAlignment(Pos.CENTER);

        // --- IL PANNELLO SCORREVOLE (SCROLLPANE) ---
        mapScrollPane = new ScrollPane(gridCenterer);
        mapScrollPane.setFitToWidth(true); // Fondamentale per far funzionare il TilePane
        mapScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;"); // Sfondo invisibile
        mapScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Nascondiamo la barra orizzontale
        mapScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Nascondiamo la barra verticale

        Label instructions = new Label("⬅ ➡ Scorri | ⬆ ⬇ Salta Riga | X Conferma");
        instructions.setStyle("-fx-font-size: 20px; -fx-text-fill: lightgray; -fx-text-alignment: center;");

        // AGGIUNGIAMO LO SCROLLPANE AL POSTO DEL MAPSCONTAINER
        mapSelectionMenu.getChildren().addAll(title, mapScrollPane, instructions);
    }
    
    private void updateMapSelectionUI() {
        for (int i = 0; i < mapNodes.size(); i++) {
            VBox node = mapNodes.get(i);
            Label nameLabel = (Label) node.getChildren().get(1); 
            
            if (i == currentMapIndex) {
                node.setScaleX(1.15);
                node.setScaleY(1.15);
                node.setEffect(new DropShadow(30, Color.YELLOW));
                nameLabel.setStyle("-fx-font-size: 26px; -fx-text-fill: yellow; -fx-font-weight: bold;");
                
                // --- INSEGUIMENTO AUTOMATICO DELLA TELECAMERA ---
                if (mapScrollPane != null) {
                    double contentHeight = mapsContainer.getBoundsInLocal().getHeight();
                    double viewportHeight = mapScrollPane.getViewportBounds().getHeight();
                    
                    if (contentHeight > viewportHeight) {
                        // Calcoliamo dove si trova il centro della mappa selezionata rispetto all'altezza totale
                        double nodeY = node.getBoundsInParent().getCenterY();
                        double targetScroll = (nodeY - (viewportHeight / 2)) / (contentHeight - viewportHeight);
                        
                        // Assicuriamoci che lo scroll non vada fuori dai limiti (tra 0.0 e 1.0)
                        mapScrollPane.setVvalue(Math.max(0.0, Math.min(1.0, targetScroll)));
                    }
                }
                
            } else {
                node.setScaleX(1.0);
                node.setScaleY(1.0);
                node.setEffect(null);
                nameLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        }
    }
    
    private void confirmMapSelection() {
        waitingForMapSelection = false;
        mapSelectionMenu.setVisible(false);
        view.getRoot().setEffect(null); // Rimuoviamo la sfocatura

        // 1. Diciamo alla View di caricare e disegnare lo sfondo scelto
        MapData selectedMap = availableMaps.get(currentMapIndex);
        view.changeBackground(selectedMap.imagePath, scene.getWidth(), scene.getHeight());
        
        // 2. FONDAMENTALE: Diciamo al Motore Fisico di aggiornare la larghezza
        // del mondo (muri invisibili) in base all'immagine appena caricata!
        model.updateWindowSize(scene.getWidth(), scene.getHeight(), view.getBgWidth());
        
     // --- DICIAMO AL MOTORE FISICO DOV'È IL PAVIMENTO! ---
        model.setGroundLevel(selectedMap.groundLevel);
    }
    
    // --- MENU DI PAUSA IN GIOCO ---
    private void createPauseMenu() {
        pauseMenu = new VBox(20);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Sfondo scuro trasparente
        pauseMenu.setVisible(false); // Inizialmente è nascosto!

        Label title = new Label("PAUSA");
        title.setStyle("-fx-font-size: 50px; -fx-text-fill: white; -fx-font-weight: bold;");

        Button resumeBtn = new Button("Riprendi Gioco");
        resumeBtn.setStyle("-fx-font-size: 20px; -fx-padding: 10 20; -fx-cursor: hand;");
        resumeBtn.setOnAction(e -> togglePause()); // Cliccarlo toglie la pausa

        Button backToMenuBtn = new Button("Torna al Menu");
        backToMenuBtn.setStyle("-fx-font-size: 20px; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-color: darkred; -fx-text-fill: white;");
        backToMenuBtn.setOnAction(e -> {
            if (gameLoop != null) gameLoop.stop();
            application.Scenes.MainMenuScene mainMenu = new application.Scenes.MainMenuScene();
            stage.setTitle("Main Menu");
            stage.setScene(mainMenu.getScenaMenu(stage));
            stage.setFullScreen(application.Utils.Settings.getInstance().isFullscreen());
        });

        Button quitBtn = new Button("Esci dal Gioco");
        quitBtn.setStyle("-fx-font-size: 20px; -fx-padding: 10 20; -fx-cursor: hand;");
        quitBtn.setOnAction(e -> System.exit(0));
        
        // SVUOTIAMO E RIEMPIAMO LA LISTA (Per la logica del controller)
        pauseButtons.clear();
        pauseButtons.add(resumeBtn);
        pauseButtons.add(backToMenuBtn);
        pauseButtons.add(quitBtn);
        
        // Applichiamo la grafica di base prima di mostrare a schermo
        updatePauseMenuSelection();

        pauseMenu.getChildren().addAll(title, resumeBtn, backToMenuBtn, quitBtn);
    }
    
    // --- AGGIORNAMENTO GRAFICO DEL MENU DI PAUSA ---
    private void updatePauseMenuSelection() {
        for (int i = 0; i < pauseButtons.size(); i++) {
            Button btn = pauseButtons.get(i);
            if (i == currentPauseIndex) {
                // Bottone Selezionato: Lo ingrandiamo un po' e gli diamo un'ombra luminosa gialla!
                btn.setScaleX(1.1);
                btn.setScaleY(1.1);
                btn.setEffect(new DropShadow(20, Color.YELLOW));
            } else {
                // Bottone NON Selezionato: Torna normale
                btn.setScaleX(1.0);
                btn.setScaleY(1.0);
                btn.setEffect(null);
            }
        }
    }

    // --- ATTIVA / DISATTIVA LA PAUSA E LA SFOCATURA ---
    private void togglePause() {
        isPaused = !isPaused;
        
        if (isPaused) {
            // Quando apri la pausa, l'indice torna sempre al primo bottone ("Riprendi")
            currentPauseIndex = 0;
            updatePauseMenuSelection();
            
            // Serve per evitare che se metti in pausa saltando, il menu clicchi subito il primo bottone!
            wasConfirmPressed = inputManager.isJumpButtonPressed(1);
            
            pauseMenu.setVisible(true);
            view.getRoot().setEffect(new GaussianBlur(25)); 
        } else {
            pauseMenu.setVisible(false);
            view.getRoot().setEffect(null); 
        }
    }
}