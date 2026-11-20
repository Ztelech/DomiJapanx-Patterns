package modelo;

import java.util.Objects;

/**
 * Director del patrón Builder.
 * Fija el orden de los pasos de construcción sin conocer
 * qué productos concretos crea cada builder.
 */
public class DirectorArmado {

    private ComboBuilder builder;

    public DirectorArmado(ComboBuilder builder) {
        setBuilder(builder);
    }

    public ComboBuilder getBuilder() {
        return builder;
    }

    /**
     * Permite cambiar de builder sin crear otro director.
     */
    public void setBuilder(ComboBuilder builder) {
        this.builder = Objects.requireNonNull(builder, "El builder no puede ser null.");
    }

    /**
     * Ejecuta todos los pasos en orden y devuelve el combo terminado.
     */
    public Combo armarCombo() {
        builder.buildPlato();
        builder.buildBebida();
        builder.buildAcompanamiento();
        return builder.getCombo();
    }
}
