package briga.galo;

import briga.galo.manager.ServerManager;

public class ServerMain {
    public static void main(String[] args) {
        // Liga o gerenciador central - ele assume a criação de salas e pareamento
        new ServerManager(12345).start();
    }
}
