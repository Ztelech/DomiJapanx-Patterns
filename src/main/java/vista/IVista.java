package vista;

import modelo.Pedido;

/**
 * Contrato común de las vistas. El controlador solo conoce esta interfaz,
 * así se puede cambiar de consola a GUI sin tocar el controlador.
 */
public interface IVista {

    /** Muestra la carta del restaurante. */
    void mostrarMenu();

    /** Muestra el detalle actual del pedido: estado, artículos y totales. */
    void mostrarDetallePedido(Pedido p);

    /** Muestra un aviso puntual al usuario. */
    void mostrarAlertaNotificacion(String msj);

    /**
     * Muestra la factura de un pedido pagado.
     * Es un método default para no obligar a cambiar las vistas existentes
     * (por ejemplo, las vistas falsas de las pruebas).
     */
    default void mostrarFactura(Pedido p, String metodoPago) {
        mostrarAlertaNotificacion("\n" + FormatoPedido.factura(p, metodoPago));
    }
}
