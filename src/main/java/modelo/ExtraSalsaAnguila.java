package modelo;

/**
 * Decorador concreto: añade salsa de anguila (unagi) al producto.
 */
public class ExtraSalsaAnguila extends AdicionDecorator {

    private static final double COSTO_ADICION = 1.50;
    private static final String NOMBRE_ADICION = "Extra Salsa de Anguila";

    public ExtraSalsaAnguila(Producto producto) {
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
