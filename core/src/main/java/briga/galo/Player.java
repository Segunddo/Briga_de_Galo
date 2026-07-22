package briga.galo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player {
    private Control control;

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

    public Player(Control control) {
        this.control = control;

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
        // Controle atualiza a física e matemática
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
