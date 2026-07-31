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

    private SkinManager() {
    }

    // caso a skin não siga a convenção "skins/<id>/". Ex: a skin "default",
    // cujos arquivos hoje estão soltos na raiz dos assets:
    //   SkinManager.getInstance().registerSkinPath("default", "");
    public void registerSkinPath(String skinId, String basePath) {
        customPaths.put(skinId, basePath);
    }

    public PlayerSkin get(String skinId) {
        return loadedSkins.computeIfAbsent(skinId, id -> {
            String basePath = customPaths.get(id);
            return basePath != null ? new PlayerSkin(id, basePath) : new PlayerSkin(id);
        });
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
