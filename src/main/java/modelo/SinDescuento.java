package modelo;

/**
 * Estrategia por defecto: devuelve el subtotal sin cambios.
 * Así el código que usa un descuento nunca necesita comprobar si es null.
 */
public class SinDescuento implements DescuentoStrategy {

    @Override
    public double aplicarDescuento(double subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("El subtotal no puede ser negativo.");
        }
        return subtotal;
    }

    @Override
    public String toString() {
        return "Sin descuento";
    }
}
