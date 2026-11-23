package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase central del sistema. Cumple cuatro papeles:
 *   - Subject del Observer: avisa a los observers cuando cambia de estado.
 *   - Contexto del State: delega avanzar() y cancelar() en su estado actual.
 *   - Receptor del Command: agregarProducto() y removerProducto().
 *   - Cliente del Strategy: calcularTotal() usa la estrategia de descuento.
 */
public class Pedido {

    private static final AtomicInteger GENERADOR_ID = new AtomicInteger(1);

    private final int id;
    private EstadoPedido estado;
    private DescuentoStrategy descuento;
    private final List<Producto> productos;
    private final List<Combo> combos;
    private final List<PedidoObserver> observers;

    public Pedido() {
        this.id = GENERADOR_ID.getAndIncrement();
        this.estado = new EstadoRecibido();
        this.descuento = new SinDescuento();
        this.productos = new ArrayList<>();
        this.combos = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    // =====================================================================
    // Getters (las listas se devuelven como solo lectura)
    // =====================================================================

    public int getId() {
        return id;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public DescuentoStrategy getDescuento() {
        return descuento;
    }

    public List<Producto> getProductos() {
        return Collections.unmodifiableList(productos);
    }

    public List<Combo> getCombos() {
        return Collections.unmodifiableList(combos);
    }

    public int getCantidadArticulos() {
        return productos.size() + combos.size();
    }

    // =====================================================================
    // Patrón State: contexto
    // =====================================================================

    /** Delega en el estado actual el paso al siguiente estado. */
    public void avanzar() {
        estado.avanzar(this);
    }

    /** Delega en el estado actual la cancelación. */
    public void cancelar() {
        estado.cancelar(this);
    }

    /**
     * Package-private: solo los estados del paquete 'modelo' cambian el estado.
     * Cada cambio de estado avisa a los observers.
     */
    void setEstado(EstadoPedido nuevoEstado) {
        this.estado = Objects.requireNonNull(nuevoEstado, "El estado no puede ser null.");
        notificarObservers();
    }

    /**
     * @return true si el pedido todavía admite agregar o quitar productos.
     */
    public boolean permiteModificaciones() {
        return estado instanceof EstadoRecibido;
    }

    // =====================================================================
    // Patrón Observer: subject
    // =====================================================================

    public void suscribir(PedidoObserver observer) {
        Objects.requireNonNull(observer, "El observer no puede ser null.");
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void desuscribir(PedidoObserver observer) {
        observers.remove(observer);
    }

    public void notificarObservers() {
        // Se recorre una copia por si algún observer se desuscribe mientras se le notifica.
        for (PedidoObserver observer : new ArrayList<>(observers)) {
            observer.actualizar(this);
        }
    }

    // =====================================================================
    // Patrón Command: receptor
    // =====================================================================

    /**
     * Agrega un producto. Los combos van a su propia lista.
     *
     * @throws IllegalStateException si el pedido ya no admite cambios.
     */
    public void agregarProducto(Producto producto) {
        Objects.requireNonNull(producto, "El producto no puede ser null.");
        verificarModificable();
        if (producto instanceof Combo) {
            combos.add((Combo) producto);
        } else {
            productos.add(producto);
        }
    }

    /**
     * Quita exactamente esa instancia (comparando por referencia),
     * empezando por la última agregada. Así 'deshacer' quita
     * justo lo que agregó el comando.
     *
     * @return true si se encontró y se quitó.
     * @throws IllegalStateException si el pedido ya no admite cambios.
     */
    public boolean removerProducto(Producto producto) {
        Objects.requireNonNull(producto, "El producto no puede ser null.");
        verificarModificable();
        if (producto instanceof Combo) {
            return removerUltimaOcurrencia(combos, producto);
        }
        return removerUltimaOcurrencia(productos, producto);
    }

    // =====================================================================
    // Patrón Strategy: cálculo de totales
    // =====================================================================

    public void setDescuento(DescuentoStrategy descuento) {
        this.descuento = Objects.requireNonNull(descuento, "La estrategia de descuento no puede ser null.");
    }

    public double calcularSubtotal() {
        double subtotalProductos = productos.stream().mapToDouble(Producto::getPrecio).sum();
        double subtotalCombos = combos.stream().mapToDouble(Combo::getPrecio).sum();
        return subtotalProductos + subtotalCombos;
    }

    public double calcularTotal() {
        return descuento.aplicarDescuento(calcularSubtotal());
    }

    public double calcularAhorro() {
        return calcularSubtotal() - calcularTotal();
    }

    // =====================================================================
    // Auxiliares privados
    // =====================================================================

    private void verificarModificable() {
        if (!permiteModificaciones()) {
            throw new IllegalStateException(String.format(
                    "El pedido #%d está %s y ya no admite cambios.", id, estado.getNombre()));
        }
    }

    private static boolean removerUltimaOcurrencia(List<? extends Producto> lista, Producto objetivo) {
        for (int i = lista.size() - 1; i >= 0; i--) {
            if (lista.get(i) == objetivo) {
                lista.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Pedido #%d [%s] - %d artículos - Total $%.2f",
                id, estado.getNombre(), getCantidadArticulos(), calcularTotal());
    }
}
