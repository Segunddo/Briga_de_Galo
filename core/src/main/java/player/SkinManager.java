package player;

import java.util.HashMap;
import java.util.Map;

public class SkinManager {
    private static final SkinManager INSTANCE = new SkinManager();

    public static SkinManager getInstance() {
        return INSTANCE;
    }

    private final Map<String, PlayerSkin> loadedSkins = new HashMap<>();
    private final Map<String, String> customPaths = new HashMap<>();

    private SkinManager() {}

    public void registerSkinPath(String skinId, String basePath) {
        customPaths.put(skinId, basePath);
    }

    public PlayerSkin get(String skinId) {
        if (loadedSkins.containsKey(skinId)) {
            return loadedSkins.get(skinId);
        }

        String basePath = customPaths.get(skinId);
        PlayerSkin skin;
        if (basePath != null) {
            skin = new PlayerSkin(skinId, basePath);
        } else {
            skin = new PlayerSkin(skinId);
        }

        loadedSkins.put(skinId, skin);
        return skin;
    }

    public boolean isLoaded(String skinId) {
        return loadedSkins.containsKey(skinId);
    }

    public void disposeAll() {
        for (PlayerSkin skin : loadedSkins.values()) {
            skin.dispose();
        }
        loadedSkins.clear();
    }
}
