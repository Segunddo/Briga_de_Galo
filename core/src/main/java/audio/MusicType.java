package audio;

public enum MusicType {
    MENU_THEME("menu_theme.mp3"),
    BATTLE_THEME("forro_soundtrack.wav"),
    ENDGAME_THEME("endgame_theme.mp3");

    private final String fileName;

    MusicType(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
