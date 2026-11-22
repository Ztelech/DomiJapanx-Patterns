package modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del patrón Builder: Combo, ComboBuilder, BentoBoxBuilder y DirectorArmado.
 */
class BuilderTest {

    private static final double DELTA = 0.0001;

    @Test
    @DisplayName("El director arma un Bento Box completo")
    void directorArmaComboCompleto() {
        Combo bento = new DirectorArmado(new BentoBoxBuilder()).armarCombo();

        assertTrue(bento.estaCompleto());
        assertNotNull(bento.getPlatoPrincipal());
        assertNotNull(bento.getBebida());
        assertNotNull(bento.getAcompanamiento());
        assertEquals("Bento Box", bento.getNombre());
    }

    @Test
    @DisplayName("El precio del combo es la suma de sus partes")
    void precioEsSumaDeLasPartes() {
        Combo bento = new DirectorArmado(new BentoBoxBuilder()).armarCombo();

        double esperado = bento.getPlatoPrincipal().getPrecio()
                + bento.getBebida().getPrecio()
                + bento.getAcompanamiento().getPrecio();

        assertEquals(esperado, bento.getPrecio(), DELTA);
        assertEquals(17.00, bento.getPrecio(), DELTA); // 9.50 + 2.00 + 2.50 + 3.00
    }

    @Test
    @DisplayName("La descripción del combo incluye sus tres partes")
    void descripcionIncluyePartes() {
        String descripcion = new DirectorArmado(new BentoBoxBuilder()).armarCombo().getDescripcion();

        assertTrue(descripcion.contains("Rollo Maki Philadelphia + Topping Tempura"));
        assertTrue(descripcion.contains("Té Verde"));
        assertTrue(descripcion.contains("Sopa Miso"));
    }

    @Test
    @DisplayName("El builder se reinicia: cada combo es una instancia nueva")
    void builderSeReutiliza() {
        DirectorArmado director = new DirectorArmado(new BentoBoxBuilder());

        Combo primero = director.armarCombo();
        Combo segundo = director.armarCombo();

        assertNotSame(primero, segundo);
        assertEquals(primero.getPrecio(), segundo.getPrecio(), DELTA);
    }

    @Test
    @DisplayName("getCombo() falla si faltan pasos de construcción")
    void comboIncompletoLanzaExcepcion() {
        BentoBoxBuilder builder = new BentoBoxBuilder();
        builder.buildPlato(); // faltan bebida y acompañamiento

        assertThrows(IllegalStateException.class, builder::getCombo);
    }

    @Test
    @DisplayName("Un combo a medio armar no está completo y sus partes vacías suman 0")
    void comboVacioNoEstaCompleto() {
        Combo combo = new Combo("Vacío");

        assertFalse(combo.estaCompleto());
        assertEquals(0.0, combo.getPrecio(), DELTA);
    }

    @Test
    @DisplayName("El director permite cambiar de builder")
    void directorCambiaDeBuilder() {
        DirectorArmado director = new DirectorArmado(new BentoBoxBuilder());
        ComboBuilder otro = new BentoBoxBuilder();

        director.setBuilder(otro);

        assertSame(otro, director.getBuilder());
    }

    @Test
    @DisplayName("Validaciones: builder null y nombre de combo vacío")
    void validaciones() {
        assertThrows(NullPointerException.class, () -> new DirectorArmado(null));
        assertThrows(IllegalArgumentException.class, () -> new Combo(" "));
    }

    @Test
    @DisplayName("Un combo es un Producto y se puede decorar")
    void comboEsProductoDecorable() {
        Combo bento = new DirectorArmado(new BentoBoxBuilder()).armarCombo();

        Producto conSalsa = new ExtraSalsaAnguila(bento);

        assertEquals(bento.getPrecio() + ExtraSalsaAnguila.getCostoAdicion(), conSalsa.getPrecio(), DELTA);
    }
}
