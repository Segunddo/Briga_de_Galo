package window;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Menu {
    private Texture panelTexture;
    private Texture buttonTexture;
    private BitmapFont font;
    private BitmapFont titleFont;

    // Variável para fazer o texto de "Press Enter" piscar
    private float blinkTimer = 0f;
    private boolean showBlinkText = true;

    public Menu() {
        // 1. Criando um painel escuro semi-transparente via código (Pixmap)
        Pixmap panelPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        panelPixmap.setColor(new Color(0, 0, 0, 0.7f)); // Preto com 70% de opacidade
        panelPixmap.fill();
        panelTexture = new Texture(panelPixmap);
        panelPixmap.dispose();

        // 2. Criando uma textura para um "botão" ou detalhe visual
        Pixmap btnPixmap = new Pixmap(200, 50, Pixmap.Format.RGBA8888);
        btnPixmap.setColor(Color.ORANGE);
        btnPixmap.fillRectangle(0, 0, 200, 50); // Preenchimento
        btnPixmap.setColor(Color.RED);
        btnPixmap.drawRectangle(0, 0, 200, 50); // Borda
        buttonTexture = new Texture(btnPixmap);
        btnPixmap.dispose();

        // 3. Fonte padrão do LibGDX para textos (não precisa de arquivo externo)
        font = new BitmapFont();
        font.getData().setScale(2f); // Aumenta o tamanho da fonte para a tela 1080p

        titleFont = new BitmapFont();
        titleFont.getData().setScale(6f); // Fonte gigante para o título
        titleFont.setColor(Color.YELLOW);
    }

    public void update(float delta) {
        // Lógica para fazer o texto piscar (alterna a cada 0.5 segundos)
        blinkTimer += delta;
        if (blinkTimer > 0.5f) {
            showBlinkText = !showBlinkText;
            blinkTimer = 0f;
        }
    }

    public void draw(SpriteBatch batch) {
        float width = 1920f;
        float height = 1080f;

        // Desenha um painel centralizado para dar destaque ao menu
        float panelWidth = 800f;
        float panelHeight = 600f;
        float panelX = (width - panelWidth) / 2f;
        float panelY = (height - panelHeight) / 2f;
        batch.draw(panelTexture, panelX, panelY, panelWidth, panelHeight);

        // Desenha o Título do Jogo
        titleFont.draw(batch, "BRIGA DE GALO", panelX + 70, panelY + 500);

        // Desenha um botão decorativo/painel para a mensagem de início
        batch.draw(buttonTexture, panelX + 200, panelY + 150, 400, 100);

        // Desenha o texto piscante por cima do botão
        if (showBlinkText) {
            font.setColor(Color.WHITE);
            font.draw(batch, "Aperte ENTER para Jogar", panelX + 230, panelY + 215);
        }

        // Rodapé com créditos ou instruções
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "ESC - Sair do Jogo", panelX + 280, panelY + 50);
    }

    public void dispose() {
        panelTexture.dispose();
        buttonTexture.dispose();
        font.dispose();
        titleFont.dispose();
    }
}
