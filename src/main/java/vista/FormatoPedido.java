package vista;

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
}
