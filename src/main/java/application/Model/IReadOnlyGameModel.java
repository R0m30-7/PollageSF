package application.Model;

public interface IReadOnlyGameModel {
    double getCameraX();
    IReadOnlyPlayer getPlayer1();
    IReadOnlyPlayer getPlayer2();
    boolean getIsGameOver();
    int getWinner();
}