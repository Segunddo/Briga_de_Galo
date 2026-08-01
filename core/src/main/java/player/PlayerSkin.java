package player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

public class PlayerSkin {
    private static final int TILE_WIDTH = 300;
    private static final int TILE_HEIGHT = 300;
    private static final float FRAME_DURATION = 0.1f;

    private final String skinId;
    private final Map<SpriteType, Texture> textures = new EnumMap<>(SpriteType.class);
    private final Map<SpriteType, Animation<TextureRegion>> animations = new EnumMap<>(SpriteType.class);

    private final TextureRegion imgIdle;
    private final TextureRegion imgLeft;
    private final TextureRegion imgRight;

    // Convenção padrão de pasta: skins/<skinId>/<arquivo>.png
    public PlayerSkin(String skinId) {
        this(skinId, "skins/" + skinId + "/");
    }

    // Use este construtor se os arquivos dessa skin não seguirem a convenção padrão
    // (por exemplo, a skin "default" que hoje está na raiz dos assets).
    public PlayerSkin(String skinId, String basePath) {
        this.skinId = skinId;

        for (SpriteType type : SpriteType.values()) {
            Texture texture = new Texture(basePath + type.getDefaultFileName());
            textures.put(type, texture);

            TextureRegion[][] frames = TextureRegion.split(texture, TILE_WIDTH, TILE_HEIGHT);

            Animation<TextureRegion> animation = new Animation<>(FRAME_DURATION, frames[0]);
            animation.setPlayMode(Animation.PlayMode.LOOP);
            animations.put(type, animation);
        }

        // Poses estáticas (parado), reaproveitando o primeiro frame de cada animação
        this.imgRight = animations.get(SpriteType.WALK_RIGHT).getKeyFrames()[0];
        this.imgLeft = animations.get(SpriteType.WALK_LEFT).getKeyFrames()[0];
        this.imgIdle = imgRight;
    }

    public String getSkinId() {
        return skinId;
    }

    public Animation<TextureRegion> getAnimation(SpriteType type) {
        return animations.get(type);
    }

    public TextureRegion getIdleFrame() {
        return imgIdle;
    }

    public TextureRegion getLeftFrame() {
        return imgLeft;
    }

    public TextureRegion getRightFrame() {
        return imgRight;
    }

    public void dispose() {
        for (Texture texture : textures.values()) {
            if (texture != null) texture.dispose();
        }
    }
}
