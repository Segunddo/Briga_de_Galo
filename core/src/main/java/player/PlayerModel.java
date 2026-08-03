package player;

import briga.galo.Utils;

public class PlayerModel {
    // Monitor de concorrência
    private final Object lock = new Object();

    // Status Físicos e Vida
    private int life = 100;
    private int hitBox = 300;
    private float x, y;
    private float velocidadeY = 0f;

    // Colisão
    private boolean isOnFloor = true;
    private float floorLimit = 50f;

    // Constantes de Física (Iguais ao seu Control original)
    private final float BASE_SPEED = 400f;
    private final float BASE_JUMP = 600f;
    private final float BASE_GRAVITY = 500f;
    private final float BASE_GLIDE = 300f;

    // STATUS ATUAIS
    private float currentSpeed = BASE_SPEED;
    private float currentJumpForce = BASE_JUMP;
    private float currentGravity = BASE_GRAVITY;

    // Variáveis de Controle de Ataque
    private boolean isAttacking = false;
    private boolean isAirAttacking = false;
    private float attackTimer = 0f;
    private final float ATTACK_DURATION = 0.25f;
    private final float DASH_SPEED = 1200f;
    private final float AIR_DASH_SPEED_Y = 800f;

    // Variáveis de Controle de Defesa
    private boolean isDefending = false;
    private float knockbackTimer = 0f;
    private float knockbackSpeed = 0f;
    private float maxStamina = 100f;
    private float currentStamina = 100f;
    private final float STAMINA_DRAIN = 333f; // Dura 1 segundo
    private final float STAMINA_REGEN = 50f; // Recupera 25 por segundo (Demora 4 segundos para encher do zero)

    // Inputs e Direção
    private boolean isWalkingRight = false;
    private boolean isWalkingLeft = false;
    private boolean isHoldingJump = false;
    private boolean isHoldingRight = false;
    private boolean isHoldingLeft = false;
    private boolean isHeadingLeft = false;
    private boolean isHeadingRight = false;

    public PlayerModel(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }

    // --- MÉTODOS DE AÇÃO (Chamados pelo Controller) ---
    public void setInputs(boolean attack, boolean jump, boolean right, boolean left, boolean defense) {
        synchronized (lock) {
            // Se já está defendendo, só precisa ter mais de 0 (Evita spammar o parry)
            boolean hasEnoughStamina = isDefending ? (currentStamina > 0) : (currentStamina >= 25f);
            this.isDefending = defense && isOnFloor && knockbackTimer <= 0 && hasEnoughStamina;
            if (this.isDefending) {
                // Se está defendendo, congela o resto!
                this.isHoldingRight = false;
                this.isHoldingLeft = false;
                this.isHoldingJump = false;
                return;
            }

            if (attack && !this.isAttacking && knockbackTimer <= 0) {
                this.isAttacking = true;
                this.attackTimer = ATTACK_DURATION;
                if (!this.isOnFloor) {
                    this.isAirAttacking = true;
                    this.velocidadeY = -AIR_DASH_SPEED_Y;
                } else {
                    this.isAirAttacking = false;
                }
            }
            this.isHoldingJump = jump;
            this.isHoldingRight = right;
            this.isHoldingLeft = left;
        }
    }

    public void updateLogic(float delta) {
        synchronized (lock) {

            // Contador da estamina
            if (isDefending) {
                // Gasta estamina enquanto segura o botão
                currentStamina -= STAMINA_DRAIN * delta;
                if (currentStamina <= 0) {
                    currentStamina = 0;
                    isDefending = false; // Quebra a guarda (força a soltar a defesa)
                }
            } else {
                // Recupera estamina se não estiver defendendo
                currentStamina += STAMINA_REGEN * delta;
                if (currentStamina > maxStamina) {
                    currentStamina = maxStamina;
                }
            }

            // Lógica do KnockBack e Ataque
            if (knockbackTimer > 0) {
                // Se sofreu parry, é arremessado para trás e não faz mais nada
                knockbackTimer -= delta;
                x += knockbackSpeed * delta;
            } else if (isAttacking) {
                handleDashAttack(delta);
            } else {
                applyHorizontalMovement(delta);
                handleJumpAction();
            }
            applyPhysics(delta);
        }
    }

    // MÉTODO QUE SOFRE O PARRY
    public void triggerRepel(boolean pushLeft) {
        synchronized (lock) {
            this.isAttacking = false;    // Cancela o ataque
            this.isAirAttacking = false;
            this.knockbackTimer = 0.2f;  // Duração do recuo (0.2 segundos)
            this.knockbackSpeed = pushLeft ? -1500f : 1500f; // Força jogando pra trás
        }
    }

    public void takeDamage(int damage) {
        synchronized (lock) {
            this.life = Math.max(0, this.life - damage);
        }
    }

    public boolean isAttacking() {
        synchronized (lock) {
            return isAttacking || isAirAttacking;
        }
    }

    public boolean isDefending() {
        synchronized (lock) {
            return isDefending;
        }
    }

    public void setX(float newX) {
        synchronized (lock) {
            this.x = newX;
        }
    }

    // --- LÓGICA INTERNA DE FÍSICA ---
    private void handleDashAttack(float delta) {
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

    private void applyHorizontalMovement(float delta) {
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

    private void handleJumpAction() {
        if (isHoldingJump && isOnFloor) {
            velocidadeY = currentJumpForce;
            isOnFloor = false;
        }
    }

    private void applyPhysics(float delta) {
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

    // --- GETTERS (Chamados pela View/Renderer e pelo GameWorld) ---
    public float getX() { synchronized (lock) { return x; } }
    public float getY() { synchronized (lock) { return y; } }
    public int getLife() { synchronized (lock) { return life; } }
    public int getHitBox() { return hitBox; }
    public float getStamina() { synchronized (lock) { return currentStamina; } }

    // Define qual estado visual a Renderer deve tocar
    public Utils.Action getCurrentAction() {
        synchronized (lock) {
            if (isDefending) return isHeadingLeft ? Utils.Action.DEFEND_LEFT : Utils.Action.DEFEND_RIGHT;
            if (isAirAttacking) return isHeadingLeft ? Utils.Action.FLY_ATTACK_LEFT : Utils.Action.FLY_ATTACK_RIGHT;
            if (isAttacking) return Utils.Action.ATTACK;
            if (!isOnFloor) {
                if (isHoldingRight) return Utils.Action.FLY_RIGHT;
                if (isHoldingLeft) return Utils.Action.FLY_LEFT;
                return Utils.Action.FLY;
            }
            if (isWalkingRight) return Utils.Action.WALK_RIGHT;
            if (isWalkingLeft) return Utils.Action.WALK_LEFT;
            if (isHeadingLeft) return Utils.Action.LEFT_HANDLE;
            if (isHeadingRight) return Utils.Action.RIGHT_HANDLE;
            return Utils.Action.IDLE;
        }
    }

    public boolean isHeadingLeft() { synchronized (lock) { return isHeadingLeft; } }
}
