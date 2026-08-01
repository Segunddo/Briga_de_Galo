package briga.galo.manager;

import briga.galo.Utils;
import briga.galo.network.ClientHandler;

/*
 * Representa uma sala de jogo 1v1.
 * O ciclo de uma sala é:
 *   1. Criada vazia (estadoAtual = WAITING) pelo ServerManager.
 *   2. Recebe o player1 via adicionarJogador().
 *   3. Recebe o player2 via adicionarJogador() -> sala fica cheia,
 *      estado vira MATCH e a thread da sala é iniciada (loop de jogo).
 *   4. Quando um jogador sai (cancelou ou caiu a conexão), removerJogador()
 *      é chamado. Se a partida estava rodando, ela é encerrada e o
 *      jogador restante é avisado (W.O.). Se a sala fica vazia, ela avisa
 *      o ServerManager pra ser removida da lista de salas ativas.
 */
public class GameRoom implements Runnable {

    // Nome de exibição da sala
    private final String nome;

    // Gerenciador central
    private final ServerManager serverManager;

    // Os dois jogadores da sala
    private ClientHandler player1;
    private ClientHandler player2;

    // Estado atual da sala/partida, enviado no início de cada payload transmitido
    private Utils.StateGame estadoAtual = Utils.StateGame.WAITING;

    /*
     * Última ação recebida de cada jogador. Índice 0 = player1, índice 1 = player2.
     * lido/zerado periodicamente pela thread da sala (run/limparAcoesInstantaneas).
     */
    private final Utils.Action[] acoesJogadores = new Utils.Action[] { Utils.Action.IDLE, Utils.Action.IDLE };

    //Só é true durante uma partida ativa
    private boolean running = false;

    public GameRoom(String nome, ServerManager serverManager) {
        this.nome = nome;
        this.serverManager = serverManager;
    }

    // return true se os dois "slots" de jogador já estão ocupados
    public synchronized boolean estaCheia() {
        return player1 != null && player2 != null;
    }

     // Adiciona um jogador na sala
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
        // Se a sala já estava cheia, a chamada é simplesmente ignorada
    }

    // Atualiza o "último estado conhecido" daquele jogador
    public synchronized void registrarAcao(int playerId, Utils.Action acao) {
        this.acoesJogadores[playerId - 1] = acao;
    }

    // Remove um jogador da sala
    public synchronized void removerJogador(ClientHandler client) {
        boolean eraP1 = (client == player1);
        boolean eraP2 = (client == player2);
        if (!eraP1 && !eraP2) return; // cliente não pertence a essa sala, nada a fazer

        if (running) {
            // Partida em andamento - encerra o loop
            running = false;
            ClientHandler oponente = eraP1 ? player2 : player1;
            if (oponente != null) {
                oponente.sendData("ENDGAME;OPPONENT_LEFT");
                oponente.limparRoom(); // libera o handler do oponente pra outra sala
            }
        }

        // Libera o slot do jogador que saiu
        if (eraP1) player1 = null;
        if (eraP2) player2 = null;

        // Se não sobrou ninguém, a sala não tem mais utilidade - remove do lobby
        if (player1 == null && player2 == null) {
            serverManager.removerSala(this);
        }
    }

    /*
     * Loop principal da sala, executado em thread própria enquanto
     * running == true. Roda a ~60 FPS (targetTime ajusta o sleep pra
     * compensar o tempo já gasto processando o tick).
     *
     * A cada iteração:
     *   1. Monta o protocolo "ESTADO;ACAO_P1;ACAO_P2" com o comando atual.
     *   2. Limpa as ações "instantâneas" (ataques)
     *   3. Envia o comando pros dois jogadores (broadcast simples).
     *   4. Dorme o tempo restante até completar 1/60s se sobrar tempo.
     */
    @Override
    public void run() {
        long targetTime = 1000 / 60; // duração alvo de cada frame em ms (16ms)

        while (running) {
            long start = System.currentTimeMillis();

            String payload;
            synchronized (this) {
                // checa running dentro do lock pode ter sido desligado por removerJogador() enquanto esperávamos o lock.
                if (!running) break;

                // Formato transmitido: "MATCH;ACAO_P1;ACAO_P2"
                payload = estadoAtual.name() + ";" + acoesJogadores[0].name() + ";" + acoesJogadores[1].name();

                // Ações instantâneas (ataques) só valem por 1 clique
                limparAcoesInstantaneas();
            }

            // Envio fora do bloco synchronized pra não segurar o lock durante a passagem do comando
            if (player1 != null) player1.sendData(payload);
            if (player2 != null) player2.sendData(payload);

            // Ajusta o sleep pra manter a taxa de 60Hz
            long elapsed = System.currentTimeMillis() - start;
            long wait = targetTime - elapsed;
            if (wait > 0) {
                try { Thread.sleep(wait); } catch (InterruptedException ignored) {}
            }
        }
    }

    // Zera as ações que são de um frame só
    private void limparAcoesInstantaneas() {
        for (int i = 0; i < acoesJogadores.length; i++) {
            if (acoesJogadores[i] == Utils.Action.ATTACK ||
                acoesJogadores[i] == Utils.Action.FLY_ATTACK_LEFT ||
                acoesJogadores[i] == Utils.Action.FLY_ATTACK_RIGHT) {
                acoesJogadores[i] = Utils.Action.IDLE;
            }
        }
    }

    // return o nome de exibição da sala
    public String getNome() { return nome; }

    // return true enquanto o loop de partida estiver ativo
    public synchronized boolean isRunning() { return running; }
}
