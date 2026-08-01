package briga.galo.test;

import briga.galo.Utils;
import briga.galo.manager.ServerManager;
import briga.galo.network.ClientDevice;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Ferramenta de teste manual (sem JUnit) para validar:
 *  - servidor rodando com start(porta)
 *  - múltiplos jogadores (clientes) conectando ao mesmo tempo
 *  - criação de sala / entrar em sala / lobby atribuindo salas automaticamente
 *  - cancelamento enquanto está esperando oponente (CANCEL_ROOM)
 *  - desconexão "bruta" (fechar socket) de um jogador esperando ou em partida
 *  - troca de ações durante uma partida (MATCH) e o eco do servidor pros dois lados
 *
 * Cada "Jogador" na tela é um ClientDevice independente, então dá pra abrir
 * quantos quiser e testar o comportamento do servidor com várias salas simultâneas.
 */
public class TestMain extends JFrame {

    private final JPanel painelClientes = new JPanel();
    private final JTextArea logServidor = new JTextArea();
    private final JLabel statusServidor = new JLabel("Servidor: parado");
    private final JSpinner spinnerPorta = new JSpinner(new SpinnerNumberModel(5000, 1024, 65535, 1));
    private int contadorJogadores = 1;
    private int portaAtual = -1;
    private boolean servidorIniciado = false;

    public TestMain() {
        super("Teste - Briga de Galo (Servidor + Múltiplos Clientes)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(criarPainelTopo(), BorderLayout.NORTH);
        add(criarPainelClientesScroll(), BorderLayout.CENTER);
        add(criarPainelLogServidor(), BorderLayout.SOUTH);
    }

    // ---------- Painel de controle do servidor ----------
    private JPanel criarPainelTopo() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painel.setBorder(new EmptyBorder(8, 8, 0, 8));

        JButton btnIniciarServidor = new JButton("Iniciar Servidor");
        JButton btnAddJogador = new JButton("+ Novo Jogador");
        btnAddJogador.setEnabled(false);

        btnIniciarServidor.addActionListener((ActionEvent e) -> {
            if (servidorIniciado) return;
            int porta = (Integer) spinnerPorta.getValue();
            portaAtual = porta;
            ServerManager serverManager = new ServerManager(porta);
            serverManager.setOnLog(this::logServidor);

            Thread serverThread = new Thread(serverManager::start, "ServerThread");
            serverThread.setDaemon(true);
            serverThread.start();

            servidorIniciado = true;
            statusServidor.setText("Servidor: rodando na porta " + porta);
            btnIniciarServidor.setEnabled(false);
            spinnerPorta.setEnabled(false);
            btnAddJogador.setEnabled(true);
        });

        btnAddJogador.addActionListener(e -> adicionarPainelJogador());

        painel.add(new JLabel("Porta:"));
        painel.add(spinnerPorta);
        painel.add(btnIniciarServidor);
        painel.add(btnAddJogador);
        painel.add(Box.createHorizontalStrut(20));
        painel.add(statusServidor);

        return painel;
    }

    private JScrollPane criarPainelClientesScroll() {
        painelClientes.setLayout(new WrapLayout(FlowLayout.LEFT, 10, 10));
        JScrollPane scroll = new JScrollPane(painelClientes);
        scroll.setBorder(new TitledBorder("Jogadores simulados"));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel criarPainelLogServidor() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(new TitledBorder("Log do servidor"));
        painel.setPreferredSize(new Dimension(100, 160));

        logServidor.setEditable(false);
        logServidor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        painel.add(new JScrollPane(logServidor), BorderLayout.CENTER);
        return painel;
    }

    private void logServidor(String msg) {
        SwingUtilities.invokeLater(() -> {
            logServidor.append(carimbo() + " " + msg + "\n");
            logServidor.setCaretPosition(logServidor.getDocument().getLength());
        });
    }

    private static String carimbo() {
        return "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]";
    }

    private void adicionarPainelJogador() {
        int id = contadorJogadores++;
        JogadorPanel painel = new JogadorPanel("Jogador " + id, portaAtual);
        painelClientes.add(painel);
        painelClientes.revalidate();
        painelClientes.repaint();
    }

    // ---------- Painel individual de um "jogador" / ClientDevice ----------
    private class JogadorPanel extends JPanel {
        private final ClientDevice device = new ClientDevice();
        private final JTextArea log = new JTextArea(10, 26);
        private final JLabel status = new JLabel("Desconectado");
        private final JButton btnConectar = new JButton("Conectar");
        private final JButton btnCriarSala = new JButton("Criar Sala");
        private final JButton btnEntrarSala = new JButton("Entrar em Sala");
        private final JButton btnCancelar = new JButton("Cancelar (voltar ao menu)");
        private final JButton btnDesconectar = new JButton("Desconectar (queda de conexão)");
        private final int porta;

        JogadorPanel(String nome, int porta) {
            this.porta = porta;
            setLayout(new BorderLayout(4, 4));
            setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(nome),
                new EmptyBorder(4, 4, 4, 4)));
            setPreferredSize(new Dimension(320, 430));

            add(status, BorderLayout.NORTH);
            add(criarPainelBotoes(), BorderLayout.CENTER);
            add(criarPainelLogEAcoes(), BorderLayout.SOUTH);

            configurarDevice();
            atualizarBotoes();
        }

        private JPanel criarPainelBotoes() {
            JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));

            btnConectar.addActionListener(e -> {
                device.connect("127.0.0.1", porta);
                atualizarBotoes();
            });

            btnCriarSala.addActionListener(e -> device.solicitarCriarSala());
            btnEntrarSala.addActionListener(e -> device.solicitarEntrarSala());
            btnCancelar.addActionListener(e -> device.solicitarCancelar());

            btnDesconectar.addActionListener(e -> {
                device.fechar();
                atualizarBotoes();
            });
            btnDesconectar.setForeground(new Color(160, 0, 0));

            p.add(btnConectar);
            p.add(btnCriarSala);
            p.add(btnEntrarSala);
            p.add(btnCancelar);
            p.add(btnDesconectar);

            p.add(criarPainelAcoesDeJogo());
            return p;
        }

        private JPanel criarPainelAcoesDeJogo() {
            JPanel p = new JPanel(new GridLayout(2, 3, 3, 3));
            p.setBorder(new TitledBorder("Ações (durante MATCH)"));

            adicionarBotaoAcao(p, "◀ Andar", Utils.Action.WALK_LEFT);
            adicionarBotaoAcao(p, "Parar", Utils.Action.IDLE);
            adicionarBotaoAcao(p, "Andar ▶", Utils.Action.WALK_RIGHT);
            adicionarBotaoAcao(p, "Defender", Utils.Action.DEFEND_LEFT);
            adicionarBotaoAcao(p, "Atacar", Utils.Action.ATTACK);
            adicionarBotaoAcao(p, "Voo+Ataque", Utils.Action.FLY_ATTACK_RIGHT);

            return p;
        }

        private void adicionarBotaoAcao(JPanel painel, String label, Utils.Action acao) {
            JButton btn = new JButton(label);
            btn.setMargin(new Insets(2, 2, 2, 2));
            btn.addActionListener(e -> device.enviarAcao(acao));
            painel.add(btn);
        }

        private JPanel criarPainelLogEAcoes() {
            JPanel p = new JPanel(new BorderLayout());
            log.setEditable(false);
            log.setLineWrap(true);
            log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            p.add(new JScrollPane(log), BorderLayout.CENTER);
            return p;
        }

        private void configurarDevice() {
            device.setOnEvento(msg -> SwingUtilities.invokeLater(() -> {
                log.append(carimbo() + " " + msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
                atualizarStatus();
                atualizarBotoes();
            }));

            device.setOnDesconectado(() -> SwingUtilities.invokeLater(() -> {
                atualizarStatus();
                atualizarBotoes();
            }));
        }

        private void atualizarStatus() {
            StringBuilder sb = new StringBuilder();
            sb.append(device.isConectado() ? "Conectado" : "Desconectado");
            sb.append(" | Estado: ").append(device.getEstadoAtual());
            if (device.getMeuPlayerId() != 0) {
                sb.append(" | Você é P").append(device.getMeuPlayerId());
            }
            if (device.getMotivoFim() != null && device.getEstadoAtual() == Utils.StateGame.ENDGAME) {
                sb.append(" | Fim: ").append(device.getMotivoFim());
            }
            status.setText(sb.toString());
        }

        private void atualizarBotoes() {
            boolean conectado = device.isConectado();
            btnConectar.setEnabled(!conectado);
            btnCriarSala.setEnabled(conectado);
            btnEntrarSala.setEnabled(conectado);
            btnCancelar.setEnabled(conectado);
            btnDesconectar.setEnabled(conectado);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TestMain().setVisible(true));
    }
}
