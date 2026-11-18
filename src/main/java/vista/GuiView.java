package vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import modelo.Pedido;

/**
 * Vista gráfica básica con Swing: un JTextArea con la carta y el pedido,
 * tres botones de acción y JOptionPane para las notificaciones.
 *
 * La vista no conoce al controlador: recibe las acciones como Runnable
 * mediante setAcciones(...). Así se mantiene la separación MVC.
 */
public class GuiView implements IVista {

    private final JFrame ventana;
    private final JTextArea areaPedido;

    private Runnable accionAvanzar;
    private Runnable accionCancelar;
    private Runnable accionDeshacer;

    public GuiView() {
        ventana = new JFrame("Cocina Oculta - Sistema de Pedidos de Sushi");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(820, 560);
        ventana.setLocationRelativeTo(null);

        areaPedido = new JTextArea();
        areaPedido.setEditable(false);
        areaPedido.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JButton btnAvanzar = new JButton("Avanzar estado");
        JButton btnCancelar = new JButton("Cancelar pedido");
        JButton btnDeshacer = new JButton("Deshacer último");

        btnAvanzar.addActionListener(e -> ejecutar(accionAvanzar));
        btnCancelar.addActionListener(e -> ejecutar(accionCancelar));
        btnDeshacer.addActionListener(e -> ejecutar(accionDeshacer));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBotones.add(btnAvanzar);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnDeshacer);

        ventana.add(new JScrollPane(areaPedido), BorderLayout.CENTER);
        ventana.add(panelBotones, BorderLayout.SOUTH);
    }

    /**
     * Conecta los botones con las acciones (normalmente métodos del controlador).
     */
    public void setAcciones(Runnable avanzar, Runnable cancelar, Runnable deshacer) {
        this.accionAvanzar = avanzar;
        this.accionCancelar = cancelar;
        this.accionDeshacer = deshacer;
    }

    @Override
    public void mostrarMenu() {
        String carta = FormatoPedido.carta();
        enEdt(() -> {
            areaPedido.setText(carta);
            ventana.setVisible(true);
        });
    }

    @Override
    public void mostrarDetallePedido(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        String texto = FormatoPedido.carta() + "\n" + FormatoPedido.detalle(p);
        enEdt(() -> {
            areaPedido.setText(texto);
            areaPedido.setCaretPosition(0);
        });
    }

    @Override
    public void mostrarAlertaNotificacion(String msj) {
        enEdt(() -> JOptionPane.showMessageDialog(
                ventana, msj, "Notificación", JOptionPane.INFORMATION_MESSAGE));
    }

    // ---------- Auxiliares privados ----------

    private static void ejecutar(Runnable accion) {
        if (accion != null) {
            accion.run();
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
