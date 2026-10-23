package application.controller;

import application.utils.Point;

// Interfaccia per creare il player dato un punto di spawn
@FunctionalInterface
public interface CharacterFactory {
	application.model.Player create(Point spawn);
}