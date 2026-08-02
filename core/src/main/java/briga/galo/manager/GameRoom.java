package briga.galo.manager;

import briga.galo.Utils;
import briga.galo.network.ClientHandler;

public class GameRoom implements Runnable {

    private final String nome;
    private final ServerManager serverManager;

    private ClientHandler player1;
    private ClientHandler player2;

    private Utils.StateGame estadoAtual = Utils.StateGame.WAITING;
    private final Utils.Action[] acoesJogadores = new Utils.Action[] { Utils.Action.IDLE, Utils.Action.IDLE };
    private boolean running = false;

    // Guarda as skins de quem entrou
    private String skinP1 = "default";
    private String skinP2 = "default";

    public GameRoom(String nome, ServerManager serverManager) {
        this.nome = nome;
        this.serverManager = serverManager;
    }

    public synchronized boolean estaCheia() {
        return player1 != null && player2 != null;
    }

    public synchronized void adicionarJogador(ClientHandler client) {
        if (player1 == null) {
            player1 = client;
            skinP1 = client.getSkin(); // Puxa a skin escolhida
            player1.setRoom(this, 1);
            player1.sendData("SETUP;1");
        } else if (player2 == null) {
            player2 = client;
            skinP2 = client.getSkin(); // Puxa a skin escolhida
            player2.setRoom(this, 2);
            player2.sendData("SETUP;2");

            // SALA CHEIA -> MANDA AS SKINS PROS DOIS E INICIA A LUTA
            String matchStartMsg = "MATCH_START;" + skinP1 + ";" + skinP2;
            player1.sendData(matchStartMsg);
            player2.sendData(matchStartMsg);

            this.estadoAtual = Utils.StateGame.MATCH;
            this.running = true;
            new Thread(this).start(); // Liga a física da sala
        }
    }

    public synchronized void registrarAcao(int playerId, Utils.Action acao) {
        this.acoesJogadores[playerId - 1] = acao;
    }

    public synchronized void removerJogador(ClientHandler client) {
        boolean eraP1 = (client == player1);
        boolean eraP2 = (client == player2);
        if (!eraP1 && !eraP2) return;

        if (running) {
            running = false;
            ClientHandler oponente = eraP1 ? player2 : player1;
            if (oponente != null) {
                oponente.sendData("ENDGAME;OPPONENT_LEFT");
                oponente.limparRoom();
            }
        }

        if (eraP1) player1 = null;
        if (eraP2) player2 = null;

        if (player1 == null && player2 == null) {
            serverManager.removerSala(this);
        }
    }

    @Override
    public void run() {
        long targetTime = 1000 / 60;

        while (running) {
            long start = System.currentTimeMillis();
            String payload;

            synchronized (this) {
                if (!running) break;

                // Protocolo de Luta: ESTADO;ACAO_P1;ACAO_P2
                payload = estadoAtual.name() + ";" + acoesJogadores[0].name() + ";" + acoesJogadores[1].name();
                limparAcoesInstantaneas();
            }

            if (player1 != null) player1.sendData(payload);
            if (player2 != null) player2.sendData(payload);

            long elapsed = System.currentTimeMillis() - start;
            long wait = targetTime - elapsed;
            if (wait > 0) {
                try { Thread.sleep(wait); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void limparAcoesInstantaneas() {
        for (int i = 0; i < acoesJogadores.length; i++) {
            if (acoesJogadores[i] == Utils.Action.ATTACK ||
                acoesJogadores[i] == Utils.Action.FLY_ATTACK_LEFT ||
                acoesJogadores[i] == Utils.Action.FLY_ATTACK_RIGHT) {
                acoesJogadores[i] = Utils.Action.IDLE;
            }
        }
    }

    public String getNome() { return nome; }
    public synchronized boolean isRunning() { return running; }
}
