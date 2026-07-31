package briga.galo;

import briga.galo.network.ClientDevice;

public class ExemploCliente {
    public static void main(String[] args) throws InterruptedException {
        ClientDevice dispositivo = new ClientDevice();
        dispositivo.connect("localhost", 12345);

        // Aguarda conectar e receber o ID do servidor
        Thread.sleep(500);

        // Loop principal do seu jogo (Renderizador / Execução)
        while (true) {
            if (dispositivo.getEstadoAtual() == Utils.StateGame.MATCH) {

                Utils.Action minhaAcao = dispositivo.getMinhaAcao();
                Utils.Action acaoInimigo = dispositivo.getAcaoOponente();

                // Exemplo de uso direto:
                // meuGalo.executar(minhaAcao);
                // galoInimigo.executar(acaoInimigo);

                System.out.println("Sou o P" + dispositivo.getMeuPlayerId() +
                    " | Minha Ação: " + minhaAcao +
                    " | Inimigo: " + acaoInimigo);
            }

            Thread.sleep(100);
        }
    }
}
