package briga.galo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player {
    private Control control;

    // Apenas Imagens e Visual
    private Texture imgIdle;
    private Texture imgAttack;
    private Texture currentImage;

    public Player(Control control) {
        this.control = control;

        // Carrega as imagens
        this.imgIdle = new Texture("idle.png");
        this.imgAttack = new Texture("attack.png");
        this.currentImage = imgIdle;
    }

    public void visual_refresh(float delta) {
        // Controle atualiza a matemática
        control.update_logic(delta);

        // Controle diz qual animação deve tocar
        Utils.Action action = control.get_visual_state();

        switch (action) {
            case ATTACK:
                // currentImage = imgAttack;
                break;
            case IDLE:
            case WALK_RIGHT:
            case WALK_LEFT:
            case JUMP:
                // Se ainda não tiver imagens para tudo, joga pro Idle
                currentImage = imgAttack;
                break;
        }
    }

    public void draw(SpriteBatch batch) {
        // Usa o X e Y diretos do controle para desenhar
        batch.draw(currentImage, control.x, control.y);
    }

    public void dispose() {
        imgIdle.dispose();
        imgAttack.dispose();
    }
}
