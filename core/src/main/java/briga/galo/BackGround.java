package briga.galo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BackGround {
    private Texture image;
    private Texture lifeBar;
    private Texture backLifeBar;

    public BackGround(int backGroundType) {
        switch (backGroundType) {
            case 0:
                image = new Texture("backGround1.png");
                break;
            default:
                break;
        }
        draw_lifeBar();
    }

    private void draw_lifeBar() {
        // Gera uma textura vermelha sólida de 1x1 pixel por código
        Pixmap pixmapG = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapG.setColor(Color.GREEN);
        pixmapG.fill();
        lifeBar = new Texture(pixmapG);

        Pixmap pixmapR = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapR.setColor(Color.RED);
        pixmapR.fill();
        backLifeBar = new Texture(pixmapR);

        pixmapG.dispose();// Limpa o pixmap da memória após gerar a textura
        pixmapR.dispose();
    }

    public void draw(SpriteBatch batch, float width, float height, float player1Life, float player2Life) {
        // Desenha a imagem do cenário no fundo
        batch.draw(image, 0, 0, width, height);

        float maxLifeBarWidth = 300f; // Largura máxima da barra quando a vida está em 100%
        float barHeight = 25f;
        float posY = 1000f; // Altura em que as barras serão desenhadas
        float marginX = 20f; // Distância das bordas laterais

        float lifePercentageP1 = Math.max(0, player1Life) / 100f;
        float currentLifeBarP1 = maxLifeBarWidth * lifePercentageP1;

        batch.draw(backLifeBar, marginX, posY, maxLifeBarWidth, barHeight);

        batch.draw(lifeBar, marginX, posY, currentLifeBarP1, barHeight);

        float lifePercentageP2 = Math.max(0, player2Life) / 100f;
        float currentLifeBarP2 = maxLifeBarWidth * lifePercentageP2;

        float backPosX_P2 = width - marginX - maxLifeBarWidth;
        batch.draw(backLifeBar, backPosX_P2, posY, maxLifeBarWidth, barHeight);

        float posX_P2 = width - marginX - currentLifeBarP2;
        batch.draw(lifeBar, posX_P2, posY, currentLifeBarP2, barHeight);
    }

    public void dispose() {
        if(image != null) image.dispose();
        if(lifeBar != null) lifeBar.dispose();
        if(backLifeBar != null) backLifeBar.dispose();
    }
}
