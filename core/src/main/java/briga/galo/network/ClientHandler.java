package briga.galo.network;

import briga.galo.Utils;
import briga.galo.manager.GameRoom;
import briga.galo.manager.ServerManager;

import java.io.*;
import java.net.Socket;

/*
 * Representa, do lado do servidor, a conexão com UM jogador
 * Fluxo de interpretação de cada linha recebida:
 *   1. Tenta interpretar como CommandsGame (CREATE_ROOM, ENTER_ROOM, CANCEL_ROOM).
 *   2. Se não for um comando reconhecido, e o jogador já estiver
 *      vinculado a uma sala, tenta interpretar como uma Utils.Action
 *   Também é responsável por avisar a GameRoom quando a conexão cai
 */
public class ClientHandler implements Runnable {

    // Socket TCP da conexão com este jogador específico.
    private final Socket socket;

    // Referência ao gerenciador central
    private final ServerManager serverManager;

    private PrintWriter out;      // stream de saída (texto) pra mandar mensagens ao cliente
    private BufferedReader in;    // stream de entrada (texto) pra ler mensagens do cliente

    // Sala em que este jogador está no momento (null se estiver fora de qualquer sala). */
    private GameRoom room;

    // Número do jogador (1 ou 2) dentro da sala atual
    private int playerId;

    // Flag simples indicando se a conexão deste jogador ainda está ativa
    private volatile boolean conectado = true;

    public ClientHandler(Socket socket, ServerManager serverManager) {
        this.socket = socket;
        this.serverManager = serverManager;
        try {
            this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Vincula este handler a uma sala e define seu número de jogador dentro dela
    public synchronized void setRoom(GameRoom room, int playerId) {
        this.room = room;
        this.playerId = playerId;
    }

    // Desvincula este handler de qualquer sala
    public synchronized void limparRoom() {
        this.room = null;
        this.playerId = 0;
    }

    // Loop principal desta conexão: fica lendo linha por linha do socket até a conexão fechar
    @Override
    public void run() {
        try {
            String rawLine;
            while ((rawLine = in.readLine()) != null) {
                String input = rawLine.trim();
                if (input.isEmpty()) continue; // ignora linhas vazias

                // 1. Tenta interpretar como Comandos de Jogo
                if (tentarTratarComoComando(input)) {
                    continue;
                }

                // 2. Se não for comando e o jogador já está em partida, trata como Ação
                GameRoom salaAtual = this.room;
                if (salaAtual != null) {
                    try {
                        Utils.Action acaoRecebida = Utils.Action.valueOf(input);
                        salaAtual.registrarAcao(playerId, acaoRecebida);
                    } catch (IllegalArgumentException e) {
                        // Mensagem não corresponde a nenhuma Action conhecida
                        System.err.println("Ação não reconhecida: " + input);
                    }
                }
                // Se room == null e não era um comando válido, a mensagem é simplesmente ignorada
            }
        } catch (Exception e) {
            System.out.println("Jogador " + (playerId != 0 ? playerId : "?") + " desconectou.");
        } finally {
            desconectar();
        }
    }

    // Tenta converter a linha recebida para o enum CommandsGame. Se não for um comando válido, retorna false silenciosamente
    private boolean tentarTratarComoComando(String input) {
        try {
            Utils.CommandsGame comando = Utils.CommandsGame.valueOf(input);
            tratarComando(comando);
            return true;
        } catch (IllegalArgumentException e) {
            // Não era um CommandsGame (pode ser uma Action) — segue o fluxo normalmente
            return false;
        }
    }

    // Executa de fato o efeito de cada comando de lobby recebido
    private void tratarComando(Utils.CommandsGame comando) {
        System.out.println("[Servidor] Processando comando: " + comando);

        switch (comando) {
            case CREATE_ROOM:
                // Cria uma sala nova e exclusiva pra este jogador
                if (room == null) serverManager.criarSalaExclusiva(this);
                break;

            case ENTER_ROOM:
                // Entra em alguma sala com vaga (ou cria uma, se não houver)
                if (room == null) serverManager.entrarEmSalaDisponivel(this);
                break;

            case CANCEL_ROOM:
                // Saída voluntária: sai da sala atual (se houver) e volta ao estado "livre"
                GameRoom salaAtual = this.room;
                if (salaAtual != null) {
                    salaAtual.removerJogador(this);
                    limparRoom();
                }
                break;
        }
    }

    // Executa a limpeza de fim de conexão
    private void desconectar() {
        conectado = false;
        GameRoom salaAtual = this.room;
        if (salaAtual != null) {
            salaAtual.removerJogador(this);
        }
        try { socket.close(); } catch (IOException ignored) {}
    }

    // return true enquanto a conexão deste jogador estiver ativa
    public boolean isConectado() { return conectado; }

    // Envia uma linha de texto ao cliente (broadcast)
    public synchronized void sendData(String data) {
        if (out != null) {
            out.println(data);
        }
    }
}
