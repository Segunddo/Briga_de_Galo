package window;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BackGround {
    private Texture image;

    // Texturas da Vida
    private Texture lifeBar;
    private Texture backLifeBar;

    // Texturas da Estamina
    private Texture staminaBar;
    private Texture backStaminaBar;

    public BackGround(int backGroundType) {
        switch (backGroundType) {
            case 0:
                image = new Texture("backGround1.png");
                break;
            default:
                break;
        }
        draw_hudElements();
    }

    private void draw_hudElements() {
        // --- GERAÇÃO DA BARRA DE VIDA ---
        Pixmap pixmapG = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapG.setColor(Color.GREEN);
        pixmapG.fill();
        lifeBar = new Texture(pixmapG);

        Pixmap pixmapR = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapR.setColor(Color.RED);
        pixmapR.fill();
        backLifeBar = new Texture(pixmapR);

        // --- GERAÇÃO DA BARRA DE ESTAMINA ---
        Pixmap pixmapB = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapB.setColor(Color.CYAN); // Azul para a estamina
        pixmapB.fill();
        staminaBar = new Texture(pixmapB);

        Pixmap pixmapGray = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapGray.setColor(Color.DARK_GRAY); // Fundo da estamina
        pixmapGray.fill();
        backStaminaBar = new Texture(pixmapGray);

        // Limpa tudo da memória
        pixmapG.dispose();
        pixmapR.dispose();
        pixmapB.dispose();
        pixmapGray.dispose();
    }

    // Atualizamos a assinatura para receber a estamina dos dois jogadores
    public void draw(SpriteBatch batch, float width, float height, float player1Life, float player2Life, float player1Stamina, float player2Stamina) {
        // Desenha a imagem do cenário no fundo
        batch.draw(image, 0, 0, width, height);

        // --- CONFIGURAÇÕES DE TAMANHO E POSIÇÃO ---
        float maxLifeBarWidth = 300f;
        float maxStaminaBarWidth = 250f; // Um pouco menor que a vida para dar um visual legal

        float lifeBarHeight = 25f;
        float staminaBarHeight = 10f; // Barrinha mais fina

        float marginX = 20f;
        float posY_Life = 1000f;
        float posY_Stamina = posY_Life - staminaBarHeight - 5f; // 5 pixels de espaço abaixo da vida

        // ================= JOGADOR 1 =================
        // Vida P1
        float lifePercentageP1 = Math.max(0, player1Life) / 100f;
        float currentLifeBarP1 = maxLifeBarWidth * lifePercentageP1;
        batch.draw(backLifeBar, marginX, posY_Life, maxLifeBarWidth, lifeBarHeight);
        batch.draw(lifeBar, marginX, posY_Life, currentLifeBarP1, lifeBarHeight);

        // Estamina P1
        float staminaPercentageP1 = Math.max(0, player1Stamina) / 100f;
        float currentStaminaBarP1 = maxStaminaBarWidth * staminaPercentageP1;
        batch.draw(backStaminaBar, marginX, posY_Stamina, maxStaminaBarWidth, staminaBarHeight);
        batch.draw(staminaBar, marginX, posY_Stamina, currentStaminaBarP1, staminaBarHeight);


        // ================= JOGADOR 2 =================
        // Vida P2 (Desenha da direita para a esquerda)
        float lifePercentageP2 = Math.max(0, player2Life) / 100f;
        float currentLifeBarP2 = maxLifeBarWidth * lifePercentageP2;
        float backPosX_Life_P2 = width - marginX - maxLifeBarWidth;
        float posX_Life_P2 = width - marginX - currentLifeBarP2;

        batch.draw(backLifeBar, backPosX_Life_P2, posY_Life, maxLifeBarWidth, lifeBarHeight);
        batch.draw(lifeBar, posX_Life_P2, posY_Life, currentLifeBarP2, lifeBarHeight);

        // Estamina P2 (Desenha da direita para a esquerda)
        float staminaPercentageP2 = Math.max(0, player2Stamina) / 100f;
        float currentStaminaBarP2 = maxStaminaBarWidth * staminaPercentageP2;
        float backPosX_Stamina_P2 = width - marginX - maxStaminaBarWidth;
        float posX_Stamina_P2 = width - marginX - currentStaminaBarP2;

        batch.draw(backStaminaBar, backPosX_Stamina_P2, posY_Stamina, maxStaminaBarWidth, staminaBarHeight);
        batch.draw(staminaBar, posX_Stamina_P2, posY_Stamina, currentStaminaBarP2, staminaBarHeight);
    }

    public void dispose() {
        if(image != null) image.dispose();
        if(lifeBar != null) lifeBar.dispose();
        if(backLifeBar != null) backLifeBar.dispose();
        if(staminaBar != null) staminaBar.dispose();
        if(backStaminaBar != null) backStaminaBar.dispose();
    }
}
