package application.Controller;

// Interfaccia per creare il player dato un punto di spawn
@FunctionalInterface
public interface CharacterFactory {
	application.Model.Player create(javafx.geometry.Point2D spawn);
}