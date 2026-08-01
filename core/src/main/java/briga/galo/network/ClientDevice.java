package briga.galo.network;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

import briga.galo.Utils;

/*
 * É a classe que a interface gráfica do jogo
 * deve instanciar e usar pra: conectar, pedir criação/entrada em sala,
 * enviar as ações do jogador e ler o
 * estado mais recente da partida pra desenhar os personagens na tela.
 *
 * Toda a comunicação é assíncrona: enviarAcao()/solicitar*() apenas
 * escrevem no socket, enquanto uma thread separada (escutarServidor)
 * fica lendo continuamente o que o servidor manda e atualizando os
 * campos internos (estadoAtual, acoesAtuais, meuPlayerId, motivoFim).
 * O código do jogo (loop de renderização) deve consultar esses campos
 * via os getters, sem se preocupar com a rede em si.
 */
public class ClientDevice {
    private Socket socket;
    private PrintWriter out;      // stream de saída pra mandar ações/comandos ao servidor
    private BufferedReader in;    // stream de entrada pra ler as mensagens do servidor

    // Número deste jogador na sala atual (1 ou 2). 0 significa fora de uma sala/partida
    private int meuPlayerId = 0;

    // Estado atual reportado pelo servidor
    private Utils.StateGame estadoAtual = Utils.StateGame.MENU;

    // Última ação conhecida de cada jogador da partida, na ordem em que o servidor manda (índice 0 = P1, índice 1 = P2)
    private final Utils.Action[] acoesAtuais = new Utils.Action[] { Utils.Action.IDLE, Utils.Action.IDLE };

    // Motivo do fim de partida reportado pelo servidor
    private String motivoFim = null;

    // Callback opcional
    private Consumer<String> onEvento;

    // Callback opcional chamado quando a conexão cai
    private Runnable onDesconectado;

    public void setOnEvento(Consumer<String> onEvento) {
        this.onEvento = onEvento;
    }

    public void setOnDesconectado(Runnable onDesconectado) {
        this.onDesconectado = onDesconectado;
    }

    // Dispara o callback de evento, se houver algum registrado
    private void notificar(String msg) {
        if (onEvento != null) onEvento.accept(msg);
    }

    // Abre a conexão TCP com o servidor no ip/porta informados
    public void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            notificar("Conectado a " + ip + ":" + port);
            new Thread(this::escutarServidor).start();
        } catch (IOException e) {
            notificar("Falha ao conectar: " + e.getMessage());
        }
    }

    // Envia uma ação de jogo ao servidor
    public synchronized void enviarAcao(Utils.Action acao) {
        if (out != null) {
            out.println(acao.name());
        }
    }

    // Loop de leitura contínua do servidor rodando em thread própria
    private void escutarServidor() {
        try {
            String rawData;
            while ((rawData = in.readLine()) != null) {
                processarServidor(rawData);
            }
        } catch (Exception ignored) {
            // socket fechado ou conexão caiu — tratado abaixo no finally
        } finally {
            synchronized (this) {
                this.estadoAtual = Utils.StateGame.MENU;
                this.meuPlayerId = 0;
            }
            notificar("Conexão encerrada.");
            if (onDesconectado != null) onDesconectado.run();
        }
    }

    /*
     * Interpreta uma linha recebida do servidor e atualiza o estado
     * interno de acordo com o tipo da mensagem (primeiro campo,
     * separado por ';'):
     *
     *  - "SETUP;<id>"        -> define meuPlayerId e entra em estado WAITING
     *                           (dentro da sala, esperando o oponente)
     *  - "ENDGAME;<motivo>"  -> partida encerrada; guarda o motivo e
     *                           libera o playerId (não pertence mais a
     *                           nenhuma sala)
     *  - "<ESTADO>;<P1>;<P2>" (3 campos) -> tick normal de partida:
     *                           atualiza estado e as ações dos dois jogadores
     */
    private synchronized void processarServidor(String rawData) {
        String[] partes = rawData.split(";");

        if (partes[0].equals("SETUP")) {
            this.meuPlayerId = Integer.parseInt(partes[1]);
            this.estadoAtual = Utils.StateGame.WAITING;
            notificar("SETUP recebido: você é o Jogador " + meuPlayerId + " (aguardando partida)");
            return;
        }

        if (partes[0].equals("ENDGAME")) {
            this.estadoAtual = Utils.StateGame.ENDGAME;
            this.motivoFim = partes.length > 1 ? partes[1] : "NORMAL";
            this.meuPlayerId = 0; // não pertence mais a nenhuma sala/partida
            notificar("ENDGAME recebido: " + motivoFim);
            return;
        }

        if (partes.length == 3) {
            // Payload padrão de tick de partida: "ESTADO;ACAO_P1;ACAO_P2"
            this.estadoAtual = Utils.StateGame.valueOf(partes[0]);
            this.acoesAtuais[0] = Utils.Action.valueOf(partes[1]);
            this.acoesAtuais[1] = Utils.Action.valueOf(partes[2]);
            notificar("MATCH: P1=" + acoesAtuais[0] + " P2=" + acoesAtuais[1]);
        }
        // Qualquer outro formato de mensagem é silenciosamente ignorado.
    }

    // ---------------------------------------------------------------
    // Métodos de leitura de estado, pensados pra serem chamados pelo
    // loop de renderização do jogo sem se confundir com quem é quem.
    // ---------------------------------------------------------------

    // return a ação do próprio jogador local
    public synchronized Utils.Action getMinhaAcao() {
        if (meuPlayerId == 0) return Utils.Action.IDLE;
        return acoesAtuais[meuPlayerId - 1];
    }

    // return a ação do jogador adversário
    public synchronized Utils.Action getAcaoOponente() {
        if (meuPlayerId == 0) return Utils.Action.IDLE;
        int oponenteId = (meuPlayerId == 1) ? 2 : 1;
        return acoesAtuais[oponenteId - 1];
    }

    // return 1 ou 2 se estiver numa sala | 0 se estiver fora de qualquer sala/partida.
    public synchronized int getMeuPlayerId() { return meuPlayerId; }

    // return o estado atual reportado pelo servidor
    public synchronized Utils.StateGame getEstadoAtual() { return estadoAtual; }

    // return o motivo do último fim de partida
    public synchronized String getMotivoFim() { return motivoFim; }

    // ---------------------------------------------------------------
    // Solicitações de lobby (chamadas a partir dos botões da tela).
    // ---------------------------------------------------------------

    // Chamado no clique do botão Criar Sala: pede ao servidor uma sala nova e exclusiva
    public synchronized void solicitarCriarSala() {
        if (out != null) {
            out.println(Utils.CommandsGame.CREATE_ROOM.name());
            notificar("Solicitado: CREATE_ROOM");
        }
    }

    // Chamado no clique do botão Entrar em Sala: pede ao servidor pra encaixar em alguma sala com vaga
    public synchronized void solicitarEntrarSala() {
        if (out != null) {
            out.println(Utils.CommandsGame.ENTER_ROOM.name());
            notificar("Solicitado: ENTER_ROOM");
        }
    }

    // voltar no menu de espera ou sai da partida
    public synchronized void solicitarCancelar() {
        if (out != null) {
            out.println(Utils.CommandsGame.CANCEL_ROOM.name());
            notificar("Solicitado: CANCEL_ROOM");
        }
        this.estadoAtual = Utils.StateGame.MENU;
        this.meuPlayerId = 0;
    }

    // Envio genérico de qualquer CommandsGame
    public synchronized void enviarComando(Utils.CommandsGame comando) {
        if (out != null) {
            out.println(comando.name());
        }
    }

    // Fecha a conexão de forma brusca
    public synchronized void fechar() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                notificar("Socket fechado manualmente (desconexão simulada).");
            }
        } catch (IOException e) {
            notificar("Erro ao fechar socket: " + e.getMessage());
        }
        this.estadoAtual = Utils.StateGame.MENU;
        this.meuPlayerId = 0;
    }

    // return true se o socket existir, estiver conectado e ainda não tiver sido fechado
    public synchronized boolean isConectado() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
