package player;

import briga.galo.Utils;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class PlayerRenderer {

    private PlayerSkin skin;
    private TextureRegion currentFrame;

    private Utils.Action currentAction = Utils.Action.IDLE;
    private float stateTime = 0f;

    public PlayerRenderer(String skinId) {
        this.skin = SkinManager.getInstance().get(skinId);
        this.currentFrame = skin.getIdleFrame();
    }

    // Troca a skin do personagem em tempo de execução
    public void setSkin(String skinId) {
        this.skin = SkinManager.getInstance().get(skinId);
    }

    public String getSkinId() {
        return skin.getSkinId();
    }

    // Atualiza o tempo da animação e escolhe o frame correto baseado no Model
    public void update(float delta, PlayerModel model) {
        Utils.Action newAction = model.getCurrentAction();
        boolean headingLeft = model.isHeadingLeft();

        if (newAction != currentAction) {
            stateTime = 0f;
            currentAction = newAction;
        } else {
            stateTime += delta;
        }

        // Seleciona o frame correto para desenhar, sempre a partir da skin atual
        switch (currentAction) {
            case WALK_RIGHT:
                currentFrame = skin.getAnimation(SpriteType.WALK_RIGHT).getKeyFrame(stateTime);
                break;
            case WALK_LEFT:
                currentFrame = skin.getAnimation(SpriteType.WALK_LEFT).getKeyFrame(stateTime);
                break;
            case FLY_RIGHT:
                currentFrame = skin.getAnimation(SpriteType.FLY_RIGHT).getKeyFrame(stateTime);
                break;
            case FLY_LEFT:
                currentFrame = skin.getAnimation(SpriteType.FLY_LEFT).getKeyFrame(stateTime);
                break;
            case FLY_ATTACK_LEFT:
                currentFrame = skin.getAnimation(SpriteType.ATTACK_LEFT).getKeyFrame(stateTime);
                break;
            case FLY_ATTACK_RIGHT:
                currentFrame = skin.getAnimation(SpriteType.ATTACK_RIGHT).getKeyFrame(stateTime);
                break;
            case ATTACK:
                currentFrame = headingLeft
                    ? skin.getAnimation(SpriteType.ATTACK_LEFT).getKeyFrame(stateTime)
                    : skin.getAnimation(SpriteType.ATTACK_RIGHT).getKeyFrame(stateTime);
                break;
            case DEFEND_LEFT:
                currentFrame = skin.getAnimation(SpriteType.DEFEND_LEFT).getKeyFrame(stateTime);
                break;
            case DEFEND_RIGHT:
                currentFrame = skin.getAnimation(SpriteType.DEFEND_RIGHT).getKeyFrame(stateTime);
                break;
            case LEFT_HANDLE:
                currentFrame = skin.getLeftFrame();
                break;
            case RIGHT_HANDLE:
                currentFrame = skin.getRightFrame();
                break;
            default:
                currentFrame = skin.getIdleFrame();
                break;
        }
    }

    // Desenha na tela lendo as coordenadas do Model
    public void draw(SpriteBatch batch, PlayerModel model) {
        if (currentFrame != null) {
            batch.draw(
                currentFrame,
                model.getX(),
                model.getY(),
                currentFrame.getRegionWidth(),
                currentFrame.getRegionHeight()
            );
        }
    }
}
