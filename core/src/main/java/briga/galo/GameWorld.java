package briga.galo;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import player.Player;
import player.PlayerModel;
import window.BackGround;

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

        // A instanciação agora é direta e muito mais limpa!
        InputHandler inputP1 = new InputHandler(Input.Keys.A, Input.Keys.D, Input.Keys.W, Input.Keys.SPACE, Input.Keys.S);
        Player p1 = new Player(50, 50, inputP1);
        players.add(p1);

        InputHandler inputP2 = new InputHandler(Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.UP, Input.Keys.ENTER, Input.Keys.DOWN);
        Player p2 = new Player(1870, 50, inputP2);
        players.add(p2);

        // INICIA UMA THREAD PARA CADA JOGADOR
        for (Player player : players) {
            Thread t = new Thread(player);
            t.start();
        }
    }

    public void update(float delta) {
        // As Threads estão calculando a física em paralelo.
        // O mundo agora só manda atualizar o visual (texturas/animação).
        for (Player player : players) {
            player.visualRefresh(delta); // Nome adaptado para o camelCase usado na refatoração
        }
        checkCombat();
    }

    public void draw(SpriteBatch batch) {
        int p1Life = players.get(0).getModel().getLife();
        int p2Life = players.get(1).getModel().getLife();

        // PEGA A ESTAMINA DOS JOGADORES (Vai de 0 a 100)
        float p1Stamina = players.get(0).getModel().getStamina();
        float p2Stamina = players.get(1).getModel().getStamina();

        backGround.draw(batch, width, height, p1Life, p2Life, p1Stamina, p2Stamina);

        for (Player player : players) {
            player.draw(batch);
        }
    }

    private boolean isColliding() {
        // Pegamos as referências dos modelos lógicos para checar colisão
        PlayerModel p1Model = players.get(0).getModel();
        PlayerModel p2Model = players.get(1).getModel();

        float p1_x = p1Model.getX();
        float p1_y = p1Model.getY();
        float p1_size = p1Model.getHitBox();

        float p2_x = p2Model.getX();
        float p2_y = p2Model.getY();
        float p2_size = p2Model.getHitBox();

        return (p1_x < p2_x + p2_size) &&
            (p1_x + p1_size > p2_x) &&
            (p1_y < p2_y + p2_size) &&
            (p1_y + p1_size > p2_y);
    }

    private void checkCombat() {
        if (isColliding()) {
            PlayerModel p1Model = players.get(0).getModel();
            PlayerModel p2Model = players.get(1).getModel();

            // JOGADOR 1 ATACA O JOGADOR 2
            if (p1Model.isAttacking()) {
                if (p2Model.isDefending()) {
                    // PARRY! P2 defende, P1 é jogado para trás.
                    boolean pushP1Left = p1Model.getX() < p2Model.getX();
                    p1Model.triggerRepel(pushP1Left);
                } else {
                    // P2 não defendeu, toma dano!
                    p2Model.takeDamage(10);
                }
            }

            // JOGADOR 2 ATACA O JOGADOR 1
            if (p2Model.isAttacking()) {
                if (p1Model.isDefending()) {
                    // PARRY! P1 defende, P2 é jogado para trás.
                    boolean pushP2Left = p2Model.getX() < p1Model.getX();
                    p2Model.triggerRepel(pushP2Left);
                } else {
                    // P1 não defendeu, toma dano!
                    p1Model.takeDamage(10);
                }
            }
        }
    }

    public void dispose() {
        backGround.dispose();
        for (Player player : players) {
            player.stopThread();
            player.dispose();
        }
    }

    public boolean isGameOver() {
        return players.get(0).getModel().getLife() <= 0 || players.get(1).getModel().getLife() <= 0;
    }

    public int getPlayer1Life() {
        return players.get(0).getModel().getLife();
    }

    public int getPlayer2Life() {
        return players.get(1).getModel().getLife();
    }
}
