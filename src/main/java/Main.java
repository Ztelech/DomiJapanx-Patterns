import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import controlador.PedidoController;
import modelo.DescuentoHoraFelizSushi;
import modelo.PantallaBarraSushi;
import modelo.PantallaDespachador;
import modelo.Pedido;
import modelo.SinDescuento;
import modelo.SistemaCaja;
import vista.ConsolaView;
import vista.GuiView;
import vista.IVista;

/**
 * Punto de entrada. Ensambla el MVC y deja el sistema listo para operarlo en vivo:
 *   1 = menú interactivo en consola
 *   2 = ventana Swing interactiva
 *
 * Uso:  java Main consola   |   java Main gui   |   java Main  (pregunta)
 *
 * Main es la "raíz de composición": crea el modelo, la vista y el controlador,
 * y conecta las acciones del usuario (botones / opciones del menú) con los
 * métodos de PedidoController. Las vistas nunca tocan el modelo directamente.
 */
public class Main {

    /** Carta de rollos maki: nombre -> precio base. Se usa para mostrar y para crear rollos. */
    private static final Map<String, Double> CARTA_ROLLOS;

    static {
        Map<String, Double> carta = new LinkedHashMap<>();
        carta.put("California", 8.00);
        carta.put("Philadelphia", 9.50);
        carta.put("Dragón", 10.00);
        carta.put("Kappa", 6.00);
        CARTA_ROLLOS = Collections.unmodifiableMap(carta);
    }

    /** Métodos de pago que se ofrecen al facturar. */
    private static final List<String> METODOS_PAGO = Collections.unmodifiableList(Arrays.asList(
            "Efectivo",
            "Tarjeta de Crédito/Débito",
            "Nequi / Transferencia"));

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        if (elegirGui(args, scanner)) {
            SwingUtilities.invokeLater(Main::iniciarGui);
        } else {
            iniciarConsola(scanner);
        }
    }

    // =====================================================================
    // Arranque de cada modo
    // =====================================================================

    private static void iniciarConsola(Scanner scanner) {
        ConsolaView consola = new ConsolaView(scanner, CARTA_ROLLOS, METODOS_PAGO);
        PedidoController controlador = crearControlador(consola);
        consola.setAcciones(new AccionesControlador(controlador));

        controlador.iniciar();
        consola.ejecutarMenu(); // bucle interactivo hasta que el usuario elige "Salir"
    }

    private static void iniciarGui() {
        GuiView gui = new GuiView(CARTA_ROLLOS, METODOS_PAGO);
        PedidoController controlador = crearControlador(gui);
        gui.setAcciones(new AccionesControlador(controlador));

        controlador.iniciar(); // muestra la ventana con el pedido vacío
    }

    /**
     * Crea el primer pedido y el controlador, y registra las pantallas a través
     * del controlador para que se vuelvan a suscribir en cada pedido nuevo.
     */
    private static PedidoController crearControlador(IVista vista) {
        PedidoController controlador = new PedidoController(new Pedido(), vista);
        controlador.registrarObserver(new PantallaBarraSushi());
        controlador.registrarObserver(new PantallaDespachador());
        controlador.registrarObserver(new SistemaCaja());
        return controlador;
    }

    private static boolean elegirGui(String[] args, Scanner scanner) {
        if (args.length > 0) {
            return args[0].equalsIgnoreCase("gui") || args[0].equals("2");
        }
        System.out.print("Elige la vista (1 = Consola, 2 = GUI): ");
        String opcion = scanner.hasNextLine() ? scanner.nextLine().trim() : "1";
        return opcion.equals("2");
    }

    // =====================================================================
    // Puente vista -> controlador
    // =====================================================================

    /**
     * Traduce cada acción del usuario en una llamada a PedidoController.
     * Implementa las dos interfaces de acciones (consola y GUI), que tienen
     * los mismos métodos, así ambas vistas comparten exactamente el mismo flujo.
     */
    private static final class AccionesControlador implements ConsolaView.Acciones, GuiView.Acciones {

        private final PedidoController controlador;

        AccionesControlador(PedidoController controlador) {
            this.controlador = Objects.requireNonNull(controlador, "El controlador no puede ser null.");
        }

        @Override
        public void agregarRollo(String tipo, boolean conTempura, int extrasSalsaAnguila) {
            Double precio = CARTA_ROLLOS.get(tipo);
            if (precio == null) {
                throw new IllegalArgumentException("Rollo desconocido: " + tipo);
            }
            controlador.agregarProducto(
                    controlador.crearRolloMaki(tipo, precio, conTempura, extrasSalsaAnguila));
        }

        @Override
        public void agregarComboBentoBox() {
            controlador.agregarProducto(controlador.crearComboBentoBox());
        }

        @Override
        public void cambiarHoraFeliz(boolean activar) {
            controlador.aplicarDescuento(activar ? new DescuentoHoraFelizSushi() : new SinDescuento());
        }

        @Override
        public void deshacer() {
            controlador.deshacerUltimaAccion();
        }

        @Override
        public void avanzarEstado() {
            controlador.avanzarEstado();
        }

        @Override
        public void cancelarPedido() {
            controlador.cancelarPedido();
        }

        @Override
        public void pagar(String metodoPago) {
            controlador.pagarPedido(metodoPago);
        }

        @Override
        public void nuevoPedido() {
            controlador.nuevoPedido();
        }
    }
}
