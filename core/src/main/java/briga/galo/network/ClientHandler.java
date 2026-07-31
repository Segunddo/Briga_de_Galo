package briga.galo.network;

import briga.galo.Utils;
import briga.galo.manager.GameRoom;
import briga.galo.manager.ServerManager;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ServerManager serverManager;
    private PrintWriter out;
    private BufferedReader in;
    private GameRoom room;
    private int playerId;

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

    public void setRoom(GameRoom room, int playerId) {
        this.room = room;
        this.playerId = playerId;
    }

    @Override
    public void run() {
        try {
            String rawLine;
            while ((rawLine = in.readLine()) != null) {
                String input = rawLine.trim();

                // 1. Tenta interpretar como Comandos de Jogo (CREATE_ROOM, ENTER_ROOM, etc)
                if (tentarTratarComoComando(input)) {
                    continue;
                }

                // 2. Se não for comando e o jogador já está em partida, trata como Ação (WALK, ATTACK, etc)
                if (room != null) {
                    Utils.Action acaoRecebida = Utils.Action.valueOf(input);
                    room.registrarAcao(playerId, acaoRecebida);
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Mensagem não reconhecida pelo protocolo: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Jogador " + playerId + " desconectou.");
        }
    }

    // Tenta converter o input para o enum CommandsGame
    private boolean tentarTratarComoComando(String input) {
        try {
            Utils.CommandsGame comando = Utils.CommandsGame.valueOf(input);
            tratarComando(comando); // Executa o manipulador de comandos
            return true;
        } catch (IllegalArgumentException e) {
            // Não era um CommandsGame (pode ser uma Action), ignora o erro e segue o fluxo
            return false;
        }
    }

    // Centraliza a execução de todos os comandos do jogo ---
    private void tratarComando(Utils.CommandsGame comando) {
        System.out.println("[Servidor] Processando comando: " + comando);

        switch (comando) {
            case CREATE_ROOM:
                serverManager.criarSalaExclusiva(this);
                break;

            case ENTER_ROOM:
                serverManager.entrarEmSalaDisponivel(this);
                break;
        }
    }

    public synchronized void sendData(String data) {
        if (out != null) {
            out.println(data);
        }
    }
}
