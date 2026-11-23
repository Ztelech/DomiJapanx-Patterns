package vista;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import modelo.DescuentoHoraFelizSushi;
import modelo.ExtraSalsaAnguila;
import modelo.Pedido;
import modelo.ToppingTempura;

/**
 * Vista de consola interactiva.
 *
 * Muestra la carta, el pedido y las notificaciones con System.out, y ofrece un
 * menú en bucle (Scanner) para operar el pedido en vivo. La vista no contiene
 * lógica de negocio: cada opción del menú solo recoge los datos del usuario y
 * llama a una de las {@link Acciones}, que Main conecta con PedidoController.
 */
public class ConsolaView implements IVista {

    /** Máximo de extras de salsa que se ofrece en el menú (límite de interfaz). */
    private static final int MAX_EXTRAS_SALSA = 5;

    /**
     * Acciones que el usuario puede disparar desde la consola.
     * La implementación (en Main) delega todo en PedidoController.
     */
    public interface Acciones {
        void agregarRollo(String tipo, boolean conTempura, int extrasSalsaAnguila);

        void agregarComboBentoBox();

        void cambiarHoraFeliz(boolean activar);

        void deshacer();

        void avanzarEstado();

        void cancelarPedido();

        void pagar(String metodoPago);

        void nuevoPedido();
    }

    private final Scanner scanner;
    private final Map<String, Double> cartaRollos;
    private final List<String> metodosPago;
    private Acciones acciones;

    /** Último pedido mostrado: solo se lee para pintar el encabezado del menú. */
    private Pedido ultimoPedido;

    /** Constructor sin menú interactivo (solo muestra), útil para pruebas o demos. */
    public ConsolaView() {
        this(new Scanner(System.in), Collections.<String, Double>emptyMap(), Collections.<String>emptyList());
    }

    /**
     * @param scanner     lector de la entrada estándar (compartido con Main para no perder entrada).
     * @param cartaRollos nombres y precios base de los rollos que se ofrecen en el menú.
     * @param metodosPago métodos de pago que se ofrecen al facturar.
     */
    public ConsolaView(Scanner scanner, Map<String, Double> cartaRollos, List<String> metodosPago) {
        this.scanner = Objects.requireNonNull(scanner, "El scanner no puede ser null.");
        this.cartaRollos = Objects.requireNonNull(cartaRollos, "La carta no puede ser null.");
        this.metodosPago = Objects.requireNonNull(metodosPago, "Los métodos de pago no pueden ser null.");
    }

    public void setAcciones(Acciones acciones) {
        this.acciones = Objects.requireNonNull(acciones, "Las acciones no pueden ser null.");
    }

    // =====================================================================
    // IVista
    // =====================================================================

    @Override
    public void mostrarMenu() {
        System.out.println(FormatoPedido.carta());
    }

    @Override
    public void mostrarDetallePedido(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        this.ultimoPedido = p;
        System.out.println(FormatoPedido.detalle(p));
    }

    @Override
    public void mostrarAlertaNotificacion(String msj) {
        System.out.println(">>> NOTIFICACIÓN: " + msj);
    }

    @Override
    public void mostrarFactura(Pedido p, String metodoPago) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        System.out.println();
        System.out.println(FormatoPedido.factura(p, metodoPago));
    }

    // =====================================================================
    // Menú interactivo
    // =====================================================================

    /**
     * Bucle principal del menú. Termina cuando el usuario elige "Salir"
     * o cuando se acaba la entrada estándar.
     */
    public void ejecutarMenu() {
        if (acciones == null) {
            throw new IllegalStateException("Primero hay que conectar las acciones con setAcciones(...).");
        }
        boolean continuar = true;
        while (continuar) {
            imprimirOpciones();
            Integer opcion = leerEntero("Elige una opción: ", 0, 10);
            if (opcion == null) {
                break; // fin de la entrada
            }
            System.out.println();
            switch (opcion) {
                case 1:
                    menuAgregarRollo();
                    break;
                case 2:
                    acciones.agregarComboBentoBox();
                    break;
                case 3:
                    acciones.cambiarHoraFeliz(!horaFelizActiva());
                    break;
                case 4:
                    acciones.deshacer();
                    break;
                case 5:
                    acciones.avanzarEstado();
                    break;
                case 6:
                    acciones.cancelarPedido();
                    break;
                case 7:
                    menuPagar();
                    break;
                case 8:
                    acciones.nuevoPedido();
                    break;
                case 9:
                    mostrarMenu();
                    break;
                case 10:
                    if (ultimoPedido != null) {
                        mostrarDetallePedido(ultimoPedido);
                    }
                    break;
                case 0:
                default:
                    continuar = false;
                    break;
            }
        }
        System.out.println("¡Gracias por usar Cocina Oculta! Hasta pronto.");
    }

    private void imprimirOpciones() {
        String encabezado = ultimoPedido == null
                ? "MENÚ"
                : String.format("MENÚ - Pedido #%d [%s]", ultimoPedido.getId(), ultimoPedido.getEstado().getNombre());
        System.out.println();
        System.out.println("==================== " + encabezado + " ====================");
        System.out.println("  1. Agregar Rollo Maki            (Decorator + Command)");
        System.out.println("  2. Agregar Combo Bento Box       (Builder + Command)");
        System.out.println("  3. " + (horaFelizActiva() ? "Desactivar" : "Activar   ")
                + " Hora Feliz -10%     (Strategy)");
        System.out.println("  4. Deshacer última acción        (Command)");
        System.out.println("  5. Avanzar estado del pedido     (State + Observer)");
        System.out.println("  6. Cancelar pedido               (State + Observer)");
        System.out.println("  7. Pagar y emitir factura");
        System.out.println("  8. Nuevo pedido");
        System.out.println("  9. Ver carta");
        System.out.println(" 10. Ver pedido actual");
        System.out.println("  0. Salir");
    }

    private void menuPagar() {
        if (metodosPago.isEmpty()) {
            mostrarAlertaNotificacion("No hay métodos de pago configurados.");
            return;
        }
        System.out.println("--- Método de pago ---");
        for (int i = 0; i < metodosPago.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, metodosPago.get(i));
        }
        System.out.println("  0. Volver");
        Integer eleccion = leerEntero("Método: ", 0, metodosPago.size());
        if (eleccion == null || eleccion == 0) {
            return;
        }
        acciones.pagar(metodosPago.get(eleccion - 1));
    }

    private void menuAgregarRollo() {
        List<String> tipos = new ArrayList<>(cartaRollos.keySet());
        if (tipos.isEmpty()) {
            mostrarAlertaNotificacion("No hay rollos en la carta.");
            return;
        }
        System.out.println("--- Elige el Rollo Maki ---");
        for (int i = 0; i < tipos.size(); i++) {
            String tipo = tipos.get(i);
            System.out.printf("  %d. %-14s $%.2f%n", i + 1, tipo, cartaRollos.get(tipo));
        }
        System.out.println("  0. Volver");
        Integer eleccion = leerEntero("Rollo: ", 0, tipos.size());
        if (eleccion == null || eleccion == 0) {
            return;
        }
        String tipo = tipos.get(eleccion - 1);

        Boolean conTempura = leerSiNo(String.format(
                "¿Agregar Topping Tempura (+$%.2f)? (s/n): ", ToppingTempura.getCostoAdicion()));
        if (conTempura == null) {
            return;
        }
        Integer salsas = leerEntero(String.format(
                "¿Cuántos Extra Salsa de Anguila (+$%.2f c/u)? (0-%d): ",
                ExtraSalsaAnguila.getCostoAdicion(), MAX_EXTRAS_SALSA), 0, MAX_EXTRAS_SALSA);
        if (salsas == null) {
            return;
        }
        acciones.agregarRollo(tipo, conTempura, salsas);
    }

    // =====================================================================
    // Auxiliares de lectura / presentación
    // =====================================================================

    /** Solo lectura del modelo para decidir el texto del menú (Activar / Desactivar). */
    private boolean horaFelizActiva() {
        return ultimoPedido != null && ultimoPedido.getDescuento() instanceof DescuentoHoraFelizSushi;
    }

    /** Lee un entero entre min y max; repite hasta que sea válido. Devuelve null si se acaba la entrada. */
    private Integer leerEntero(String mensaje, int min, int max) {
        while (true) {
            System.out.print(mensaje);
            if (!scanner.hasNextLine()) {
                return null;
            }
            String linea = scanner.nextLine().trim();
            try {
                int valor = Integer.parseInt(linea);
                if (valor >= min && valor <= max) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // se vuelve a pedir
            }
            System.out.printf("  Opción no válida. Escribe un número entre %d y %d.%n", min, max);
        }
    }

    /** Lee s/n; repite hasta que sea válido. Devuelve null si se acaba la entrada. */
    private Boolean leerSiNo(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            if (!scanner.hasNextLine()) {
                return null;
            }
            String linea = scanner.nextLine().trim().toLowerCase();
            if (linea.equals("s") || linea.equals("si") || linea.equals("sí")) {
                return true;
            }
            if (linea.equals("n") || linea.equals("no")) {
                return false;
            }
            System.out.println("  Responde 's' o 'n'.");
        }
    }
}
