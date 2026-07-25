package briga.galo;

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

    // Variáveis para a animação das reticências (...)
    private float timer = 0f;
    private int dotCount = 0;

    public PlayerChanging() {
        // Painel do Jogador 1 (Azul semi-transparente)
        Pixmap pix1 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix1.setColor(new Color(0, 0, 0.5f, 0.6f));
        pix1.fill();
        p1Panel = new Texture(pix1);
        pix1.dispose();

        // Painel do Jogador 2 (Vermelho semi-transparente)
        Pixmap pix2 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix2.setColor(new Color(0.5f, 0, 0, 0.6f));
        pix2.fill();
        p2Panel = new Texture(pix2);
        pix2.dispose();

        // Fontes
        font = new BitmapFont();
        font.getData().setScale(3f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(5f);
        titleFont.setColor(Color.WHITE);
    }

    public void update(float delta) {
        // Faz as reticências andarem a cada 0.4 segundos
        timer += delta;
        if (timer > 0.4f) {
            dotCount = (dotCount + 1) % 4; // Vai de 0 a 3 e zera
            timer = 0f;
        }
    }

    public void draw(SpriteBatch batch, float screenWidth, float screenHeight) {
        // Cálculo para dividir a tela no meio com uma margem
        float margin = 40f;
        float panelWidth = (screenWidth / 2f) - (margin * 1.5f);
        float panelHeight = screenHeight - (margin * 4f);

        // Desenha Painel Esquerdo (P1)
        batch.draw(p1Panel, margin, margin * 2, panelWidth, panelHeight);

        // Desenha Painel Direito (P2)
        batch.draw(p2Panel, (screenWidth / 2f) + (margin / 2f), margin * 2, panelWidth, panelHeight);

        // Título Central
        // O X é aproximado para ficar no meio
        titleFont.draw(batch, "SALA DE ESPERA", screenWidth / 2f - 250, screenHeight - 40);

        // --- Textos do Jogador 1 ---
        font.setColor(Color.CYAN);
        font.draw(batch, "JOGADOR 1", margin + 50, screenHeight - 150);

        font.setColor(Color.GREEN);
        font.draw(batch, "Status: PRONTO", margin + 50, screenHeight - 250);

        // --- Textos do Jogador 2 ---
        float p2TextX = (screenWidth / 2f) + (margin / 2f) + 50;
        font.setColor(Color.CORAL);
        font.draw(batch, "JOGADOR 2", p2TextX, screenHeight - 150);

        font.setColor(Color.YELLOW);
        // Monta a string "Aguardando" com a quantidade certa de pontos
        String waitingText = "Status: Aguardando Conexao";
        for (int i = 0; i < dotCount; i++) {
            waitingText += ".";
        }
        font.draw(batch, waitingText, p2TextX, screenHeight - 250);

        // --- Instrução Provisória para testes ---
        font.setColor(Color.WHITE);
        font.draw(batch, "[ ENTER ] Simular Entrada do P2 e Iniciar", screenWidth / 2f - 350, margin + 40);
    }

    public void dispose() {
        p1Panel.dispose();
        p2Panel.dispose();
        font.dispose();
        titleFont.dispose();
    }
}
