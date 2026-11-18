package modelo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del patrón Observer: Pedido como subject y los PedidoObserver.
 */
class ObserverTest {

    private Pedido pedido;
    private ObserverEspia espia;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        espia = new ObserverEspia();
        pedido.suscribir(espia);
    }

    @Test
    @DisplayName("Cada cambio de estado notifica al observer con el estado nuevo")
    void notificaEnCadaCambioDeEstado() {
        pedido.avanzar();
        pedido.avanzar();

        assertEquals(2, espia.notificaciones);
        assertSame(pedido, espia.ultimoPedido);
        assertInstanceOf(EstadoPreparacion.class, espia.estadosRecibidos.get(0));
        assertInstanceOf(EstadoDespachado.class, espia.estadosRecibidos.get(1));
    }

    @Test
    @DisplayName("Cancelar también notifica")
    void cancelarNotifica() {
        pedido.cancelar();

        assertEquals(1, espia.notificaciones);
        assertInstanceOf(EstadoCancelado.class, espia.estadosRecibidos.get(0));
    }

    @Test
    @DisplayName("Una transición no válida no notifica")
    void transicionInvalidaNoNotifica() {
        pedido.avanzar();
        pedido.avanzar(); // DESPACHADO
        int antes = espia.notificaciones;

        pedido.avanzar();
        pedido.cancelar();

        assertEquals(antes, espia.notificaciones);
    }

    @Test
    @DisplayName("Agregar productos no es un cambio de estado y no notifica")
    void agregarProductoNoNotifica() {
        pedido.agregarProducto(new RolloMakiBase("California", 8.00));

        assertEquals(0, espia.notificaciones);
    }

    @Test
    @DisplayName("Todos los observers suscritos reciben la notificación")
    void variosObserversReciben() {
        ObserverEspia otro = new ObserverEspia();
        pedido.suscribir(otro);

        pedido.avanzar();

        assertEquals(1, espia.notificaciones);
        assertEquals(1, otro.notificaciones);
    }

    @Test
    @DisplayName("Un observer desuscrito deja de recibir notificaciones")
    void desuscribirDetieneNotificaciones() {
        pedido.desuscribir(espia);

        pedido.avanzar();

        assertEquals(0, espia.notificaciones);
    }

    @Test
    @DisplayName("Suscribir dos veces el mismo observer no duplica las notificaciones")
    void suscripcionDuplicadaSeIgnora() {
        pedido.suscribir(espia);

        pedido.avanzar();

        assertEquals(1, espia.notificaciones);
    }

    @Test
    @DisplayName("Un observer se puede desuscribir mientras se le notifica")
    void desuscribirDuranteNotificacion() {
        PedidoObserver seVa = p -> p.desuscribir(espia);
        pedido.suscribir(seVa);

        assertDoesNotThrow(() -> pedido.avanzar());
    }

    @Test
    @DisplayName("Los observers concretos reaccionan sin errores en todos los estados")
    void observersConcretosFuncionan() {
        pedido.suscribir(new PantallaBarraSushi());
        pedido.suscribir(new PantallaDespachador());
        pedido.suscribir(new SistemaCaja());
        pedido.agregarProducto(new RolloMakiBase("California", 8.00));

        assertDoesNotThrow(() -> {
            pedido.avanzar();
            pedido.avanzar();
        });
        assertDoesNotThrow(() -> new Pedido().cancelar());
    }

    @Test
    @DisplayName("No se puede suscribir un observer null")
    void suscribirNullLanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> pedido.suscribir(null));
    }

    /** Observer de prueba que registra lo que recibe. */
    private static final class ObserverEspia implements PedidoObserver {

        private int notificaciones;
        private Pedido ultimoPedido;
        private final List<EstadoPedido> estadosRecibidos = new ArrayList<>();

        @Override
        public void actualizar(Pedido p) {
            notificaciones++;
            ultimoPedido = p;
            estadosRecibidos.add(p.getEstado());
        }
    }
}
