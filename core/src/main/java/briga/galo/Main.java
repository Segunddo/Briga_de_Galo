package briga.galo;

import briga.galo.manager.ServerManager;
import briga.galo.network.ClientDevice;

public class Main {private static final int PORTA = 12345;

    public static void main(String[] args) throws InterruptedException {
        imprimirCabecalho("1. INICIANDO O SERVIDOR CENTRAL");

        // 1. Sobe o Servidor Central em background
        Thread serverThread = new Thread(() -> new ServerManager(PORTA).start());
        serverThread.setDaemon(true);
        serverThread.start();

        pausa(400); // Aguarda o ServerSocket inicializar a porta

        imprimirCabecalho("2. TESTANDO COMANDO CREATE_ROOM (JOGADOR 1 - SALA 1)");

        ClientDevice p1_sala1 = new ClientDevice();
        p1_sala1.connect("localhost", PORTA);
        pausa(100);

        // Envia o comando via enum tipado Utils.CommandsGame
        System.out.println(">> P1 enviando comando: " + Utils.CommandsGame.CREATE_ROOM);
        p1_sala1.enviarComando(Utils.CommandsGame.CREATE_ROOM);
        pausa(300);

        asserter("Jogador 1 recebeu o ID 1 via Handshake", p1_sala1.getMeuPlayerId() == 1);
        System.out.println("Status de P1: Sala criada com sucesso! Aguardando oponente...");

        imprimirCabecalho("3. TESTANDO COMANDO ENTER_ROOM (JOGADOR 2 - SALA 1)");

        ClientDevice p2_sala1 = new ClientDevice();
        p2_sala1.connect("localhost", PORTA);
        pausa(100);

        // Envia o comando via enum tipado Utils.CommandsGame
        System.out.println(">> P2 enviando comando: " + Utils.CommandsGame.ENTER_ROOM);
        p2_sala1.enviarComando(Utils.CommandsGame.ENTER_ROOM);
        pausa(300);

        asserter("Jogador 2 entrou na sala e recebeu o ID 2", p2_sala1.getMeuPlayerId() == 2);
        asserter("Estado da Sala #1 mudou para MATCH para o P1", p1_sala1.getEstadoAtual() == Utils.StateGame.MATCH);
        asserter("Estado da Sala #1 mudou para MATCH para o P2", p2_sala1.getEstadoAtual() == Utils.StateGame.MATCH);

        imprimirCabecalho("4. TESTANDO CRIAR E ENTRAR NA SALA #2 (INDEPENDENTE)");

        ClientDevice p1_sala2 = new ClientDevice();
        ClientDevice p2_sala2 = new ClientDevice();

        // P1 da Sala 2 cria uma nova sala
        p1_sala2.connect("localhost", PORTA);
        pausa(100);
        p1_sala2.enviarComando(Utils.CommandsGame.CREATE_ROOM);
        pausa(200);

        // P2 da Sala 2 entra na sala disponível
        p2_sala2.connect("localhost", PORTA);
        pausa(100);
        p2_sala2.enviarComando(Utils.CommandsGame.ENTER_ROOM);
        pausa(300);

        asserter("P1 da Sala 2 identificou-se como Jogador 1", p1_sala2.getMeuPlayerId() == 1);
        asserter("P2 da Sala 2 identificou-se como Jogador 2", p2_sala2.getMeuPlayerId() == 2);
        asserter("Sala #2 iniciou no estado MATCH", p1_sala2.getEstadoAtual() == Utils.StateGame.MATCH);

        imprimirCabecalho("5. TESTANDO ENVIO DE AÇÕES E VISÃO DOS GALOS (SALA 1)");

        p1_sala1.enviarAcao(Utils.Action.WALK_RIGHT);
        p2_sala1.enviarAcao(Utils.Action.DEFEND_LEFT);
        pausa(100);

        System.out.println("Visão do P1 (Sala 1) -> Minha Ação: " + p1_sala1.getMinhaAcao() + " | Oponente: " + p1_sala1.getAcaoOponente());
        System.out.println("Visão do P2 (Sala 1) -> Minha Ação: " + p2_sala1.getMinhaAcao() + " | Oponente: " + p2_sala1.getAcaoOponente());

        asserter("P1 (Sala 1) executa WALK_RIGHT", p1_sala1.getMinhaAcao() == Utils.Action.WALK_RIGHT);
        asserter("P1 (Sala 1) vê oponente em DEFEND_LEFT", p1_sala1.getAcaoOponente() == Utils.Action.DEFEND_LEFT);
        asserter("P2 (Sala 1) executa DEFEND_LEFT", p2_sala1.getMinhaAcao() == Utils.Action.DEFEND_LEFT);
        asserter("P2 (Sala 1) vê oponente em WALK_RIGHT", p2_sala1.getAcaoOponente() == Utils.Action.WALK_RIGHT);

        imprimirCabecalho("6. TESTANDO ISOLAMENTO ENTRE SALA 1 E SALA 2");

        // P1 da Sala 2 voa para a direita. A Sala 1 não pode sofrer alteração.
        p1_sala2.enviarAcao(Utils.Action.FLY_RIGHT);
        pausa(100);

        System.out.println("Ação enviada na Sala 2 -> P1 (Sala 2): " + p1_sala2.getMinhaAcao());
        System.out.println("Verificando Sala 1   -> P1: " + p1_sala1.getMinhaAcao() + " | P2: " + p2_sala1.getMinhaAcao());

        asserter("P1 da Sala 2 está executando FLY_RIGHT", p1_sala2.getMinhaAcao() == Utils.Action.FLY_RIGHT);
        asserter("Ações da Sala 1 PERMANECERAM INTACTAS",
            p1_sala1.getMinhaAcao() == Utils.Action.WALK_RIGHT && p2_sala1.getMinhaAcao() == Utils.Action.DEFEND_LEFT);

        imprimirCabecalho("7. TESTANDO AUTO-RESET DE ATAQUE INSTANTÂNEO (ATTACK)");

        p1_sala1.enviarAcao(Utils.Action.ATTACK);
        pausa(15); // Frame do ataque

        System.out.println("Ação capturada no frame do ataque: " + p1_sala1.getMinhaAcao());

        pausa(100); // Tempo para o game loop processar o reset para IDLE

        System.out.println("Ação capturada nos frames seguintes: " + p1_sala1.getMinhaAcao());
        asserter("Ataque resetou para IDLE no frame seguinte", p1_sala1.getMinhaAcao() == Utils.Action.IDLE);

        imprimirCabecalho("✅ TUDO APRESENTOU 100% DE SUCESSO! SEU PROTOCOLO ESTÁ PRONTO!");
        System.exit(0);
    }

    // --- MÉTODOS DE SUPORTE DOS TESTES ---

    private static void asserter(String descricao, boolean condicao) {
        if (condicao) {
            System.out.println("  [ PASSOU ] " + descricao);
        } else {
            System.err.println("  [ FALHOU ] " + descricao);
            System.exit(1);
        }
    }

    private static void imprimirCabecalho(String titulo) {
        System.out.println("\n=================================================================");
        System.out.println(" " + titulo);
        System.out.println("=================================================================");
    }

    private static void pausa(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
