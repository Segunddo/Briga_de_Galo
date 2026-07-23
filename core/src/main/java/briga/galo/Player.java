package briga.galo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player {
    private Control control;
    private InputHandler inputHandler;
    private int playerLife;
    private int playerHitBox;

    private Texture LeftSprite;
    private Texture RightSprite;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> walkLeftAnimation;

    // imagens estaticas
    private TextureRegion imgIdle;
    private TextureRegion imgJump;
    private TextureRegion imgLeft;
    private TextureRegion imgRight;

    // Variável para controlar o tempo da animação
    private float stateTime;

    // Região atual que será desenhada
    private TextureRegion currentFrame;

    // Configurações do Sprite (Baseado no pedido de 50x50px)
    private static final int FRAME_COLS = 4; // Número de quadros na horizontal
    private static final int FRAME_ROWS = 1; // Número de linhas
    private static final int TILE_WIDTH = 50; // Largura de cada quadro
    private static final int TILE_HEIGHT = 50; // Altura de cada quadro

    // Construtor atualizado recebendo o InputHandler
    public Player(Control control, InputHandler inputHandler) {
        this.control = control;
        this.inputHandler = inputHandler;
        playerLife = 100; // Valor padrão (100 de vida)
        playerHitBox = 20; // 20x20

        this.RightSprite = new Texture("galo_spritesheet_right.png");
        this.LeftSprite = new Texture("galo_spritesheet_left.png");

        // esse tipo é um recorte da imagem
        // como tem 4 imagens para cada animação, dividi em 1 linha e 4 colunas
        TextureRegion[][] tmpRight = TextureRegion.split(RightSprite, TILE_WIDTH, TILE_HEIGHT);
        TextureRegion[][] tmpLeft = TextureRegion.split(LeftSprite, TILE_WIDTH, TILE_HEIGHT);

        // Velocidade da animação (0.1f = 10 quadros por segundo)
        float frameDuration = 0.2f;

        // roda a animação em loop
        TextureRegion[] walkRightFrames = tmpRight[0];
        this.walkRightAnimation = new Animation<>(frameDuration, walkRightFrames);
        this.walkRightAnimation.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion[] walkLeftFrames = tmpLeft[0];
        this.walkLeftAnimation = new Animation<>(frameDuration, walkLeftFrames);
        this.walkLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // peguei poses estaticas do "vetor" de imagens
        this.imgIdle = tmpRight[0][0]; // Galo parado olhando pra direita
        this.imgJump = tmpRight[0][1]; // Um quadro qualquer pra pose de pulo
        this.imgLeft = tmpLeft[0][0];
        this.imgRight = tmpRight[0][0];

        this.currentFrame = imgIdle;
        this.stateTime = 0f;
    }

    public void visual_refresh(float delta) {
        // Se existir um teclado físico atrelado a este jogador, lê os dados e injeta na física
        if (inputHandler != null) {
            control.set_inputs(
                inputHandler.isAttack(),
                inputHandler.isJump(),
                inputHandler.isRight(),
                inputHandler.isLeft()
            );
        }

        // Controle atualiza a física e matemática usando os inputs recebidos
        control.update_logic(delta);

        // Atualiza o tempo acumulado da animação
        stateTime += delta;

        // Pega o estado visual desejado pelo controle
        Utils.Action action = control.get_visual_state();

        // Seleciona o quadro correto baseado na animação e no tempo
        switch (action) {
            case WALK_RIGHT:
                // getKeyFrame pega o quadro correto baseado no stateTime atual
                currentFrame = walkRightAnimation.getKeyFrame(stateTime);
                break;
            case WALK_LEFT:
                currentFrame = walkLeftAnimation.getKeyFrame(stateTime);
                break;
            case JUMP:
                currentFrame = imgJump;
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
        return playerLife;
    }

    public int get_player_hitBox() {
        return playerHitBox;
    }

    public float get_x() {
        return control.x;
    }

    public float get_y() {
        return control.y;
    }

    // Retorna se o usuário clicou em atacar
    public boolean is_attacking() {
        return control.is_attacking();
    }

    // Método para aplicar o dano
    public void take_damage(int damage) {
        this.playerLife -= damage;
        // Evita que a vida fique negativa
        if (this.playerLife < 0) {
            this.playerLife = 0;
        }
    }

    public void draw(SpriteBatch batch) {
        if (currentFrame != null) {
            batch.draw(currentFrame, control.x, control.y, TILE_WIDTH, TILE_HEIGHT);
        }
    }

    public void dispose() {
        LeftSprite.dispose();
        RightSprite.dispose();
    }
}
