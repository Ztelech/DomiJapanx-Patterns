package modelo;

/**
 * Estado del patrón State.
 * Cada estado concreto decide qué pasa cuando el pedido intenta
 * avanzar o cancelarse, y a qué estado pasa después.
 * El contexto es la clase Pedido.
 */
public interface EstadoPedido {

    /**
     * Intenta pasar el pedido al siguiente estado del flujo.
     */
    void avanzar(Pedido p);

    /**
     * Intenta cancelar el pedido.
     */
    void cancelar(Pedido p);

    /**
     * @return nombre legible del estado (para la vista y los mensajes).
     */
    String getNombre();
}
