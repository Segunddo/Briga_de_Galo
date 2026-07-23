package briga.galo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Control {
    public float x;
    public float y;
    private float velocidadeY = 0f;

    public boolean isOnFloor = true;
    private float floorLimit = 50f;

    // STATUS BASE
    private final float BASE_SPEED = 400f;
    private final float BASE_JUMP = 600f;
    private final float BASE_GRAVITY = 1200f;
    private final float BASE_GLIDE = 300f;

    // STATUS ATUAIS
    private float currentSpeed = BASE_SPEED;
    private float currentJumpForce = BASE_JUMP;
    private float currentGravity = BASE_GRAVITY;

    // Variáveis de estado
    private boolean isAttacking = false;
    private boolean isWalkingRight = false;
    private boolean isWalkingLeft = false;
    private boolean isHoldingJump = false;
    private boolean isHoldingRight = false;
    private boolean isHoldingLeft = false;

    public Control(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }

    // Método principal que chama os módulos
    public void update_logic(float delta) {
        read_inputs();
        apply_horizontal_movement(delta);
        handle_jump_action();
        apply_physics(delta);
    }

    // Apenas lê o que o jogador quer fazer
    private void read_inputs() {
        isWalkingLeft = false;
        isWalkingRight = false;
        isAttacking = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        isHoldingJump = Gdx.input.isKeyPressed(Input.Keys.W);
        isHoldingRight = Gdx.input.isKeyPressed(Input.Keys.D);
        isHoldingLeft = Gdx.input.isKeyPressed(Input.Keys.A);
    }

    // Movimento no Eixo X
    private void apply_horizontal_movement(float delta) {
        isWalkingLeft = false;
        isWalkingRight = false; // Zera o estado todo frame
        if (isHoldingRight) {
            x += currentSpeed * delta;
            isWalkingRight = true;
        }
        if (isHoldingLeft) {
            x -= currentSpeed * delta;
            isWalkingLeft = true;
        }
    }

    // Verifica se pode pular
    private void handle_jump_action() {
        if (isHoldingJump && isOnFloor) {
            velocidadeY = currentJumpForce;
            isOnFloor = false;
        }
    }

    // Física
    private void apply_physics(float delta) {
        if (isOnFloor) return;

        // Lógica de Planar
        float gravityToApply = currentGravity;
        if (velocidadeY < 0 && isHoldingJump) {
            gravityToApply = BASE_GLIDE;
        }

        velocidadeY -= gravityToApply * delta;
        y += velocidadeY * delta;

        // Bateu no chão
        if (y <= floorLimit) {
            y = floorLimit;
            velocidadeY = 0;
            isOnFloor = true;
        }
    }

    public Utils.Action get_visual_state() {
        if (isAttacking) {
            return Utils.Action.ATTACK;
        } else if (!isOnFloor) {
            return Utils.Action.JUMP;
        } else if (isWalkingRight) {
            return Utils.Action.WALK_RIGHT;
        } else if (isWalkingLeft){
            return Utils.Action.WALK_LEFT;
        } else {
            return Utils.Action.IDLE;
        }
    }

    // Retorna se o usuário clicou em atacar
    public boolean is_attacking() {
        return this.isAttacking;
    }
}
