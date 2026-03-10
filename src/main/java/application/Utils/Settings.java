/*
 * Questa classe serve per salvare tutte le impostazioni modificabili del gioco
 */
package application.Utils;

public class Settings {
	private boolean isFullscreen;
	private boolean isAudioOn;
	private int numberOfPlayers;
	
	public Settings() {
		this.isFullscreen = false;
		this.isAudioOn = false;
		this.numberOfPlayers = 2;
	}
	
	public boolean isFullscreen() {
		return isFullscreen;
	}
	
	public void setFullscreen(boolean fullscreen) {
		isFullscreen = fullscreen;
	}
	
	public int getNumberOfPlayers() {
		return numberOfPlayers;
	}
	
	public void setNumberOfPlayers(int number) {
		numberOfPlayers = number;
	}
	
	public boolean getisAudioOn() {
		return isAudioOn;
	}
	
	public void setIsAudioOn(boolean audioOn) {
		isAudioOn = audioOn;
	}
}
