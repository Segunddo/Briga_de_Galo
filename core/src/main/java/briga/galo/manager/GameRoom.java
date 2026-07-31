package briga.galo.manager;

import briga.galo.Utils;
import briga.galo.network.ClientHandler;

public class GameRoom implements Runnable {
    private final String nome;
    private ClientHandler player1;
    private ClientHandler player2;

    private Utils.StateGame estadoAtual = Utils.StateGame.MENU;
    private final Utils.Action[] acoesJogadores = new Utils.Action[] { Utils.Action.IDLE, Utils.Action.IDLE };
    private boolean running = false;

    public GameRoom(String nome) {
        this.nome = nome;
    }

    public synchronized boolean estaCheia() {
        return player1 != null && player2 != null;
    }

    // Adiciona o jogador, define a ID dele (1 ou 2) e envia o Handshake
    public synchronized void adicionarJogador(ClientHandler client) {
        if (player1 == null) {
            player1 = client;
            player1.setRoom(this, 1);
            player1.sendData("SETUP;1"); // Avisa o dispositivo que ele é o P1
        } else if (player2 == null) {
            player2 = client;
            player2.setRoom(this, 2);
            player2.sendData("SETUP;2"); // Avisa o dispositivo que ele é o P2

            // Sala cheia -> inicia a partida
            this.estadoAtual = Utils.StateGame.MATCH;
            this.running = true;
            new Thread(this).start(); // Inicia a Thread da sala
        }
    }

    public synchronized void registrarAcao(int playerId, Utils.Action acao) {
        this.acoesJogadores[playerId - 1] = acao;
    }

    @Override
    public void run() {
        long targetTime = 1000 / 60; // 60 FPS

        while (running) {
            long start = System.currentTimeMillis();

            String payload;
            synchronized (this) {
                // Formato transmitido: "MATCH;ACAO_P1;ACAO_P2"
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
}
