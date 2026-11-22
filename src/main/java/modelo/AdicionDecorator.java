package modelo;

import java.util.Objects;

/**
 * Decorador abstracto del patrón Decorator.
 * Implementa Producto y envuelve otro Producto (base o ya decorado).
 * Por defecto delega en el producto envuelto. Las subclases añaden
 * su propia descripción y su costo.
 */
public abstract class AdicionDecorator implements Producto {

    private final Producto productoEnvuelto;

    protected AdicionDecorator(Producto productoEnvuelto) {
        this.productoEnvuelto = Objects.requireNonNull(
                productoEnvuelto, "El producto a decorar no puede ser null.");
    }

    /**
     * Acceso controlado al producto envuelto, solo para las subclases.
     */
    protected Producto getProductoEnvuelto() {
        return productoEnvuelto;
    }

    @Override
    public String getDescripcion() {
        return productoEnvuelto.getDescripcion();
    }

    @Override
    public double getPrecio() {
        return productoEnvuelto.getPrecio();
    }

    @Override
    public String toString() {
        return String.format("%s ($%.2f)", getDescripcion(), getPrecio());
    }
}
