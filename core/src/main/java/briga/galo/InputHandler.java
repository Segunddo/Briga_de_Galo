package briga.galo;

import com.badlogic.gdx.Gdx;

public class InputHandler {
    private int keyLeft, keyRight, keyJump, keyAttack;

    public InputHandler(int keyLeft, int keyRight, int keyJump, int keyAttack) {
        this.keyLeft = keyLeft;
        this.keyRight = keyRight;
        this.keyJump = keyJump;
        this.keyAttack = keyAttack;
    }

    public boolean isLeft() { return Gdx.input.isKeyPressed(keyLeft); }
    public boolean isRight() { return Gdx.input.isKeyPressed(keyRight); }
    public boolean isJump() { return Gdx.input.isKeyPressed(keyJump); }
    public boolean isAttack() { return Gdx.input.isKeyJustPressed(keyAttack); }
}
