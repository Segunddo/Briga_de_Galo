package briga.galo.network;

import briga.galo.Buffer;
import briga.galo.network.Connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

// classe de dispositivos ativos como notebooks com via de entrada e saída para multi computadores
public class Device extends Connection {

    // variaveis de entrada de dados vindas pela conexão
    private final Buffer<String> inputBuffer;
    private Thread exibicaoThread;

    // identificação para ajustar o multiplayer
    private Integer playerNumber;

    // construtor que instancia a entrada de dados e a saída que vai pra connection
    public Device(Socket socket, Integer id, Buffer<String> outputBuffer, BufferedReader reader) {
        super(socket, id, outputBuffer, reader);
        this.inputBuffer = new Buffer<>();
    }

    public Integer getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(Integer playerNumber) {
        this.playerNumber = playerNumber;
    }

    // metodo que passa o buffer da classe pro programa tratar
    public Buffer<String> getInputBuffer() {
        return inputBuffer;
    }

    //metodo de conexão com as vias de entrada e saida de dados via tcp
    @Override
    public void connect() throws IOException {
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        active = true;

        // thread separada pra receber a ordem e mandar pro programa exibir
        exibicaoThread = new Thread(this::ouvirOrdens, "device-" + id + "-display");
        exibicaoThread.start();

        System.out.println("Device " + id + " conectado.");
    }

    //metodo de recebimento dos dados pra mandar pra tela
    private void ouvirOrdens() {
        while (active) {
            String ordem = inputBuffer.pop(); // fica esperando até ter algo (está na classe do buffer)
            if (ordem == null) {
                continue; // interrupção enquanto espera (tem q fazer)
            }
            enviarParaTela(ordem);
        }
    }

    // metodo que manda os dados do server
    private synchronized void enviarParaTela(String ordem) {
        try {
            writer.write(ordem);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Erro ao exibir ordem no device " + id + ": " + e.getMessage());
        }
    }

    // metodo de desconexao
    @Override
    public void disconnect() {
        active = false;
        if (exibicaoThread != null) {
            exibicaoThread.interrupt();
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar device " + id + ": " + e.getMessage());
        }
        System.out.println("Device " + id + " desconectado.");
    }

    // metodo de tratamento de dados (tem q preencher)
    @Override
    protected String treatData(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return "DEV;" + id + ";" + raw.trim();
    }
}
