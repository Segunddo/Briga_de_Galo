package briga.galo;

import briga.galo.network.ClientDevice;
import com.badlogic.gdx.Gdx;
import java.util.concurrent.atomic.AtomicBoolean;

public class InputHandler {
    private int keyLeft, keyRight, keyJump, keyAttack, keyDefense;
    private final AtomicBoolean attackPending = new AtomicBoolean(false);

    // Variáveis para Rede
    private ClientDevice client;
    private boolean isLocal;
    private Utils.Action lastRemoteAction = Utils.Action.IDLE;

    // Construtor para o Jogador LOCAL (teclado físico)
    public InputHandler(int keyLeft, int keyRight, int keyJump, int keyAttack, int keyDefense) {
        this.keyLeft = keyLeft;
        this.keyRight = keyRight;
        this.keyJump = keyJump;
        this.keyAttack = keyAttack;
        this.keyDefense = keyDefense;
        this.isLocal = true;
    }

    // Construtor para o Jogador REMOTO (teclado simulado via rede)
    public InputHandler(ClientDevice client) {
        this.client = client;
        this.isLocal = false;
    }

    public void poll() {
        if (isLocal) {
            // Se sou eu, leio o teclado real
            if (Gdx.input.isKeyJustPressed(keyAttack)) {
                attackPending.set(true);
            }
        } else {
            // O jogador remoto atualiza sua intenção de ataque baseada na última ação que o servidor mandou
            Utils.Action remoteAction = client.getAcaoOponente();

            boolean isAttackingNow = remoteAction.name().contains("ATTACK");
            boolean wasAttackingBefore = lastRemoteAction.name().contains("ATTACK");

            // Dispara o trigger de ataque apenas na transição para a ação de ataque
            if (isAttackingNow && !wasAttackingBefore) {
                attackPending.set(true);
            }

            lastRemoteAction = remoteAction;
        }
    }

    public boolean isLeft() {
        if (isLocal) return Gdx.input.isKeyPressed(keyLeft);
        // Só anda se o servidor mandar a string WALK_LEFT ou FLY_LEFT
        return lastRemoteAction == Utils.Action.WALK_LEFT || lastRemoteAction == Utils.Action.FLY_LEFT;
    }

    public boolean isRight() {
        if (isLocal) return Gdx.input.isKeyPressed(keyRight);
        return lastRemoteAction == Utils.Action.WALK_RIGHT || lastRemoteAction == Utils.Action.FLY_RIGHT;
    }

    public boolean isJump() {
        if (isLocal) return Gdx.input.isKeyPressed(keyJump);
        return lastRemoteAction.name().contains("FLY");
    }

    public boolean isDefense() {
        if (isLocal) return Gdx.input.isKeyPressed(keyDefense);
        return lastRemoteAction.name().contains("DEFEND");
    }

    public boolean consumeAttack() {
        return attackPending.compareAndSet(true, false);
    }
}
