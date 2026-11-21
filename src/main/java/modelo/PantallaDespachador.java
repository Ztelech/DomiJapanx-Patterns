package modelo;

import java.util.Objects;

/**
 * Observer concreto: pantalla del despachador que asigna repartidores.
 */
public class PantallaDespachador implements PedidoObserver {

    private static final String ETIQUETA = "[PANTALLA DESPACHADOR]";

    @Override
    public void actualizar(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        EstadoPedido estado = p.getEstado();

        if (estado instanceof EstadoPreparacion) {
            System.out.printf("%s Pedido #%d en cocina: asignar un repartidor.%n",
                    ETIQUETA, p.getId());
        } else if (estado instanceof EstadoDespachado) {
            System.out.printf("%s Pedido #%d EN CAMINO. Cobrar al cliente: $%.2f%n",
                    ETIQUETA, p.getId(), p.calcularTotal());
        } else if (estado instanceof EstadoCancelado) {
            System.out.printf("%s Pedido #%d cancelado: liberar al repartidor asignado.%n",
                    ETIQUETA, p.getId());
        } else {
            System.out.printf("%s Pedido #%d ahora está %s.%n",
                    ETIQUETA, p.getId(), estado.getNombre());
        }
    }
}
