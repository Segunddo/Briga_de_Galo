package briga.galo;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import player.Player;
import player.PlayerModel;
import window.BackGround;
import briga.galo.network.ClientDevice;

import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;

public class GameWorld {
    private List<Player> players;
    private BackGround backGround;
    private final float width = 1920f;
    private final float height = 1080f;

    public GameWorld(ClientDevice client, String p1SkinId, String p2SkinId) {
        backGround = new BackGround(0);
        players = new ArrayList<>();

        int myId = client.getMeuPlayerId();

        // JOGADOR 1
        InputHandler inputP1 = (myId == 1)
            ? new InputHandler(Input.Keys.A, Input.Keys.D, Input.Keys.W, Input.Keys.SPACE, Input.Keys.S)
            : new InputHandler(client);

        Player p1 = new Player(0, 50, inputP1, p1SkinId);
        if (myId == 1) {
            p1.setActionListener(new Consumer<Utils.Action>() {
                @Override
                public void accept(Utils.Action acao) {
                    client.enviarAcao(acao);
                }
            });
        }
        players.add(p1);

        //JOGADOR 2
        InputHandler inputP2 = (myId == 2)
            ? new InputHandler(Input.Keys.A, Input.Keys.D, Input.Keys.W, Input.Keys.SPACE, Input.Keys.S)
            : new InputHandler(client);

        Player p2 = new Player(1870, 50, inputP2, p2SkinId);
        if (myId == 2) {
            p2.setActionListener(new Consumer<Utils.Action>() {
                @Override
                public void accept(Utils.Action acao) {
                    client.enviarAcao(acao);
                }
            });
        }
        players.add(p2);

        // Inicia as threads
        for (Player player : players) {
            Thread t = new Thread(player);
            t.start();
        }
    }

    public void update(float delta) {
        for (Player player : players) {
            player.pollInput();
        }
        for (Player player : players) {
            player.visualRefresh(delta);
        }

        checkCombat();
        checkBodyCollision();
        checkMapLimit();
    }

    public void draw(SpriteBatch batch) {
        int p1Life = players.get(0).getModel().getLife();
        int p2Life = players.get(1).getModel().getLife();
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

        // Se nenhuma dessas exclusões for falsa, significa que não há espaço livre entre eles em nenhum dos 4 lados.
        return (p1_x < p2_x + p2_size) &&
            (p1_x + p1_size > p2_x) &&
            (p1_y < p2_y + p2_size) &&
            (p1_y + p1_size > p2_y);
    }

    private void checkBodyCollision() {
        PlayerModel p1Model = players.get(0).getModel();
        PlayerModel p2Model = players.get(1).getModel();

        float p1_x = p1Model.getX();
        float p1_y = p1Model.getY();
        float hitBox = p1Model.getHitBox();

        float p2_x = p2Model.getX();
        float p2_y = p2Model.getY();

        // Checa se eles estão em alturas diferentes (Permite pular por cima)
        // Se a base de um estiver acima do topo do outro, eles não se empurram
        if (p1_y >= p2_y + hitBox || p2_y >= p1_y + hitBox) {
            return; // Estão em alturas diferentes, ignora a colisão de corpos
        }

        // Checa se estão colidindo horizontalmente
        if (p1_x < p2_x + hitBox && p1_x + hitBox > p2_x) {

            // Descobre quem está na esquerda e quem está na direita
            PlayerModel leftPlayer = (p1_x < p2_x) ? p1Model : p2Model;
            PlayerModel rightPlayer = (p1_x < p2_x) ? p2Model : p1Model;

            // Calcula a "invasão" (Quantos pixels um entrou dentro do outro)
            float overlap = (leftPlayer.getX() + hitBox) - rightPlayer.getX();

            if (overlap > 0) {
                // Empurra cada um metade da distância de invasão para trás
                leftPlayer.setX(leftPlayer.getX() - (overlap / 2f));
                rightPlayer.setX(rightPlayer.getX() + (overlap / 2f));
            }
        }
    }

    private void checkMapLimit() {
        // Pegamos as referências dos modelos lógicos para checar colisão
        PlayerModel p1Model = players.get(0).getModel();
        PlayerModel p2Model = players.get(1).getModel();

        // Limites do Jogador 1
        if (p1Model.getX() < 0) {
            p1Model.setX(0);
        } else if (p1Model.getX() + p1Model.getHitBox() > width) {
            p1Model.setX(width - p1Model.getHitBox());
        }

        // Limites do Jogador 2
        if (p2Model.getX() < 0) {
            p2Model.setX(0);
        } else if (p2Model.getX() + p2Model.getHitBox() > width) {
            p2Model.setX(width - p2Model.getHitBox());
        }
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
                    p2Model.takeDamage(1);
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
                    p1Model.takeDamage(1);
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

    public int getPlayer1Life() { return players.get(0).getModel().getLife(); }
    public int getPlayer2Life() { return players.get(1).getModel().getLife(); }
}
