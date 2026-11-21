package modelo;

import java.util.Objects;

/**
 * Estado inicial: la cocina oculta recibió el pedido.
 *   avanzar()  → EstadoPreparacion
 *   cancelar() → EstadoCancelado
 */
public class EstadoRecibido implements EstadoPedido {

    private static final String NOMBRE = "RECIBIDO";

    @Override
    public void avanzar(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        System.out.printf("[Pedido #%d] %s -> EN PREPARACIÓN: el sushiman empieza a armar los rollos.%n",
                p.getId(), NOMBRE);
        p.setEstado(new EstadoPreparacion());
    }

    @Override
    public void cancelar(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        System.out.printf("[Pedido #%d] %s -> CANCELADO: el pedido se canceló antes de prepararse.%n",
                p.getId(), NOMBRE);
        p.setEstado(new EstadoCancelado());
    }

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String toString() {
        return NOMBRE;
    }
}
