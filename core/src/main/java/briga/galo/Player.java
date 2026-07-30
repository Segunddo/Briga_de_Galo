package briga.galo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player implements Runnable {

    // a thread do player escreve x, y, isHeadingLeft e playerLife
    // via control.update_logic() e take_damage().
    // OUTRAS THREADS (a de renderizacao, e a thread do adversario quando checa
    // colisao) leem esses mesmos valores atraves de get_x(), get_y(),
    // is_attacking() e get_player_life().

    private volatile boolean running = true;

    // Monitor que protege o estado compartilhado deste Player (posicao,
    // direcao e vida). Cada instancia de Player tem o seu proprio lock,
    // entao jogadores diferentes nao competem pelo mesmo monitor.
    private final Object lock = new Object();

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
    // (só lida/escrita pela thread de renderização, dentro de visual_refresh/draw,
    // por isso não precisa do monitor)
    private Utils.Action currentAction = Utils.Action.IDLE;

    // Variável para controlar o tempo da animação
    private float stateTime;

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

            // Lê os inputs do teclado (só esta thread escreve/lê os inputs, sem risco)
            if (inputHandler != null) {
                control.set_inputs(
                    inputHandler.isAttack(),
                    inputHandler.isJump(),
                    inputHandler.isRight(),
                    inputHandler.isLeft()
                );
            }

            // Regiao critica: update_logic() é quem altera x, y e isHeadingLeft
            // dentro de Control. Qualquer thread que leia esses valores (render,
            // ou o adversário checando colisão) usa o MESMO monitor (lock),
            // então nunca vê um estado "pela metade".
            synchronized (lock) {
                control.update_logic(threadDelta);
            }

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
        Utils.Action newAction;
        boolean headingLeft;

        // Le o estado de Control de forma atômica, protegida
        // pelo mesmo monitor usado em run().
        synchronized (lock) {
            newAction = control.get_visual_state();
            headingLeft = control.isHeadingLeft;
        }

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
                if (headingLeft) {
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

    public int get_player_life() {
        synchronized (lock) {
            return playerLife;
        }
    }

    public int get_player_hitBox() {
        return playerHitBox;
    }

    public float get_x() {
        synchronized (lock) {
            return control.x;
        }
    }

    public float get_y() {
        synchronized (lock) {
            return control.y;
        }
    }

    public boolean is_attacking() {
        synchronized (lock) {
            return control.is_attacking();
        }
    }

    public void take_damage(int damage) {
        // atualização de física do jogador, precisa do monitor.
        synchronized (lock) {
            this.playerLife -= damage;
            if (this.playerLife < 0) {
                this.playerLife = 0;
            }
        }
    }

    public void draw(SpriteBatch batch) {
        if (currentFrame != null) {
            // Le x e y de uma vez só, dentro do monitor, para garantir que
            // as duas coordenadas pertencem ao mesmo instante (evita
            // desenhar com x novo e y antigo, por exemplo).
            float x, y;
            synchronized (lock) {
                x = control.x;
                y = control.y;
            }

            batch.draw(
                currentFrame,
                x,
                y,
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
