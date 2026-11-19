package modelo;

/**
 * Decorador concreto: añade topping de tempura crujiente al producto.
 */
public class ToppingTempura extends AdicionDecorator {

    private static final double COSTO_ADICION = 2.00;
    private static final String NOMBRE_ADICION = "Topping Tempura";

    public ToppingTempura(Producto producto) {
        super(producto);
    }

    public static double getCostoAdicion() {
        return COSTO_ADICION;
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " + " + NOMBRE_ADICION;
    }

    @Override
    public double getPrecio() {
        return super.getPrecio() + COSTO_ADICION;
    }
}
