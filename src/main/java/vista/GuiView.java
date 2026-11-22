package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import modelo.Combo;
import modelo.DescuentoHoraFelizSushi;
import modelo.ExtraSalsaAnguila;
import modelo.Pedido;
import modelo.Producto;
import modelo.ToppingTempura;

/**
 * Vista gráfica interactiva con Swing (sin librerías externas).
 *
 * Distribución:
 *   - Izquierda: armar el pedido (rollo, adiciones, combo y Hora Feliz).
 *   - Derecha:   resumen del pedido (estado, artículos, subtotal, descuento y total).
 *   - Abajo:     historial en vivo (observers y avisos del controlador) + botones de estado.
 *
 * La vista no contiene lógica de negocio ni conoce al controlador: cada botón
 * solo llama a una de las {@link Acciones}, que Main conecta con PedidoController.
 * Las salidas de los observers (que escriben en System.out) se copian al historial,
 * así no hace falta mostrar ventanas emergentes en cada clic.
 */
public class GuiView implements IVista {

    /**
     * Acciones que el usuario puede disparar desde la ventana.
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

    // ---------- Paleta ----------
    private static final Color COLOR_ENCABEZADO = new Color(0x2B2D42);
    private static final Color COLOR_RECIBIDO = new Color(0x1E6FD9);
    private static final Color COLOR_PREPARACION = new Color(0xE08E0B);
    private static final Color COLOR_DESPACHADO = new Color(0x2E9E5B);
    private static final Color COLOR_CANCELADO = new Color(0xC0392B);
    private static final Color COLOR_TEXTO_SUAVE = new Color(0x6B6F80);

    private static final int MAX_EXTRAS_SALSA = 5;

    private final Map<String, Double> cartaRollos;
    private Acciones acciones;

    // ---------- Componentes ----------
    private final JFrame ventana;
    private final JLabel lblPedido;
    private final JComboBox<String> cmbRollo;
    private final JCheckBox chkTempura;
    private final JCheckBox chkSalsa;
    private final JSpinner spnSalsa;
    private final JCheckBox chkHoraFeliz;
    private final JComboBox<String> cmbMetodoPago;
    private final JLabel lblEstado;
    private final JTextArea areaArticulos;
    private final JLabel lblSubtotal;
    private final JLabel lblDescuento;
    private final JLabel lblTotal;
    private final JTextPane historial;

    /** Salida original de la consola (antes de copiarla al historial). */
    private final PrintStream consolaOriginal;
    private final SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

    /**
     * @param cartaRollos nombres y precios base de los rollos que se ofrecen en el selector.
     * @param metodosPago métodos de pago que se ofrecen al facturar.
     */
    public GuiView(Map<String, Double> cartaRollos, List<String> metodosPago) {
        this.cartaRollos = Objects.requireNonNull(cartaRollos, "La carta no puede ser null.");
        Objects.requireNonNull(metodosPago, "Los métodos de pago no pueden ser null.");
        usarAspectoDelSistema();

        ventana = new JFrame("Cocina Oculta - Sistema de Pedidos de Sushi");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ----- Encabezado -----
        JLabel lblTitulo = new JLabel("Cocina Oculta  |  Sistema de Pedidos de Sushi");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        lblTitulo.setForeground(Color.WHITE);
        lblPedido = new JLabel("Pedido #-");
        lblPedido.setFont(lblPedido.getFont().deriveFont(Font.BOLD, 15f));
        lblPedido.setForeground(Color.WHITE);
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(COLOR_ENCABEZADO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JButton btnNuevoPedido = new JButton("Nuevo Pedido");
        btnNuevoPedido.setFont(btnNuevoPedido.getFont().deriveFont(Font.BOLD));
        btnNuevoPedido.addActionListener(e -> ejecutar(() -> acciones.nuevoPedido()));
        JPanel encabezadoDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        encabezadoDerecha.setOpaque(false);
        encabezadoDerecha.add(lblPedido);
        encabezadoDerecha.add(btnNuevoPedido);
        encabezado.add(lblTitulo, BorderLayout.WEST);
        encabezado.add(encabezadoDerecha, BorderLayout.EAST);

        // ----- Panel izquierdo: armar pedido -----
        cmbRollo = new JComboBox<>(cartaRollos.keySet().toArray(new String[0]));
        cmbRollo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                String texto = value == null ? "" : String.format("%s  -  $%.2f",
                        value, GuiView.this.cartaRollos.get(value));
                return super.getListCellRendererComponent(list, texto, index, isSelected, cellHasFocus);
            }
        });
        chkTempura = new JCheckBox(String.format("Topping Tempura  (+$%.2f)", ToppingTempura.getCostoAdicion()));
        chkSalsa = new JCheckBox(String.format("Extra Salsa de Anguila  (+$%.2f c/u)",
                ExtraSalsaAnguila.getCostoAdicion()));
        spnSalsa = new JSpinner(new SpinnerNumberModel(1, 1, MAX_EXTRAS_SALSA, 1));
        spnSalsa.setEnabled(false);
        chkSalsa.addActionListener(e -> spnSalsa.setEnabled(chkSalsa.isSelected()));

        JButton btnAgregarRollo = new JButton("Agregar Rollo");
        btnAgregarRollo.addActionListener(e -> ejecutar(() -> {
            int salsas = chkSalsa.isSelected() ? (Integer) spnSalsa.getValue() : 0;
            acciones.agregarRollo((String) cmbRollo.getSelectedItem(), chkTempura.isSelected(), salsas);
        }));

        JButton btnAgregarCombo = new JButton("Agregar Combo Bento Box");
        btnAgregarCombo.addActionListener(e -> ejecutar(() -> acciones.agregarComboBentoBox()));

        chkHoraFeliz = new JCheckBox("Hora Feliz Sushi  (-10%)");
        chkHoraFeliz.setFont(chkHoraFeliz.getFont().deriveFont(Font.BOLD));
        // ActionListener solo se dispara con el clic del usuario, no con setSelected(...).
        chkHoraFeliz.addActionListener(e -> ejecutar(() -> acciones.cambiarHoraFeliz(chkHoraFeliz.isSelected())));

        cmbMetodoPago = new JComboBox<>(metodosPago.toArray(new String[0]));
        JButton btnPagar = new JButton("Pagar y Emitir Factura");
        btnPagar.setFont(btnPagar.getFont().deriveFont(Font.BOLD));
        btnPagar.addActionListener(e -> ejecutar(() -> acciones.pagar((String) cmbMetodoPago.getSelectedItem())));

        JPanel panelSalsa = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelSalsa.setOpaque(false);
        panelSalsa.add(chkSalsa);
        panelSalsa.add(Box.createHorizontalStrut(6));
        panelSalsa.add(new JLabel("Cant.:"));
        panelSalsa.add(Box.createHorizontalStrut(4));
        panelSalsa.add(spnSalsa);

        JPanel panelIzquierdo = new JPanel(new GridBagLayout());
        panelIzquierdo.setBorder(seccion("Armar pedido"));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.insets = new Insets(4, 8, 4, 8);

        panelIzquierdo.add(subtitulo("Rollo Maki  (Decorator)"), c);
        panelIzquierdo.add(cmbRollo, c);
        panelIzquierdo.add(chkTempura, c);
        panelIzquierdo.add(panelSalsa, c);
        panelIzquierdo.add(btnAgregarRollo, c);
        panelIzquierdo.add(new JSeparator(), c);
        panelIzquierdo.add(subtitulo("Combo  (Builder)"), c);
        panelIzquierdo.add(nota("Philadelphia + Tempura, Té Verde y Sopa Miso"), c);
        panelIzquierdo.add(btnAgregarCombo, c);
        panelIzquierdo.add(new JSeparator(), c);
        panelIzquierdo.add(subtitulo("Promoción  (Strategy)"), c);
        panelIzquierdo.add(chkHoraFeliz, c);
        panelIzquierdo.add(new JSeparator(), c);
        panelIzquierdo.add(subtitulo("Pago y Factura"), c);
        panelIzquierdo.add(cmbMetodoPago, c);
        panelIzquierdo.add(btnPagar, c);
        c.weighty = 1; // empuja todo hacia arriba
        panelIzquierdo.add(Box.createVerticalGlue(), c);
        panelIzquierdo.setPreferredSize(new Dimension(340, 0));

        // ----- Panel derecho: resumen del pedido -----
        lblEstado = new JLabel("RECIBIDO", SwingConstants.CENTER);
        lblEstado.setOpaque(true);
        lblEstado.setForeground(Color.WHITE);
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 14f));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelEstado.add(new JLabel("Estado:  "));
        panelEstado.add(lblEstado);

        areaArticulos = new JTextArea();
        areaArticulos.setEditable(false);
        areaArticulos.setLineWrap(true);
        areaArticulos.setWrapStyleWord(true);
        areaArticulos.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        areaArticulos.setMargin(new Insets(6, 8, 6, 8));

        lblSubtotal = valor(false);
        lblDescuento = valor(false);
        lblTotal = valor(true);
        JPanel panelTotales = new JPanel(new GridLayout(3, 2, 8, 4));
        panelTotales.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 4));
        panelTotales.add(new JLabel("Subtotal:"));
        panelTotales.add(lblSubtotal);
        panelTotales.add(new JLabel("Descuento:"));
        panelTotales.add(lblDescuento);
        JLabel lblTextoTotal = new JLabel("TOTAL:");
        lblTextoTotal.setFont(lblTextoTotal.getFont().deriveFont(Font.BOLD, 16f));
        panelTotales.add(lblTextoTotal);
        panelTotales.add(lblTotal);

        JPanel panelDerecho = new JPanel(new BorderLayout(0, 8));
        panelDerecho.setBorder(seccion("Resumen del pedido"));
        panelDerecho.add(panelEstado, BorderLayout.NORTH);
        panelDerecho.add(new JScrollPane(areaArticulos), BorderLayout.CENTER);
        panelDerecho.add(panelTotales, BorderLayout.SOUTH);

        // ----- Panel inferior: historial + botones de estado -----
        historial = new JTextPane();
        historial.setEditable(false);
        historial.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollHistorial = new JScrollPane(historial);
        scrollHistorial.setPreferredSize(new Dimension(0, 170));

        JButton btnAvanzar = new JButton("Avanzar estado");
        JButton btnCancelar = new JButton("Cancelar pedido");
        JButton btnDeshacer = new JButton("Deshacer último");
        JButton btnLimpiar = new JButton("Limpiar historial");
        btnAvanzar.addActionListener(e -> ejecutar(() -> acciones.avanzarEstado()));
        btnCancelar.addActionListener(e -> ejecutar(() -> acciones.cancelarPedido()));
        btnDeshacer.addActionListener(e -> ejecutar(() -> acciones.deshacer()));
        btnLimpiar.addActionListener(e -> historial.setText(""));

        JPanel panelBotones = new JPanel(new BorderLayout());
        JPanel botonesEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botonesEstado.add(new JLabel("State / Command:"));
        botonesEstado.add(btnAvanzar);
        botonesEstado.add(btnCancelar);
        botonesEstado.add(btnDeshacer);
        panelBotones.add(botonesEstado, BorderLayout.WEST);
        panelBotones.add(btnLimpiar, BorderLayout.EAST);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(seccion("Historial y notificaciones en vivo (Observer)"));
        panelInferior.add(panelBotones, BorderLayout.NORTH);
        panelInferior.add(scrollHistorial, BorderLayout.CENTER);

        // ----- Ensamblado -----
        JPanel centro = new JPanel(new BorderLayout(10, 0));
        centro.add(panelIzquierdo, BorderLayout.WEST);
        centro.add(panelDerecho, BorderLayout.CENTER);

        JPanel contenido = new JPanel(new BorderLayout(0, 10));
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(panelInferior, BorderLayout.SOUTH);

        ventana.add(encabezado, BorderLayout.NORTH);
        ventana.add(contenido, BorderLayout.CENTER);
        ventana.setSize(1040, 800);
        ventana.setMinimumSize(new Dimension(900, 740));
        ventana.setLocationRelativeTo(null);

        // Los observers escriben en System.out: se copia esa salida al historial.
        consolaOriginal = System.out;
        redirigirConsolaAlHistorial();
    }

    /** Conecta los controles con las acciones (implementadas en Main sobre PedidoController). */
    public void setAcciones(Acciones acciones) {
        this.acciones = Objects.requireNonNull(acciones, "Las acciones no pueden ser null.");
    }

    // =====================================================================
    // IVista
    // =====================================================================

    @Override
    public void mostrarMenu() {
        enEdt(() -> {
            ventana.setVisible(true);
            registrar("Bienvenido a Cocina Oculta. Arma el pedido con el panel izquierdo.");
        });
    }

    @Override
    public void mostrarDetallePedido(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        // Se arma todo el texto aquí (solo lectura del modelo) y se pinta en el EDT.
        final String nombreEstado = p.getEstado().getNombre();
        final String articulos = textoArticulos(p);
        final String subtotal = dinero(p.calcularSubtotal());
        final String descuento = String.format("%s  (-$%.2f)", p.getDescuento(), p.calcularAhorro());
        final String total = dinero(p.calcularTotal());
        final boolean horaFeliz = p.getDescuento() instanceof DescuentoHoraFelizSushi;
        final int id = p.getId();

        enEdt(() -> {
            lblPedido.setText("Pedido #" + id);
            lblEstado.setText(nombreEstado);
            lblEstado.setBackground(colorEstado(nombreEstado));
            areaArticulos.setText(articulos);
            areaArticulos.setCaretPosition(0);
            lblSubtotal.setText(subtotal);
            lblDescuento.setText(descuento);
            lblTotal.setText(total);
            chkHoraFeliz.setSelected(horaFeliz);
        });
    }

    @Override
    public void mostrarAlertaNotificacion(String msj) {
        consolaOriginal.println(">>> NOTIFICACIÓN: " + msj);
        enEdt(() -> registrar(">>> " + msj));
    }

    @Override
    public void mostrarFactura(Pedido p, String metodoPago) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        final String factura = FormatoPedido.factura(p, metodoPago);
        final int id = p.getId();
        consolaOriginal.println(factura);
        enEdt(() -> {
            JTextArea area = new JTextArea(factura);
            area.setEditable(false);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            area.setMargin(new Insets(10, 14, 10, 14));
            area.setCaretPosition(0);
            JScrollPane scroll = new JScrollPane(area);
            int alto = Math.min(560, area.getPreferredSize().height + 6);
            scroll.setPreferredSize(new Dimension(area.getPreferredSize().width + 24, alto));
            JOptionPane.showMessageDialog(ventana, scroll,
                    "Factura - Pedido #" + id, JOptionPane.PLAIN_MESSAGE);
        });
    }

    // =====================================================================
    // Auxiliares de presentación
    // =====================================================================

    private static String textoArticulos(Pedido p) {
        StringBuilder sb = new StringBuilder();
        sb.append("COMBOS\n");
        if (p.getCombos().isEmpty()) {
            sb.append("   (ninguno)\n");
        }
        for (Combo combo : p.getCombos()) {
            sb.append(String.format("   • %s   $%.2f%n", combo.getDescripcion(), combo.getPrecio()));
        }
        sb.append("\nROLLOS / PRODUCTOS\n");
        if (p.getProductos().isEmpty()) {
            sb.append("   (ninguno)\n");
        }
        for (Producto producto : p.getProductos()) {
            sb.append(String.format("   • %s   $%.2f%n", producto.getDescripcion(), producto.getPrecio()));
        }
        return sb.toString();
    }

    private static String dinero(double monto) {
        return String.format("$%.2f", monto);
    }

    private static Color colorEstado(String nombreEstado) {
        String n = nombreEstado.toUpperCase();
        if (n.startsWith("EN PREP")) {
            return COLOR_PREPARACION;
        }
        if (n.startsWith("DESPACH")) {
            return COLOR_DESPACHADO;
        }
        if (n.startsWith("CANCEL")) {
            return COLOR_CANCELADO;
        }
        return COLOR_RECIBIDO;
    }

    /** Agrega una línea con hora al historial, con color según quién la envía. Llamar en el EDT. */
    private void registrar(String linea) {
        if (linea.trim().isEmpty()) {
            return;
        }
        SimpleAttributeSet estilo = new SimpleAttributeSet();
        if (linea.startsWith(">>>")) {
            StyleConstants.setForeground(estilo, new Color(0xB35C00));
            StyleConstants.setBold(estilo, true);
        } else if (linea.startsWith("[PANTALLA BARRA")) {
            StyleConstants.setForeground(estilo, new Color(0x00897B));
        } else if (linea.startsWith("[PANTALLA DESPACHADOR")) {
            StyleConstants.setForeground(estilo, new Color(0x7B1FA2));
        } else if (linea.startsWith("[SISTEMA CAJA")) {
            StyleConstants.setForeground(estilo, COLOR_DESPACHADO);
        } else if (linea.startsWith("[Pedido")) {
            StyleConstants.setForeground(estilo, COLOR_RECIBIDO);
            StyleConstants.setBold(estilo, true);
        } else {
            StyleConstants.setForeground(estilo, Color.DARK_GRAY);
        }
        StyledDocument doc = historial.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), "[" + formatoHora.format(new Date()) + "] " + linea + "\n", estilo);
        } catch (BadLocationException e) {
            // no debería ocurrir al insertar al final
        }
        historial.setCaretPosition(doc.getLength());
    }

    /** Copia todo lo que se imprime en System.out (observers, estados) al historial, sin perder la consola. */
    private void redirigirConsolaAlHistorial() {
        OutputStream copia = new OutputStream() {
            private final ByteArrayOutputStream linea = new ByteArrayOutputStream();

            @Override
            public synchronized void write(int b) {
                if (b == '\n') {
                    final String texto;
                    try {
                        texto = linea.toString("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException(e);
                    }
                    linea.reset();
                    consolaOriginal.println(texto);
                    enEdt(() -> registrar(texto));
                } else if (b != '\r') {
                    linea.write(b);
                }
            }
        };
        try {
            System.setOut(new PrintStream(copia, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // UTF-8 siempre existe; si fallara, simplemente no se copia la consola.
        }
    }

    /** Ejecuta una acción del usuario sin dejar que una excepción rompa la ventana. */
    private void ejecutar(Runnable accion) {
        if (acciones == null) {
            registrar(">>> La vista todavía no está conectada al controlador.");
            return;
        }
        try {
            accion.run();
        } catch (RuntimeException e) {
            registrar(">>> Error: " + e.getMessage());
        }
    }

    private static javax.swing.border.Border seccion(String titulo) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(titulo),
                BorderFactory.createEmptyBorder(4, 6, 6, 6));
    }

    private static JLabel subtitulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
        return lbl;
    }

    private static JLabel nota(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(COLOR_TEXTO_SUAVE);
        return lbl;
    }

    private static JLabel valor(boolean destacado) {
        JLabel lbl = new JLabel("$0.00", SwingConstants.RIGHT);
        if (destacado) {
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 16f));
        }
        return lbl;
    }

    private static void usarAspectoDelSistema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si no se puede, se queda el aspecto por defecto de Swing.
        }
    }

    /** Swing exige tocar los componentes desde el Event Dispatch Thread (EDT). */
    private static void enEdt(Runnable tarea) {
        if (SwingUtilities.isEventDispatchThread()) {
            tarea.run();
        } else {
            SwingUtilities.invokeLater(tarea);
        }
    }
}
