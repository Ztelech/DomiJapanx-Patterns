package modelo;

import java.util.Objects;

/**
 * Comando concreto: agrega un producto a un pedido.
 * El receptor es Pedido.
 *   ejecutar() → pedido.agregarProducto(producto)
 *   deshacer() → pedido.removerProducto(producto)
 */
public class ComandoAgregarProducto implements Comando {

    private final Pedido pedido;
    private final Producto producto;
    private boolean ejecutado;

    public ComandoAgregarProducto(Pedido pedido, Producto producto) {
        this.pedido = Objects.requireNonNull(pedido, "El pedido no puede ser null.");
        this.producto = Objects.requireNonNull(producto, "El producto no puede ser null.");
        this.ejecutado = false;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Producto getProducto() {
        return producto;
    }

    public boolean isEjecutado() {
        return ejecutado;
    }

    @Override
    public void ejecutar() {
        if (ejecutado) {
            throw new IllegalStateException("Este comando ya fue ejecutado.");
        }
        pedido.agregarProducto(producto);
        ejecutado = true;
    }

    @Override
    public void deshacer() {
        if (!ejecutado) {
            throw new IllegalStateException("No se puede deshacer un comando que no se ha ejecutado.");
        }
        pedido.removerProducto(producto);
        ejecutado = false;
    }

    @Override
    public String toString() {
        return "Agregar: " + producto.getDescripcion();
    }
}
