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
        "default", "armadura", "gledson"
    };

    private Texture panelTexture;
    private Texture cardTexture;
    private Texture readyTexture;

    private BitmapFont titleFont;
    private BitmapFont font;
    private BitmapFont smallFont;

    private int selectedIndex = 0;
    private boolean isReady = false;

    private float blinkTimer = 0f;
    private boolean showBlinkText = true;

    public CharacterSelect() {
        // Fundo do painel centralizado e mais elegante
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(new Color(0.1f, 0.12f, 0.2f, 0.85f));
        pix.fill();
        panelTexture = new Texture(pix);
        pix.dispose();

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

    public void reset() {
        selectedIndex = 0;
        isReady = false;
    }

    public boolean isPlayer1Ready() {
        return isReady;
    }

    public void update(float delta) {
        blinkTimer += delta;
        if (blinkTimer > 0.5f) {
            showBlinkText = !showBlinkText;
            blinkTimer = 0f;
        }

        if (!isReady) {
            // Suporta tanto A/D quanto Setas
            if (Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                selectedIndex = (selectedIndex - 1 + AVAILABLE_SKINS.length) % AVAILABLE_SKINS.length;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                selectedIndex = (selectedIndex + 1) % AVAILABLE_SKINS.length;
            }
            // Suporta tanto ESPAÇO quanto ENTER
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                isReady = true;
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            isReady = false;
        }
    }

    public void draw(SpriteBatch batch, float screenWidth, float screenHeight) {
        float panelWidth = 700f;
        float panelHeight = 500f;

        // Matemática para cravar o painel exatamente no meio da tela
        float panelX = (screenWidth - panelWidth) / 2f;
        float panelY = (screenHeight - panelHeight) / 2f;

        batch.draw(panelTexture, panelX, panelY, panelWidth, panelHeight);

        titleFont.draw(batch, "ESCOLHA SEU PERSONAGEM", screenWidth / 2f - 380, screenHeight - 60);

        drawPlayerSide(batch, "SEU LUTADOR", Color.CYAN, panelX, panelY, panelWidth, panelHeight, selectedIndex, isReady,
            "A / D - Trocar   SPACE - Confirmar");

        // Mensagem fora do painel avisando o estado atual
        if (isReady && showBlinkText) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "Confirmado! Entrando na sala...", screenWidth / 2f - 210, panelY - 40);
        }
    }

    private void drawPlayerSide(SpriteBatch batch, String label, Color labelColor,
                                float x, float y, float width, float height,
                                int selectedIdx, boolean ready, String instructions) {

        smallFont.setColor(labelColor);
        smallFont.draw(batch, label, x + width / 2f - 90, y + height - 40);

        // A "Carta" agora é maior e fica bem no centro do painel
        float cardWidth = width - 160f;
        float cardHeight = 300f;
        float cardX = x + 80f;
        float cardY = y + height - 380f;

        batch.draw(ready ? readyTexture : cardTexture, cardX, cardY, cardWidth, cardHeight);

        font.setColor(Color.WHITE);
        String skinName = AVAILABLE_SKINS[selectedIdx];

        float textOffsetX = skinName.length() * 11f;
        font.draw(batch, skinName.toUpperCase(), cardX + (cardWidth / 2f) - textOffsetX, cardY + (cardHeight / 2f) + 15);

        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "(" + (selectedIdx + 1) + "/" + AVAILABLE_SKINS.length + ")",
            cardX + (cardWidth / 2f) - 35f, cardY + (cardHeight / 2f) - 30);

        if (ready) {
            smallFont.setColor(Color.GREEN);
            smallFont.draw(batch, "PRONTO!", cardX + (cardWidth / 2f) - 50f, cardY + cardHeight - 20);
        }

        smallFont.setColor(Color.GRAY);
        smallFont.draw(batch, instructions, x + width / 2f - 200, y + 40);
    }

    public String getPlayer1SkinId() {
        return AVAILABLE_SKINS[selectedIndex];
    }

    public void dispose() {
        panelTexture.dispose();
        cardTexture.dispose();
        readyTexture.dispose();
        titleFont.dispose();
        font.dispose();
        smallFont.dispose();
    }
}
