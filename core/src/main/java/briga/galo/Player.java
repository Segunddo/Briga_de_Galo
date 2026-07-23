package briga.galo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player {
    private Control control;
    private int playerLife;
    private int playerHitBox;

    // Apenas Imagens e Visual
    private Texture imgIdle;
    private Texture imgAttack;
    private Texture imgFallingAttack;
    private Texture imgFlying;
    private Texture imgWalking;
    private Texture imgWalkingRight;
    private Texture currentImage;


    public Player(Control control) {
        this.control = control;
        playerLife = 100; // Valor padrão (100 de vida)
        playerHitBox = 20; // 20x20

        // Carrega as imagens
        this.imgIdle = new Texture("idle.png");
        this.imgAttack = new Texture("attack.png");
        this.imgFallingAttack = new Texture("falling_attack.png");
        this.imgFlying = new Texture("flying.png");
        this.imgWalking = new Texture("walking.png");
        this.imgWalkingRight = new Texture("walkingRight.png");
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
                currentImage = imgWalkingRight;
                break;
            case WALK_LEFT:
                currentImage = imgWalking;
                break;
            case JUMP:
                currentImage = imgFlying;
                break;
        }
    }

    public int get_player_life() {
        return playerLife;
    }

    public int get_player_hitBox() {
        return playerHitBox;
    }

    public float get_x() {
        return control.x;
    }

    public float get_y() {
        return control.y;
    }

    // Retorna se o usuário clicou em atacar
    public boolean is_attacking() {
        return control.is_attacking();
    }

    // Método para aplicar o dano
    public void take_damage(int damage) {
        this.playerLife -= damage;
        // Evita que a vida fique negativa
        if (this.playerLife < 0) {
            this.playerLife = 0;
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
