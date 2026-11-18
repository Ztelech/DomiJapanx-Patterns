package controlador;

import java.util.Objects;

import modelo.BentoBoxBuilder;
import modelo.Combo;
import modelo.ComandoAgregarProducto;
import modelo.DescuentoStrategy;
import modelo.DirectorArmado;
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
 */
public class PedidoController implements PedidoObserver {

    private final Pedido modelo;
    private final IVista vista;
    private final HistorialComandos historial;

    public PedidoController(Pedido modelo, IVista vista) {
        this.modelo = Objects.requireNonNull(modelo, "El modelo no puede ser null.");
        this.vista = Objects.requireNonNull(vista, "La vista no puede ser null.");
        this.historial = new HistorialComandos();
        this.modelo.suscribir(this);
    }

    /** Muestra la carta y el pedido inicial. */
    public void iniciar() {
        vista.mostrarMenu();
        vista.mostrarDetallePedido(modelo);
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
        EstadoPedido estadoAnterior = modelo.getEstado();
        modelo.cancelar();
        if (modelo.getEstado() == estadoAnterior) {
            vista.mostrarAlertaNotificacion("No se puede cancelar: el pedido está "
                    + estadoAnterior.getNombre() + ".");
        }
        vista.mostrarDetallePedido(modelo);
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
