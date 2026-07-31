package briga.galo.network;

import briga.galo.Utils;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyHandler extends KeyAdapter {
    private final ClientDevice dispositivo;

    // Travas locais para evitar "flood" de rede enquanto segura a tecla
    private boolean segurandoDireita = false;
    private boolean segurandoEsquerda = false;
    private boolean segurandoDefesa = false;

    public KeyHandler(ClientDevice dispositivo) {
        this.dispositivo = dispositivo;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // 1. Ações Contínuas (Só envia 1 vez no momento em que aperta)
        if (code == KeyEvent.VK_RIGHT && !segurandoDireita) {
            segurandoDireita = true;
            dispositivo.enviarAcao(Utils.Action.WALK_RIGHT);
        }
        else if (code == KeyEvent.VK_LEFT && !segurandoEsquerda) {
            segurandoEsquerda = true;
            dispositivo.enviarAcao(Utils.Action.WALK_LEFT);
        }
        else if (code == KeyEvent.VK_DOWN && !segurandoDefesa) {
            segurandoDefesa = true;
            dispositivo.enviarAcao(Utils.Action.DEFEND_LEFT);
        }
        // 2. Ações Instantâneas (Ataque pode ser disparado por clique)
        else if (code == KeyEvent.VK_SPACE) {
            dispositivo.enviarAcao(Utils.Action.ATTACK);
        }
        else if (code == KeyEvent.VK_UP) {
            dispositivo.enviarAcao(Utils.Action.FLY_ATTACK_RIGHT);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        // Quando solta uma tecla de movimento/defesa, avisa o servidor que voltou pro IDLE
        if (code == KeyEvent.VK_RIGHT) {
            segurandoDireita = false;
            dispositivo.enviarAcao(Utils.Action.IDLE);
        }
        else if (code == KeyEvent.VK_LEFT) {
            segurandoEsquerda = false;
            dispositivo.enviarAcao(Utils.Action.IDLE);
        }
        else if (code == KeyEvent.VK_DOWN) {
            segurandoDefesa = false;
            dispositivo.enviarAcao(Utils.Action.IDLE);
        }
    }
}
