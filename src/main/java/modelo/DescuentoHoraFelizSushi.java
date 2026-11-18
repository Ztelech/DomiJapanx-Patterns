package modelo;

/**
 * Estrategia concreta: descuento porcentual de hora feliz.
 * Por defecto descuenta el 10%, pero el porcentaje se puede configurar.
 */
public class DescuentoHoraFelizSushi implements DescuentoStrategy {

    private static final double PORCENTAJE_POR_DEFECTO = 10.0;

    private final double porcentaje;

    public DescuentoHoraFelizSushi() {
        this(PORCENTAJE_POR_DEFECTO);
    }

    public DescuentoHoraFelizSushi(double porcentaje) {
        if (porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException(
                    "El porcentaje de descuento debe estar entre 0 y 100.");
        }
        this.porcentaje = porcentaje;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    @Override
    public double aplicarDescuento(double subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("El subtotal no puede ser negativo.");
        }
        return subtotal - (subtotal * porcentaje / 100.0);
    }

    @Override
    public String toString() {
        return String.format("Hora Feliz Sushi (-%.0f%%)", porcentaje);
    }
}
