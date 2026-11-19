package modelo;

/**
 * Comando del patrón Command.
 * Encapsula una acción sobre el pedido para poder ejecutarla,
 * guardarla en un historial y deshacerla.
 */
public interface Comando {

    /** Ejecuta la acción sobre el receptor. */
    void ejecutar();

    /** Revierte el efecto de ejecutar(). */
    void deshacer();
}
