package modelo;

/**
 * Componente del patrón Decorator.
 * Define el contrato común para cualquier producto del menú,
 * ya sea un producto base o un producto decorado con adiciones.
 */
public interface Producto {

    /**
     * @return descripción completa del producto, incluidas sus adiciones.
     */
    String getDescripcion();

    /**
     * @return precio total del producto, incluidas sus adiciones.
     */
    double getPrecio();
}
