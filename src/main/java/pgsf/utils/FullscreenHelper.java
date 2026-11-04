package pgsf.utils;

import javafx.stage.Stage;

/**
 * Applica lo schermo intero in modo compatibile con Linux.
 * Su alcuni window manager (es. Cinnamon/Muffin) una finestra non ridimensionabile
 * non può andare a schermo intero: la rendiamo ridimensionabile prima di entrarci
 * e la blocchiamo di nuovo quando si torna in finestra.
 */
public final class FullscreenHelper {

    private FullscreenHelper() {}

    public static void apply(Stage stage, boolean fullscreen) {
        if (fullscreen) {
            stage.setResizable(true);
            stage.setFullScreen(true);
        } else {
            stage.setFullScreen(false);
            stage.setResizable(false);
        }
    }

    /** Quando si esce dallo schermo intero (es. con ESC) la finestra torna non ridimensionabile. */
    public static void install(Stage stage) {
        stage.fullScreenProperty().addListener((obs, wasFull, isFull) -> {
            if (!isFull) {
                stage.setResizable(false);
            }
        });
    }
}
