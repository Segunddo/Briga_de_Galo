package briga.galo;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.List;

public class GameWorld {
    private List<Player> players;
    private BackGround backGround;
    private final float width = 1920f;
    private final float height = 1080f;

    public GameWorld() {
        backGround = new BackGround(0);
        players = new ArrayList<>();

        // Player 1 (Comandos clássicos)
        Control controlP1 = new Control(50, 50);
        InputHandler inputP1 = new InputHandler(Input.Keys.A, Input.Keys.D, Input.Keys.W, Input.Keys.SPACE);
        players.add(new Player(controlP1, inputP1));

        // Player 2 (Setas direcionais)
        Control controlP2 = new Control(1870, 50);
        InputHandler inputP2 = new InputHandler(Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.UP, Input.Keys.ENTER);
        players.add(new Player(controlP2, inputP2));
    }

    public void update(float delta) {
        for (Player player : players) {
            player.visual_refresh(delta);
        }
        check_combat();
    }

    public void draw(SpriteBatch batch) {
        backGround.draw(batch, width, height, players.get(0).get_player_life(), players.get(1).get_player_life());
        for (Player player : players) {
            player.draw(batch);
        }
    }

    private boolean is_colliding() {
        float p1_x = players.get(0).get_x();
        float p1_y = players.get(0).get_y();
        float p1_size = players.get(0).get_player_hitBox();

        float p2_x = players.get(1).get_x();
        float p2_y = players.get(1).get_y();
        float p2_size = players.get(1).get_player_hitBox();

        return (p1_x < p2_x + p2_size) &&
            (p1_x + p1_size > p2_x) &&
            (p1_y < p2_y + p2_size) &&
            (p1_y + p1_size > p2_y);
    }

    private void check_combat() {
        if (is_colliding()) {
            if (players.get(0).is_attacking()) players.get(1).take_damage(10);
            if (players.get(1).is_attacking()) players.get(0).take_damage(10);
        }
    }

    public void dispose() {
        backGround.dispose();
        for (Player player : players) {
            player.dispose();
        }
    }
}
