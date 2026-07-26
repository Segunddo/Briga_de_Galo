package briga.galo;

public class Utils {
    public enum Action {
        WALK_RIGHT,
        WALK_LEFT,
        JUMP,
        FLY_RIGHT,
        FLY_LEFT,
        ATTACK,
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
