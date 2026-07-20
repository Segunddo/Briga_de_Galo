package briga.galo.manager;

import briga.galo.Buffer;
import briga.galo.network.Connection;
import briga.galo.network.Controller;
import briga.galo.network.Device;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// classe de manuseio de conexões pra focar tudo de tratamento de dados e passagem de input
public class ConnectionManager implements Runnable {

    // variaveis de socket pra gerenciar o servidor
    private final ServerSocket serverSocket;
    private final Buffer<String> commandBuffer;
    // variaveis de threads para as conexões
    private final ExecutorService pool;
    private final List<Connection> connections;
    // variaveis de controle de execução para sincronizar e organizar o fluxo de dados
    private volatile boolean running;
    private int proximoId;

    // limite de jogadores simultaneos (Controller OU Device contam, os dois mandam input)
    private static final int MAX_JOGADORES = 4;

    // instanciação das variáveis de serviço
    public ConnectionManager(ServerSocket serverSocket, Buffer<String> commandBuffer) {
        this.serverSocket = serverSocket;
        this.commandBuffer = commandBuffer;
        this.pool = Executors.newCachedThreadPool();
        this.connections = new ArrayList<>();
        this.running = true;
        this.proximoId = 1;
    }

    // metodo runnable pra ficar gerenciando e adicionando conexões
    @Override
    public void run() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                Connection conexao = identificarEInstanciar(socket);
                if (conexao == null) {
                    continue; // conexao recusada
                }
                registrar(conexao);
                pool.execute(conexao);
            } catch (IOException e) {
                if (running) {
                    System.err.println("Erro ao aceitar conexao: " + e.getMessage());
                }
            }
        }
    }

    // usa um sistema simples de aperto de mão para identificar e armazenar as conexões
    // primeira linha que o cliente manda tem que ser "CONTROLLER" ou "DEVICE" pra identificar
    private Connection identificarEInstanciar(Socket socket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String tipo = reader.readLine();

        // o corte de vaga vale pros dois tipos: Controller e Device mandam
        // input de jogador, entao os dois disputam o mesmo limite
        if (contarJogadores() >= MAX_JOGADORES) {
            System.out.println("Conexao recusada: limite de " + MAX_JOGADORES + " jogadores atingido.");
            socket.close();
            return null;
        }

        if ("CONTROLLER".equalsIgnoreCase(tipo)) {
            Controller controller = new Controller(socket, proximoId(), commandBuffer, reader);
            controller.setOnDisconnect(() -> remover(controller));
            return controller;
        }
        Device device = new Device(socket, proximoId(), commandBuffer, reader);
        device.setOnDisconnect(() -> remover(device));
        return device;
    }

    private synchronized Integer proximoId() {
        return proximoId++;
    }

    private synchronized void registrar(Connection conexao) {
        connections.add(conexao);
        // Controller e Device sempre ganham um numero
        int numeroDoJogador = contarJogadores(); // ja inclui a que acabou de entrar
        conexao.setPlayerNumber(numeroDoJogador);
    }

    // conta quem ocupa vaga de jogador
    private synchronized int contarJogadores() {
        int total = 0;
        for (Connection c : connections) {
            if (c instanceof Controller || c instanceof Device) {
                total++;
            }
        }
        return total;
    }

    public synchronized int totalConectados() {
        return connections.size();
    }

    public synchronized int totalJogadores() {
        return contarJogadores();
    }

    // chamado via callback quando uma conexao desconecta, pra ela sair da lista e liberar o slot
    private synchronized void remover(Connection conexao) {
        connections.remove(conexao);
        System.out.println("Conexao " + conexao.getId() + " removida (total conectado agora: " + connections.size() + ").");
    }

    // metodo de broadcast de ordem para os dispositivos conectados
    public synchronized void broadcastParaDevices(String ordem) {
        for (Connection c : connections) {
            if (c instanceof Device) {
                ((Device) c).getInputBuffer().push(ordem);
            }
        }
    }

    // metodo de desligar o manager e todas as conexões
    public void shutdown() {
        running = false;
        pool.shutdownNow();
        List<Connection> copia;
        synchronized (this) {
            copia = new ArrayList<>(connections);
        }

        // evita quebra do iterador pra destruir com segurança
        for (Connection c : copia) {
            c.disconnect();
        }
    }
}
