package player;

import briga.galo.InputHandler;
import briga.galo.Utils;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.function.Consumer;

public class Player implements Runnable {
    private volatile boolean running = true;
    private PlayerModel model;
    private PlayerRenderer renderer;
    private InputHandler inputHandler;

    // Callback para enviar a ação resolvida para o servidor
    private Consumer<Utils.Action> actionListener;
    private Utils.Action lastAction = Utils.Action.IDLE;

    public Player(float startX, float startY, InputHandler inputHandler, String skinId) {
        this.model = new PlayerModel(startX, startY);
        this.renderer = new PlayerRenderer(skinId);
        this.inputHandler = inputHandler;
    }

    // Registra quem vai escutar a mudança de ação (o GameWorld passará o ClientDevice aqui)
    public void setActionListener(Consumer<Utils.Action> listener) {
        this.actionListener = listener;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            float threadDelta = (now - lastTime) / 1000000000f;
            lastTime = now;

            if (inputHandler != null) {
                model.setInputs(
                    inputHandler.consumeAttack(),
                    inputHandler.isJump(),
                    inputHandler.isRight(),
                    inputHandler.isLeft(),
                    inputHandler.isDefense()
                );
            }

            // Atualiza a física e os estados
            model.updateLogic(threadDelta);

            // Se for o jogador local, verifica se a ação visual mudou e envia para a rede
            if (actionListener != null) {
                Utils.Action currentAction = model.getCurrentAction();
                // Isso evita que fique enviando "lixo" mandando apenas os dados que realmente mudaram algo
                if (currentAction != lastAction) {
                    actionListener.accept(currentAction);
                    lastAction = currentAction;
                }
            }

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void pollInput() {
        if (inputHandler != null) {
            inputHandler.poll();
        }
    }

    public void visualRefresh(float delta) {
        renderer.update(delta, model);
    }

    public void draw(SpriteBatch batch) {
        renderer.draw(batch, model);
    }

    public PlayerModel getModel() {
        return this.model;
    }

    public void stopThread() {
        this.running = false;
    }

    public void dispose() {
        SkinManager.getInstance().disposeAll();
    }
}
