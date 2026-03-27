package application;

import java.io.File;

public class Launcher {
    public static void main(String[] args) {
        
        // --- 1. CONFIGURIAMO LE LIBRERIE HARDWARE PRIMA DI TUTTO ---
        try {
            // Cerca la cartella "natives" accanto al file .jar
            String nativesPath = new File("natives").getAbsolutePath();
            
            // Forza JInput a leggere i file .dll / .so da questa cartella
            System.setProperty("net.java.games.input.librarypath", nativesPath);
            System.setProperty("org.lwjgl.librarypath", nativesPath); // Aggiunto per sicurezza
            
            System.out.println("Librerie native impostate su: " + nativesPath);
        } catch (Exception e) {
            System.out.println("Errore di caricamento librerie: " + e.getMessage());
        }

        // --- 2. ORA AVVIAMO IL GIOCO VERO E PROPRIO ---
        Main.main(args); 
    }
}