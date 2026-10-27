package pgsf.model;

import pgsf.utils.Point;

// Interfaccia per creare il player dato un punto di spawn
@FunctionalInterface
public interface CharacterFactory {
	pgsf.model.Player create(Point spawn);
}