package briga.galo.network;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;
import briga.galo.Utils;

public class ClientDevice {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private int meuPlayerId = 0;
    private Utils.StateGame estadoAtual = Utils.StateGame.MENU;
    private final Utils.Action[] acoesAtuais = new Utils.Action[] { Utils.Action.IDLE, Utils.Action.IDLE };
    private String motivoFim = null;

    // --- NOVAS VARIÁVEIS PARA AS SKINS ---
    private String skinP1 = "default";
    private String skinP2 = "default";

    private Consumer<String> onEvento;
    private Runnable onDesconectado;

    public void setOnEvento(Consumer<String> onEvento) { this.onEvento = onEvento; }
    public void setOnDesconectado(Runnable onDesconectado) { this.onDesconectado = onDesconectado; }
    private void notificar(String msg) { if (onEvento != null) onEvento.accept(msg); }

    public void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            socket.setTcpNoDelay(true);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            notificar("Conectado a " + ip + ":" + port);
            new Thread(this::escutarServidor).start();
        } catch (IOException e) {
            notificar("Falha ao conectar: " + e.getMessage());
        }
    }

    public synchronized void enviarAcao(Utils.Action acao) {
        if (out != null) out.println(acao.name());
    }

    public synchronized void enviarPronto() {
        if (out != null) {
            out.println("READY");
        }
    }

    private void escutarServidor() {
        try {
            String rawData;
            while ((rawData = in.readLine()) != null) {
                processarServidor(rawData);
            }
        } catch (Exception ignored) {
        } finally {
            synchronized (this) {
                this.estadoAtual = Utils.StateGame.MENU;
                this.meuPlayerId = 0;
            }
            notificar("Conexão encerrada.");
            if (onDesconectado != null) onDesconectado.run();
        }
    }

    private synchronized void processarServidor(String rawData) {
        String[] partes = rawData.split(";");

        // --- NOVO EVENTO: O SERVIDOR AVISA AS SKINS E COMEÇA A LUTA ---
        if (partes[0].equals("MATCH_START")) {
            this.skinP1 = partes[1];
            this.skinP2 = partes[2];
            this.estadoAtual = Utils.StateGame.MATCH;
            notificar("Partida iniciada! Skins: P1=" + skinP1 + ", P2=" + skinP2);
            return;
        }

        if (partes[0].equals("SETUP")) {
            this.meuPlayerId = Integer.parseInt(partes[1]);
            this.estadoAtual = Utils.StateGame.WAITING;
            return;
        }

        if (partes[0].equals("ENDGAME")) {
            this.estadoAtual = Utils.StateGame.ENDGAME;
            this.motivoFim = partes.length > 1 ? partes[1] : "NORMAL";
            this.meuPlayerId = 0;
            return;
        }

        if (partes.length == 3) {
            this.estadoAtual = Utils.StateGame.valueOf(partes[0]);
            this.acoesAtuais[0] = Utils.Action.valueOf(partes[1]);
            this.acoesAtuais[1] = Utils.Action.valueOf(partes[2]);
        }
    }

    public synchronized Utils.Action getMinhaAcao() { return meuPlayerId == 0 ? Utils.Action.IDLE : acoesAtuais[meuPlayerId - 1]; }
    public synchronized Utils.Action getAcaoOponente() { return meuPlayerId == 0 ? Utils.Action.IDLE : acoesAtuais[(meuPlayerId == 1) ? 1 : 0]; }
    public synchronized int getMeuPlayerId() { return meuPlayerId; }
    public synchronized Utils.StateGame getEstadoAtual() { return estadoAtual; }
    public synchronized String getMotivoFim() { return motivoFim; }

    // --- GETTERS DE SKINS ---
    public synchronized String getSkinP1() { return skinP1; }
    public synchronized String getSkinP2() { return skinP2; }

    // --- AGORA ENVIAMOS A SKIN JUNTO COM O PEDIDO DE SALA ---
    public synchronized void solicitarCriarSala(String skinId) {
        if (out != null) out.println(Utils.CommandsGame.CREATE_ROOM.name() + ";" + skinId);
    }

    public synchronized void solicitarEntrarSala(String skinId) {
        if (out != null) out.println(Utils.CommandsGame.ENTER_ROOM.name() + ";" + skinId);
    }

    public synchronized void solicitarCancelar() {
        if (out != null) out.println(Utils.CommandsGame.CANCEL_ROOM.name());
        this.estadoAtual = Utils.StateGame.MENU;
        this.meuPlayerId = 0;
    }

    public synchronized void fechar() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        this.estadoAtual = Utils.StateGame.MENU;
        this.meuPlayerId = 0;
    }

    public synchronized boolean isConectado() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
