package modelo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Observer concreto: sistema de caja que registra cobros y anulaciones.
 *
 * Además de mostrar cada aviso en consola, lo guarda en el archivo de texto
 * {@value #ARCHIVO_AUDITORIA} en modo append (solo agregar): cada evento queda
 * como una línea nueva con fecha y hora, y la clase no ofrece ningún método
 * para editar ni borrar líneas ya escritas.
 */
public class SistemaCaja implements PedidoObserver {

    private static final String ETIQUETA = "[SISTEMA CAJA]";
    private static final String ARCHIVO_AUDITORIA = "auditoria.log";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void actualizar(Pedido p) {
        Objects.requireNonNull(p, "El pedido no puede ser null.");
        EstadoPedido estado = p.getEstado();
        String mensaje;

        if (estado instanceof EstadoPreparacion) {
            mensaje = String.format("Pedido #%d confirmado. Subtotal $%.2f | %s | Total $%.2f",
                    p.getId(), p.calcularSubtotal(), p.getDescuento(), p.calcularTotal());
        } else if (estado instanceof EstadoDespachado) {
            mensaje = String.format("Cobro registrado del pedido #%d: $%.2f",
                    p.getId(), p.calcularTotal());
        } else if (estado instanceof EstadoCancelado) {
            mensaje = String.format("Pedido #%d cancelado: anular el cobro.", p.getId());
        } else {
            mensaje = String.format("Pedido #%d ahora está %s.", p.getId(), estado.getNombre());
        }

        System.out.println(ETIQUETA + " " + mensaje);
        registrarEnAuditoria(mensaje);
    }

    /**
     * Agrega el evento al final del archivo de auditoría (lo crea si no existe).
     * Si el archivo no se puede escribir, se avisa por consola y el pedido sigue su flujo.
     */
    private static synchronized void registrarEnAuditoria(String mensaje) {
        String linea = LocalDateTime.now().format(FORMATO_FECHA) + " | " + mensaje + System.lineSeparator();
        try {
            Files.write(Paths.get(ARCHIVO_AUDITORIA), linea.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println(ETIQUETA + " No se pudo escribir en " + ARCHIVO_AUDITORIA + ": " + e.getMessage());
        }
    }
}
