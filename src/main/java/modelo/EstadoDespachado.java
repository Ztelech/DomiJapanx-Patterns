package modelo;

import java.util.Objects;

/**
 * Estado final: el pedido ya salió de la cocina.
 * No puede avanzar más ni cancelarse.
 */
public class EstadoDespachado implements EstadoPedido {

    private static final String NOMBRE = "DESPACHADO";

    @Override
    public void avanzar(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        System.out.printf("[Pedido #%d] Acción no válida: el pedido ya está %s, es el estado final.%n",
                p.getId(), NOMBRE);
        // El estado no cambia.
    }

    @Override
    public void cancelar(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        System.out.printf("[Pedido #%d] Acción no válida: un pedido %s ya no se puede cancelar.%n",
                p.getId(), NOMBRE);
        // El estado no cambia.
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
