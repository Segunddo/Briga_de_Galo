package briga.galo;

import com.badlogic.gdx.Gdx;

import java.util.concurrent.atomic.AtomicBoolean;

public class InputHandler {
    private int keyLeft, keyRight, keyJump, keyAttack, keyDefense;

    // isKeyJustPressed só é "true" durante o frame exato em que a tecla foi
    // apertada - a flag é reiniciada a cada frame pela thread principal.
    // Como a thread de física do Player lê os inputs fora de sincronia com
    // o loop de renderização, um poll direto ali perde o toque na maioria
    // das vezes. Por isso: capturamos o toque aqui em poll() (chamado pela
    // thread principal, uma vez por frame) e guardamos como pendente até a
    // thread do jogador consumir com consumeAttack().

    private final AtomicBoolean attackPending = new AtomicBoolean(false);

    public InputHandler(int keyLeft, int keyRight, int keyJump, int keyAttack, int keyDefense) {
        this.keyLeft = keyLeft;
        this.keyRight = keyRight;
        this.keyJump = keyJump;
        this.keyAttack = keyAttack;
        this.keyDefense = keyDefense;
    }

    // IMPORTANTE: chame isso uma vez por frame, sempre na thread principal
    // (a mesma que roda o render/poll de eventos do LibGDX). Em GameWorld,
    // isso é feito em update(), antes de visualRefresh de cada jogador.
    public void poll() {
        if (Gdx.input.isKeyJustPressed(keyAttack)) {
            attackPending.set(true);
        }
    }

    public boolean isLeft() { return Gdx.input.isKeyPressed(keyLeft); }
    public boolean isRight() { return Gdx.input.isKeyPressed(keyRight); }
    public boolean isJump() { return Gdx.input.isKeyPressed(keyJump); }
    public boolean isDefense() { return Gdx.input.isKeyPressed(keyDefense); }

    // Consome o toque de ataque pendente (chamado pela thread de física do Player).
    // Retorna true no máximo uma vez por toque, mesmo que várias iterações
    // da thread de física aconteçam entre um frame e outro.
    public boolean consumeAttack() {

        return attackPending.compareAndSet(true, false);
    }
}
