package modelo;

import java.util.Objects;

/**
 * Observer concreto: sistema de caja que registra cobros y anulaciones.
 */
public class SistemaCaja implements PedidoObserver {

    private static final String ETIQUETA = "[SISTEMA CAJA]";

    @Override
    public void actualizar(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        EstadoPedido estado = p.getEstado();

        if (estado instanceof EstadoPreparacion) {
            System.out.printf("%s Pedido #%d confirmado. Subtotal $%.2f | %s | Total $%.2f%n",
                    ETIQUETA, p.getId(), p.calcularSubtotal(), p.getDescuento(), p.calcularTotal());
        } else if (estado instanceof EstadoDespachado) {
            System.out.printf("%s Cobro registrado del pedido #%d: $%.2f%n",
                    ETIQUETA, p.getId(), p.calcularTotal());
        } else if (estado instanceof EstadoCancelado) {
            System.out.printf("%s Pedido #%d cancelado: anular el cobro.%n",
                    ETIQUETA, p.getId());
        } else {
            System.out.printf("%s Pedido #%d ahora está %s.%n",
                    ETIQUETA, p.getId(), estado.getNombre());
        }
    }
}
