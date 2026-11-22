package modelo;

import java.util.Objects;

/**
 * Estado final: el pedido fue cancelado.
 * Es el destino de cancelar() en Recibido y en Preparación.
 * No admite más transiciones.
 */
public class EstadoCancelado implements EstadoPedido {

    private static final String NOMBRE = "CANCELADO";

    @Override
    public void avanzar(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        System.out.printf("[Pedido #%d] Acción no válida: un pedido %s no puede avanzar.%n",
                p.getId(), NOMBRE);
    }

    @Override
    public void cancelar(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        System.out.printf("[Pedido #%d] Acción no válida: el pedido ya está %s.%n",
                p.getId(), NOMBRE);
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
