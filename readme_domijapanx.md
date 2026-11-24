# DomiJapanX — Sistema de Gestión de Pedidos (Cocina Oculta de Sushi)

Aplicación desarrollada en **Java** bajo la arquitectura **Modelo-Vista-Controlador (MVC)** para la gestión operativa de pedidos en una cocina oculta de sushi. El sistema comparte un único núcleo de negocio desacoplado que opera indistintamente desde una **interfaz de consola interactiva** y una **interfaz gráfica en Swing**, aplicando **6 patrones de diseño** y respaldado por **53 pruebas unitarias**.

> **Documentación completa:** Los diagramas UML (Casos de Uso, Clases General, los 6 diagramas por patrón y Secuencia), los requisitos funcionales/no funcionales y la justificación técnica se encuentran disponibles en la **[Wiki del repositorio](https://github.com/Ztelech/DomiJapanx-Patterns/wiki)**.

---

## Funcionalidades implementadas

* **Menú y personalización dinámica de platos (Decorator):** Creación de Rollos Maki base (*California*, *Philadelphia*, *Dragón*, *Kappa*) con adiciones apilables (*Topping Tempura* y *Extra Salsa de Anguila*) que recalculan precio y descripción en tiempo real.
* **Ensamblaje de combos paso a paso (Builder):** Construcción guiada y validada del combo *Bento Box* (plato principal, bebida y acompañamiento) antes de integrarlo al pedido.
* **Promociones y cálculo de totales (Strategy):** Aplicación intercambiable de reglas de descuento (*Hora Feliz Sushi -10%* y *Sin Descuento*) sin modificar la lógica central de `Pedido`.
* **Ciclo de vida estricto del pedido (State):** Control de transiciones válidas (`RECIBIDO` → `EN PREPARACIÓN` → `DESPACHADO` o `CANCELADO`), bloqueando modificaciones o cancelaciones no permitidas según el estado actual.
* **Historial y reversión de acciones (Command):** Encapsulación de adiciones de productos para permitir deshacer (*Undo*) errores de digitación mientras el pedido siga abierto.
* **Notificaciones en tiempo real (Observer):** Aviso automático y desacoplado a las pantallas operativas (**Barra de Sushi**, **Despachador**, **Sistema de Caja** y la vista activa) ante cada cambio de estado.
* **Registro de auditoría inalterable:** `SistemaCaja` persiste cada evento de confirmación, cobro o anulación con marca de tiempo en `auditoria.log` en modo solo escritura (*append-only*).
* **Operación continua y facturación simulada:** Creación secuencial de múltiples pedidos independientes (`#1`, `#2`, `#3`...), selección de método de pago simulado (*Efectivo*, *Tarjeta de Crédito/Débito*, *Nequi / Transferencia*) y emisión de factura detallada.
* **Doble interfaz sobre el mismo núcleo:** Menú interactivo en consola y ventana gráfica Swing comunicados exclusivamente a través de `IVista` y `PedidoController`.

*(Nota de alcance: pasarelas de pago reales, autenticación/login por roles y persistencia en base de datos para cierres de caja históricos quedan fuera del alcance de este prototipo operativo).*

---

## Patrones de diseño aplicados

| Patrón | Categoría | Problema que resuelve en DomiJapanX | Clases principales (`modelo` / `controlador`) |
| :--- | :--- | :--- | :--- |
| **Builder** | Creacional | Ensamblar combos complejos paso a paso evitando objetos incompletos o constructores rígidos. | `Combo`, `ComboBuilder`, `BentoBoxBuilder`, `DirectorArmado` |
| **Decorator** | Estructural | Personalizar rollos con múltiples adiciones dinámicas sin generar explosión de subclases. | `Producto`, `RolloMakiBase`, `AdicionDecorator`, `ToppingTempura`, `ExtraSalsaAnguila` |
| **Observer** | Comportamiento | Notificar cambios de estado a cocina, despacho, caja y vista sin acoplar el núcleo a las pantallas. | `Pedido` (Subject), `PedidoObserver`, `PantallaBarraSushi`, `PantallaDespachador`, `SistemaCaja`, `PedidoController` |
| **Strategy** | Comportamiento | Intercambiar fórmulas de descuento en tiempo de ejecución evitando cadenas de `if/else`. | `DescuentoStrategy`, `DescuentoHoraFelizSushi`, `SinDescuento` |
| **State** | Comportamiento | Delegar reglas de transición y restricciones del ciclo de vida del pedido en cada estado. | `EstadoPedido`, `EstadoRecibido`, `EstadoPreparacion`, `EstadoDespachado`, `EstadoCancelado` |
| **Command** | Comportamiento | Encapsular la adición de productos en un historial para soportar la operación *Deshacer* (*Undo*). | `Comando`, `ComandoAgregarProducto`, `HistorialComandos` |

---

## Estructura del proyecto (MVC)

```text
DomiJapanx-Patterns/
├── pom.xml
├── src/
│   ├── main/java/
│   │   ├── Main.java                  # Punto de entrada: ensambla MVC y permite elegir Consola o GUI
│   │   ├── modelo/                    # Núcleo de negocio y los 6 patrones de diseño
│   │   ├── controlador/               # PedidoController e HistorialComandos
│   │   └── vista/                     # IVista, ConsolaView, GuiView y FormatoPedido
│   └── test/java/
│       ├── modelo/                    # BuilderTest, DecoratorTest, ObserverTest, StateTest, StrategyTest
│       └── controlador/               # CommandTest
```

---

##  Requisitos previos, compilación y ejecución

### Requisitos

* **Java JDK 8 o superior** (probado con JDK 8 / OpenJDK 26).
* **Apache Maven 3.6+**.
* **IDE (Opcional):** IntelliJ IDEA u otro de tu preferencia para ejecución y desarrollo asistido.



### 1. Obtener el proyecto (Clonar o Descargar ZIP)

**Opción A: Clonar el repositorio**

```bash
git clone https://github.com/Ztelech/DomiJapanx-Patterns.git
cd DomiJapanx-Patterns

```

**Opción B: Descarga básica en ZIP**

1. En la página del repositorio en GitHub, haz clic en el botón verde **`<> Code`** y selecciona **`Download ZIP`**.
2. Descomprime el archivo `.zip` descargado en la carpeta de tu preferencia.

### 2. Ejecutar en un IDE (Ejemplo: IntelliJ IDEA)

1. Abre IntelliJ IDEA y selecciona **`Open`** en la pantalla de inicio (o **`File > Open`** en el menú superior).
2. Selecciona la carpeta descomprimida `DomiJapanx-Patterns` (asegúrate de seleccionar el directorio que contiene el archivo `pom.xml`) y haz clic en **`OK`**.
3. Haz clic en **`Trust Project`** si el IDE lo solicita. IntelliJ detectará automáticamente la configuración de Maven y descargará las dependencias.
4. En el panel lateral del proyecto, navega a `src > main > java` y abre el archivo `Main.java`.
5. Haz clic en el ícono de reproducción verde (:arrow_forward:) junto a `public class Main` y selecciona **`Run 'Main.main()'`**.
6. Interactúa con la consola del IDE para seleccionar entre la vista de consola (`1`) o la gráfica (`2`).

### 3. Compilación y ejecución desde Terminal (Alternativa)

Si prefieres usar la consola, ejecuta primero las 53 pruebas unitarias para validar los patrones:

```bash
mvn test

```

Luego compila el proyecto:

```bash
mvn compile

```

Inicia `Main` desde la raíz del repositorio:

```bash
java -cp target/classes Main

```

El programa te preguntará qué vista deseas abrir:

* Ingresa **`1`** para operar desde la **Consola interactiva**.
* Ingresa **`2`** para abrir la **Interfaz Gráfica (Swing)**.

También puedes lanzar directamente la vista deseada pasando el argumento:

```bash
java -cp target/classes Main consola
java -cp target/classes Main gui

```


## Declaración de uso de herramientas de IA

En cumplimiento de las reglas de integridad académica del curso, declaramos el uso de las siguientes herramientas de inteligencia artificial como apoyo durante el desarrollo:

* **Gemini:** Traducción de los diagramas UML elaborados por el equipo a sintaxis Mermaid para su visualización en la Wiki de GitHub y apoyo en la revisión y coherencia documental.
* **Claude Code:** Apoyo en la estructuración de la interfaz gráfica Swing (`GuiView`), refinamiento de la suite de pruebas unitarias, integración del registro *append-only* (`auditoria.log`) en `SistemaCaja` y apoyo en la preparación de recursos visuales para el video de sustentación.

El 100% de la arquitectura, el código fuente, los diagramas y las decisiones de diseño fueron revisados, validados y comprendidos por los tres integrantes del equipo.

---

## Integrantes del equipo

* **Jeff Kenneth Maldonado Algarra** (`@Ztelech`)  
* **Brenda Madeline Martinez Enriquez** (`@Brendam16`) 
* **Daniel Sebastián Linares Martinez** (`@danielsebastianlinares87`) 