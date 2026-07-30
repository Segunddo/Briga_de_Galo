package player;

import briga.galo.Utils;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class PlayerRenderer {
    private static final int TILE_WIDTH = 300;
    private static final int TILE_HEIGHT = 300;

    // Todas as texturas e animações ficam aqui
    private Texture rightSprite, leftSprite, flyRightSprite, flyLeftSprite, attackLeftSprite, attackRightSprite;
    private Animation<TextureRegion> walkRight, walkLeft, flyRight, flyLeft, attackLeft, attackRight;
    private TextureRegion imgIdle, imgLeft, imgRight, currentFrame;

    private Utils.Action currentAction = Utils.Action.IDLE;
    private float stateTime = 0f;

    public PlayerRenderer() {
        // 1. CARREGA AS TEXTURAS (Arquivos PNG)
        this.rightSprite = new Texture("walking_right_sprite.png");
        this.leftSprite = new Texture("walking_left_sprite.png");
        this.flyRightSprite = new Texture("fly_right_sprite.png");
        this.flyLeftSprite = new Texture("fly_left_sprite.png");
        this.attackLeftSprite = new Texture("attack_sprite_left.png");
        this.attackRightSprite = new Texture("attack_sprite_right.png");

        // 2. CORTA OS SPRITESHEETS
        TextureRegion[][] tmpRight = TextureRegion.split(rightSprite, TILE_WIDTH, TILE_HEIGHT);
        TextureRegion[][] tmpLeft = TextureRegion.split(leftSprite, TILE_WIDTH, TILE_HEIGHT);
        TextureRegion[][] tmpFlyRight = TextureRegion.split(flyRightSprite, TILE_WIDTH, TILE_HEIGHT);
        TextureRegion[][] tmpFlyLeft = TextureRegion.split(flyLeftSprite, TILE_WIDTH, TILE_HEIGHT);
        TextureRegion[][] tmpAttackLeft = TextureRegion.split(attackLeftSprite, TILE_WIDTH, TILE_HEIGHT);
        TextureRegion[][] tmpAttackRight = TextureRegion.split(attackRightSprite, TILE_WIDTH, TILE_HEIGHT);

        float frameDuration = 0.1f;

        // 3. CRIA AS ANIMAÇÕES
        this.walkRight = new Animation<>(frameDuration, tmpRight[0]);
        this.walkRight.setPlayMode(Animation.PlayMode.LOOP);

        this.walkLeft = new Animation<>(frameDuration, tmpLeft[0]);
        this.walkLeft.setPlayMode(Animation.PlayMode.LOOP);

        this.flyRight = new Animation<>(frameDuration, tmpFlyRight[0]);
        this.flyRight.setPlayMode(Animation.PlayMode.LOOP);

        this.flyLeft = new Animation<>(frameDuration, tmpFlyLeft[0]);
        this.flyLeft.setPlayMode(Animation.PlayMode.LOOP);

        this.attackLeft = new Animation<>(frameDuration, tmpAttackLeft[0]);
        this.attackLeft.setPlayMode(Animation.PlayMode.LOOP);

        this.attackRight = new Animation<>(frameDuration, tmpAttackRight[0]);
        this.attackRight.setPlayMode(Animation.PlayMode.LOOP);

        // 4. POSES ESTÁTICAS (Parado)
        this.imgIdle = tmpRight[0][0];
        this.imgLeft = tmpLeft[0][0];
        this.imgRight = tmpRight[0][0];

        this.currentFrame = imgIdle;
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

        // 5. SELECIONA O FRAME CORRETO PARA DESENHAR
        switch (currentAction) {
            case WALK_RIGHT:
                currentFrame = walkRight.getKeyFrame(stateTime);
                break;
            case WALK_LEFT:
                currentFrame = walkLeft.getKeyFrame(stateTime);
                break;
            case FLY_RIGHT:
                currentFrame = flyRight.getKeyFrame(stateTime);
                break;
            case FLY_LEFT:
                currentFrame = flyLeft.getKeyFrame(stateTime);
                break;
            case FLY_ATTACK_LEFT:
                currentFrame = attackLeft.getKeyFrame(stateTime);
                break;
            case FLY_ATTACK_RIGHT:
                currentFrame = attackRight.getKeyFrame(stateTime);
                break;
            case ATTACK:
                if (headingLeft) {
                    currentFrame = attackLeft.getKeyFrame(stateTime);
                } else {
                    currentFrame = attackRight.getKeyFrame(stateTime);
                }
                break;
            case LEFT_HANDLE:
                currentFrame = imgLeft;
                break;
            case RIGHT_HANDLE:
                currentFrame = imgRight;
                break;
            default:
                currentFrame = imgIdle;
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

    // Limpa a memória das texturas quando o jogo fecha
    public void dispose() {
        if (leftSprite != null) leftSprite.dispose();
        if (rightSprite != null) rightSprite.dispose();
        if (flyRightSprite != null) flyRightSprite.dispose();
        if (flyLeftSprite != null) flyLeftSprite.dispose();
        if (attackLeftSprite != null) attackLeftSprite.dispose();
        if (attackRightSprite != null) attackRightSprite.dispose();
    }
}
