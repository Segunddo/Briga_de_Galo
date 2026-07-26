package briga.galo;

public class Utils {
    public enum Action {
        WALK_RIGHT,
        WALK_LEFT,
        FLY_RIGHT,
        FLY_LEFT,
        ATTACK,
        FLY_ATTACK_LEFT,
        FLY_ATTACK_RIGHT,
        LEFT_HANDLE,
        RIGHT_HANDLE,
        IDLE
    }
    public enum StateGame{
        MENU,
        PLAYER_CHANGING,
        MATCH,
        ENDGAME
    }
}
