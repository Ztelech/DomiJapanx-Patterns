package modelo;

/**
 * Builder abstracto del patrón Builder.
 * Define los pasos para construir un Combo.
 */
public interface ComboBuilder {

    /** Construye y asigna el plato principal del combo. */
    void buildPlato();

    /** Construye y asigna la bebida del combo. */
    void buildBebida();

    /** Construye y asigna el acompañamiento del combo. */
    void buildAcompanamiento();

    /**
     * Devuelve el combo construido y deja el builder listo para armar otro.
     *
     * @throws IllegalStateException si el combo no está completo.
     */
    Combo getCombo();
}
