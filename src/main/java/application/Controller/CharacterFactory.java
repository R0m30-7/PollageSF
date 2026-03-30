package application.Controller;

//Questa interfaccia dice: "Dammi un punto di spawn e io ti creo il Player"
@FunctionalInterface
public interface CharacterFactory {
	application.Model.Player create(javafx.geometry.Point2D spawn);
}