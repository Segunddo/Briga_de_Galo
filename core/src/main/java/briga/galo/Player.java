package briga.galo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player {
    private Control control;

    // Apenas Imagens e Visual
    private Texture imgIdle;
    private Texture imgAttack;
    private Texture imgFallingAttack;
    private Texture imgFlying;
    private Texture imgWalking;
    private Texture currentImage;


    public Player(Control control) {
        this.control = control;

        // Carrega as imagens
        this.imgIdle = new Texture("idle.png");
        this.imgAttack = new Texture("attack.png");
        this.imgFallingAttack = new Texture("falling_attack.png");
        this.imgFlying = new Texture("flying.png");
        this.imgWalking = new Texture("walking.png");
        this.currentImage = imgIdle;
    }

    public void visual_refresh(float delta) {
        // Controle atualiza a matemática
        control.update_logic(delta);

        // Controle diz qual animação deve tocar
        Utils.Action action = control.get_visual_state();

        switch (action) {
            case ATTACK:
                 currentImage = imgAttack;
                break;
            case IDLE:
                currentImage = imgIdle;
                break;
            case WALK_RIGHT:
                currentImage = imgWalking;
                break;
            case WALK_LEFT:
                currentImage = imgWalking;
                break;
            case JUMP:
                currentImage = imgFlying;
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
