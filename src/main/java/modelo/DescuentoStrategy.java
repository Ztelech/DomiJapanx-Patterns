package modelo;

/**
 * Estrategia del patrón Strategy.
 * Cada implementación define una forma distinta de calcular el total con descuento.
 */
public interface DescuentoStrategy {

    /**
     * @param subtotal monto antes del descuento (debe ser >= 0).
     * @return monto final después de aplicar el descuento.
     */
    double aplicarDescuento(double subtotal);
}
