package controlador;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import modelo.BentoBoxBuilder;
import modelo.Combo;
import modelo.ComandoAgregarProducto;
import modelo.DescuentoStrategy;
import modelo.DirectorArmado;
import modelo.EstadoCancelado;
import modelo.EstadoPedido;
import modelo.ExtraSalsaAnguila;
import modelo.Pedido;
import modelo.PedidoObserver;
import modelo.Producto;
import modelo.RolloMakiBase;
import modelo.ToppingTempura;
import vista.IVista;

/**
 * Controlador MVC. Recibe las acciones del usuario, las aplica al modelo
 * (mediante comandos cuando hace falta deshacer) y actualiza la vista.
 *
 * También es un observer del pedido: cuando cambia el estado,
 * reenvía el aviso a la vista (consola o GUI).
 *
 * Maneja el ciclo completo del restaurante: armar el pedido, cobrarlo
 * (factura) y empezar un pedido nuevo con los mismos observers.
 */
public class PedidoController implements PedidoObserver {

    private Pedido modelo;
    private final IVista vista;
    private final HistorialComandos historial;

    /** Observers externos (pantallas, caja...) que se vuelven a suscribir en cada pedido nuevo. */
    private final List<PedidoObserver> observersRegistrados;

    /** true cuando el pedido actual ya fue pagado y facturado. */
    private boolean pedidoPagado;

    public PedidoController(Pedido modelo, IVista vista) {
        this.modelo = Objects.requireNonNull(modelo, "El modelo no puede ser null.");
        this.vista = Objects.requireNonNull(vista, "La vista no puede ser null.");
        this.historial = new HistorialComandos();
        this.observersRegistrados = new ArrayList<>();
        this.modelo.suscribir(this);
    }

    /** Muestra la carta y el pedido inicial. */
    public void iniciar() {
        vista.mostrarMenu();
        vista.mostrarDetallePedido(modelo);
    }

    /**
     * Registra un observer externo (por ejemplo, una pantalla de cocina) y lo
     * suscribe al pedido actual. Quedará suscrito también a los pedidos nuevos.
     * El controlador se mantiene como último observer para que su aviso
     * llegue después de las pantallas.
     */
    public void registrarObserver(PedidoObserver observer) {
        Objects.requireNonNull(observer, "El observer no puede ser null.");
        if (!observersRegistrados.contains(observer)) {
            observersRegistrados.add(observer);
        }
        modelo.desuscribir(this);
        modelo.suscribir(observer);
        modelo.suscribir(this);
    }

    public Pedido getPedidoActual() {
        return modelo;
    }

    // =====================================================================
    // Creación de productos (Decorator y Builder)
    // =====================================================================

    /**
     * Crea un rollo maki con sus adiciones (Decorator).
     */
    public Producto crearRolloMaki(String nombre, double precioBase,
                                   boolean conTempura, int extrasSalsaAnguila) {
        if (extrasSalsaAnguila < 0) {
            throw new IllegalArgumentException("La cantidad de extras no puede ser negativa.");
        }
        Producto rollo = new RolloMakiBase(nombre, precioBase);
        if (conTempura) {
            rollo = new ToppingTempura(rollo);
        }
        for (int i = 0; i < extrasSalsaAnguila; i++) {
            rollo = new ExtraSalsaAnguila(rollo);
        }
        return rollo;
    }

    /**
     * Arma un combo Bento Box usando el Director (Builder).
     */
    public Combo crearComboBentoBox() {
        DirectorArmado director = new DirectorArmado(new BentoBoxBuilder());
        return director.armarCombo();
    }

    // =====================================================================
    // Acciones sobre el pedido (Command, Strategy y State)
    // =====================================================================

    /** Agrega un producto mediante un comando, para poder deshacerlo. */
    public void agregarProducto(Producto producto) {
        if (rechazarSiYaPagado("agregar productos")) {
            return;
        }
        try {
            historial.guardarComando(new ComandoAgregarProducto(modelo, producto));
            vista.mostrarDetallePedido(modelo);
        } catch (IllegalStateException | IllegalArgumentException | NullPointerException e) {
            vista.mostrarAlertaNotificacion("No se pudo agregar el producto: " + e.getMessage());
        }
    }

    /** Deshace el último comando ejecutado, si el pedido todavía lo permite. */
    public void deshacerUltimaAccion() {
        if (!historial.hayComandosParaDeshacer()) {
            vista.mostrarAlertaNotificacion("No hay acciones para deshacer.");
            return;
        }
        if (rechazarSiYaPagado("deshacer cambios")) {
            return;
        }
        if (!modelo.permiteModificaciones()) {
            vista.mostrarAlertaNotificacion("El pedido ya está "
                    + modelo.getEstado().getNombre() + ": no se pueden deshacer cambios.");
            return;
        }
        historial.deshacerUltimo();
        vista.mostrarAlertaNotificacion("Se deshizo la última acción.");
        vista.mostrarDetallePedido(modelo);
    }

    /** Cambia la estrategia de descuento del pedido. */
    public void aplicarDescuento(DescuentoStrategy descuento) {
        if (rechazarSiYaPagado("cambiar el descuento")) {
            vista.mostrarDetallePedido(modelo); // re-sincroniza la vista (p. ej. la casilla de Hora Feliz)
            return;
        }
        modelo.setDescuento(descuento);
        vista.mostrarDetallePedido(modelo);
    }

    /** Avanza el estado. Los observers se disparan desde el modelo. */
    public void avanzarEstado() {
        EstadoPedido estadoAnterior = modelo.getEstado();
        modelo.avanzar();
        if (modelo.getEstado() == estadoAnterior) {
            vista.mostrarAlertaNotificacion("No se puede avanzar: el pedido está "
                    + estadoAnterior.getNombre() + ".");
        }
        vista.mostrarDetallePedido(modelo);
    }

    /** Intenta cancelar el pedido. */
    public void cancelarPedido() {
        if (rechazarSiYaPagado("cancelarlo")) {
            return;
        }
        EstadoPedido estadoAnterior = modelo.getEstado();
        modelo.cancelar();
        if (modelo.getEstado() == estadoAnterior) {
            vista.mostrarAlertaNotificacion("No se puede cancelar: el pedido está "
                    + estadoAnterior.getNombre() + ".");
        }
        vista.mostrarDetallePedido(modelo);
    }

    // =====================================================================
    // Pago, factura y nuevo pedido
    // =====================================================================

    /**
     * Cobra el pedido actual con el método indicado y emite la factura.
     * No se factura un pedido vacío, un pedido CANCELADO ni uno ya pagado.
     *
     * @return true si el pago se registró.
     */
    public boolean pagarPedido(String metodoPago) {
        if (metodoPago == null || metodoPago.trim().isEmpty()) {
            vista.mostrarAlertaNotificacion("Elige un método de pago antes de facturar.");
            return false;
        }
        if (pedidoPagado) {
            vista.mostrarAlertaNotificacion(String.format(
                    "El pedido #%d ya fue pagado. Inicia un nuevo pedido.", modelo.getId()));
            return false;
        }
        if (modelo.getEstado() instanceof EstadoCancelado) {
            vista.mostrarAlertaNotificacion(String.format(
                    "No se puede facturar: el pedido #%d está CANCELADO.", modelo.getId()));
            return false;
        }
        if (modelo.getCantidadArticulos() == 0) {
            vista.mostrarAlertaNotificacion(String.format(
                    "No se puede facturar: el pedido #%d está vacío.", modelo.getId()));
            return false;
        }
        pedidoPagado = true;
        vista.mostrarAlertaNotificacion(String.format(
                "Cobro registrado: pedido #%d pagado con %s por $%.2f.",
                modelo.getId(), metodoPago, modelo.calcularTotal()));
        vista.mostrarFactura(modelo, metodoPago);
        return true;
    }

    /**
     * Empieza un pedido nuevo (su ID lo asigna el modelo, que lo incrementa),
     * vuelve a suscribir los observers registrados y el propio controlador,
     * limpia el historial de comandos y actualiza la vista.
     */
    public void nuevoPedido() {
        Pedido anterior = modelo;
        for (PedidoObserver observer : observersRegistrados) {
            anterior.desuscribir(observer);
        }
        anterior.desuscribir(this);

        modelo = new Pedido();
        for (PedidoObserver observer : observersRegistrados) {
            modelo.suscribir(observer);
        }
        modelo.suscribir(this);

        historial.limpiar();
        pedidoPagado = false;

        vista.mostrarAlertaNotificacion(String.format(
                "Se inició el nuevo pedido #%d (el pedido #%d quedó cerrado).", modelo.getId(), anterior.getId()));
        vista.mostrarDetallePedido(modelo);
    }

    private boolean rechazarSiYaPagado(String accion) {
        if (!pedidoPagado) {
            return false;
        }
        vista.mostrarAlertaNotificacion(String.format(
                "El pedido #%d ya fue pagado: no se puede %s. Inicia un nuevo pedido.", modelo.getId(), accion));
        return true;
    }

    // =====================================================================
    // Observer: reenvía los cambios de estado a la vista
    // =====================================================================

    @Override
    public void actualizar(Pedido p) {
        vista.mostrarAlertaNotificacion(String.format(
                "El pedido #%d cambió a %s.", p.getId(), p.getEstado().getNombre()));
    }
}
