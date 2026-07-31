package player;

public enum SpriteType {
    WALK_RIGHT("walk_right.png"),
    WALK_LEFT("walk_left.png"),
    FLY_RIGHT("fly_right.png"),
    FLY_LEFT("fly_left.png"),
    ATTACK_RIGHT("attack_right.png"),
    ATTACK_LEFT("attack_left.png"),
    DEFEND_RIGHT("defend_right.png"),
    DEFEND_LEFT("defend_left.png");

    private final String defaultFileName;

    SpriteType(String defaultFileName) {
        this.defaultFileName = defaultFileName;
    }

    public String getDefaultFileName() {
        return defaultFileName;
    }
}
