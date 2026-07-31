package window;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class JoinMatch {

    // Representa uma partida disponível na lista (dado fictício por enquanto)
    public static class MatchInfo {
        public final String hostName;
        public final String mapName;
        public final int ping;

        public MatchInfo(String hostName, String mapName, int ping) {
            this.hostName = hostName;
            this.mapName = mapName;
            this.ping = ping;
        }
    }

    private Texture panelTexture;
    private Texture rowTexture;
    private Texture selectedRowTexture;
    private BitmapFont titleFont;
    private BitmapFont font;
    private BitmapFont smallFont;

    private List<MatchInfo> availableMatches;
    private int selectedIndex = 0;

    private float blinkTimer = 0f;
    private boolean showBlinkText = true;

    public JoinMatch() {
        // Fundo do painel
        Pixmap panelPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        panelPixmap.setColor(new Color(0, 0, 0, 0.75f));
        panelPixmap.fill();
        panelTexture = new Texture(panelPixmap);
        panelPixmap.dispose();

        // Linha normal da lista
        Pixmap rowPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        rowPixmap.setColor(new Color(1f, 1f, 1f, 0.08f));
        rowPixmap.fill();
        rowTexture = new Texture(rowPixmap);
        rowPixmap.dispose();

        // Linha selecionada (destaque)
        Pixmap selRowPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        selRowPixmap.setColor(new Color(1f, 0.6f, 0f, 0.55f));
        selRowPixmap.fill();
        selectedRowTexture = new Texture(selRowPixmap);
        selRowPixmap.dispose();

        titleFont = new BitmapFont();
        titleFont.getData().setScale(5f);
        titleFont.setColor(Color.WHITE);

        font = new BitmapFont();
        font.getData().setScale(2.2f);

        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.6f);
        smallFont.setColor(Color.GRAY);

        // Lista de partidas fictícia, só para preencher a interface
        availableMatches = new ArrayList<>();
        availableMatches.add(new MatchInfo("Galo_do_Zeca", "Terreiro Central", 24));
        availableMatches.add(new MatchInfo("BrigaoBR", "Curral Velho", 41));
        availableMatches.add(new MatchInfo("MestreDoGalinheiro", "Arena Sertao", 58));
        availableMatches.add(new MatchInfo("Cacareco99", "Terreiro Central", 33));
        availableMatches.add(new MatchInfo("PenaDeAco", "Curral Velho", 77));
    }

    public void update(float delta) {
        blinkTimer += delta;
        if (blinkTimer > 0.5f) {
            showBlinkText = !showBlinkText;
            blinkTimer = 0f;
        }

        if (availableMatches.isEmpty()) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedIndex = (selectedIndex - 1 + availableMatches.size()) % availableMatches.size();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedIndex = (selectedIndex + 1) % availableMatches.size();
        }
    }

    public void draw(SpriteBatch batch, float screenWidth, float screenHeight) {
        float panelWidth = 1000f;
        float panelHeight = 800f;
        float panelX = (screenWidth - panelWidth) / 2f;
        float panelY = (screenHeight - panelHeight) / 2f;

        batch.draw(panelTexture, panelX, panelY, panelWidth, panelHeight);

        titleFont.draw(batch, "PARTIDAS DISPONIVEIS", panelX + 150, panelY + panelHeight - 40);

        float listX = panelX + 40;
        float listWidth = panelWidth - 80;
        float headerY = panelY + panelHeight - 130;

        // Cabeçalho das colunas
        smallFont.setColor(Color.GRAY);
        smallFont.draw(batch, "SALA", listX + 20, headerY);
        smallFont.draw(batch, "MAPA", listX + 420, headerY);
        smallFont.draw(batch, "PING", listX + 750, headerY);

        float rowHeight = 90f;
        float firstRowY = headerY - 40f;

        if (availableMatches.isEmpty()) {
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "Nenhuma partida encontrada no momento...", listX + 20, panelY + panelHeight / 2f);
        } else {
            for (int i = 0; i < availableMatches.size(); i++) {
                MatchInfo match = availableMatches.get(i);
                float rowY = firstRowY - (i * rowHeight);
                boolean isSelected = (i == selectedIndex);

                batch.draw(isSelected ? selectedRowTexture : rowTexture,
                    listX, rowY - rowHeight + 20f, listWidth, rowHeight - 10f);

                font.setColor(isSelected ? Color.BLACK : Color.WHITE);
                font.draw(batch, match.hostName, listX + 20, rowY - 15f);
                font.draw(batch, match.mapName, listX + 420, rowY - 15f);

                Color pingColor = match.ping < 40 ? Color.GREEN : (match.ping < 70 ? Color.YELLOW : Color.RED);
                font.setColor(isSelected ? Color.BLACK : pingColor);
                font.draw(batch, match.ping + "ms", listX + 750, rowY - 15f);
            }
        }

        // Rodapé com instruções (piscando)
        if (showBlinkText) {
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "W/S - Navegar   ENTER - Entrar na Partida   ESC - Voltar", panelX + 90, panelY + 40);
        }
    }

    public MatchInfo getSelectedMatch() {
        if (availableMatches.isEmpty()) return null;
        return availableMatches.get(selectedIndex);
    }

    public void dispose() {
        panelTexture.dispose();
        rowTexture.dispose();
        selectedRowTexture.dispose();
        titleFont.dispose();
        font.dispose();
        smallFont.dispose();
    }
}
