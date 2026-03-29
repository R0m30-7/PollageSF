package application.Model;

public class AnimData {
	public int row;
	public int startCol;
	public int frameCount;
	public long speedNs;	// Tempo tra un frame e l'altro
	public boolean loop;	// True = ciclo continuo (camminata), False = si ferma all'ultimo frame (Pugno)
	
	public AnimData(int row, int startCol, int frameCount, long speedMs, boolean loop) {
		this.row = row;
		this.startCol = startCol;
		this.frameCount = frameCount;
		this.speedNs = speedMs * 1_000_000L;	// Converte da ms a ns
		this.loop = loop;
	}
}
