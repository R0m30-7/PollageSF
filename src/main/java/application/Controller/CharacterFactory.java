package application.Controller;

import application.Utils.Point;

// Interfaccia per creare il player dato un punto di spawn
@FunctionalInterface
public interface CharacterFactory {
	application.Model.Player create(Point spawn);
}