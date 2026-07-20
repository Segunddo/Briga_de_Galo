package briga.galo.network;

import briga.galo.Buffer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

// classe abstrata pra fazer as conexões com o servidor
public abstract class Connection implements Runnable {

    // variáveis ponteiro pra passar os dados entre as classes e dispositivos
    protected Socket socket;
    protected Integer id;
    protected BufferedReader reader;
    protected BufferedWriter writer;
    protected volatile boolean active;

    // buffer compartilhado que vem do manager pra mandar os comandos certinhos
    protected final Buffer<String> outputBuffer;

    protected Runnable onDisconnect;

    public void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    protected Integer playerNumber;

    public Integer getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(Integer playerNumber) {
        this.playerNumber = playerNumber;
    }

    // construtor da classe pai aqui que chava como super nas outras pela herança
    public Connection(Socket socket, Integer id, Buffer<String> outputBuffer, BufferedReader reader) {
        this.socket = socket;
        this.id = id;
        this.outputBuffer = outputBuffer;
        this.reader = reader;
        this.active = false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // metodos de conexão
    public abstract void connect() throws IOException;

    public abstract void disconnect();

    // metodo de tratamento, cada classe trata e manda o código do seu jeito
    protected abstract String treatData(String raw);

    // mandar os dados pro buffer do sistema que é compartilhado por todos
    protected synchronized void pushData(String raw) {
        String tratado = treatData(raw);
        if (tratado != null) {
            outputBuffer.push(tratado);
        }
    }

    // metodo runnable pra ficar recebendo os dados (cada um tem o seu, mas esse é o caso geral)
    @Override
    public void run() {
        try {
            connect();
            String linha;
            while (active && (linha = reader.readLine()) != null) {
                pushData(linha);
            }
        } catch (IOException e) {
            System.err.println("Conexao " + id + " caiu: " + e.getMessage());
        } finally {
            disconnect();
        }
    }
}
