package briga.galo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player implements Runnable {

    //PROBLEMA: DO JEITO QUE AS THREADS FORAM CRIADAS
    //GEROU PROBLEMA DE RACE CONDITION
    //PORTANTO, VOU USAR MONITORES PARA RESOLVER.

    //DO JEITO QUE ESTA AGORA, VARIAS THREADS ESTAO ACESSANDO AS POSIÇÕES SIMULTANEAMENTE
    //ISSO PODE GERAR PROBLEMAS NA HORA DE CHECAR COLISOES

    private volatile boolean running = true;

    private Control control;
    private InputHandler inputHandler;
    private int playerLife;
    private int playerHitBox;

    private Texture LeftSprite;
    private Texture RightSprite;
    private Texture flyRightSprite;
    private Texture flyLeftSprite;
    private Texture attackLeftSprite;
    private Texture attackRightSprite;

    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> flyRightAnimation;
    private Animation<TextureRegion> flyLeftAnimation;
    private Animation<TextureRegion> attackLeftAnimation;
    private Animation<TextureRegion> attackRightAnimation;

    // imagens estaticas
    private TextureRegion imgIdle;
    private TextureRegion imgLeft;
    private TextureRegion imgRight;

    // Região atual que será desenhada
    private TextureRegion currentFrame;

    // GUARDA A AÇÃO ATUAL PARA SABERMOS QUANDO MUDAR
    private Utils.Action currentAction = Utils.Action.IDLE;

    // Variável para controlar o tempo da animação
    private float stateTime;

    // Configurações do Sprite (Andar: 1200x300 -> 4 quadros de 300x300)
    private static final int TILE_WIDTH = 300;
    private static final int TILE_HEIGHT = 300;

    public Player(Control control, InputHandler inputHandler) {
        this.control = control;
        this.inputHandler = inputHandler;
        this.playerLife = 100;
        this.playerHitBox = 300;

        this.RightSprite = new Texture("walking_right_sprite.png");
        this.LeftSprite = new Texture("walking_left_sprite.png");
        this.flyRightSprite = new Texture("fly_right_sprite.png");
        this.flyLeftSprite = new Texture("fly_left_sprite.png");
        this.attackLeftSprite = new Texture("attack_sprite_left.png");
        this.attackRightSprite = new Texture("attack_sprite_right.png");

        // Corta os spritesheet nas dimensões corretas
        TextureRegion[][] tmpRight = TextureRegion.split(RightSprite, TILE_WIDTH, TILE_HEIGHT);
        TextureRegion[][] tmpLeft = TextureRegion.split(LeftSprite, TILE_WIDTH, TILE_HEIGHT);
        TextureRegion[][] tmpFlyRight = TextureRegion.split(flyRightSprite, TILE_WIDTH, TILE_HEIGHT);
        TextureRegion[][] tmpFlyLeft = TextureRegion.split(flyLeftSprite, TILE_WIDTH, TILE_HEIGHT);
        TextureRegion[][] tmpAttackLeft = TextureRegion.split(attackLeftSprite, TILE_WIDTH, TILE_HEIGHT);
        TextureRegion[][] tmpAttackRight = TextureRegion.split(attackRightSprite, TILE_WIDTH, TILE_HEIGHT);

        float frameDuration = 0.1f;

        this.walkRightAnimation = new Animation<>(frameDuration, tmpRight[0]);
        this.walkRightAnimation.setPlayMode(Animation.PlayMode.LOOP);

        this.walkLeftAnimation = new Animation<>(frameDuration, tmpLeft[0]);
        this.walkLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);

        this.flyRightAnimation = new Animation<>(frameDuration, tmpFlyRight[0]);
        this.flyRightAnimation.setPlayMode(Animation.PlayMode.LOOP);

        this.flyLeftAnimation = new Animation<>(frameDuration, tmpFlyLeft[0]);
        this.flyLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);

        this.attackLeftAnimation = new Animation<>(frameDuration, tmpAttackLeft[0]);
        this.attackLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);

        this.attackRightAnimation = new Animation<>(frameDuration, tmpAttackRight[0]);
        this.attackRightAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Poses estáticas
        this.imgIdle = tmpRight[0][0];
        this.imgLeft = tmpLeft[0][0];
        this.imgRight = tmpRight[0][0];

        this.currentFrame = imgIdle;
        this.stateTime = 0f;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();

        while (running) {
            // Calcula o próprio "delta" de tempo da Thread
            long now = System.nanoTime();
            float threadDelta = (now - lastTime) / 1000000000f;
            lastTime = now;

            // Lê os inputs do teclado
            if (inputHandler != null) {
                control.set_inputs(
                    inputHandler.isAttack(),
                    inputHandler.isJump(),
                    inputHandler.isRight(),
                    inputHandler.isLeft()
                );
            }

            // Atualiza a física na Thread
            control.update_logic(threadDelta);

            // Pausa a thread por 16 milissegundos para rodar a aprox. 60 FPS
            // Isso impede que a Thread consuma 100% da CPU do computador
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        this.running = false;
    }

    public void visual_refresh(float delta) {
        Utils.Action newAction = control.get_visual_state();

        //Se a ação mudou, nós resetamos o relógio da animação!
        if (newAction != currentAction) {
            stateTime = 0f;
            currentAction = newAction;
        } else {
            // Só avança o tempo se o personagem estiver fazendo a mesma coisa
            stateTime += delta;
        }

        switch (currentAction) {
            case WALK_RIGHT:
                currentFrame = walkRightAnimation.getKeyFrame(stateTime);
                break;
            case WALK_LEFT:
                currentFrame = walkLeftAnimation.getKeyFrame(stateTime);
                break;
            case FLY_RIGHT:
                currentFrame = flyRightAnimation.getKeyFrame(stateTime);
                break;
            case FLY_LEFT:
                currentFrame = flyLeftAnimation.getKeyFrame(stateTime);
                break;
            case FLY_ATTACK_LEFT:
                currentFrame = attackLeftAnimation.getKeyFrame(stateTime);
                break;
            case FLY_ATTACK_RIGHT:
                currentFrame = attackRightAnimation.getKeyFrame(stateTime);
                break;
            case ATTACK:
                if (control.isHeadingLeft){ // todo: mudar essa checagem para dentro do control
                    currentFrame = attackLeftAnimation.getKeyFrame(stateTime);
                } else {
                    currentFrame = attackRightAnimation.getKeyFrame(stateTime);
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

    public int get_player_life() { return playerLife; }
    public int get_player_hitBox() { return playerHitBox; }
    public float get_x() { return control.x; }
    public float get_y() { return control.y; }
    public boolean is_attacking() { return control.is_attacking(); }

    public void take_damage(int damage) {
        this.playerLife -= damage;
        if (this.playerLife < 0) {
            this.playerLife = 0;
        }
    }

    public void draw(SpriteBatch batch) {
        if (currentFrame != null) {
            // Desenha respeitando a largura e altura reais do quadro atual
            batch.draw(
                currentFrame,
                control.x,
                control.y,
                currentFrame.getRegionWidth(),
                currentFrame.getRegionHeight()
            );
        }
    }

    public void dispose() {
        if (LeftSprite != null) LeftSprite.dispose();
        if (RightSprite != null) RightSprite.dispose();
        if (flyRightSprite != null) flyRightSprite.dispose();
        if (flyLeftSprite != null) flyLeftSprite.dispose();
        if (attackLeftSprite != null) attackLeftSprite.dispose();
        if (attackRightSprite != null) attackRightSprite.dispose();
    }
}
