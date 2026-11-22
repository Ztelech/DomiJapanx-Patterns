package modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del patrón Decorator: Producto, RolloMakiBase y sus adiciones.
 */
class DecoratorTest {

    private static final double DELTA = 0.0001;

    @Test
    @DisplayName("Un rollo base devuelve su nombre y su precio sin cambios")
    void rolloBaseSinAdiciones() {
        Producto rollo = new RolloMakiBase("California", 8.00);

        assertEquals("Rollo Maki California", rollo.getDescripcion());
        assertEquals(8.00, rollo.getPrecio(), DELTA);
    }

    @Test
    @DisplayName("ToppingTempura suma su costo y su descripción")
    void toppingTempuraSumaCosto() {
        Producto rollo = new ToppingTempura(new RolloMakiBase("California", 8.00));

        assertEquals("Rollo Maki California + Topping Tempura", rollo.getDescripcion());
        assertEquals(8.00 + ToppingTempura.getCostoAdicion(), rollo.getPrecio(), DELTA);
    }

    @Test
    @DisplayName("ExtraSalsaAnguila suma su costo y su descripción")
    void extraSalsaAnguilaSumaCosto() {
        Producto rollo = new ExtraSalsaAnguila(new RolloMakiBase("Kappa", 6.00));

        assertEquals("Rollo Maki Kappa + Extra Salsa de Anguila", rollo.getDescripcion());
        assertEquals(6.00 + ExtraSalsaAnguila.getCostoAdicion(), rollo.getPrecio(), DELTA);
    }

    @Test
    @DisplayName("Los decoradores se apilan, incluso repitiendo la misma adición")
    void decoradoresSeApilan() {
        Producto rollo = new RolloMakiBase("California", 8.00);
        rollo = new ToppingTempura(rollo);
        rollo = new ExtraSalsaAnguila(rollo);
        rollo = new ExtraSalsaAnguila(rollo);

        assertEquals(13.00, rollo.getPrecio(), DELTA); // 8.00 + 2.00 + 1.50 + 1.50
        assertEquals("Rollo Maki California + Topping Tempura"
                + " + Extra Salsa de Anguila + Extra Salsa de Anguila", rollo.getDescripcion());
    }

    @Test
    @DisplayName("Un decorador sigue siendo un Producto")
    void decoradorEsProducto() {
        Producto rollo = new ToppingTempura(new RolloMakiBase("Dragón", 10.00));

        assertTrue(rollo instanceof AdicionDecorator);
        assertTrue(rollo instanceof Producto);
    }

    @Test
    @DisplayName("No se puede decorar un producto null")
    void decorarNullLanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> new ToppingTempura(null));
        assertThrows(NullPointerException.class, () -> new ExtraSalsaAnguila(null));
    }

    @Test
    @DisplayName("El rollo base rechaza nombre vacío o precio negativo")
    void rolloBaseValidaArgumentos() {
        assertThrows(IllegalArgumentException.class, () -> new RolloMakiBase("  ", 8.00));
        assertThrows(IllegalArgumentException.class, () -> new RolloMakiBase(null, 8.00));
        assertThrows(IllegalArgumentException.class, () -> new RolloMakiBase("California", -1.00));
    }
}
