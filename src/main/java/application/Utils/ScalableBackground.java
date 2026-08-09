package application.Utils;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Crea un ImageView da usare come sfondo che si comporta come
 * "-fx-background-size: cover" + "-fx-background-position: center center",
 * ma con scaling uniforme (stesso fattore su X e Y, mai deformato) e
 * centratura garantita, anche con aspect ratio della finestra molto diversi
 * da quello dell'immagine.
 */
public class ScalableBackground {

    public static ImageView create(String imagePath, Scene scene) {
        Image image = new Image(imagePath);
        ImageView bg = new ImageView(image);
        bg.setPreserveRatio(true);
        bg.setSmooth(true);

        Runnable resize = () -> {
            double imgW = image.getWidth();
            double imgH = image.getHeight();
            if (imgW <= 0 || imgH <= 0) return;

            // Fattore unico: l'immagine copre sempre entrambe le dimensioni,
            // ritagliando l'eccedenza (lo StackPane la centra, la finestra la clippa).
            double scale = Math.max(scene.getWidth() / imgW, scene.getHeight() / imgH);

            bg.setFitWidth(imgW * scale);
            bg.setFitHeight(imgH * scale);
        };

        scene.widthProperty().addListener((obs, oldVal, newVal) -> resize.run());
        scene.heightProperty().addListener((obs, oldVal, newVal) -> resize.run());

        // Prima esecuzione: rimandata di un tick, perché alla creazione la Scene
        // potrebbe non avere ancora larghezza/altezza definitive.
        javafx.application.Platform.runLater(resize);

        return bg;
    }
}