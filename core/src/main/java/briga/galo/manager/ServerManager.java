package briga.galo.manager;

import briga.galo.network.ClientHandler;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerManager {
    private final int porta;
    private final List<GameRoom> salas = new ArrayList<>();
    private int contadorSalas = 1;

    public ServerManager(int porta) {
        this.porta = porta;
    }

    // Inicia o servidor central
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("[Servidor Central] Rodando na porta " + porta);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket, this);
                new Thread(client).start();

                // Aloca o jogador recém-conectado automaticamente na sala correta
                entrarEmSalaDisponivel(client);
            }
        } catch (Exception e) {
            System.err.println("[Servidor] Encerrado: " + e.getMessage());
        }
    }

    // Tenta entrar em uma sala com vaga; se não houver nenhuma, cria uma sala nova
    public synchronized void entrarEmSalaDisponivel(ClientHandler client) {
        for (GameRoom sala : salas) {
            if (!sala.estaCheia()) {
                System.out.println("[Lobby] Jogador entrando na " + sala.getNome());
                sala.adicionarJogador(client);
                return;
            }
        }

        // Se não achou sala com vaga, cria uma nova sala e adiciona o jogador
        GameRoom novaSala = criarSala();
        novaSala.adicionarJogador(client);
    }

    // Cria uma nova sala e adiciona na lista gerenciada
    public synchronized GameRoom criarSala() {
        String nomeSala = "Sala #" + contadorSalas++;
        GameRoom novaSala = new GameRoom(nomeSala);
        salas.add(novaSala);
        System.out.println("[Lobby] Nova sala criada: " + nomeSala);
        return novaSala;
    }

    public synchronized void criarSalaExclusiva(ClientHandler client) {
        GameRoom novaSala = criarSala();
        novaSala.adicionarJogador(client);
    }
}
