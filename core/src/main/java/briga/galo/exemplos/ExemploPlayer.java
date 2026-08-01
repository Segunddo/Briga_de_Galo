package briga.galo.exemplos;

import briga.galo.Utils;
import briga.galo.network.ClientDevice;

import java.util.Scanner;

/**
 * Exemplo de inicialização e uso do lado JOGADOR (o dispositivo/tela de
 * cada cliente, sem se preocupar com o servidor).
 *
 * Esse exemplo é um console simples só pra deixar claro QUANDO e COMO
 * chamar cada método do ClientDevice. Na tela real do jogo, troque:
 *   - a leitura via Scanner pelos cliques dos botões do menu (Criar Sala /
 *     Entrar em Sala / Cancelar);
 *   - o "envio manual de ações" pelo KeyHandler já pronto (basta registrar
 *     `new KeyHandler(device)` como KeyListener da sua tela de jogo);
 *   - os prints de log pela sua lógica de renderização, lendo
 *     device.getEstadoAtual() / getMinhaAcao() / getAcaoOponente() a cada
 *     frame do seu loop gráfico.
 *
 * O ciclo de uso é sempre o mesmo, em 3 passos:
 *   1. connect(ip, porta)               -> abre a conexão com o servidor
 *   2. solicitarCriarSala() OU solicitarEntrarSala()  -> entra no lobby
 *   3. a partir daí, o próprio servidor avisa quando a partida começa
 *      (estadoAtual muda pra MATCH) e quando ela termina (ENDGAME).
 */
public class ExemploPlayer {

    public static void main(String[] args) {
        ClientDevice device = new ClientDevice();
        Scanner scanner = new Scanner(System.in);

        // ---------------------------------------------------------------
        // 1. Registrar os "ouvintes" ANTES de conectar, pra não perder
        //    nenhum evento (SETUP, ticks de MATCH, ENDGAME, desconexão).
        //    Na tela real do jogo, é aqui que você atualizaria a UI.
        // ---------------------------------------------------------------
        device.setOnEvento(msg -> System.out.println("[EVENTO] " + msg));
        device.setOnDesconectado(() -> System.out.println("[AVISO] Conexão com o servidor caiu."));

        // ---------------------------------------------------------------
        // 2. Conectar no servidor (troque o IP pelo endereço real do host
        //    quando for jogar em rede; localhost serve pra testar na
        //    mesma máquina com o ExemploServer rodando em paralelo).
        // ---------------------------------------------------------------
        System.out.println("Conectando ao servidor...");
        device.connect("127.0.0.1", 5000);

        // Pequena espera só pra dar tempo da thread de conexão terminar
        // antes de mostrarmos o menu (na tela real isso não é necessário,
        // já que a UI reage aos callbacks/eventos, não a um Scanner).
        aguardar(300);

        // ---------------------------------------------------------------
        // 3. Menu principal: escolher entre criar sala ou entrar em uma.
        //    Isso corresponde exatamente aos botões "Criar Sala" e
        //    "Entrar em Sala" da tela do jogo.
        // ---------------------------------------------------------------
        System.out.println("\n1 - Criar Sala");
        System.out.println("2 - Entrar em Sala (pega a primeira com vaga, ou cria uma)");
        System.out.print("Escolha: ");
        String escolha = scanner.nextLine().trim();

        if (escolha.equals("1")) {
            device.solicitarCriarSala();
        } else {
            device.solicitarEntrarSala();
        }

        // ---------------------------------------------------------------
        // 4. Enquanto espera o oponente (estadoAtual == WAITING), o
        //    jogador pode desistir a qualquer momento — isso é o botão
        //    "Cancelar"/"Voltar" da tela de espera do jogo.
        // ---------------------------------------------------------------
        System.out.println("Aguardando oponente... (digite 'c' e ENTER pra cancelar a qualquer momento)");

        Thread threadCancelamento = new Thread(() -> {
            while (device.getEstadoAtual() == Utils.StateGame.WAITING) {
                if (scanner.hasNextLine()) {
                    String linha = scanner.nextLine();
                    if (linha.equalsIgnoreCase("c")) {
                        device.solicitarCancelar();
                        System.out.println("Sala cancelada. Voltando ao menu.");
                        break;
                    }
                }
            }
        });
        threadCancelamento.setDaemon(true);
        threadCancelamento.start();

        // Espera até a partida começar (MATCH), o jogador cancelar (volta
        // pra MENU) ou a conexão cair.
        while (device.getEstadoAtual() == Utils.StateGame.WAITING && device.isConectado()) {
            aguardar(200);
        }

        if (device.getEstadoAtual() != Utils.StateGame.MATCH) {
            System.out.println("Não entrou em partida (cancelado ou desconectado). Encerrando exemplo.");
            return;
        }

        // ---------------------------------------------------------------
        // 5. Partida em andamento: a partir daqui, em um jogo de verdade,
        //    o KeyHandler já cuida de mandar as ações automaticamente
        //    conforme o jogador aperta as teclas:
        //
        //        minhaTela.addKeyListener(new KeyHandler(device));
        //
        //    Aqui no console, simulamos isso manualmente lendo comandos
        //    digitados e chamando enviarAcao() na mão.
        // ---------------------------------------------------------------
        System.out.println("\nPartida iniciada! Você é o Jogador " + device.getMeuPlayerId());
        System.out.println("Comandos: a=atacar, e=andar direita, q=andar esquerda, s=parado, sair=encerrar\n");

        while (device.getEstadoAtual() == Utils.StateGame.MATCH) {
            System.out.print("Ação> ");
            if (!scanner.hasNextLine()) break;
            String cmd = scanner.nextLine().trim().toLowerCase();

            switch (cmd) {
                case "a":
                    device.enviarAcao(Utils.Action.ATTACK);
                    break;
                case "e":
                    device.enviarAcao(Utils.Action.WALK_RIGHT);
                    break;
                case "q":
                    device.enviarAcao(Utils.Action.WALK_LEFT);
                    break;
                case "s":
                    device.enviarAcao(Utils.Action.IDLE);
                    break;
                case "sair":
                    device.solicitarCancelar(); // sai da partida em andamento (conta como W.O.)
                    System.out.println("Você saiu da partida.");
                    return;
                default:
                    System.out.println("Comando não reconhecido.");
            }

            // Em um jogo de verdade, isso NÃO seria lido aqui manualmente —
            // seria consultado a cada frame do loop de renderização:
            System.out.println("   -> Minha ação (confirmada pelo servidor): " + device.getMinhaAcao());
            System.out.println("   -> Ação do oponente: " + device.getAcaoOponente());
        }

        // ---------------------------------------------------------------
        // 6. Fim de partida: pode ser normal ou porque o oponente saiu
        //    (device.getMotivoFim() traz o detalhe, ex: "OPPONENT_LEFT").
        // ---------------------------------------------------------------
        if (device.getEstadoAtual() == Utils.StateGame.ENDGAME) {
            System.out.println("\nPartida encerrada. Motivo: " + device.getMotivoFim());
        }
    }

    private static void aguardar(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
