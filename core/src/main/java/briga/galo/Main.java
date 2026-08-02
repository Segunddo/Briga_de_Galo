package briga.galo;

import audio.AudioManager;
import audio.MusicType;
import briga.galo.network.ClientDevice;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import window.EndGame;
import window.Menu;
import window.PlayerChanging;
import window.CharacterSelect;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;

    private GameWorld world;
    private Menu menu;
    private PlayerChanging playerChanging;
    private CharacterSelect characterSelect;
    private EndGame endGame;

    private ClientDevice client;
    private Utils.StateGame localState;

    // Flag para lembrar se o jogador clicou em "Criar" ou "Entrar" antes de ir pra tela de skins
    private boolean isCreatingRoom = false;

    private final float width = 1920f;
    private final float height = 1080f;

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(width, height, camera);

        menu = new Menu();
        playerChanging = new PlayerChanging();
        characterSelect = new CharacterSelect();
        endGame = new EndGame();

        client = new ClientDevice();
        client.connect("127.0.0.1", 5000);
        localState = Utils.StateGame.MENU;
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        Utils.StateGame networkState = client.getEstadoAtual();

        if (world != null && world.isGameOver() && networkState == Utils.StateGame.MATCH) {
            client.solicitarCancelar();
            endGame.setWinner(world.getPlayer1Life(), world.getPlayer2Life());
            localState = Utils.StateGame.ENDGAME;
        } else if (networkState == Utils.StateGame.ENDGAME && localState != Utils.StateGame.ENDGAME) {
            endGame.setWinner(world != null ? world.getPlayer1Life() : 0,
                world != null ? world.getPlayer2Life() : 0);
            localState = Utils.StateGame.ENDGAME;
        // Se o jogo local não forçou um EndGame, a tela copia o que o servidor ditar
        } else if (!worldGameOverLocked() && localState != Utils.StateGame.CHARACTER_SELECT) {
            localState = networkState;
        }

        switch (localState) {
            case MENU:
                if (world != null) {
                    world.dispose();
                    world = null;
                }
                menu.update(delta);
                menu.draw(batch);

                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    // Ao invés de ir direto pro lobby da rede, vai pra tela de selecionar o Galo
                    if (menu.isCreateMatchSelected()) {
                        isCreatingRoom = true;
                        characterSelect.reset();
                        localState = Utils.StateGame.CHARACTER_SELECT;
                    } else if (menu.isJoinMatchSelected()) {
                        isCreatingRoom = false;
                        characterSelect.reset();
                        localState = Utils.StateGame.CHARACTER_SELECT;
                    }
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    Gdx.app.exit();
                }
                break;

            case CHARACTER_SELECT:
                characterSelect.update(delta);
                characterSelect.draw(batch, width, height);

                if (characterSelect.isPlayer1Ready()) {

                    // Pega a skin real que você escolheu na interface
                    String mySkin = characterSelect.getPlayer1SkinId();

                    if (isCreatingRoom) {
                        client.solicitarCriarSala(mySkin);
                    } else {
                        client.solicitarEntrarSala(mySkin);
                    }

                    localState = Utils.StateGame.WAITING;
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    localState = Utils.StateGame.MENU;
                }
                break;

            case WAITING:
                playerChanging.update(delta);
                playerChanging.draw(batch, width, height);
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    client.solicitarCancelar();
                }
                break;

            case MATCH:
                if (world == null) {
                    // O servidor mandou iniciar. Pega a skin exata de cada um e cria a arena.
                    world = new GameWorld(client, client.getSkinP1(), client.getSkinP2());
                    AudioManager.getInstance().playMusic(MusicType.BATTLE_THEME);
                }
                world.update(delta);
                world.draw(batch);
                break;

            case ENDGAME:
                if (world != null) world.draw(batch);
                endGame.update(delta);
                endGame.draw(batch, width, height);

                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    client.solicitarCancelar();
                    localState = Utils.StateGame.MENU;
                }
                break;
        }

        batch.end();
    }

    private boolean worldGameOverLocked() {
        return localState == Utils.StateGame.ENDGAME;
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
        if (playerChanging != null) playerChanging.dispose();
        if (characterSelect != null) characterSelect.dispose();
        if (endGame != null) endGame.dispose();
        AudioManager.getInstance().dispose();
        if (client != null) client.fechar();
    }
}
