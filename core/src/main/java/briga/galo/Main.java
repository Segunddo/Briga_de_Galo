package briga.galo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Player player1;

    @Override
    public void create() {
        batch = new SpriteBatch();

        Control controlP1 = new Control(50, 50);
        player1 = new Player(controlP1);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        player1.visual_refresh(delta);

        // Desenha na tela
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        player1.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        player1.dispose();
    }
}
