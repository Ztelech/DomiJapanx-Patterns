package modelo;

/**
 * Builder concreto: arma un combo "Bento Box".
 *   - Plato: Rollo Maki Philadelphia con Topping Tempura (usa el Decorator)
 *   - Bebida: Té verde
 *   - Acompañamiento: Sopa miso
 */
public class BentoBoxBuilder implements ComboBuilder {

    private static final String NOMBRE_COMBO = "Bento Box";

    private Combo combo;

    public BentoBoxBuilder() {
        reiniciar();
    }

    private void reiniciar() {
        this.combo = new Combo(NOMBRE_COMBO);
    }

    @Override
    public void buildPlato() {
        Producto plato = new RolloMakiBase("Philadelphia", 9.50);
        plato = new ToppingTempura(plato);
        combo.setPlatoPrincipal(plato);
    }

    @Override
    public void buildBebida() {
        combo.setBebida(new ArticuloSimple("Té Verde", 2.50));
    }

    @Override
    public void buildAcompanamiento() {
        combo.setAcompanamiento(new ArticuloSimple("Sopa Miso", 3.00));
    }

    @Override
    public Combo getCombo() {
        if (!combo.estaCompleto()) {
            throw new IllegalStateException(
                    "El combo está incompleto: ejecuta todos los pasos de construcción.");
        }
        Combo resultado = combo;
        reiniciar(); // el builder queda listo para armar otro combo
        return resultado;
    }

    /**
     * Producto simple sin adiciones (bebidas y acompañamientos).
     * Es privado y anidado porque, de momento, solo lo usa este builder.
     */
    private static final class ArticuloSimple implements Producto {

        private final String descripcion;
        private final double precio;

        private ArticuloSimple(String descripcion, double precio) {
            if (descripcion == null || descripcion.isBlank()) {
                throw new IllegalArgumentException("La descripción no puede estar vacía.");
            }
            if (precio < 0) {
                throw new IllegalArgumentException("El precio no puede ser negativo.");
            }
            this.descripcion = descripcion.trim();
            this.precio = precio;
        }

        @Override
        public String getDescripcion() {
            return descripcion;
        }

        @Override
        public double getPrecio() {
            return precio;
        }
    }
}
