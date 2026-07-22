package briga.galo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private List<Player> players;

    @Override
    public void create() {
        batch = new SpriteBatch();

        players = new ArrayList<>();

        // Jogador 1: WASD + espaço
        Control controlP1 = new Control(50, 50, Input.Keys.A, Input.Keys.D, Input.Keys.W, Input.Keys.SPACE);
        players.add(new Player(controlP1));

        // Jogador 2: setas + enter
        Control controlP2 = new Control(400, 50, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.UP, Input.Keys.ENTER);
        players.add(new Player(controlP2));

        // metodo simples pra criar personagens novos (apenas para debug, depois vou fazer em threads)
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        for (Player player : players) {
            player.visual_refresh(delta);
        }

        // Desenha na tela
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        for (Player player : players) {
            player.draw(batch);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        for (Player player : players) {
            player.dispose();
        }
    }
}
