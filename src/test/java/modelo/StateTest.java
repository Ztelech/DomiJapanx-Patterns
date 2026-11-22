package modelo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del patrón State: transiciones de EstadoPedido sobre el contexto Pedido.
 */
class StateTest {

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
    }

    @Test
    @DisplayName("Un pedido nuevo empieza en RECIBIDO")
    void estadoInicialEsRecibido() {
        assertInstanceOf(EstadoRecibido.class, pedido.getEstado());
        assertTrue(pedido.permiteModificaciones());
    }

    @Test
    @DisplayName("Flujo completo: RECIBIDO -> EN PREPARACIÓN -> DESPACHADO")
    void flujoCompletoDeAvance() {
        pedido.avanzar();
        assertInstanceOf(EstadoPreparacion.class, pedido.getEstado());

        pedido.avanzar();
        assertInstanceOf(EstadoDespachado.class, pedido.getEstado());
    }

    @Test
    @DisplayName("DESPACHADO es final: no avanza")
    void despachadoNoAvanza() {
        pedido.avanzar();
        pedido.avanzar();
        EstadoPedido despachado = pedido.getEstado();

        pedido.avanzar();

        assertSame(despachado, pedido.getEstado());
    }

    @Test
    @DisplayName("DESPACHADO no se puede cancelar")
    void despachadoNoSeCancela() {
        pedido.avanzar();
        pedido.avanzar();

        pedido.cancelar();

        assertInstanceOf(EstadoDespachado.class, pedido.getEstado());
    }

    @Test
    @DisplayName("Se puede cancelar desde RECIBIDO")
    void cancelarDesdeRecibido() {
        pedido.cancelar();

        assertInstanceOf(EstadoCancelado.class, pedido.getEstado());
    }

    @Test
    @DisplayName("Se puede cancelar desde EN PREPARACIÓN")
    void cancelarDesdePreparacion() {
        pedido.avanzar();

        pedido.cancelar();

        assertInstanceOf(EstadoCancelado.class, pedido.getEstado());
    }

    @Test
    @DisplayName("Un pedido CANCELADO no avanza ni se vuelve a cancelar")
    void canceladoEsFinal() {
        pedido.cancelar();
        EstadoPedido cancelado = pedido.getEstado();

        pedido.avanzar();
        pedido.cancelar();

        assertSame(cancelado, pedido.getEstado());
    }

    @Test
    @DisplayName("Solo se puede modificar el pedido en RECIBIDO")
    void soloRecibidoPermiteModificaciones() {
        pedido.avanzar();
        assertFalse(pedido.permiteModificaciones());

        Producto rollo = new RolloMakiBase("California", 8.00);
        assertThrows(IllegalStateException.class, () -> pedido.agregarProducto(rollo));
    }

    @Test
    @DisplayName("Los estados rechazan un pedido null")
    void estadosRechazanNull() {
        assertThrows(NullPointerException.class, () -> new EstadoRecibido().avanzar(null));
        assertThrows(NullPointerException.class, () -> new EstadoPreparacion().cancelar(null));
    }
}
