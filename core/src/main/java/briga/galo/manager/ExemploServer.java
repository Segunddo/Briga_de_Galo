package briga.galo.manager;

/**
 * Exemplo de inicialização do lado SERVIDOR.
 *
 * Esse é o programa que roda numa máquina central (ou num dos dispositivos,
 * se for um servidor "host" local) e fica esperando os dois jogadores se
 * conectarem. Repare que TODA a complexidade (lobby, salas, loop de
 * partida a 60 FPS, desconexão, etc.) já está encapsulada dentro do
 * ServerManager e da GameRoom — do ponto de vista de quem sobe o
 * servidor, o uso é literalmente isso:
 *
 *   new ServerManager(porta).start();
 *
 * start() é bloqueante (fica rodando pra sempre aceitando conexões), então
 * se você quiser fazer mais alguma coisa no mesmo processo (uma GUI de
 * administração, por exemplo), chame start() dentro de uma Thread separada.
 */
public class ExemploServer {

    public static void main(String[] args) {
        int porta = 5000; // porta TCP em que o servidor vai escutar

        ServerManager servidor = new ServerManager(porta);

        // Opcional: registra um "ouvinte" de log só pra acompanhar o que
        // está acontecendo no console (conexões aceitas, salas criadas/
        // removidas, etc). Isso não é necessário pro funcionamento —
        // é só conveniência de observação/depuração.
        servidor.setOnLog(mensagem -> System.out.println("[LOG] " + mensagem));

        System.out.println("Subindo o servidor na porta " + porta + "...");

        // Chamada única e bloqueante: a partir daqui o servidor fica
        // aceitando conexões e gerenciando salas sozinho, pra sempre.
        // Se quiser continuar o main() fazendo outra coisa, troque essa
        // linha por:
        //
        //   Thread threadServidor = new Thread(servidor::start);
        //   threadServidor.setDaemon(true);
        //   threadServidor.start();
        //
        servidor.start();

        // Nada depois daqui é executado enquanto start() estiver bloqueando
        // (a não ser que você tenha usado a versão em Thread acima).
    }
}
