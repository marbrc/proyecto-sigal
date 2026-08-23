package mx.utng.controller;
 
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
 
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import mx.utng.dao.AsignacionDAO;
import mx.utng.dao.AvisoDAO;
import mx.utng.dao.EspacioDAO;
import mx.utng.model.Asignaciones;
import mx.utng.model.Aviso;
import mx.utng.model.DetalleItem;
import mx.utng.model.EspacioRegistro;
 
 
public class InicioController {
 
 
    // ---- Tarjetas del dashboard ----
    @FXML private VBox cardEspacios;
    @FXML private VBox cardAsignaciones;
    @FXML private VBox cardDisponibles;
    @FXML private VBox cardAvisos;
 
    // ---- Calendario ----
    @FXML private Label lblMesCalendario;
    @FXML private Button btnMesAnterior;
    @FXML private Button btnMesSiguiente;
    @FXML private VBox calGridDias;
 
    // ---- Asignaciones del día / Avisos recientes ----
    @FXML private Label lblFechaAsignaciones;
    @FXML private VBox panelAsignacionesLista;
    @FXML private VBox panelAvisosLista;
 
    // ---- Modal de detalle ----
    @FXML private StackPane modalOverlay;
    @FXML private Label lblModalTitulo;
    @FXML private Label lblModalSubtitulo;
    @FXML private Button btnCerrarModal;
    @FXML private Button btnGuardarCambios;
    @FXML private ListView<DetalleItem> listaDetalle;
 
 
    //====================================================
    // VARIABLES GLOBALES
    //====================================================
 
    /** Duracion de las transiciones cortas */
    private static final Duration DURACION_CORTA = Duration.millis(160);
 
    /** Duracion de las transiciones del modal. */
    private static final Duration DURACION_MODAL = Duration.millis(220);
 
    /** Duracion del fundido al cambiar de mes en el calendario. */
    private static final Duration DURACION_CALENDARIO = Duration.millis(180);
 
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "MX"));
    private static final DateTimeFormatter FORMATO_FECHA_LARGA = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "MX"));
 
    /** Nombre de usuario actualmente en sesion (aqui esta simulado por ahora) */
    private String usuarioActual = "Usuario";
 
    /** Rol del usuario actualmente en sesion (tmb simulado por ahora) */
    private String rolActual = "Usuario";
 
    private final EspacioDAO espacioDAO = new EspacioDAO();
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final AvisoDAO avisoDAO = new AvisoDAO();
 
    /** Mes que se esta mostrando actualmente en el mini-calendario. */
    private YearMonth mesActual = YearMonth.now();
 
    /** Dia seleccionado en el calendario (por defecto, hoy). */
    private LocalDate fechaSeleccionada = LocalDate.now();
 
    /** Referencia al menu principal, para los enlaces "Ver más" / "Ver todos". */
    private MenuController menuController;
 
    /** Etiquetas de las 4 columnas que se muestran en el modal (cambian segun la tarjeta). */
    private String etiqueta1 = "Nombre";
    private String etiqueta2 = "Tipo";
    private String etiqueta3 = "Capacidad";
    private String etiqueta4 = "Estado";
 
    /** De que tarjeta viene el modal actualmente abierto (para saber a que tabla guardar). */
    private enum TipoModal { ESPACIOS, ASIGNACIONES, DISPONIBLES, AVISOS }
    private TipoModal modalActual;
 
    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }
 
 
    @FXML
    public void initialize() {
 
        configurarListaDetalle();
        configurarEfectosTarjetas();
        cargarDashboard();
        construirCalendario();
        cargarAsignacionesDelDia(fechaSeleccionada);
        cargarAvisosPanel();
        reproducirEntradaDashboard();
 
    }
 
 
 
    //====================================================
    // DASHBOARD
    //====================================================
 
    /**
     * Pone la carga completa del Dashboard: estadisticas, calendario,
     * asignaciones del dia y avisos. Es el unico metodo que necesitamos
     * volver a llamar si quiero refrescar todo de golpe (por ejemplo
     * despues de guardar cambios en un modulo)
     */
    private void cargarDashboard() {
        cargarEstadisticas();
    }
 
    /**
     * Actualiza los 4 numeros grandes de las tarjetas del dashboard.
     *
     * Los Labels de esos numeros no tienen fx:id en el FXML actual
     * (estan en Scene Builder), asi que se localizan por su
     * styleClass dentro de cada tarjeta usando lookup() y esto NO
     * requiere modificar el FXML ni agregar fx:id nuevos.
     */
    private void cargarEstadisticas() {
        actualizarNumeroTarjeta(cardEspacios, ".stat-number-blue", espacioDAO.contarTotal());
        actualizarNumeroTarjeta(cardAsignaciones, ".stat-number-purple", asignacionDAO.contarDeHoy());
        actualizarNumeroTarjeta(cardDisponibles, ".stat-number-green", espacioDAO.contarDisponiblesAhora());
        actualizarNumeroTarjeta(cardAvisos, ".stat-number-orange", avisoDAO.contarNoLeidos());
    }
 
    /**
     * Refresca todo el Dashboard de golpe. Pensado para llamarse
     * despues de que un modulo hijo (por ejemplo, Registro de
     * Espacios) guarde cambios que afecten a las tarjetas o listas
     * del Inicio
     */
    public void actualizarDashboard() {
        cargarDashboard();
        construirCalendario();
        cargarAsignacionesDelDia(fechaSeleccionada);
        cargarAvisosPanel();
    }
 
 
    //====================================================
    // CALENDARIO (conectado a datos reales)
    //====================================================
 
    @FXML
    private void onMesAnterior(ActionEvent event) {
        mesActual = mesActual.minusMonths(1);
        construirCalendario();
    }
 
    @FXML
    private void onMesSiguiente(ActionEvent event) {
        mesActual = mesActual.plusMonths(1);
        construirCalendario();
    }
 
    /**
     * Reconstruye por completo la cuadricula de dias del mes visible
     * (calGridDias), usando datos reales: resalta el dia de hoy, el
     * dia seleccionado y marca con un punto los dias que ya tienen
     * asignaciones activas
     */
    private void construirCalendario() {
        if (calGridDias == null) return;
 
        lblMesCalendario.setText(capitalizar(mesActual.format(FORMATO_MES)));
 
        Set<Integer> diasConAsignaciones = asignacionDAO.diasConAsignacionesEnMes(mesActual);
        LocalDate hoy = LocalDate.now();
 
        LocalDate primerDiaMes = mesActual.atDay(1);
        // Lunes = 1 ... Domingo = 7 -> cuantos dias hay que retroceder para
        // que la primera fila empiece en lunes
        int retroceso = primerDiaMes.getDayOfWeek().getValue() - 1;
        LocalDate cursor = primerDiaMes.minusDays(retroceso);
 
        VBox nuevoContenido = new VBox(6.0);
 
        for (int fila = 0; fila < 6; fila++) {
            HBox filaSemana = new HBox(2.0);
            for (int col = 0; col < 7; col++) {
                LocalDate fecha = cursor;
                boolean delMesActual = YearMonth.from(fecha).equals(mesActual);
                boolean esHoy = fecha.equals(hoy);
                boolean esSeleccionado = fecha.equals(fechaSeleccionada);
                boolean tieneAsignaciones = delMesActual && diasConAsignaciones.contains(fecha.getDayOfMonth());
 
                filaSemana.getChildren().add(crearCeldaDia(fecha, delMesActual, esHoy, esSeleccionado, tieneAsignaciones));
                cursor = cursor.plusDays(1);
            }
            nuevoContenido.getChildren().add(filaSemana);
        }
 
        FadeTransition salida = new FadeTransition(DURACION_CALENDARIO, calGridDias);
        salida.setFromValue(1.0);
        salida.setToValue(0.0);
        salida.setOnFinished(evento -> {
            calGridDias.getChildren().setAll(nuevoContenido.getChildren());
            FadeTransition entrada = new FadeTransition(DURACION_CALENDARIO, calGridDias);
            entrada.setFromValue(0.0);
            entrada.setToValue(1.0);
            entrada.play();
        });
        salida.play();
    }
 
    private VBox crearCeldaDia(LocalDate fecha, boolean delMesActual, boolean esHoy, boolean esSeleccionado, boolean tieneAsignaciones) {
        Label numero = new Label(String.valueOf(fecha.getDayOfMonth()));
        numero.setMaxWidth(Double.MAX_VALUE);
        numero.setAlignment(Pos.CENTER);
 
        if (!delMesActual) {
            numero.getStyleClass().add("cal-day-muted");
        } else if (esSeleccionado) {
            numero.getStyleClass().add("cal-day-selected");
        } else if (esHoy) {
            numero.getStyleClass().add("cal-day-today");
        } else {
            numero.getStyleClass().add("cal-day");
        }
 
        Circle punto = new Circle(2.6);
        if (esHoy && delMesActual) {
            punto.getStyleClass().add("legend-dot-purple");
        } else if (tieneAsignaciones) {
            punto.getStyleClass().add("legend-dot-blue");
        } else {
            punto.setOpacity(0.0);
        }
 
        VBox celda = new VBox(3.0, numero, punto);
        celda.setAlignment(Pos.CENTER);
        HBox.setHgrow(celda, Priority.ALWAYS);
 
        if (delMesActual) {
            celda.setOnMouseClicked(evento -> seleccionarDia(fecha));
            aplicarEfectoHoverSuave(celda);
        }
 
        return celda;
    }
 
    /**
     * El usuario dio clic en un dia del mini-calendario: lo marca como
     * seleccionado y refresca el panel "Asignaciones del día" con las
     * asignaciones reales de esa fecha
     */
    private void seleccionarDia(LocalDate fecha) {
        fechaSeleccionada = fecha;
        construirCalendario();
        cargarAsignacionesDelDia(fecha);
    }
 
    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) return texto;
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
 
 
    //====================================================
    // ASIGNACIONES DEL DÍA (panel inferior central)
    //====================================================
 
    private void cargarAsignacionesDelDia(LocalDate fecha) {
        if (panelAsignacionesLista == null) return;
 
        boolean esHoy = fecha.equals(LocalDate.now());
        lblFechaAsignaciones.setText((esHoy ? "Hoy, " : "") + fecha.format(FORMATO_FECHA_LARGA));
 
        ObservableList<Asignaciones> asignaciones = asignacionDAO.listarPorFecha(fecha);
 
        panelAsignacionesLista.getChildren().clear();
 
        if (asignaciones.isEmpty()) {
            panelAsignacionesLista.getChildren().add(crearEstadoVacio("No hay asignaciones para este día."));
            return;
        }
 
        for (Asignaciones a : asignaciones) {
            panelAsignacionesLista.getChildren().add(crearFilaAsignacion(a));
        }
    }
 
    private HBox crearFilaAsignacion(Asignaciones a) {
        boolean ocupado = a.getEstado() != null && a.getEstado().toLowerCase().contains("ocup");
 
        HBox fila = new HBox(12.0);
        fila.getStyleClass().add(ocupado ? "asignacion-item-orange" : "asignacion-item-blue");
 
        VBox info = new VBox(4.0);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label hora = new Label(a.getHoraInicio() + " - " + a.getHoraTermino());
        hora.getStyleClass().add("asignacion-time");
        Label nombre = new Label(a.getEspacio());
        nombre.getStyleClass().add("asignacion-name");
        Label sub = new Label((a.getMateria() != null && !a.getMateria().equals("—")) ? a.getMateria() : a.getNombreSolicitante());
        sub.getStyleClass().add("asignacion-sub");
        info.getChildren().addAll(hora, nombre, sub);
 
        Label badge = new Label(a.getEstado());
        badge.getStyleClass().add(ocupado ? "badge-ocupado" : "badge-asignado");
 
        fila.getChildren().addAll(info, badge);
        fila.setOpacity(0.0);
        FadeTransition entrada = new FadeTransition(Duration.millis(220), fila);
        entrada.setFromValue(0.0);
        entrada.setToValue(1.0);
        entrada.play();
        return fila;
    }
 
    @FXML
    private void onVerMasAsignaciones(MouseEvent event) {
        if (menuController != null) {
            menuController.abrirModulo("fx_asignaciones");
        }
    }
 
 
    //====================================================
    // AVISOS RECIENTES (panel inferior derecho)
    //====================================================
 
    private void cargarAvisosPanel() {
        if (panelAvisosLista == null) return;
 
        ObservableList<Aviso> avisos = avisoDAO.listarNoLeidos();
        panelAvisosLista.getChildren().clear();
 
        if (avisos.isEmpty()) {
            panelAvisosLista.getChildren().add(crearEstadoVacio("No hay avisos pendientes."));
            return;
        }
 
        int max = Math.min(avisos.size(), 4);
        String[] coloresIcono = {"purple", "blue", "green"};
 
        for (int i = 0; i < max; i++) {
            panelAvisosLista.getChildren().add(crearFilaAviso(avisos.get(i), coloresIcono[i % coloresIcono.length]));
        }
    }
 
    private HBox crearFilaAviso(Aviso a, String color) {
        HBox fila = new HBox(12.0);
        fila.getStyleClass().add("notice-item");
 
        StackPane iconoBox = new StackPane();
        iconoBox.getStyleClass().add("notice-icon-box-" + color);
        Circle punto = new Circle(4.5);
        punto.getStyleClass().add("legend-dot-" + color);
        iconoBox.getChildren().add(punto);
 
        VBox texto = new VBox(3.0);
        HBox.setHgrow(texto, Priority.ALWAYS);
        Label titulo = new Label(a.getTipoAviso());
        titulo.getStyleClass().add("notice-title");
        titulo.setWrapText(true);
        Label descripcion = new Label(a.getDescripcion());
        descripcion.getStyleClass().add("notice-text");
        descripcion.setWrapText(true);
        Label fecha = new Label(a.getFecha());
        fecha.getStyleClass().add("notice-time");
        texto.getChildren().addAll(titulo, descripcion, fecha);
 
        fila.getChildren().addAll(iconoBox, texto);
        return fila;
    }
 
    @FXML
    private void onVerTodosAvisos(MouseEvent event) {
        if (menuController != null) {
            menuController.abrirModulo("fx_avisos");
        }
    }
 
    private Label crearEstadoVacio(String mensaje) {
        Label lbl = new Label(mensaje);
        lbl.getStyleClass().add("empty-state-label");
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER);
        return lbl;
    }
 
 
    //====================================================
    // TARJETAS -> MODAL
    //====================================================
 
    @FXML
    private void onCardEspacios(MouseEvent event) {
        ObservableList<EspacioRegistro> espacios = espacioDAO.listarTodos();
 
        ObservableList<DetalleItem> datos = FXCollections.observableArrayList();
        for (EspacioRegistro e : espacios) {
            datos.add(new DetalleItem(e.getIdEspacio(), e.getNombre(), e.getTipo(), String.valueOf(e.getCapacidad()), e.getEstado()));
        }
 
        modalActual = TipoModal.ESPACIOS;
        etiqueta1 = "Nombre"; etiqueta2 = "Tipo"; etiqueta3 = "Capacidad"; etiqueta4 = "Estado";
        abrirModal("Espacios registrados", espacios.size() + " espacios en total", datos);
    }
 
    @FXML
    private void onCardAsignaciones(MouseEvent event) {
        ObservableList<Asignaciones> asignaciones = asignacionDAO.listarDeHoy();
 
        ObservableList<DetalleItem> datos = FXCollections.observableArrayList();
        for (Asignaciones a : asignaciones) {
            datos.add(new DetalleItem(
                    a.getIdAsignacion(),
                    a.getEspacio(),
                    a.getHoraInicio() + " - " + a.getHoraTermino(),
                    a.getNumAlumnos() + " alumnos",
                    a.getEstado()));
        }
 
        String hoyTexto = LocalDate.now().format(FORMATO_FECHA_LARGA);
        modalActual = TipoModal.ASIGNACIONES;
        etiqueta1 = "Espacio"; etiqueta2 = "Horario"; etiqueta3 = "Alumnos"; etiqueta4 = "Estado";
        abrirModal("Asignaciones de hoy", hoyTexto + " · " + asignaciones.size() + " asignaciones", datos);
    }
 
    @FXML
    private void onCardDisponibles(MouseEvent event) {
        ObservableList<EspacioRegistro> disponibles = espacioDAO.listarDisponiblesAhora();
 
        ObservableList<DetalleItem> datos = FXCollections.observableArrayList();
        for (EspacioRegistro e : disponibles) {
            datos.add(new DetalleItem(e.getIdEspacio(), e.getNombre(), e.getTipo(), String.valueOf(e.getCapacidad()), e.getEstado()));
        }
 
        modalActual = TipoModal.DISPONIBLES;
        etiqueta1 = "Nombre"; etiqueta2 = "Tipo"; etiqueta3 = "Capacidad"; etiqueta4 = "Estado";
        abrirModal("Espacios disponibles ahora", disponibles.size() + " espacios libres en este momento", datos);
    }
 
    @FXML
    private void onCardAvisos(MouseEvent event) {
        ObservableList<Aviso> avisos = avisoDAO.listarNoLeidos();
 
        ObservableList<DetalleItem> datos = FXCollections.observableArrayList();
        for (Aviso a : avisos) {
            datos.add(new DetalleItem(a.getIdAviso(), a.getDescripcion(), a.getTipoAviso(), "-", a.getEstado()));
        }
 
        modalActual = TipoModal.AVISOS;
        etiqueta1 = "Aviso"; etiqueta2 = "Tipo"; etiqueta3 = "Detalle"; etiqueta4 = "Estado";
        abrirModal("Avisos sin leer", avisos.size() + " avisos pendientes de revisar", datos);
    }
 
    /**
     * Busca dentro de una tarjeta (por su styleClass, sin necesidad de
     * fx:id) el Label del numero grande y le asigna un nuevo valor
     */
    private void actualizarNumeroTarjeta(VBox tarjeta, String selectorCss, int valor) {
        if (tarjeta == null) return;
        Node nodo = tarjeta.lookup(selectorCss);
        if (nodo instanceof Label) {
            Label numero = (Label) nodo;
            numero.setText(String.valueOf(valor));
        }
    }
 
    //====================================================
    // MODAL
    //====================================================
 
    /**
     * Muestra el modal de detalle con el titulo, subtitulo y datos
     * indicados, animando su entrada con fade + escalado
     */
    private void abrirModal(String titulo, String subtitulo, ObservableList<DetalleItem> datos) {
        lblModalTitulo.setText(titulo);
        lblModalSubtitulo.setText(subtitulo);
        listaDetalle.setItems(datos);
 
        modalOverlay.setVisible(true);
        modalOverlay.setMouseTransparent(false);
 
        StackPane tarjetaModal = (StackPane) modalOverlay.getChildren().get(0);
        animarAperturaModal(tarjetaModal);
    }
 
    @FXML
    private void onCerrarModal(ActionEvent event) {
        StackPane tarjetaModal = (StackPane) modalOverlay.getChildren().get(0);
        animarCierreModal(tarjetaModal);
    }
 
 
    //====================================================
    // LISTA DE DETALLE (tarjetas, ya no tabla)
    //====================================================
 
    private void configurarListaDetalle() {
        listaDetalle.setCellFactory(lv -> new DetalleCardCell());
        listaDetalle.setFocusTraversable(false);
    }
 
 
    /**
     * Celda personalizada que dibuja cada elemento del modal como una
     * tarjeta espaciosa (nombre + estado + un par de datos), en vez de
     * filas de tabla. Doble clic o el boton de lapiz activan edicion
     * en linea; el boton de bote de basura elimina la tarjeta.
     */
    private class DetalleCardCell extends ListCell<DetalleItem> {
 
        private boolean editando = false;
 
        { setPrefWidth(0); }
 
        @Override
        protected void updateItem(DetalleItem item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
 
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
 
            setGraphic(editando ? construirVistaEdicion(item) : construirVistaLectura(item));
        }
 
        private Node construirVistaLectura(DetalleItem item) {
            VBox card = new VBox(10.0);
            card.getStyleClass().add("detail-card");
            // La edicion en linea se desactivo: usa la pantalla real de cada modulo para editar.
 
            HBox encabezado = new HBox(10.0);
            encabezado.setAlignment(Pos.CENTER_LEFT);
            Label nombre = new Label(item.getNombre());
            nombre.getStyleClass().add("detail-card-title");
            nombre.setWrapText(true);
            HBox.setHgrow(nombre, Priority.ALWAYS);
 
            Label badge = new Label(item.getEstado());
            badge.getStyleClass().add(claseBadge(item.getEstado()));
 
 
            Button btnEliminar = new Button("🗑");
            btnEliminar.getStyleClass().add("detail-btn-icon-danger");
            btnEliminar.setOnAction(e -> eliminarItem(item));
 
            encabezado.getChildren().addAll(nombre, badge, btnEliminar);
 
            HBox datos = new HBox(28.0);
            datos.getChildren().add(campoLectura(etiqueta2, item.getTipo()));
            if (!"-".equals(item.getCapacidad())) {
                datos.getChildren().add(campoLectura(etiqueta3, item.getCapacidad()));
            }
 
            card.getChildren().addAll(encabezado, datos);
            return card;
        }
 
        private VBox campoLectura(String etiqueta, String valor) {
            Label lbl = new Label(etiqueta.toUpperCase());
            lbl.getStyleClass().add("detail-field-label");
            Label val = new Label(valor);
            val.getStyleClass().add("detail-field-value");
            return new VBox(2.0, lbl, val);
        }
 
        private Node construirVistaEdicion(DetalleItem item) {
            VBox card = new VBox(10.0);
            card.getStyleClass().add("detail-card");
            card.getStyleClass().add("detail-card-editando");
 
            TextField txtNombre = new TextField(item.getNombre());
            txtNombre.setPromptText(etiqueta1);
            txtNombre.getStyleClass().add("detail-edit-field");
 
            TextField txtTipo = new TextField(item.getTipo());
            txtTipo.setPromptText(etiqueta2);
            txtTipo.getStyleClass().add("detail-edit-field");
 
            TextField txtCapacidad = new TextField(item.getCapacidad());
            txtCapacidad.setPromptText(etiqueta3);
            txtCapacidad.getStyleClass().add("detail-edit-field");
 
            TextField txtEstado = new TextField(item.getEstado());
            txtEstado.setPromptText(etiqueta4);
            txtEstado.getStyleClass().add("detail-edit-field");
 
            HBox filaCampos = new HBox(10.0, txtNombre, txtTipo, txtCapacidad, txtEstado);
            HBox.setHgrow(txtNombre, Priority.ALWAYS);
 
            Button btnGuardar = new Button("Guardar");
            btnGuardar.getStyleClass().add("modal-btn-primary");
            btnGuardar.setOnAction(e -> {
                item.setNombre(txtNombre.getText());
                item.setTipo(txtTipo.getText());
                item.setCapacidad(txtCapacidad.getText());
                item.setEstado(txtEstado.getText());
 
                if (guardarItemEnBaseDeDatos(item)) {
                    editando = false;
                    updateItem(item, false);
                    actualizarDashboard();
                } else {
                    mostrarNotificacion(
                            "No se pudo guardar",
                            "No se pudo guardar el cambio en la base de datos.",
                            Alert.AlertType.ERROR);
                }
            });
 
            Button btnCancelar = new Button("Cancelar");
            btnCancelar.getStyleClass().add("modal-btn-secondary");
            btnCancelar.setOnAction(e -> {
                editando = false;
                updateItem(item, false);
            });
 
            HBox acciones = new HBox(10.0, new Region(), btnCancelar, btnGuardar);
            HBox.setHgrow(acciones.getChildren().get(0), Priority.ALWAYS);
 
            card.getChildren().addAll(filaCampos, acciones);
            return card;
        }
 
        private String claseBadge(String estado) {
            if (estado == null) return "detail-badge-blue";
            String e = estado.toLowerCase();
            if (e.contains("dispon") || e.contains("leí") && !e.contains("no leí")) return "detail-badge-green";
            if (e.contains("ocup")) return "detail-badge-orange";
            if (e.contains("cancel") || e.contains("no leí")) return "detail-badge-red";
            return "detail-badge-blue";
        }
    }
 
 
    //====================================================
    // ANIMACIONES
    //====================================================
 
    /**
     * Animacion de entrada del modal: aparece con fade-in y un
     * ligero efecto de escalado desde 90% hasta 100%
     */
    private void animarAperturaModal(StackPane tarjetaModal) {
        FadeTransition fade = new FadeTransition(DURACION_MODAL, modalOverlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
 
        tarjetaModal.setScaleX(0.9);
        tarjetaModal.setScaleY(0.9);
        ScaleTransition escala = new ScaleTransition(DURACION_MODAL, tarjetaModal);
        escala.setFromX(0.9);
        escala.setFromY(0.9);
        escala.setToX(1.0);
        escala.setToY(1.0);
 
        ParallelTransition entrada = new ParallelTransition(fade, escala);
        entrada.play();
    }
 
    /**
     * Animacion de cierre del modal: fade-out y al terminar se oculta
     * por completo (visible=false) para no seguir capturando clics
     */
    private void animarCierreModal(StackPane tarjetaModal) {
        FadeTransition fade = new FadeTransition(DURACION_MODAL, modalOverlay);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(evento -> {
            modalOverlay.setVisible(false);
            modalOverlay.setMouseTransparent(true);
        });
        fade.play();
    }
 
    /**
     * Animacion de entrada del Dashboard: las 4 tarjetas aparecen una
     * despues de otra (fade + pequeno desplazamiento hacia arriba)
     * dando una sensacion de carga suave
     */
    private void reproducirEntradaDashboard() {
        VBox[] tarjetas = { cardEspacios, cardAsignaciones, cardDisponibles, cardAvisos };
 
        SequentialTransition secuencia = new SequentialTransition();
        secuencia.setDelay(Duration.millis(80));
 
        for (VBox tarjeta : tarjetas) {
            if (tarjeta == null) continue;
 
            tarjeta.setOpacity(0.0);
            tarjeta.setTranslateY(14.0);
 
            FadeTransition fade = new FadeTransition(Duration.millis(280), tarjeta);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
 
            TranslateTransition subida = new TranslateTransition(Duration.millis(280), tarjeta);
            subida.setFromY(14.0);
            subida.setToY(0.0);
 
            ParallelTransition entradaTarjeta = new ParallelTransition(fade, subida);
            entradaTarjeta.setDelay(Duration.millis(60));
 
            secuencia.getChildren().add(entradaTarjeta);
        }
 
        secuencia.play();
    }
 
 
    //====================================================
    // EFECTOS
    //====================================================
 
    /**
     * Agrega un ligero efecto de "levantado" a las tarjetas del dashboard.
     */
    private void configurarEfectosTarjetas() {
        VBox[] tarjetas = { cardEspacios, cardAsignaciones, cardDisponibles, cardAvisos };
 
        for (VBox tarjeta : tarjetas) {
            if (tarjeta == null) continue;
            aplicarEfectoHover(tarjeta);
        }
    }
 
    private void aplicarEfectoHover(Node nodo) {
        nodo.setOnMouseEntered(evento -> {
            TranslateTransition subida = new TranslateTransition(DURACION_CORTA, nodo);
            subida.setToY(-4.0);
 
            ScaleTransition escala = new ScaleTransition(DURACION_CORTA, nodo);
            escala.setToX(1.02);
            escala.setToY(1.02);
 
            new ParallelTransition(subida, escala).play();
        });
 
        nodo.setOnMouseExited(evento -> {
            TranslateTransition bajada = new TranslateTransition(DURACION_CORTA, nodo);
            bajada.setToY(0.0);
 
            ScaleTransition escala = new ScaleTransition(DURACION_CORTA, nodo);
            escala.setToX(1.0);
            escala.setToY(1.0);
 
            new ParallelTransition(bajada, escala).play();
        });
    }
 
    /** Version mas discreta del hover, para celdas pequenas como los dias del calendario. */
    private void aplicarEfectoHoverSuave(Node nodo) {
        nodo.setOnMouseEntered(evento -> {
            ScaleTransition escala = new ScaleTransition(DURACION_CORTA, nodo);
            escala.setToX(1.12);
            escala.setToY(1.12);
            escala.play();
        });
        nodo.setOnMouseExited(evento -> {
            ScaleTransition escala = new ScaleTransition(DURACION_CORTA, nodo);
            escala.setToX(1.0);
            escala.setToY(1.0);
            escala.play();
        });
    }
 
 
    //====================================================
    // METODOS AUXILIARES
    //====================================================
 
    /**
     * TODO: aqui conectamos mi DAO para guardar listaDetalle.getItems()
     * en tu base de datos real (INSERT/UPDATE/DELETE segun
     * corresponda comparando contra el estado previo)
     */
    @FXML
    private void onGuardarCambios(ActionEvent event) {
        int totalFilas = listaDetalle.getItems().size();
        System.out.println("Guardar cambios -> " + totalFilas + " tarjetas");
 
        mostrarNotificacion(
                "Cambios guardados",
                "Se guardaron " + totalFilas + " registros correctamente.",
                Alert.AlertType.INFORMATION);
 
        actualizarDashboard();
    }
 
    /**
     * Elimina de verdad el registro (espacio/asignacion/aviso) segun el
     * tipo de modal abierto, y solo si la base de datos confirma el
     * borrado quita la tarjeta de la lista y refresca el Dashboard.
     */
    private void eliminarItem(DetalleItem item) {
        if (item.getId() <= 0) {
            // Fila nueva que nunca se guardo en la BD: solo quitarla localmente.
            listaDetalle.getItems().remove(item);
            return;
        }
 
        boolean eliminado = switch (modalActual) {
            case ESPACIOS, DISPONIBLES -> espacioDAO.eliminar(item.getId());
            case ASIGNACIONES -> asignacionDAO.eliminar(item.getId());
            case AVISOS -> avisoDAO.eliminar(item.getId());
        };
 
        if (eliminado) {
            listaDetalle.getItems().remove(item);
            actualizarDashboard();
        } else {
            mostrarNotificacion(
                    "No se pudo eliminar",
                    "Este registro no se puede eliminar (puede tener datos relacionados, por ejemplo un espacio con asignaciones).",
                    Alert.AlertType.WARNING);
        }
    }
 
    /**
     * Guarda en la base de datos real los 4 campos editables de una
     * tarjeta del modal, segun el tipo de modal abierto. Devuelve
     * false si no se pudo guardar (para no cerrar el modo edicion).
     *
     * Nota: las Asignaciones no se editan desde aqui (ver btnEditar) porque
     * el modal solo tiene 4 campos genericos y una asignacion real necesita
     * mas datos (profesor, materia, grupo, motivo, etc.).
     */
    private boolean guardarItemEnBaseDeDatos(DetalleItem item) {
        switch (modalActual) {
            case ESPACIOS, DISPONIBLES -> {
                EspacioRegistro actual = espacioDAO.buscarPorId(item.getId());
                if (actual == null) return false;
 
                int capacidad;
                try {
                    capacidad = Integer.parseInt(item.getCapacidad().replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ex) {
                    capacidad = actual.getCapacidad();
                }
 
                EspacioRegistro actualizado = new EspacioRegistro(
                        actual.getClave(),
                        item.getNombre(),
                        item.getTipo(),
                        capacidad,
                        item.getEstado(),
                        actual.getDescripcion());
 
                return espacioDAO.actualizar(item.getId(), actualizado);
            }
            case AVISOS -> {
                return avisoDAO.actualizarCampos(item.getId(), item.getTipo(), item.getNombre(), item.getEstado());
            }
            default -> {
                return false;
            }
        }
    }
 
    /**
     * Muestra una notificacion simple al usuario mediante un Alert
     * nativo de JavaFX
     */
    private void mostrarNotificacion(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
 
}