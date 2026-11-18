package modelo;

import java.util.Objects;

/**
 * Producto complejo del patrón Builder.
 * Un combo está formado por un plato principal, una bebida y un acompañamiento.
 *
 * También implementa Producto, así que un combo se puede tratar como cualquier
 * otro artículo del menú (por ejemplo, agregarlo a un pedido).
 *
 * Los setters son package-private: solo los builders del paquete 'modelo'
 * pueden armar un combo. Desde fuera del paquete es de solo lectura.
 */
public class Combo implements Producto {

    private final String nombre;
    private Producto platoPrincipal;
    private Producto bebida;
    private Producto acompanamiento;

    public Combo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del combo no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    // ---------- Getters públicos ----------

    public String getNombre() {
        return nombre;
    }

    public Producto getPlatoPrincipal() {
        return platoPrincipal;
    }

    public Producto getBebida() {
        return bebida;
    }

    public Producto getAcompanamiento() {
        return acompanamiento;
    }

    // ---------- Setters de paquete (solo para los builders) ----------

    void setPlatoPrincipal(Producto platoPrincipal) {
        this.platoPrincipal = Objects.requireNonNull(
                platoPrincipal, "El plato principal no puede ser null.");
    }

    void setBebida(Producto bebida) {
        this.bebida = Objects.requireNonNull(
                bebida, "La bebida no puede ser null.");
    }

    void setAcompanamiento(Producto acompanamiento) {
        this.acompanamiento = Objects.requireNonNull(
                acompanamiento, "El acompañamiento no puede ser null.");
    }

    /**
     * @return true si el combo tiene sus tres partes asignadas.
     */
    public boolean estaCompleto() {
        return platoPrincipal != null && bebida != null && acompanamiento != null;
    }

    // ---------- Implementación de Producto ----------

    @Override
    public String getDescripcion() {
        StringBuilder sb = new StringBuilder("Combo ").append(nombre).append(" [");
        sb.append("Plato: ").append(describir(platoPrincipal));
        sb.append(" | Bebida: ").append(describir(bebida));
        sb.append(" | Acompañamiento: ").append(describir(acompanamiento));
        sb.append("]");
        return sb.toString();
    }

    /**
     * Precio total: suma de las tres partes. Una parte sin asignar suma 0.
     */
    @Override
    public double getPrecio() {
        return precioDe(platoPrincipal) + precioDe(bebida) + precioDe(acompanamiento);
    }

    // ---------- Auxiliares privados ----------

    private static String describir(Producto p) {
        return p == null ? "(sin asignar)" : p.getDescripcion();
    }

    private static double precioDe(Producto p) {
        return p == null ? 0.0 : p.getPrecio();
    }

    @Override
    public String toString() {
        return String.format("%s ($%.2f)", getDescripcion(), getPrecio());
    }
}
