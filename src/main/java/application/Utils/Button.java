package application.Utils;

import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Button extends ImageView{
	private static final int nStates = 3;
	private double dimMult = 1;	// Di quanto moltiplicare la dimensione del pulsante
	Image[] butStatesImg = new Image[nStates];
	private Runnable actionToDo;
	private Image imageAtlas;
	
	public Button(String path, int imgX, int imgY, int imgWidth, int imgHeight) {
		imageAtlas = new Image(path);
		LoadImages(path, imgX, imgY, imgWidth, imgHeight);
		this.setImage(butStatesImg[0]);
		this.setCursor(Cursor.HAND);
		
		this.setFitWidth(imgWidth * dimMult);
		this.setFitHeight(imgHeight * dimMult);

		setImageOnMouseEvent();
	}

	private void LoadImages(String path, int startX, int startY, int width, int height) {
		for(int i = 0; i < nStates; i++) {
			butStatesImg[i] = subImage(imageAtlas, startX + width * i, startY, width, height);
		}
	}

	private void setImageOnMouseEvent() {
		this.setOnMouseEntered(e -> {
			this.setImage(butStatesImg[1]);
		});
		
		this.setOnMouseExited(e -> {
			this.setImage(butStatesImg[0]);
		});
		
		this.setOnMousePressed(e -> {
			this.setImage(butStatesImg[2]);
		});
		
		this.setOnMouseReleased(e -> {
			if(this.contains(e.getX(), e.getY())) {
				this.setImage(butStatesImg[1]);
			} else {
				this.setImage(butStatesImg[0]);				
			}
		});
		
		this.setOnMouseClicked(e -> {
			if(actionToDo != null) {
				actionToDo.run();
			}
		});
	}
	
	public void setAction(Runnable action) {
		this.actionToDo = action;
	}
	
	public int getNStates() {
		return nStates;
	}
	
	private Image subImage(Image source, int x, int y, int width, int height) {
	    WritableImage writableImage = new WritableImage(width, height);
	    PixelReader reader = source.getPixelReader();
	    PixelWriter writer = writableImage.getPixelWriter();

	    // Copy pixels from the source image to the writable image
	    writer.setPixels(0, 0, width, height, reader, x, y);
	    return writableImage;
	}
}