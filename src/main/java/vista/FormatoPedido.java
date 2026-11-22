package vista;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import modelo.Combo;
import modelo.ExtraSalsaAnguila;
import modelo.Pedido;
import modelo.Producto;
import modelo.ToppingTempura;

/**
 * Utilidad del paquete 'vista' que arma los textos de la carta y del detalle.
 * Las dos vistas la usan para no repetir el formato.
 * Es package-private: no se ve fuera de 'vista'.
 */
final class FormatoPedido {

    private FormatoPedido() {
        // Clase de utilidad: no se instancia.
    }

    static String carta() {
        StringBuilder sb = new StringBuilder();
        sb.append("================ COCINA OCULTA - CARTA ================\n");
        sb.append("Rollos Maki\n");
        sb.append("  California ................ $8.00\n");
        sb.append("  Philadelphia .............. $9.50\n");
        sb.append("  Dragón .................... $10.00\n");
        sb.append("  Kappa ..................... $6.00\n");
        sb.append("Adiciones\n");
        sb.append(String.format("  Topping Tempura ........... +$%.2f\n",
                ToppingTempura.getCostoAdicion()));
        sb.append(String.format("  Extra Salsa de Anguila .... +$%.2f\n",
                ExtraSalsaAnguila.getCostoAdicion()));
        sb.append("Combos\n");
        sb.append("  Bento Box (Philadelphia + Tempura, Té Verde, Sopa Miso)\n");
        sb.append("Promociones\n");
        sb.append("  Hora Feliz Sushi .......... -10%\n");
        sb.append("=======================================================\n");
        return sb.toString();
    }

    static String detalle(Pedido p) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("------------------ PEDIDO #%d ------------------\n", p.getId()));
        sb.append(String.format("Estado: %s\n", p.getEstado().getNombre()));

        sb.append("Combos:\n");
        if (p.getCombos().isEmpty()) {
            sb.append("  (ninguno)\n");
        }
        for (Combo combo : p.getCombos()) {
            sb.append(String.format("  - %s  $%.2f\n", combo.getDescripcion(), combo.getPrecio()));
        }

        sb.append("Productos:\n");
        if (p.getProductos().isEmpty()) {
            sb.append("  (ninguno)\n");
        }
        for (Producto producto : p.getProductos()) {
            sb.append(String.format("  - %s  $%.2f\n", producto.getDescripcion(), producto.getPrecio()));
        }

        sb.append(String.format("Subtotal:  $%.2f\n", p.calcularSubtotal()));
        sb.append(String.format("Descuento: %s (-$%.2f)\n", p.getDescuento(), p.calcularAhorro()));
        sb.append(String.format("TOTAL:     $%.2f\n", p.calcularTotal()));
        sb.append("------------------------------------------------\n");
        return sb.toString();
    }

    // =====================================================================
    // Factura
    // =====================================================================

    /** Ancho total de la factura en caracteres (pensada para fuente monoespaciada). */
    private static final int ANCHO_FACTURA = 52;
    /** Ancho de la columna de descripción; el resto es para el precio. */
    private static final int ANCHO_DESCRIPCION = 38;

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss");

    /**
     * Arma el recibo de un pedido pagado: encabezado, número de pedido, fecha y hora,
     * combos y rollos, subtotal, descuento (Strategy), método de pago y total pagado.
     */
    static String factura(Pedido p, String metodoPago) {
        String doble = repetir('=', ANCHO_FACTURA) + "\n";
        String simple = repetir('-', ANCHO_FACTURA) + "\n";

        StringBuilder sb = new StringBuilder();
        sb.append(doble);
        sb.append(centrar("COCINA OCULTA")).append('\n');
        sb.append(centrar("Sushi a domicilio")).append('\n');
        sb.append(centrar("FACTURA DE VENTA")).append('\n');
        sb.append(doble);
        sb.append(String.format("Pedido #:        %d%n", p.getId()));
        sb.append(String.format("Fecha y hora:    %s%n", LocalDateTime.now().format(FORMATO_FECHA)));
        sb.append(String.format("Estado:          %s%n", p.getEstado().getNombre()));
        sb.append(simple);

        sb.append("COMBOS\n");
        if (p.getCombos().isEmpty()) {
            sb.append("  (ninguno)\n");
        }
        for (Combo combo : p.getCombos()) {
            lineaArticulo(sb, combo.getDescripcion(), combo.getPrecio());
        }
        sb.append("ROLLOS\n");
        if (p.getProductos().isEmpty()) {
            sb.append("  (ninguno)\n");
        }
        for (Producto producto : p.getProductos()) {
            lineaArticulo(sb, producto.getDescripcion(), producto.getPrecio());
        }

        sb.append(simple);
        sb.append(columnas("Subtotal:", String.format("$%.2f", p.calcularSubtotal())));
        sb.append(columnas("Descuento: " + p.getDescuento(), String.format("-$%.2f", p.calcularAhorro())));
        sb.append(simple);
        sb.append(columnas("Método de pago:", metodoPago));
        sb.append(columnas("TOTAL PAGADO:", String.format("$%.2f", p.calcularTotal())));
        sb.append(doble);
        sb.append(centrar("¡Gracias por su compra!")).append('\n');
        sb.append(centrar("Vuelva pronto")).append('\n');
        sb.append(doble);
        return sb.toString();
    }

    /** Escribe un artículo: la descripción partida en varias líneas y el precio en la primera. */
    private static void lineaArticulo(StringBuilder sb, String descripcion, double precio) {
        String[] lineas = partir(descripcion, ANCHO_DESCRIPCION - 2);
        String textoPrecio = String.format("$%.2f", precio);
        sb.append(String.format("  %-" + (ANCHO_DESCRIPCION - 2) + "s%" + (ANCHO_FACTURA - ANCHO_DESCRIPCION) + "s%n",
                lineas[0], textoPrecio));
        for (int i = 1; i < lineas.length; i++) {
            sb.append("    ").append(lineas[i]).append('\n');
        }
    }

    /** Texto a la izquierda y valor alineado a la derecha en una sola línea. */
    private static String columnas(String izquierda, String derecha) {
        int espacios = Math.max(1, ANCHO_FACTURA - izquierda.length() - derecha.length());
        return izquierda + repetir(' ', espacios) + derecha + "\n";
    }

    private static String centrar(String texto) {
        int margen = Math.max(0, (ANCHO_FACTURA - texto.length()) / 2);
        return repetir(' ', margen) + texto;
    }

    /** Parte un texto por palabras en líneas de como máximo 'ancho' caracteres. */
    private static String[] partir(String texto, int ancho) {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.trim().split("\\s+")) {
            if (actual.length() > 0 && actual.length() + 1 + palabra.length() > ancho) {
                lineas.add(actual.toString());
                actual.setLength(0);
            }
            if (actual.length() > 0) {
                actual.append(' ');
            }
            actual.append(palabra);
        }
        lineas.add(actual.toString());
        return lineas.toArray(new String[0]);
    }

    /** Equivalente a String.repeat(), que no existe en Java 8. */
    private static String repetir(char c, int veces) {
        char[] arr = new char[Math.max(0, veces)];
        Arrays.fill(arr, c);
        return new String(arr);
    }
}
