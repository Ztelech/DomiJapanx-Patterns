package controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import modelo.ComandoAgregarProducto;
import modelo.Pedido;
import modelo.Producto;
import modelo.RolloMakiBase;
import vista.IVista;

/**
 * Pruebas del patrón Command: ComandoAgregarProducto, HistorialComandos
 * y su uso desde PedidoController.
 */
class CommandTest {

    private static final double DELTA = 0.0001;

    private Pedido pedido;
    private HistorialComandos historial;
    private Producto california;
    private Producto kappa;

    @BeforeEach
    void setUp() {
        pedido = new Pedido();
        historial = new HistorialComandos();
        california = new RolloMakiBase("California", 8.00);
        kappa = new RolloMakiBase("Kappa", 6.00);
    }

    @Test
    @DisplayName("guardarComando ejecuta el comando y lo apila")
    void guardarComandoEjecutaYApila() {
        historial.guardarComando(new ComandoAgregarProducto(pedido, california));

        assertEquals(1, pedido.getCantidadArticulos());
        assertEquals(1, historial.getCantidadComandos());
        assertTrue(historial.hayComandosParaDeshacer());
    }

    @Test
    @DisplayName("deshacerUltimo quita el producto agregado")
    void deshacerQuitaProducto() {
        historial.guardarComando(new ComandoAgregarProducto(pedido, california));

        assertTrue(historial.deshacerUltimo());

        assertEquals(0, pedido.getCantidadArticulos());
        assertEquals(0.0, pedido.calcularTotal(), DELTA);
        assertFalse(historial.hayComandosParaDeshacer());
    }

    @Test
    @DisplayName("Deshacer sigue el orden LIFO")
    void deshacerEsLifo() {
        historial.guardarComando(new ComandoAgregarProducto(pedido, california));
        historial.guardarComando(new ComandoAgregarProducto(pedido, kappa));

        historial.deshacerUltimo();

        assertEquals(1, pedido.getProductos().size());
        assertSame(california, pedido.getProductos().get(0));
    }

    @Test
    @DisplayName("Deshacer con el historial vacío devuelve false")
    void deshacerHistorialVacio() {
        assertFalse(historial.deshacerUltimo());
    }

    @Test
    @DisplayName("Deshacer quita la instancia exacta aunque haya otra igual")
    void deshacerQuitaInstanciaExacta() {
        Producto otraCalifornia = new RolloMakiBase("California", 8.00);
        historial.guardarComando(new ComandoAgregarProducto(pedido, california));
        historial.guardarComando(new ComandoAgregarProducto(pedido, otraCalifornia));

        historial.deshacerUltimo();

        assertSame(california, pedido.getProductos().get(0));
    }

    @Test
    @DisplayName("Un comando no se puede ejecutar dos veces ni deshacer sin ejecutar")
    void reglasDelComando() {
        ComandoAgregarProducto comando = new ComandoAgregarProducto(pedido, california);

        assertThrows(IllegalStateException.class, comando::deshacer);

        comando.ejecutar();
        assertTrue(comando.isEjecutado());
        assertThrows(IllegalStateException.class, comando::ejecutar);
    }

    @Test
    @DisplayName("Si ejecutar() falla, el comando no se guarda en el historial")
    void comandoFallidoNoSeApila() {
        pedido.avanzar(); // EN PREPARACIÓN: ya no admite cambios

        assertThrows(IllegalStateException.class,
                () -> historial.guardarComando(new ComandoAgregarProducto(pedido, california)));
        assertEquals(0, historial.getCantidadComandos());
    }

    @Test
    @DisplayName("Validaciones: comando y argumentos null")
    void validaciones() {
        assertThrows(NullPointerException.class, () -> historial.guardarComando(null));
        assertThrows(NullPointerException.class, () -> new ComandoAgregarProducto(null, california));
        assertThrows(NullPointerException.class, () -> new ComandoAgregarProducto(pedido, null));
    }

    @Test
    @DisplayName("El controlador agrega y deshace mediante comandos")
    void controladorAgregaYDeshace() {
        VistaFalsa vista = new VistaFalsa();
        PedidoController controlador = new PedidoController(pedido, vista);

        controlador.agregarProducto(controlador.crearComboBentoBox());
        controlador.agregarProducto(controlador.crearRolloMaki("Kappa", 6.00, false, 0));
        controlador.deshacerUltimaAccion();

        assertEquals(1, pedido.getCombos().size());
        assertTrue(pedido.getProductos().isEmpty());
        assertTrue(vista.alertas.contains("Se deshizo la última acción."));
    }

    @Test
    @DisplayName("El controlador no deshace cambios en un pedido que ya avanzó")
    void controladorNoDeshaceTrasAvanzar() {
        VistaFalsa vista = new VistaFalsa();
        PedidoController controlador = new PedidoController(pedido, vista);
        controlador.agregarProducto(california);
        controlador.avanzarEstado();

        controlador.deshacerUltimaAccion();

        assertEquals(1, pedido.getCantidadArticulos());
        assertTrue(vista.ultimaAlerta().contains("no se pueden deshacer"));
    }

    /** Vista de prueba que guarda las alertas en vez de mostrarlas. */
    private static final class VistaFalsa implements IVista {

        private final List<String> alertas = new ArrayList<>();

        @Override
        public void mostrarMenu() {
            // No hace nada en las pruebas.
        }

        @Override
        public void mostrarDetallePedido(Pedido p) {
            // No hace nada en las pruebas.
        }

        @Override
        public void mostrarAlertaNotificacion(String msj) {
            alertas.add(msj);
        }

        String ultimaAlerta() {
            return alertas.get(alertas.size() - 1);
        }
    }
}
