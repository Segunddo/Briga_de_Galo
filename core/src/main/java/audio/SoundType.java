package audio;

public enum SoundType {
    ATTACK("attack.wav"),
    HIT("hit.wav"),
    JUMP("jump.wav"),
    DEFEND("defend.wav"),
    PARRY("parry.wav"),
    MENU_MOVE("menu_move.wav"),
    MENU_CONFIRM("menu_confirm.wav"),
    MATCH_START("match_start.wav"),
    VICTORY("victory.wav");

    private final String fileName;

    SoundType(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
