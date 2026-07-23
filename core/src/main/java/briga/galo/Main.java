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
    private OrthographicCamera camera;
    private Viewport viewport;
    private GameWorld world; // Apenas inicializamos o mundo do jogo

    private final float width = 1920f;
    private final float height = 1080f;

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(width, height, camera);
        world = new GameWorld();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // O Mundo processa toda a matemática e física do frame
        world.update(delta);

        // Prepara a câmera e limpa a tela
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.setProjectionMatrix(camera.combined);

        // O Mundo desenha os componentes visuais
        batch.begin();
        world.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        world.dispose();
    }
}
