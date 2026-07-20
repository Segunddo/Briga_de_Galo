package briga.galo.network;

import briga.galo.Buffer;
import briga.galo.network.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

// classe de recebimento dos dados das esp32 ou controle com o sistema linear de recebimento
public class Controller extends Connection {

    // joga os dados de conexão do manager pra estabelecer a conexão
    public Controller(Socket socket, Integer id, Buffer<String> outputBuffer, BufferedReader reader) {
        super(socket, id, outputBuffer, reader);
    }

    // metodo de conexão via tcp
    @Override
    public void connect() throws IOException {
        active = true;
        System.out.println("Controller " + id + " conectado.");
    }

    // metodo de desconectar
    @Override
    public void disconnect() {
        active = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar controller " + id + ": " + e.getMessage());
        }
        System.out.println("Controller " + id + " desconectado.");
        if (onDisconnect != null) {
            onDisconnect.run();
        }
    }

    // metodo de tratamento de dados para ser interpretado e implementado dependendo dos comandos do jogo
    @Override
    protected String treatData(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        // aqui entra o parsing especifico do protocolo do joystick/ESP32
        return "CTRL;" + id + ";" + raw.trim();
    }
}
