package modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del patrón Strategy: DescuentoStrategy y sus implementaciones.
 */
class StrategyTest {

    private static final double DELTA = 0.0001;

    @Test
    @DisplayName("SinDescuento devuelve el subtotal sin cambios")
    void sinDescuentoNoCambiaSubtotal() {
        DescuentoStrategy estrategia = new SinDescuento();

        assertEquals(100.00, estrategia.aplicarDescuento(100.00), DELTA);
        assertEquals(0.00, estrategia.aplicarDescuento(0.00), DELTA);
    }

    @Test
    @DisplayName("La Hora Feliz descuenta un 10% por defecto")
    void horaFelizDescuentaDiezPorCiento() {
        DescuentoStrategy estrategia = new DescuentoHoraFelizSushi();

        assertEquals(90.00, estrategia.aplicarDescuento(100.00), DELTA);
        assertEquals(27.45, estrategia.aplicarDescuento(30.50), DELTA);
    }

    @Test
    @DisplayName("La Hora Feliz admite un porcentaje configurable")
    void horaFelizPorcentajeConfigurable() {
        DescuentoHoraFelizSushi estrategia = new DescuentoHoraFelizSushi(25);

        assertEquals(25.0, estrategia.getPorcentaje(), DELTA);
        assertEquals(75.00, estrategia.aplicarDescuento(100.00), DELTA);
    }

    @Test
    @DisplayName("Los límites 0% y 100% son válidos")
    void limitesDelPorcentaje() {
        assertEquals(50.00, new DescuentoHoraFelizSushi(0).aplicarDescuento(50.00), DELTA);
        assertEquals(0.00, new DescuentoHoraFelizSushi(100).aplicarDescuento(50.00), DELTA);
    }

    @Test
    @DisplayName("Porcentajes fuera de rango lanzan excepción")
    void porcentajeFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> new DescuentoHoraFelizSushi(-1));
        assertThrows(IllegalArgumentException.class, () -> new DescuentoHoraFelizSushi(101));
    }

    @Test
    @DisplayName("Un subtotal negativo lanza excepción en ambas estrategias")
    void subtotalNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new SinDescuento().aplicarDescuento(-5));
        assertThrows(IllegalArgumentException.class, () -> new DescuentoHoraFelizSushi().aplicarDescuento(-5));
    }

    @Test
    @DisplayName("El pedido usa SinDescuento por defecto y cambia de estrategia en ejecución")
    void pedidoCambiaDeEstrategia() {
        Pedido pedido = new Pedido();
        pedido.agregarProducto(new RolloMakiBase("California", 8.00));
        pedido.agregarProducto(new RolloMakiBase("Philadelphia", 12.00));

        assertInstanceOf(SinDescuento.class, pedido.getDescuento());
        assertEquals(20.00, pedido.calcularTotal(), DELTA);

        pedido.setDescuento(new DescuentoHoraFelizSushi());

        assertEquals(20.00, pedido.calcularSubtotal(), DELTA);
        assertEquals(18.00, pedido.calcularTotal(), DELTA);
        assertEquals(2.00, pedido.calcularAhorro(), DELTA);
    }

    @Test
    @DisplayName("El pedido no acepta una estrategia null")
    void pedidoRechazaEstrategiaNull() {
        assertThrows(NullPointerException.class, () -> new Pedido().setDescuento(null));
    }
}
