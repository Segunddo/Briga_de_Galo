package briga.galo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Player player1;
    private Player player2;
    private BackGround backGround;

    private OrthographicCamera camera;
    private Viewport viewport;

    // Resolução base do seu jogo
    private final float width = 1920f;
    private final float height = 1080f;

    @Override
    public void create() {
        batch = new SpriteBatch();
        backGround = new BackGround(0);

        // O FitViewport mantém a proporção perfeita do seu cenário e dos galos,
        camera = new OrthographicCamera();
        viewport = new FitViewport(width, height, camera);

        // Player 1 fica na esquerda da tala inicialmente
        Control controlP1 = new Control(50, 50);
        player1 = new Player(controlP1);

        // Player 2 fica na direita da tala inicialmente
        Control controlP2 = new Control(1870, 50);
        player2 = new Player(controlP2);
    }

    // Esse método avisa o jogo quando a tela muda de tamanho (maximiza/tela cheia)
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    private void check_combat() {
        // Pega as posições e tamanhos
        float p1_x = player1.get_x();
        float p1_y = player1.get_y();
        float p1_size = player1.get_player_hitBox();

        float p2_x = player2.get_x();
        float p2_y = player2.get_y();
        float p2_size = player2.get_player_hitBox();

        // Calcula a colisão (Verifica se eles estão sobrepostos)
        boolean isColliding = (p1_x < p2_x + p2_size) &&
            (p1_x + p1_size > p2_x) &&
            (p1_y < p2_y + p2_size) &&
            (p1_y + p1_size > p2_y);

        // Se eles estiverem encostados, aplicamos o dano de quem apertou o botão
        if (isColliding) {

            // Se o Player 1 atacou, o Player 2 toma dano
            if (player1.is_attacking()) {
                player2.take_damage(10);
            }

            // Se o Player 2 atacou, o Player 1 toma dano
            if (player2.is_attacking()) {
                player1.take_damage(10);
            }
        }
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        player1.visual_refresh(delta);
        player2.visual_refresh(delta);
        check_combat();

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        // Manda o SpriteBatch usar a lente da câmera ajustada
        batch.setProjectionMatrix(camera.combined);

        // Desenha na tela
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        backGround.draw(batch, width, height, player1.get_player_life(), player2.get_player_life());

        player1.draw(batch);
        player2.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        player1.dispose();
        player2.dispose();
        backGround.dispose();
    }
}
