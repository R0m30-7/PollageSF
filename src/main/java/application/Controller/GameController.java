/*
 * Contiene il game loop e chiama gli update
 */
package application.Controller;

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
import javafx.stage.Stage;
import javafx.scene.effect.GaussianBlur;

public class GameController {
    private GameModel model;
    private GameView view;
    private InputManager inputManager;
    private Stage stage;
    
    // Variabili per la gestione del menu in gioco (quello che dice di connettere i controller)
    private boolean waitingForControllers = true;	// Finchè è true il gioco rimane in pausa
    private VBox connectionMenu;
    private Label p1Label;
    private Label p2Label;

    public GameController(Stage stage) {
        this.stage = stage;
        this.view = new GameView();
        this.model = new GameModel(view.getBgWidth(), view.getBgHeight());
        this.inputManager = new InputManager();
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
    	
    	// Passiamo a PlayScene la root con i livelli
        PlayScene playScene = new PlayScene();
        // Chiediamo a PlayScene di creare la scena passandole il root della nostra View
        Scene scene = playScene.getScene(mainRoot);
        
        // --- ASCOLTATORI DI RIDIMENSIONAMENTO ---
        // Se l'utente allarga o stringe la finestra...
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            double newW = newVal.doubleValue();
            double currentH = scene.getHeight(); // Prendiamo l'altezza attuale
            model.updateWindowSize(newW, currentH);
            view.updateWindowSize(newW, currentH);
        });

        // Se l'utente alza o abbassa la finestra...
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            double currentW = scene.getWidth(); // Prendiamo la larghezza attuale
            double newH = newVal.doubleValue();
            model.updateWindowSize(currentW, newH);
            view.updateWindowSize(currentW, newH);
        });

        // 3. Impostiamo la finestra e avviamo il gioco
        stage.setTitle(GameConfig.GAME_TITLE_STRING);
        stage.setScene(scene);
        stage.show();

        startGameLoop();
    }

    private void startGameLoop() {
        AnimationTimer timer = new AnimationTimer() {
            private long lastTime = System.nanoTime();
            private double accumulator = 0.0;

            @Override
            public void handle(long now) {
                long frameTime = now - lastTime;
                lastTime = now;
                
                if(waitingForControllers) {
                	// --- MODALITÀ MENU IN PAUSA ---
                    inputManager.update(); // Sentiamo se si collegano
                    
                    // Aggiorniamo le etichette se trovano un controller
                    if (inputManager.isPlayer1Connected()) {
                        p1Label.setText("Giocatore 1: CONNESSO! 🎮");
                        p1Label.setStyle("-fx-font-size: 24px; -fx-text-fill: #00FF00; -fx-font-weight: bold;");
                    }
                    if (inputManager.isPlayer2Connected()) {
                        p2Label.setText("Giocatore 2: CONNESSO! 🎮");
                        p2Label.setStyle("-fx-font-size: 24px; -fx-text-fill: #00FF00; -fx-font-weight: bold;");
                    }

                    // Se sono entrambi connessi, chiudiamo il menu
                    if (inputManager.isPlayer1Connected() && inputManager.isPlayer2Connected()) {
                        closeConnectionMenu();
                    }
                    
                    // IMPORTANTE: Resettiamo l'accumulatore, così quando togliamo la pausa
                    // il gioco non cerca di "recuperare" i secondi persi sparando i giocatori nello spazio!
                    accumulator = 0; 
                    
                } else {
	                // --- MODALITÀ GIOCO ATTIVO ---
                	accumulator += frameTime;
	
	                while (accumulator >= GameConfig.TIME_PER_TICK) {
	                    // 1. Scansiona i controller e assegna automaticamente chi preme i tasti
	                    inputManager.update();      
	                    // 2. Passa i dati al Model per muovere i giocatori attivi
	                    model.update(inputManager); 
	                    accumulator -= GameConfig.TIME_PER_TICK;
	                }
	                // 3. Disegna a schermo
	                view.render(model);
	            }
            }
        };
        timer.start();
    }
    
    // --- MENU CHE CHIEDE DI CONNETTERE I CONTROLLER ---
    private void createConnectionMenu() {
        connectionMenu = new VBox(20); // 20 è lo spazio tra un elemento e l'altro
        connectionMenu.setAlignment(Pos.CENTER);
        
        // Uno sfondo leggermente scuro per far risaltare il testo sul blur
        connectionMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        Label title = new Label("CONNETTI I CONTROLLER");
        title.setStyle("-fx-font-size: 40px; -fx-text-fill: white; -fx-font-weight: bold;");

        p1Label = new Label("Giocatore 1: IN ATTESA (Premi un tasto)");
        p1Label.setStyle("-fx-font-size: 24px; -fx-text-fill: yellow;");

        p2Label = new Label("Giocatore 2: IN ATTESA (Premi un tasto)");
        p2Label.setStyle("-fx-font-size: 24px; -fx-text-fill: yellow;");

        Button bypassButton = new Button("Forza Avvio (Test 1 Controller)");
        bypassButton.setStyle("-fx-font-size: 18px; -fx-padding: 10 20 10 20; -fx-cursor: hand;");
        
        // Se premi il pulsante, chiude il menu forzatamente!
        bypassButton.setOnAction(e -> closeConnectionMenu());

        connectionMenu.getChildren().addAll(title, p1Label, p2Label, bypassButton);
    }
    
    // --- CHIUSURA DEL MENU CHE CHIEDE DI CONNETTERE I CONTROLLER ---
    private void closeConnectionMenu() {
        waitingForControllers = false;		// Sblocca il Game Loop
        connectionMenu.setVisible(false);	// Nasconde il menu
        view.getRoot().setEffect(null);		// Rimuove la sfocatura (blur) e mostra il gioco limpido!
    }
}