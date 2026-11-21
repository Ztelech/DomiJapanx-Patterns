package modelo;

import java.util.Objects;

/**
 * Observer concreto: pantalla de la barra donde trabajan los sushimen.
 * Le interesa sobre todo cuándo empieza la preparación y cuándo se cancela un pedido.
 */
public class PantallaBarraSushi implements PedidoObserver {

    private static final String ETIQUETA = "[PANTALLA BARRA SUSHI]";

    @Override
    public void actualizar(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        EstadoPedido estado = p.getEstado();

        if (estado instanceof EstadoPreparacion) {
            System.out.printf("%s ¡Nuevo pedido #%d para preparar! (%d artículos)%n",
                    ETIQUETA, p.getId(), p.getCantidadArticulos());
            for (Combo combo : p.getCombos()) {
                System.out.println("      - " + combo.getDescripcion());
            }
            for (Producto producto : p.getProductos()) {
                System.out.println("      - " + producto.getDescripcion());
            }
        } else if (estado instanceof EstadoCancelado) {
            System.out.printf("%s Pedido #%d CANCELADO: detener la preparación.%n",
                    ETIQUETA, p.getId());
        } else {
            System.out.printf("%s Pedido #%d ahora está %s. Sin acciones para la barra.%n",
                    ETIQUETA, p.getId(), estado.getNombre());
        }
    }
}
