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
    private final float BASE_GRAVITY = 500f;
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

    // Variaveis de controle para os ataques
    private float attackTimer = 0f;
    private final float ATTACK_DURATION = 0.25f; // Tempo que a investida dura
    private final float DASH_SPEED = 1200f; // Velocidade horizontal do ataque
    private final float AIR_DASH_SPEED_Y = 800f; // Velocidade que ele é arremessado para baixo
    private boolean isAirAttacking = false;

    // Variáveis para saber para onde o galo está olhando quando parado
    public boolean isHeadingLeft = false;
    public boolean isHeadingRight = false;

    // Construtor
    public Control(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }

    // O Player injeta a intenção aqui, seja vinda do InputHandler (teclado local) ou da Rede (Servidor)
    public void set_inputs(boolean attack, boolean jump, boolean right, boolean left) {
        // Só permite iniciar um ataque se já não estiver no meio de um
        if (attack && !this.isAttacking) {
            this.isAttacking = true;
            this.attackTimer = ATTACK_DURATION;

            // Se NÃO estiver no chão, é um ataque aéreo!
            if (!this.isOnFloor) {
                this.isAirAttacking = true;
                // Aplica uma força negativa forte para ele descer com tudo (Mergulho)
                this.velocidadeY = -AIR_DASH_SPEED_Y;
            } else {
                this.isAirAttacking = false;
            }
        }

        this.isHoldingJump = jump;
        this.isHoldingRight = right;
        this.isHoldingLeft = left;
    }

    // Chama todos os cálculos matemáticos do frame
    public void update_logic(float delta) {
        if (isAttacking) {
            // Se estiver atacando, substitui o movimento normal pela investida
            handle_dash_attack(delta);
        } else {
            // Se não estiver atacando, move e pula normalmente
            apply_horizontal_movement(delta);
            handle_jump_action();
        }

        // A física (gravidade e colisões com o chão) continua sendo aplicada em ambos os casos
        apply_physics(delta);
    }

    // Lida com o deslocamento forçado do ataque
    private void handle_dash_attack(float delta) {
        attackTimer -= delta;

        // Descobre a direção baseada em para onde o galo estava olhando antes do ataque
        float direction = isHeadingLeft ? -1f : 1f;

        // Aplica a velocidade horizontal massiva (Investida)
        x += DASH_SPEED * direction * delta;

        // O ataque acaba se o tempo esgotar OU se for um ataque aéreo e o galo bater no chão
        if (attackTimer <= 0 || (isAirAttacking && isOnFloor)) {
            isAttacking = false;
            isAirAttacking = false;
        }
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

        if (isAirAttacking){
            if(isHeadingLeft){
                return Utils.Action.FLY_ATTACK_LEFT;
            } else {
                return Utils.Action.FLY_ATTACK_RIGHT;
            }
        } else if (isAttacking) {
            return Utils.Action.ATTACK;
        }
        else if (!isOnFloor) {
            // Se não está no chão (seja subindo no pulo ou caindo), toca a animação de voo
            if (isHeadingLeft) {
                return Utils.Action.FLY_LEFT;
            } else {
                return Utils.Action.FLY_RIGHT;
            }
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
