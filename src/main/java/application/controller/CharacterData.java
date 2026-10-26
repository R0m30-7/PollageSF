package application.controller;

import application.model.CharacterFactory;

public class CharacterData {
    public String displayName;
    public String pfpPath;
    public CharacterFactory factory;

    // Costruttore completo
    public CharacterData(String name, String pfp, CharacterFactory factory) {
        this.displayName = name;
        this.pfpPath = pfp;
        this.factory = factory;
    }

    // Costruttore ridotto (per i personaggi non giocabili)
    public CharacterData(String name, String pfp) {
        this.displayName = name;
        this.pfpPath = pfp;
        this.factory = null;
    }
}