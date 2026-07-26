package briga.galo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EndGame {
    private Texture overlayTexture;
    private BitmapFont font;
    private BitmapFont titleFont;

    private float blinkTimer = 0f;
    private boolean showBlinkText = true;

    // Variável para guardar o texto de quem venceu
    private String winnerText = "";
    private Color winnerColor = Color.WHITE;

    public EndGame() {
        // Fundo escuro semi-transparente para cobrir a tela
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(new Color(0, 0, 0, 0.85f)); // Muito escuro, 85% opaco
        pix.fill();
        overlayTexture = new Texture(pix);
        pix.dispose();

        font = new BitmapFont();
        font.getData().setScale(3f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(6f);
    }

    // Método que será chamado assim que o jogo acabar para definir o vencedor
    public void setWinner(int player1Life, int player2Life) {
        if (player1Life <= 0 && player2Life <= 0) {
            winnerText = "EMPATE!";
            winnerColor = Color.YELLOW;
        } else if (player2Life <= 0) {
            winnerText = "JOGADOR 1 VENCEU!";
            winnerColor = Color.CYAN; // Cor que usamos no PlayerChanging
        } else {
            winnerText = "JOGADOR 2 VENCEU!";
            winnerColor = Color.CORAL; // Cor que usamos no PlayerChanging
        }
    }

    public void update(float delta) {
        blinkTimer += delta;
        if (blinkTimer > 0.5f) {
            showBlinkText = !showBlinkText;
            blinkTimer = 0f;
        }
    }

    public void draw(SpriteBatch batch, float screenWidth, float screenHeight) {
        // Desenha o fundo cobrindo tudo
        batch.draw(overlayTexture, 0, 0, screenWidth, screenHeight);

        // Título FIM DE JOGO
        titleFont.setColor(Color.WHITE);
        titleFont.draw(batch, "FIM DE PARTIDA", (screenWidth / 2f) - 300, screenHeight - 200);

        // Quem venceu
        titleFont.setColor(winnerColor);
        titleFont.draw(batch, winnerText, (screenWidth / 2f) - 420, screenHeight / 2f + 50);

        // Mensagem para voltar (Piscando)
        if (showBlinkText) {
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "Aperte ENTER para voltar ao Menu", (screenWidth / 2f) - 320, screenHeight / 2f - 200);
        }
    }

    public void dispose() {
        overlayTexture.dispose();
        font.dispose();
        titleFont.dispose();
    }
}
