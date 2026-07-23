package briga.galo;

public class Control {
    // Posição na tela
    public float x;
    public float y;
    private float velocidadeY = 0f;

    // Colisão com o chão
    public boolean isOnFloor = true;
    private float floorLimit = 50f;

    // STATUS BASE (Constantes)
    private final float BASE_SPEED = 400f;
    private final float BASE_JUMP = 600f;
    private final float BASE_GRAVITY = 1200f;
    private final float BASE_GLIDE = 300f; // Gravidade menor ao segurar o pulo caindo

    // STATUS ATUAIS (Podem ser alterados por itens ou debuffs no futuro)
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

    // Variáveis para saber para onde o galo está olhando quando parado
    private boolean isHeadingLeft = false;
    private boolean isHeadingRight = false;

    // Construtor
    public Control(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }

    // O Player injeta a intenção aqui, seja vinda do InputHandler (teclado local) ou da Rede (Servidor)
    public void set_inputs(boolean attack, boolean jump, boolean right, boolean left) {
        this.isAttacking = attack;
        this.isHoldingJump = jump;
        this.isHoldingRight = right;
        this.isHoldingLeft = left;
    }

    // Chama todos os cálculos matemáticos do frame
    public void update_logic(float delta) {
        apply_horizontal_movement(delta);
        handle_jump_action();
        apply_physics(delta);
    }

    // Movimentação no Eixo X
    private void apply_horizontal_movement(float delta) {
        isWalkingLeft = false;
        isWalkingRight = false;

        if (isHoldingRight) {
            x += currentSpeed * delta;
            isWalkingRight = true;
            isHeadingRight = true;
            isHeadingLeft = false;
        }

        if (isHoldingLeft) {
            x -= currentSpeed * delta;
            isWalkingLeft = true;
            isHeadingLeft = true;
            isHeadingRight = false;
        }
    }

    // Lógica de pulo
    private void handle_jump_action() {
        if (isHoldingJump && isOnFloor) {
            velocidadeY = currentJumpForce;
            isOnFloor = false;
        }
    }

    // Física e Gravidade
    private void apply_physics(float delta) {
        if (isOnFloor) return;

        float gravityToApply = currentGravity;

        // Efeito de "planar" ou cair mais devagar se continuar segurando o botão de pulo
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

    // Retorna a animação que deve ser tocada na classe Player
    public Utils.Action get_visual_state() {
        if (isAttacking) {
            return Utils.Action.ATTACK;
        } else if (!isOnFloor) {
            return Utils.Action.JUMP;
        } else if (isWalkingRight) {
            return Utils.Action.WALK_RIGHT;
        } else if (isWalkingLeft) {
            return Utils.Action.WALK_LEFT;
        } else if (isHeadingLeft) {
            return Utils.Action.LEFT_HANDLE;
        } else if (isHeadingRight) {
            return Utils.Action.RIGHT_HANDLE;
        } else {
            return Utils.Action.IDLE;
        }
    }

    // Usado pelo GameWorld para checar dano
    public boolean is_attacking() {
        return this.isAttacking;
    }
}
