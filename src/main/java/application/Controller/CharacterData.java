package application.Controller;

public class CharacterData {
    public String displayName;
    public String pfpPath;
    public CharacterFactory factory;

    // Costruttore completo (per i personaggi giocabili come Turnip e Ascanio)
    public CharacterData(String name, String pfp, CharacterFactory factory) {
        this.displayName = name;
        this.pfpPath = pfp;
        this.factory = factory;
    }

    // Costruttore ridotto (per i personaggi bloccati, che non hanno un factory!)
    public CharacterData(String name, String pfp) {
        this.displayName = name;
        this.pfpPath = pfp;
        this.factory = null;
    }
}