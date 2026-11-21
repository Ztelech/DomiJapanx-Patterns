package modelo;

import java.util.Objects;

/**
 * Estado intermedio: el pedido se está preparando en cocina.
 *   avanzar()  → EstadoDespachado
 *   cancelar() → EstadoCancelado (todavía se permite)
 */
public class EstadoPreparacion implements EstadoPedido {

    private static final String NOMBRE = "EN PREPARACIÓN";

    @Override
    public void avanzar(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        System.out.printf("[Pedido #%d] %s -> DESPACHADO: el pedido salió con el repartidor.%n",
                p.getId(), NOMBRE);
        p.setEstado(new EstadoDespachado());
    }

    @Override
    public void cancelar(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        System.out.printf("[Pedido #%d] %s -> CANCELADO: se detiene la preparación en cocina.%n",
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
