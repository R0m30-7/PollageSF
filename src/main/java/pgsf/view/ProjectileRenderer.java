package pgsf.view;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import pgsf.model.AnimData;
import pgsf.model.Hitbox;
import pgsf.model.IReadOnlyProjectile;

public class ProjectileRenderer {
    // --- Cache CONDIVISE tra tutti i proiettili ---
    // Ogni colpo crea un nuovo ProjectileRenderer: se ognuno ricaricasse l'atlas
    // e riscalasse i frame, il gioco scatterebbe a ogni sparo.
    // Atlas già caricati (chiave: percorso dell'immagine della skin)
    private static final java.util.Map<String, Image> atlasCache = new java.util.HashMap<>();
    // Frame già ritagliati e scalati (chiave: percorso + rettangolo + scala)
    private static final java.util.Map<String, Image> frameCache = new java.util.HashMap<>();

    // --- Variabili per lo Sprite Animato ---
    private ImageView spriteView;
    private Image atlasImage;
    private String atlasPath;

    // Gestione dell'animazione
    private int currentFrame = 0;
    private long lastFrameTime;

    public ProjectileRenderer(IReadOnlyProjectile projectile) {
        this.atlasPath = projectile.getAtlasPath();

        // --- Caricamento dello sprite (dalla cache se già usato) ---
        this.atlasImage = atlasCache.computeIfAbsent(atlasPath, path -> {
            try {
                return new Image(getClass().getResourceAsStream(path));
            } catch (Exception e) {
                System.out.println("Errore caricamento atlas proiettile: " + path);
                return null;
            }
        });

        this.spriteView = new ImageView();
        this.spriteView.setSmooth(false); // Non vogliamo che lo sprite sia sfuocato
        this.lastFrameTime = System.nanoTime();
    }

    public Node getNode() {
        return spriteView;
    }

    public void render(IReadOnlyProjectile projectile, double camX) {
        AnimData data = projectile.getAnimData();
        if (atlasImage == null || data == null) return;

        // ==========================================
        // AVANZAMENTO DEI FRAME
        // ==========================================
        long now = System.nanoTime();
        if (now - lastFrameTime > data.speedNs) {
            if (data.loop) {
                currentFrame = (currentFrame + 1) % data.frameCount;
            } else {
                currentFrame = Math.min(currentFrame + 1, data.frameCount - 1);
            }
            lastFrameTime = now;
        }

        // Prendiamo dal TextureAtlas le coordinate del frame esatto e la scala
        Rectangle2D frameRect = projectile.getAtlas().getFrame(data.row, data.startCol, currentFrame);
        Image croppedFrame = getCroppedFrame(frameRect, projectile.getRenderScale());
        spriteView.setImage(croppedFrame);

        // ==========================================
        // POSIZIONE: SPRITE CENTRATO SULLA HITBOX
        // ==========================================
        // I frame hanno dimensioni diverse: centrandoli sulla hitbox il proiettile non "balla"
        Hitbox box = projectile.getBoundingBox();
        double centerX = box.getX() + box.getWidth() / 2.0;
        double centerY = box.getY() + box.getHeight() / 2.0;

        double px = centerX - croppedFrame.getWidth() / 2.0;
        double py = centerY - croppedFrame.getHeight() / 2.0;

        // Lo sprite nell'atlas va verso destra: se va a sinistra lo specchiamo
        // (scaleX ribalta attorno al centro del nodo, quindi la posizione resta uguale)
        spriteView.setScaleX(projectile.isMovingRight() ? 1 : -1);

        // Applichiamo posizione assoluta e offset telecamera direttamente sul nodo
        spriteView.setLayoutX(Math.round(px - camX));
        spriteView.setLayoutY(Math.round(py));
    }

    // Stesso Nearest-Neighbor via software di PlayerRenderer, ma senza auto-trim
    // dei piedi (il proiettile è centrato, non appoggiato a terra)
    private Image getCroppedFrame(Rectangle2D rect, double scale) {
        String key = atlasPath + "_" + (int) rect.getMinX() + "_" + (int) rect.getMinY() + "_"
                   + (int) rect.getWidth() + "_" + (int) rect.getHeight() + "_" + scale;

        return frameCache.computeIfAbsent(key, k -> {
            javafx.scene.image.PixelReader reader = atlasImage.getPixelReader();

            int dstW = Math.max(1, (int) Math.round(rect.getWidth() * scale));
            int dstH = Math.max(1, (int) Math.round(rect.getHeight() * scale));

            WritableImage scaledImg = new WritableImage(dstW, dstH);
            javafx.scene.image.PixelWriter writer = scaledImg.getPixelWriter();

            for (int y = 0; y < dstH; y++) {
                for (int x = 0; x < dstW; x++) {
                    int srcX = (int) (rect.getMinX() + (x / scale));
                    int srcY = (int) (rect.getMinY() + (y / scale));
                    writer.setArgb(x, y, reader.getArgb(srcX, srcY));
                }
            }
            return scaledImg;
        });
    }
}
