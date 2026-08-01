package briga.galo.manager;

import briga.galo.network.ClientHandler;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/*
 * abre o ServerSocket na porta configurada e fica aceitando conexões indefinidamente. Cada
 * conexão aceita vira um ClientHandler rodando em sua própria thread.
 *
 * lobby central: mantém a lista de GameRoom ativas e decide em qual sala cada jogador entra.
 *
 * Uso pretendido: basta instanciar com a porta desejada e chamar
 * start() — o resto (aceitar conexões, criar salas, etc.) é automático.
 * Exemplo:
 *   new ServerManager(5000).start();
 */
public class ServerManager {

    // Porta TCP em que o servidor vai escutar
    private final int porta;

    // Lista de salas atualmente ativas
    private final List<GameRoom> salas = new ArrayList<>();

    // Contador incremental usado só pra *gerar nomes* de sala únicos
    private int contadorSalas = 1;

    // mostrar o que está acontecendo no servidor. Não interfere em nada da lógica do jogo
    private Consumer<String> onLog;

    public ServerManager(int porta) {
        this.porta = porta;
    }

    // ouvinte de log
    public void setOnLog(Consumer<String> onLog) {
        this.onLog = onLog;
    }

    // console (System.out) ou externo, se houver
    private void log(String msg) {
        System.out.println(msg);
        if (onLog != null) onLog.accept(msg);
    }

    /*
     * Inicia o servidor: abre o socket na porta configurada e entra num
     * loop infinito aceitando novas conexões. Cada conexão vira um
     * ClientHandler novo, rodando em thread própria (pra não bloquear o
     * aceite de outras conexões enquanto lê/escreve dessa).
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            log("[Servidor Central] Rodando na porta " + porta);

            while (true) {
                Socket socket = serverSocket.accept(); // bloqueia até uma nova conexão chegar
                ClientHandler client = new ClientHandler(socket, this);
                new Thread(client).start(); // cada cliente tem sua própria thread de leitura
                log("[Servidor] Nova conexão aceita: " + socket.getRemoteSocketAddress());
            }
        } catch (Exception e) {
            // Se o ServerSocket for fechado ou der algum erro, o loop encerra aqui.
            log("[Servidor] Encerrado: " + e.getMessage());
        }
    }

    /*
     * Tenta encaixar o cliente numa sala existente que ainda não esteja
     * cheia. Se nenhuma sala tiver vaga, cria uma sala nova
     * e coloca o jogador nela como primeiro ocupante.
     */
    public synchronized void entrarEmSalaDisponivel(ClientHandler client) {
        for (GameRoom sala : salas) {
            if (!sala.estaCheia()) {
                log("[Lobby] Jogador entrando na " + sala.getNome());
                sala.adicionarJogador(client);
                return;
            }
        }
        // Nenhuma sala com vaga encontrada - cria uma nova e entra nela
        GameRoom novaSala = criarSala();
        novaSala.adicionarJogador(client);
    }

    /*
     * Cria uma nova sala vazia, dá um nome sequencial pra ela e já a
     * registra na lista de salas ativas do lobby
     */
    public synchronized GameRoom criarSala() {
        String nomeSala = "Sala #" + contadorSalas++;
        GameRoom novaSala = new GameRoom(nomeSala, this);
        salas.add(novaSala);
        log("[Lobby] Nova sala criada: " + nomeSala);
        return novaSala;
    }

    // Cria uma sala nova exclusivamente pro cliente informado, ignorando qualquer sala já existente com vaga
    public synchronized void criarSalaExclusiva(ClientHandler client) {
        GameRoom novaSala = criarSala();
        novaSala.adicionarJogador(client);
    }

    // Remove uma sala da lista de salas ativas do lobby
    public synchronized void removerSala(GameRoom sala) {
        if (salas.remove(sala)) {
            log("[Lobby] Sala removida: " + sala.getNome());
        }
    }

    // retorna os nomes das salas ativas no momento, indicando se cada uma está cheia (em partida) ou aguardando o segundo jogador
    public synchronized List<String> listarSalas() {
        List<String> nomes = new ArrayList<>();
        for (GameRoom s : salas) {
            nomes.add(s.getNome() + (s.estaCheia() ? " (cheia)" : " (aguardando)"));
        }
        return nomes;
    }
}
