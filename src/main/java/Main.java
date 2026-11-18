import java.util.Scanner;

import controlador.PedidoController;
import modelo.Combo;
import modelo.DescuentoHoraFelizSushi;
import modelo.PantallaBarraSushi;
import modelo.PantallaDespachador;
import modelo.Pedido;
import modelo.Producto;
import modelo.SistemaCaja;
import vista.ConsolaView;
import vista.GuiView;
import vista.IVista;

/**
 * Ensambla el MVC y ejecuta una demostración de los 6 patrones:
 * Decorator, Builder, Strategy, State, Command y Observer.
 *
 * Uso:  java Main consola   |   java Main gui   |   java Main  (pregunta)
 */
public class Main {

    public static void main(String[] args) {
        boolean usarGui = elegirGui(args);

        // ---------- Modelo + observers ----------
        Pedido pedido = new Pedido();
        pedido.suscribir(new PantallaBarraSushi());
        pedido.suscribir(new PantallaDespachador());
        pedido.suscribir(new SistemaCaja());

        // ---------- Vista ----------
        IVista vista;
        GuiView gui = null;
        if (usarGui) {
            gui = new GuiView();
            vista = gui;
        } else {
            vista = new ConsolaView();
        }

        // ---------- Controlador ----------
        PedidoController controlador = new PedidoController(pedido, vista);
        if (gui != null) {
            gui.setAcciones(controlador::avanzarEstado,
                            controlador::cancelarPedido,
                            controlador::deshacerUltimaAccion);
        }

        controlador.iniciar();

        // ---------- Demo ----------
        titulo("1. BUILDER: armando un combo Bento Box");
        Combo bento = controlador.crearComboBentoBox();
        controlador.agregarProducto(bento);

        titulo("2. DECORATOR: Rollo Dragón + Tempura + Extra Salsa de Anguila");
        Producto dragon = controlador.crearRolloMaki("Dragón", 10.00, true, 1);
        controlador.agregarProducto(dragon);

        titulo("3. COMMAND: se agrega un Kappa por error y se deshace");
        Producto kappa = controlador.crearRolloMaki("Kappa", 6.00, false, 0);
        controlador.agregarProducto(kappa);
        controlador.deshacerUltimaAccion();

        titulo("4. STRATEGY: se aplica el descuento de Hora Feliz");
        controlador.aplicarDescuento(new DescuentoHoraFelizSushi());

        if (usarGui) {
            vista.mostrarAlertaNotificacion(
                    "Demo cargada. Usa los botones para avanzar el estado y ver los observers en la consola.");
            return; // la ventana sigue abierta y responde a los botones
        }

        titulo("5. STATE + OBSERVER: RECIBIDO -> EN PREPARACIÓN");
        controlador.avanzarEstado();

        titulo("6. STATE + OBSERVER: EN PREPARACIÓN -> DESPACHADO");
        controlador.avanzarEstado();

        titulo("7. Transiciones no válidas sobre un pedido DESPACHADO");
        controlador.cancelarPedido();
        controlador.deshacerUltimaAccion();
        controlador.agregarProducto(controlador.crearRolloMaki("California", 8.00, false, 0));
    }

    private static boolean elegirGui(String[] args) {
        if (args.length > 0) {
            return args[0].equalsIgnoreCase("gui");
        }
        System.out.print("Elige la vista (1 = Consola, 2 = GUI): ");
        Scanner scanner = new Scanner(System.in);
        String opcion = scanner.hasNextLine() ? scanner.nextLine().trim() : "1";
        return opcion.equals("2");
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("==================== " + texto + " ====================");
    }
}
