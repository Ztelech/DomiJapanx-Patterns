package modelo;

/**
 * Observer del patrón Observer.
 * Lo implementa cualquier componente que quiera enterarse
 * de los cambios de estado de un Pedido.
 */
public interface PedidoObserver {

    /**
     * Se invoca cada vez que el pedido observado cambia de estado.
     *
     * @param p pedido que cambió (permite consultar su estado actual).
     */
    void actualizar(Pedido p);
}
