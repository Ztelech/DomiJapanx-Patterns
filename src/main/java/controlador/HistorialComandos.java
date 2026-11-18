package controlador;

import java.util.Objects;
import java.util.Stack;

import modelo.Comando;

/**
 * Invocador del patrón Command.
 * Ejecuta comandos y los apila para poder deshacerlos en orden inverso (LIFO).
 */
public class HistorialComandos {

    private final Stack<Comando> historial;

    public HistorialComandos() {
        this.historial = new Stack<>();
    }

    /**
     * Ejecuta el comando y, si no falla, lo apila en el historial.
     * Si ejecutar() lanza una excepción, el comando no se guarda.
     */
    public void guardarComando(Comando c) {
        Objects.requireNonNull(c, "El comando no puede ser null.");
        c.ejecutar();
        historial.push(c);
    }

    /**
     * Saca el último comando del historial y lo deshace.
     *
     * @return true si se deshizo un comando; false si el historial estaba vacío.
     */
    public boolean deshacerUltimo() {
        if (historial.isEmpty()) {
            return false;
        }
        Comando ultimo = historial.pop();
        ultimo.deshacer();
        return true;
    }

    public boolean hayComandosParaDeshacer() {
        return !historial.isEmpty();
    }

    public int getCantidadComandos() {
        return historial.size();
    }

    public void limpiar() {
        historial.clear();
    }
}
