package application.model;

import java.util.List;

public interface IReadOnlyGameModel {
    double getCameraX();
    IReadOnlyPlayer getPlayer1();
    IReadOnlyPlayer getPlayer2();
    boolean getIsGameOver();
    int getWinner();
    List<? extends IReadOnlyProjectile> getProjectiles();
}