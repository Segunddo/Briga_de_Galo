package briga.galo;

import java.util.LinkedList;
import java.util.Queue;

// Template de buffer pro tratamento de dados de forma modular
public class Buffer<T> {

    //fila pra usar o esquema FIFO no objetivo com uma lista duplamente encadeada
    private final Queue<T> queue = new LinkedList<>();

    //sincronismo pra colocar os dados na fila
    public synchronized void push(T item) {
        queue.add(item);
        notifyAll();
    }

    //sincronismo pra retirar da lista sem acesso multiplo
    public synchronized T pop() {
        while (queue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                //volta a interrupção pra não ficar o loop sem interrupção do sistema
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return queue.poll();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized int size() {
        return queue.size();
    }
}
