/*
 * Contiene il game loop e chiama gli update
 */
package application.Controller;

import application.Model.GameModel;
import application.Scenes.PlayScene;
import application.Utils.GameConfig;
import application.View.GameView;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameController {
    private GameModel model;
    private GameView view;
    private InputManager inputManager;
    private Stage stage;

    public GameController(Stage stage) {
        this.stage = stage;
        this.view = new GameView();
        this.model = new GameModel(view.getBgWidth(), view.getBgHeight());
        this.inputManager = new InputManager();
    }

    public void startGame() {
        // 1. Istanziamo la PlayScene
        PlayScene playScene = new PlayScene();
        
        // 2. Chiediamo a PlayScene di creare la scena passandole il root della nostra View
        Scene scene = playScene.getScene(view.getRoot());

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
        };
        timer.start();
    }
}