package window;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CharacterSelect {

    private static final String[] AVAILABLE_SKINS = {
        "default", "laranja", "gledson"
    };

    private Texture p1Panel;
    private Texture p2Panel;
    private Texture cardTexture;
    private Texture readyTexture;

    private BitmapFont titleFont;
    private BitmapFont font;
    private BitmapFont smallFont;

    private int p1Index = 0;
    private int p2Index = 0;
    private boolean p1Ready = false;
    private boolean p2Ready = false;

    private float blinkTimer = 0f;
    private boolean showBlinkText = true;

    public CharacterSelect() {
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

        Pixmap cardPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        cardPixmap.setColor(new Color(1f, 1f, 1f, 0.12f));
        cardPixmap.fill();
        cardTexture = new Texture(cardPixmap);
        cardPixmap.dispose();

        Pixmap readyPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        readyPixmap.setColor(new Color(0f, 1f, 0f, 0.35f));
        readyPixmap.fill();
        readyTexture = new Texture(readyPixmap);
        readyPixmap.dispose();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(5f);
        titleFont.setColor(Color.WHITE);

        font = new BitmapFont();
        font.getData().setScale(2.4f);

        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.8f);
    }

    // Reseta a seleção - chame isso sempre que entrar nesta tela
    public void reset() {
        p1Index = 0;
        p2Index = 0;
        p1Ready = false;
        p2Ready = false;
    }

    public void update(float delta) {
        blinkTimer += delta;
        if (blinkTimer > 0.5f) {
            showBlinkText = !showBlinkText;
            blinkTimer = 0f;
        }

        // Jogador 1: A/D navega, SPACE confirma, S cancela a confirmação
        if (!p1Ready) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                p1Index = (p1Index - 1 + AVAILABLE_SKINS.length) % AVAILABLE_SKINS.length;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                p1Index = (p1Index + 1) % AVAILABLE_SKINS.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                p1Ready = true;
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            p1Ready = false;
        }

        // Jogador 2: SETAS navega, ENTER confirma, DOWN cancela a confirmação
        if (!p2Ready) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                p2Index = (p2Index - 1 + AVAILABLE_SKINS.length) % AVAILABLE_SKINS.length;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                p2Index = (p2Index + 1) % AVAILABLE_SKINS.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                p2Ready = true;
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            p2Ready = false;
        }
    }

    public void draw(SpriteBatch batch, float screenWidth, float screenHeight) {
        float margin = 40f;
        float panelWidth = (screenWidth / 2f) - (margin * 1.5f);
        float panelHeight = screenHeight - (margin * 4f);

        float p1X = margin;
        float p2X = (screenWidth / 2f) + (margin / 2f);
        float panelY = margin * 2;

        batch.draw(p1Panel, p1X, panelY, panelWidth, panelHeight);
        batch.draw(p2Panel, p2X, panelY, panelWidth, panelHeight);

        titleFont.draw(batch, "ESCOLHA SEU PERSONAGEM", screenWidth / 2f - 380, screenHeight - 40);

        drawPlayerSide(batch, "JOGADOR 1", Color.CYAN, p1X, panelY, panelWidth, panelHeight, p1Index, p1Ready,
            "A / D - Trocar    SPACE - Confirmar");

        drawPlayerSide(batch, "JOGADOR 2", Color.CORAL, p2X, panelY, panelWidth, panelHeight, p2Index, p2Ready,
            "SETAS - Trocar    ENTER - Confirmar");

        // Mensagem central piscando quando os dois estiverem prontos
        if (p1Ready && p2Ready && showBlinkText) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "Iniciando partida...", screenWidth / 2f - 160, panelY + 40);
        }
    }

    private void drawPlayerSide(SpriteBatch batch, String label, Color labelColor,
                                float x, float y, float width, float height,
                                int selectedIndex, boolean ready, String instructions) {

        smallFont.setColor(labelColor);
        smallFont.draw(batch, label, x + 50, y + height - 60);

        // "Carta" com o nome do personagem selecionado
        float cardWidth = width - 100f;
        float cardHeight = 260f;
        float cardX = x + 50f;
        float cardY = y + height - 420f;

        batch.draw(ready ? readyTexture : cardTexture, cardX, cardY, cardWidth, cardHeight);

        font.setColor(Color.WHITE);
        String skinName = AVAILABLE_SKINS[selectedIndex];
        font.draw(batch, skinName.toUpperCase(), cardX + 40, cardY + (cardHeight / 2f) + 15);

        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "(" + (selectedIndex + 1) + "/" + AVAILABLE_SKINS.length + ")",
            cardX + 40, cardY + (cardHeight / 2f) - 30);

        if (ready) {
            smallFont.setColor(Color.GREEN);
            smallFont.draw(batch, "PRONTO!", cardX + 40, cardY + cardHeight - 20);
        }

        smallFont.setColor(Color.GRAY);
        smallFont.draw(batch, instructions, x + 50, y + 60);
    }

    public String getPlayer1SkinId() {
        return AVAILABLE_SKINS[p1Index];
    }

    public String getPlayer2SkinId() {
        return AVAILABLE_SKINS[p2Index];
    }

    public boolean isBothReady() {
        return p1Ready && p2Ready;
    }

    public void dispose() {
        p1Panel.dispose();
        p2Panel.dispose();
        cardTexture.dispose();
        readyTexture.dispose();
        titleFont.dispose();
        font.dispose();
        smallFont.dispose();
    }
}
