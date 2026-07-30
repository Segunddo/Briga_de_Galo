package briga.galo;

import com.badlogic.gdx.Gdx;

public class InputHandler {
    private int keyLeft, keyRight, keyJump, keyAttack, keyDefense;

    public InputHandler(int keyLeft, int keyRight, int keyJump, int keyAttack, int keyDefense) {
        this.keyLeft = keyLeft;
        this.keyRight = keyRight;
        this.keyJump = keyJump;
        this.keyAttack = keyAttack;
        this.keyDefense = keyDefense;
    }

    public boolean isLeft() { return Gdx.input.isKeyPressed(keyLeft); }
    public boolean isRight() { return Gdx.input.isKeyPressed(keyRight); }
    public boolean isJump() { return Gdx.input.isKeyPressed(keyJump); }
    public boolean isAttack() { return Gdx.input.isKeyJustPressed(keyAttack); }
    public boolean isDefense() { return Gdx.input.isKeyPressed(keyDefense); }
}
