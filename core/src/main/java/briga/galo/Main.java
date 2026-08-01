package briga.galo;

import audio.AudioManager;
import audio.MusicType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import window.CharacterSelect;
import window.JoinMatch;
import window.PlayerChanging;
import window.EndGame;
import window.Menu;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;

    // Nossas instâncias de telas e mundo
    private GameWorld world;
    private Menu menu;
    private JoinMatch joinMatch;
    private PlayerChanging playerChanging;
    private CharacterSelect characterSelect;
    private EndGame endGame;

    // A Máquina de Estados
    private Utils.StateGame currentState;

    private final float width = 1920f;
    private final float height = 1080f;

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(width, height, camera);

        // Inicializa o estado inicial
        currentState = Utils.StateGame.MENU;
        menu = new Menu();
        joinMatch = new JoinMatch();
        playerChanging = new PlayerChanging();
        characterSelect = new CharacterSelect();
        endGame = new EndGame();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // MÁQUINA DE ESTADOS
        switch (currentState) {
            case MENU:
                // Atualiza a lógica do menu (navegação entre opções e efeito de piscar)
                menu.update(delta);

                // Desenha o menu na tela
                menu.draw(batch);

                // Transição de Estado, de acordo com a opção selecionada
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    if (menu.isCreateMatchSelected()) {
                        currentState = Utils.StateGame.PLAYER_CHANGING;
                    } else if (menu.isJoinMatchSelected()) {
                        currentState = Utils.StateGame.JOIN_MATCH;
                    }
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    Gdx.app.exit(); // Fecha o jogo
                }
                break;

            case JOIN_MATCH:
                // Atualiza a navegação da lista de partidas
                joinMatch.update(delta);

                // Desenha a lista de partidas disponíveis
                joinMatch.draw(batch, width, height);

                // Aperte ENTER para "entrar" na partida selecionada
                // (por enquanto só avança para a sala de espera, sem lógica de conexão)
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    currentState = Utils.StateGame.PLAYER_CHANGING;
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    currentState = Utils.StateGame.MENU;
                }
                break;

            case PLAYER_CHANGING:
                // Atualiza a animação de espera
                playerChanging.update(delta);

                // Desenha a sala de espera
                playerChanging.draw(batch, width, height);

                // Provisório: Aperte ENTER para simular que o P2 conectou
                // Assim que os dois estiverem "prontos", vai pra seleção de personagem
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    characterSelect.reset();
                    currentState = Utils.StateGame.CHARACTER_SELECT;
                }
                break;

            case CHARACTER_SELECT:
                // Cada jogador escolhe e confirma seu personagem
                characterSelect.update(delta);

                // Desenha a tela de seleção
                characterSelect.draw(batch, width, height);

                // Assim que os dois jogadores confirmarem, a partida começa
                if (characterSelect.isBothReady()) {
                    if (world != null) world.dispose();
                    world = new GameWorld(characterSelect.getPlayer1SkinId(), characterSelect.getPlayer2SkinId());
                    AudioManager.getInstance().playMusic(MusicType.BATTLE_THEME);
                    currentState = Utils.StateGame.MATCH;
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    // Cancela e volta pro menu
                    currentState = Utils.StateGame.MENU;
                }
                break;

            case MATCH:
                world.update(delta);
                world.draw(batch);

                // Checa se alguém morreu para mudar de estado
                if (world != null && world.isGameOver()) {
                    // Passa a vida dos jogadores para a tela de endgame descobrir quem ganhou
                    endGame.setWinner(world.getPlayer1Life(), world.getPlayer2Life());
                    currentState = Utils.StateGame.ENDGAME;
                }
                break;

            case ENDGAME:
                // No endgame, desenhamos o mundo parado no fundo
                if (world != null) {
                    world.draw(batch);
                }

                // Atualiza e desenha a tela de fim de jogo por cima
                endGame.update(delta);
                endGame.draw(batch, width, height);

                // Se apertar ENTER, destrói o mundo atual e volta pro menu
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    if (world != null) {
                        world.dispose();
                        world = null;
                    }
                    currentState = Utils.StateGame.MENU;
                }
                break;
        }
        batch.end();
    }

    private void checkEndGameCondition() {
        if (world != null && world.isGameOver()) {
            currentState = Utils.StateGame.ENDGAME;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (world != null) world.dispose();
        if (menu != null) menu.dispose();
        if (joinMatch != null) joinMatch.dispose();
        if (playerChanging != null) playerChanging.dispose();
        if (characterSelect != null) characterSelect.dispose();
        if (endGame != null) endGame.dispose();
        AudioManager.getInstance().dispose();
    }
}
