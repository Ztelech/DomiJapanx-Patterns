package vista;

import java.util.Objects;

import modelo.Pedido;

/**
 * Vista de consola: muestra todo con System.out.println.
 */
public class ConsolaView implements IVista {

    @Override
    public void mostrarMenu() {
        System.out.println(FormatoPedido.carta());
    }

    @Override
    public void mostrarDetallePedido(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        System.out.println(FormatoPedido.detalle(p));
    }

    @Override
    public void mostrarAlertaNotificacion(String msj) {
        System.out.println(">>> NOTIFICACIÓN: " + msj);
    }
}
