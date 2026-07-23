package briga.galo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BackGround {
    private Texture image;
    private Texture lifeBar;

    public BackGround(int backGroundType) {
        switch (backGroundType) {
            case 0:
                image = new Texture("backGround1.png");
                break;
            default:
                break;
        }
        // Cria a barra de vida dos players
        draw_lifeBar();
    }

    private void draw_lifeBar() {
        // Gera uma textura vermelha sólida de 1x1 pixel por código
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.GREEN);
        pixmap.fill();
        lifeBar = new Texture(pixmap);
        pixmap.dispose(); // Limpa o pixmap da memória após gerar a textura
    }

    public void draw(SpriteBatch batch, float width, float height, float player1Life, float player2Life) {
        // Desenha a imagem do cenário no fundo
        batch.draw(image, 0, 0, width, height);

        float maxLifeBarWidth = 300f; // Largura máxima da barra quando a vida está em 100%
        float barHeight = 25f;
        float posY = 1000f; // Altura em que as barras serão desenhadas
        float marginX = 20f; // Distância das bordas laterais

        // --- BARRA DO PLAYER 1 (Esquerda) ---
        float lifePercentageP1 = Math.max(0, player1Life) / 100f;
        float currentLifeBarP1 = maxLifeBarWidth * lifePercentageP1;

        // Desenha a barra do P1 começando da margem esquerda e crescendo para a direita
        batch.draw(lifeBar, marginX, posY, currentLifeBarP1, barHeight);

        // --- BARRA DO PLAYER 2 (Direita) ---
        float lifePercentageP2 = Math.max(0, player2Life) / 100f;
        float currentLifeBarP2 = maxLifeBarWidth * lifePercentageP2;

        // Isso faz com que, ao perder vida, a barra encolha em direção à borda direita.
        float posX_P2 = width - marginX - currentLifeBarP2;

        batch.draw(lifeBar, posX_P2, posY, currentLifeBarP2, barHeight);
    }

    public void dispose() {
        if(image != null) image.dispose();
        if(lifeBar != null) lifeBar.dispose();
    }
}
