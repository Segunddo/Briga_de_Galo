package briga.galo.network;

import java.io.*;
import java.net.Socket;

import briga.galo.Utils;

public class ClientDevice {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private int meuPlayerId = 0; // Guardará se é 1 ou 2
    private Utils.StateGame estadoAtual = Utils.StateGame.MENU;
    private final Utils.Action[] acoesAtuais = new Utils.Action[] { Utils.Action.IDLE, Utils.Action.IDLE };

    public void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(this::escutarServidor).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void enviarAcao(Utils.Action acao) {
        if (out != null) {
            out.println(acao.name());
        }
    }

    private void escutarServidor() {
        try {
            String rawData;
            while ((rawData = in.readLine()) != null) {
                processarServidor(rawData);
            }
        } catch (Exception ignored) {}
    }

    private synchronized void processarServidor(String rawData) {
        String[] partes = rawData.split(";");

        // Trata a mensagem de Setup inicial
        if (partes[0].equals("SETUP")) {
            this.meuPlayerId = Integer.parseInt(partes[1]);
            System.out.println("[Dispositivo] Conectado com sucesso! Você é o Jogador " + meuPlayerId);
            return;
        }

        // Trata o loop da partida: MATCH;P1_ACTION;P2_ACTION
        if (partes.length == 3) {
            this.estadoAtual = Utils.StateGame.valueOf(partes[0]);
            this.acoesAtuais[0] = Utils.Action.valueOf(partes[1]);
            this.acoesAtuais[1] = Utils.Action.valueOf(partes[2]);
        }
    }

    // --- MÉTODOS PARA O SEU JOGO LER OS DADOS SEM SE CONFUNDIR ---

    public synchronized Utils.Action getMinhaAcao() {
        if (meuPlayerId == 0) return Utils.Action.IDLE;
        return acoesAtuais[meuPlayerId - 1];
    }

    public synchronized Utils.Action getAcaoOponente() {
        if (meuPlayerId == 0) return Utils.Action.IDLE;
        int oponenteId = (meuPlayerId == 1) ? 2 : 1;
        return acoesAtuais[oponenteId - 1];
    }

    public synchronized int getMeuPlayerId() { return meuPlayerId; }
    public synchronized Utils.StateGame getEstadoAtual() { return estadoAtual; }

    // Chama no clique do botão Criar Sala
    public synchronized void solicitarCriarSala() {
        if (out != null) {
            out.println("CREATE_ROOM");
        }
    }

    // Chama no clique do botão Entrar em Sala
    public synchronized void solicitarEntrarSala() {
        if (out != null) {
            out.println("ENTER_ROOM");
        }
    }

    public synchronized void enviarComando(Utils.CommandsGame comando) {
        if (out != null) {
            out.println(comando.name());
        }
    }
}
