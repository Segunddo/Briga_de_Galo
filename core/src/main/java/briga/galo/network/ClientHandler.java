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
    private volatile boolean conectado = true;

    // Guarda a skin do player
    private String minhaSkin = "default";

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

    public synchronized void setRoom(GameRoom room, int playerId) {
        this.room = room;
        this.playerId = playerId;
    }

    public synchronized void limparRoom() {
        this.room = null;
        this.playerId = 0;
    }

    // Método que a GameRoom vai usar para puxar a sua skin
    public String getSkin() {
        return minhaSkin;
    }

    @Override
    public void run() {
        try {
            String rawLine;
            while ((rawLine = in.readLine()) != null) {
                String input = rawLine.trim();
                if (input.isEmpty()) continue;

                // Quebrando a mensagem que chega: "CREATE_ROOM;gledson"
                String[] partes = input.split(";");
                String header = partes[0];

                if (tentarTratarComoComando(partes)) {
                    continue;
                }

                GameRoom salaAtual = this.room;
                if (salaAtual != null) {
                    try {
                        Utils.Action acaoRecebida = Utils.Action.valueOf(header);
                        salaAtual.registrarAcao(playerId, acaoRecebida);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Ação não reconhecida: " + input);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Jogador " + (playerId != 0 ? playerId : "?") + " desconectou.");
        } finally {
            desconectar();
        }
    }

    private boolean tentarTratarComoComando(String[] partes) {
        try {
            Utils.CommandsGame comando = Utils.CommandsGame.valueOf(partes[0]);
            tratarComando(comando, partes);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void tratarComando(Utils.CommandsGame comando, String[] partes) {
        System.out.println("[Servidor] Processando comando: " + comando);

        switch (comando) {
            case CREATE_ROOM:
                if (room == null) {
                    // Pega a parte [1] ("gledson") e salva a skin antes de criar a sala
                    if (partes.length > 1) this.minhaSkin = partes[1];
                    serverManager.criarSalaExclusiva(this);
                }
                break;

            case ENTER_ROOM:
                if (room == null) {
                    // Salva a skin ANTES de entrar na sala
                    if (partes.length > 1) this.minhaSkin = partes[1];
                    serverManager.entrarEmSalaDisponivel(this);
                }
                break;

            case CANCEL_ROOM:
                GameRoom salaAtual = this.room;
                if (salaAtual != null) {
                    salaAtual.removerJogador(this);
                    limparRoom();
                }
                break;
        }
    }

    private void desconectar() {
        conectado = false;
        GameRoom salaAtual = this.room;
        if (salaAtual != null) {
            salaAtual.removerJogador(this);
        }
        try { socket.close(); } catch (IOException ignored) {}
    }

    public boolean isConectado() { return conectado; }

    public synchronized void sendData(String data) {
        if (out != null) {
            out.println(data);
        }
    }
}
