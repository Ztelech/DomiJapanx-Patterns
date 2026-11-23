package modelo;

import java.util.Objects;

/**
 * Componente concreto del patrón Decorator.
 * Representa un rollo maki sin adiciones, que se puede envolver con decoradores.
 */
public class RolloMakiBase implements Producto {

    private final String nombre;
    private final double precioBase;

    public RolloMakiBase(String nombre, double precioBase) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del rollo no puede estar vacío.");
        }
        if (precioBase < 0) {
            throw new IllegalArgumentException("El precio base no puede ser negativo.");
        }
        this.nombre = nombre.trim();
        this.precioBase = precioBase;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    @Override
    public String getDescripcion() {
        return "Rollo Maki " + nombre;
    }

    @Override
    public double getPrecio() {
        return precioBase;
    }

    @Override
    public String toString() {
        return String.format("%s ($%.2f)", getDescripcion(), getPrecio());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolloMakiBase)) return false;
        RolloMakiBase otro = (RolloMakiBase) o;
        return Double.compare(precioBase, otro.precioBase) == 0
                && nombre.equals(otro.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, precioBase);
    }
}
