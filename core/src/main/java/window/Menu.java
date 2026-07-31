package window;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Menu {
    private Texture panelTexture;
    private Texture buttonTexture;
    private Texture selectedButtonTexture;
    private BitmapFont font;
    private BitmapFont titleFont;

    // Variável para fazer o texto piscar
    private float blinkTimer = 0f;
    private boolean showBlinkText = true;

    // Opções do menu principal
    private final String[] options = {"CRIAR PARTIDA", "ENTRAR EM PARTIDA"};
    private int selectedOption = 0;

    public Menu() {
        // 1. Painel escuro semi-transparente via código (Pixmap)
        Pixmap panelPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        panelPixmap.setColor(new Color(0, 0, 0, 0.7f));
        panelPixmap.fill();
        panelTexture = new Texture(panelPixmap);
        panelPixmap.dispose();

        // 2. Textura do botão (não selecionado)
        Pixmap btnPixmap = new Pixmap(420, 70, Pixmap.Format.RGBA8888);
        btnPixmap.setColor(Color.ORANGE);
        btnPixmap.fillRectangle(0, 0, 420, 70);
        btnPixmap.setColor(Color.RED);
        btnPixmap.drawRectangle(0, 0, 420, 70);
        buttonTexture = new Texture(btnPixmap);
        btnPixmap.dispose();

        // 3. Textura do botão selecionado (destaque)
        Pixmap selPixmap = new Pixmap(420, 70, Pixmap.Format.RGBA8888);
        selPixmap.setColor(Color.GOLD);
        selPixmap.fillRectangle(0, 0, 420, 70);
        selPixmap.setColor(Color.YELLOW);
        selPixmap.drawRectangle(0, 0, 420, 70);
        selPixmap.drawRectangle(1, 1, 418, 68);
        selectedButtonTexture = new Texture(selPixmap);
        selPixmap.dispose();

        // 4. Fontes padrão do LibGDX
        font = new BitmapFont();
        font.getData().setScale(2f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(6f);
        titleFont.setColor(Color.YELLOW);
    }

    public void update(float delta) {
        // Efeito de piscar do texto de instrução
        blinkTimer += delta;
        if (blinkTimer > 0.5f) {
            showBlinkText = !showBlinkText;
            blinkTimer = 0f;
        }

        // Navegação entre as opções
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedOption = (selectedOption - 1 + options.length) % options.length;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedOption = (selectedOption + 1) % options.length;
        }
    }

    public void draw(SpriteBatch batch) {
        float width = 1920f;
        float height = 1080f;

        // Painel centralizado
        float panelWidth = 800f;
        float panelHeight = 600f;
        float panelX = (width - panelWidth) / 2f;
        float panelY = (height - panelHeight) / 2f;
        batch.draw(panelTexture, panelX, panelY, panelWidth, panelHeight);

        // Título do jogo
        titleFont.draw(batch, "BRIGA DE GALO", panelX + 70, panelY + 520);

        // Desenha as opções do menu, uma embaixo da outra
        float buttonWidth = 420f;
        float buttonHeight = 70f;
        float buttonX = panelX + (panelWidth - buttonWidth) / 2f;
        float firstButtonY = panelY + 340f;
        float spacing = 110f;

        for (int i = 0; i < options.length; i++) {
            float buttonY = firstButtonY - (i * spacing);
            boolean isSelected = (i == selectedOption);

            Texture texture = isSelected ? selectedButtonTexture : buttonTexture;
            batch.draw(texture, buttonX, buttonY, buttonWidth, buttonHeight);

            // O texto da opção selecionada pisca para chamar atenção
            boolean shouldDrawText = !isSelected || showBlinkText;
            if (shouldDrawText) {
                font.setColor(isSelected ? Color.BLACK : Color.WHITE);
                font.draw(batch, options[i], buttonX + 40, buttonY + 45);
            }
        }

        // Rodapé com instruções
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "W/S ou SETAS - Navegar   ENTER - Confirmar", panelX + 130, panelY + 90);
        font.draw(batch, "ESC - Sair do Jogo", panelX + 280, panelY + 50);
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public boolean isCreateMatchSelected() {
        return selectedOption == 0;
    }

    public boolean isJoinMatchSelected() {
        return selectedOption == 1;
    }

    public void dispose() {
        panelTexture.dispose();
        buttonTexture.dispose();
        selectedButtonTexture.dispose();
        font.dispose();
        titleFont.dispose();
    }
}
