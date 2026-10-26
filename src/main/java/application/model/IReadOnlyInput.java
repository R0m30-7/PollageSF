package application.model;

public interface IReadOnlyInput {
    double getLeftStickX(int playerNumber);
    double getLeftStickY(int playerNumber);
    boolean isJumpButtonPressed(int playerNumber);
    boolean isPunchButtonPressed(int playerNumber);
    boolean isHookButtonPressed(int playerNumber);
    boolean isKickButtonPressed(int playerNumber);
    boolean isHadoukenButtonPressed(int playerNumber);
    boolean isDefendButtonPressed(int playerNumber);
}