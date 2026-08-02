package window;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PlayerChanging {
    private Texture p1Panel;
    private Texture p2Panel;
    private BitmapFont font;
    private BitmapFont titleFont;

    private float timer = 0f;
    private int dotCount = 0;

    public PlayerChanging() {
        Pixmap pix1 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix1.setColor(new Color(0, 0, 0.5f, 0.6f));
        pix1.fill();
        p1Panel = new Texture(pix1);
        pix1.dispose();

        Pixmap pix2 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix2.setColor(new Color(0.5f, 0, 0, 0.6f));
        pix2.fill();
        p2Panel = new Texture(pix2);
        pix2.dispose();

        font = new BitmapFont();
        font.getData().setScale(3f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(5f);
        titleFont.setColor(Color.WHITE);
    }

    public void update(float delta) {
        timer += delta;
        if (timer > 0.4f) {
            dotCount = (dotCount + 1) % 4;
            timer = 0f;
        }
    }

    public void draw(SpriteBatch batch, float screenWidth, float screenHeight) {
        float margin = 40f;
        float panelWidth = (screenWidth / 2f) - (margin * 1.5f);
        float panelHeight = screenHeight - (margin * 4f);

        batch.draw(p1Panel, margin, margin * 2, panelWidth, panelHeight);
        batch.draw(p2Panel, (screenWidth / 2f) + (margin / 2f), margin * 2, panelWidth, panelHeight);

        titleFont.draw(batch, "SALA DE ESPERA ONLINE", screenWidth / 2f - 300, screenHeight - 40);

        font.setColor(Color.CYAN);
        font.draw(batch, "VOCÊ", margin + 50, screenHeight - 150);
        font.setColor(Color.GREEN);
        font.draw(batch, "Status: PRONTO", margin + 50, screenHeight - 250);

        float p2TextX = (screenWidth / 2f) + (margin / 2f) + 50;
        font.setColor(Color.CORAL);
        font.draw(batch, "ADVERSÁRIO", p2TextX, screenHeight - 150);

        font.setColor(Color.YELLOW);
        String waitingText = "Status: Aguardando Conexao";
        for (int i = 0; i < dotCount; i++) {
            waitingText += ".";
        }
        font.draw(batch, waitingText, p2TextX, screenHeight - 250);

        font.setColor(Color.WHITE);
        font.draw(batch, "[ ESC ] Cancelar Busca", screenWidth / 2f - 180, margin + 40);
    }

    public void dispose() {
        p1Panel.dispose();
        p2Panel.dispose();
        font.dispose();
        titleFont.dispose();
    }
}
