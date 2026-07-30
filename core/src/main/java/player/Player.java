package player;

import briga.galo.InputHandler;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player implements Runnable {
    private volatile boolean running = true;

    // O jogador possui um Model (dados) e um Renderer (visual)
    private PlayerModel model;
    private PlayerRenderer renderer;
    private InputHandler inputHandler;

    public Player(float startX, float startY, InputHandler inputHandler) {
        this.model = new PlayerModel(startX, startY);
        this.renderer = new PlayerRenderer();
        this.inputHandler = inputHandler;
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
                    inputHandler.isAttack(),
                    inputHandler.isJump(),
                    inputHandler.isRight(),
                    inputHandler.isLeft(),
                    inputHandler.isDefense()
                );
            }

            // A atualização lógica agora fica toda no model
            model.updateLogic(threadDelta);

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Chamado na thread principal de renderização do LibGDX
    public void visualRefresh(float delta) {
        renderer.update(delta, model);
    }

    // Chamado na thread principal de renderização do LibGDX
    public void draw(SpriteBatch batch) {
        renderer.draw(batch, model);
    }

    public PlayerModel getModel() {
        return this.model; // Permite que o servidor ou GameWorld acessem vida e hitbox
    }

    public void stopThread() {
        this.running = false;
    }

    public void dispose() {
        renderer.dispose();
    }
}
