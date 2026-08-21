package mx.utng.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import mx.utng.dao.AvisoDAO;
import mx.utng.model.Aviso;

/**
 * Controlador de la pantalla "Avisos" (fx_avisos.fxml).
 *
 * Muestra el historial de avisos/incidencias guardado en tb_aviso,
 * permite filtrarlo, registrar nuevos avisos, marcarlos como leídos
 * y eliminarlos. Todas las operaciones se hacen contra la base de
 * datos a través de AvisoDAO.
 */
public class AvisosController implements Initializable {

    // -------- Filtros --------
    @FXML private ComboBox<String> cmbFiltroEspacio;
    @FXML private ComboBox<String> cmbFiltroTipo;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Button btnLimpiarFiltros;
    @FXML private Button btnBuscar;
    @FXML private TextArea txtDescripcion;
    @FXML private TextArea txtComentarios;

    // -------- Encabezado / acciones --------
    @FXML private Button btnNuevoAviso;
    @FXML private Button btnMarcarLeidos;
    @FXML private Label lblContador;

    // -------- Tabla --------
    @FXML private TableView<Aviso> tblAvisos;
    @FXML private TableColumn<Aviso, String> colFecha;
    @FXML private TableColumn<Aviso, String> colEspacio;
    @FXML private TableColumn<Aviso, String> colHoraInicio;
    @FXML private TableColumn<Aviso, String> colHoraTermino;
    @FXML private TableColumn<Aviso, String> colTipo;
    @FXML private TableColumn<Aviso, String> colDescripcion;
    @FXML private TableColumn<Aviso, String> colComentarios;
    @FXML private TableColumn<Aviso, String> colEstado;
    @FXML private TableColumn<Aviso, Void> colAcciones;

    // -------- Resumen --------
    @FXML private Label lblInformacion;
    @FXML private Label lblAdvertencia;
    @FXML private Label lblError;
    @FXML private Label lblExito;

    private static final DateTimeFormatter FORMATO_FECHA_UI = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] TIPOS = { "Información", "Advertencia", "Error", "Éxito" };
    private static final String[] ESTADOS = { "No leído", "Leído" };

    private AvisoDAO avisoDAO = new AvisoDAO();


    /** texto del combo -> ID_Espacio real en BD (para el filtro y el dialogo "Nuevo aviso") */
    private Map<String, Integer> mapaEspacios;

    private final ObservableList<Aviso> avisos = FXCollections.observableArrayList();
    private FilteredList<Aviso> avisosFiltrados;

    /** referencia al menú principal, para saber qué usuario está en sesión */
    private MenuController menuController;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarCatalogos();
        configurarTabla();             // Aquí se crea 'avisosFiltrados'
        cargarDatos();                 // Aquí se llena la lista 'avisos' con la BD
        configurarFiltrosIniciales();
    }

    // ============================================================
    //  CARGA DE DATOS
    // ============================================================

    private void cargarCatalogos() {
        mapaEspacios = avisoDAO.listarEspacios();

        cmbFiltroEspacio.getItems().add("Todos");
        cmbFiltroEspacio.getItems().addAll(mapaEspacios.keySet());
        cmbFiltroEspacio.setValue("Todos");

        cmbFiltroTipo.getItems().add("Todos");
        cmbFiltroTipo.getItems().addAll(TIPOS);
        cmbFiltroTipo.setValue("Todos");

        cmbFiltroEstado.getItems().add("Todos");
        cmbFiltroEstado.getItems().addAll(ESTADOS);
        cmbFiltroEstado.setValue("Todos");
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colEspacio.setCellValueFactory(new PropertyValueFactory<>("espacio"));
        colHoraInicio.setCellValueFactory(new PropertyValueFactory<>("horaInicio"));
        colHoraTermino.setCellValueFactory(new PropertyValueFactory<>("horaTermino"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoAviso"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colComentarios.setCellValueFactory(new PropertyValueFactory<>("comentarios"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colTipo.setCellFactory(col -> new TipoBadgeCell());
        colEstado.setCellFactory(col -> new EstadoBadgeCell());
        colAcciones.setCellFactory(col -> new AccionesCell());

        avisosFiltrados = new FilteredList<>(avisos, a -> true);

        tblAvisos.setItems(avisosFiltrados);
    }

    /** Trae los avisos y el resumen desde la base de datos y refresca la pantalla. */
    private void cargarDatos() {
        avisos.setAll(avisoDAO.listarTodos());
        actualizarResumen();
        actualizarContador();
    }

    private void configurarFiltrosIniciales() {
        aplicarFiltros();
    }

    // ============================================================
    //  FILTROS
    // ============================================================

    

    @FXML
    private void onBuscar() {
        buscar();
    }

    private void buscar() {

    Integer idEspacio = null;
    String espacioSel = cmbFiltroEspacio.getValue();
    if (espacioSel != null && !espacioSel.equals("Todos")) {
        idEspacio = mapaEspacios.get(espacioSel);
    }

    String tipoSel = cmbFiltroTipo.getValue();
    String tipo = (tipoSel != null && !tipoSel.equals("Todos")) ? tipoSel : null;

    String estadoSel = cmbFiltroEstado.getValue();
    String estado = (estadoSel != null && !estadoSel.equals("Todos")) ? estadoSel : null;

    String descripcion = (txtDescripcion != null) ? txtDescripcion.getText() : "";
    String comentarios = (txtComentarios != null) ? txtComentarios.getText() : "";

    LocalDate fechaDesde = dpDesde.getValue();
    LocalDate fechaHasta = dpHasta.getValue();

    var resultados = avisoDAO.buscarAvisos(
        idEspacio,
        tipo, descripcion, comentarios,
        estado,
        fechaDesde, fechaHasta
    );

    avisos.setAll(resultados);
    lblContador.setText(String.valueOf(resultados.size()));
}


    @FXML
    private void onLimpiarFiltros() {
        cmbFiltroEspacio.setValue("Todos");
        cmbFiltroTipo.setValue("Todos");
        cmbFiltroEstado.setValue("Todos");
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        aplicarFiltros();
    }

private void aplicarFiltros() {
    String espacio = cmbFiltroEspacio.getValue();
    String tipo = cmbFiltroTipo.getValue();
    String estado = cmbFiltroEstado.getValue();
    LocalDate desde = dpDesde.getValue();
    LocalDate hasta = dpHasta.getValue();

    DateTimeFormatter fmtUI = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    avisosFiltrados.setPredicate(aviso -> {
        boolean coincideEspacio = espacio == null || espacio.equals("Todos") || espacio.equals(aviso.getEspacio());
        boolean coincideTipo = tipo == null || tipo.equals("Todos") || tipo.equals(aviso.getTipoAviso());
        boolean coincideEstado = estado == null || estado.equals("Todos") || estado.equals(aviso.getEstado());

        String fechaStr = aviso.getFecha();
        if (fechaStr == null || fechaStr.isBlank()) return false;

        LocalDate fechaAviso;
        try {
            // Asumiendo que aviso.getFecha() es exactamente dd/MM/yyyy
            fechaAviso = LocalDate.parse(fechaStr, fmtUI);
        } catch (Exception e) {
            return false; // evita que reviente y desaparezca el FXML
        }

        boolean coincideDesde = desde == null || !fechaAviso.isBefore(desde);
        boolean coincideHasta = hasta == null || !fechaAviso.isAfter(hasta);

        return coincideEspacio && coincideTipo && coincideEstado && coincideDesde && coincideHasta;
    });

    actualizarContador();
}


    private void actualizarContador() {
        int total = avisosFiltrados == null ? 0 : avisosFiltrados.size();
        lblContador.setText(String.valueOf(total));
    }

    private void actualizarResumen() {
        Map<String, Integer> conteo = avisoDAO.contarPorTipo();
        lblInformacion.setText(String.valueOf(conteo.getOrDefault("Información", 0)));
        lblAdvertencia.setText(String.valueOf(conteo.getOrDefault("Advertencia", 0)));
        lblError.setText(String.valueOf(conteo.getOrDefault("Error", 0)));
        lblExito.setText(String.valueOf(conteo.getOrDefault("Éxito", 0)));
    }

    // ============================================================
    //  NUEVO AVISO
    // ============================================================

    @FXML
    private void onNuevoAviso() {
        Dialog<Aviso> dialogo = construirDialogoNuevoAviso();
        Optional<Aviso> resultado = dialogo.showAndWait();

        resultado.ifPresent(nuevo -> {
            Integer idEspacio = nuevo.getIdEspacio();
            int idUsuario = (menuController != null) ? menuController.getIdUsuarioActual() : 1;

            if (idUsuario <= 0) {
                mostrarAlerta(AlertType.WARNING, "Sesión no disponible",
                        "No se pudo identificar al usuario en sesión. Vuelve a iniciar sesión e intenta de nuevo.");
                return;
            }

            boolean guardado = avisoDAO.insertar(nuevo, idEspacio, idUsuario);
            if (guardado) {
                cargarDatos();
                aplicarFiltros();
            } else {
                mostrarAlerta(AlertType.ERROR, "No se pudo guardar",
                        "Ocurrió un error al registrar el aviso en la base de datos. Intenta de nuevo.");
            }
        });
    }

    /**
     * Construye el dialogo "Nuevo aviso" (Tipo, Espacio opcional,
     * Descripción, Comentarios) con la misma temática visual del
     * resto del sistema.
     */
private Dialog<Aviso> construirDialogoNuevoAviso() {
    Dialog<Aviso> dialogo = new Dialog<>();
    dialogo.setTitle("Nuevo aviso");
    dialogo.setHeaderText("Registrar nuevo aviso");

    DialogPane panel = dialogo.getDialogPane();
    panel.getStylesheets().add(getClass().getResource("/mx/utng/view/styles_avisos.css").toExternalForm());
    panel.getStyleClass().add("themed-dialog");
    panel.setMinWidth(440.0);
    panel.setMaxWidth(900.0);
    panel.setMaxHeight(700.0); // Aumentado para acomodar nuevos campos

    Label icono = new Label("📢");
    icono.getStyleClass().add("header-icon");
    StackPane cajaIcono = new StackPane(icono);
    cajaIcono.getStyleClass().add("header-icon-box");
    panel.setGraphic(cajaIcono);

    ButtonType btnGuardarType = new ButtonType("Guardar", ButtonData.OK_DONE);
    ButtonType btnCancelarType = new ButtonType("Cancelar", ButtonData.CANCEL_CLOSE);
    panel.getButtonTypes().addAll(btnCancelarType, btnGuardarType);

    ComboBox<String> cmbTipo = new ComboBox<>();
    cmbTipo.getItems().addAll(TIPOS);
    cmbTipo.setPromptText("Selecciona un tipo");
    cmbTipo.getStyleClass().add("dialog-combo");
    cmbTipo.setMaxWidth(Double.MAX_VALUE);

    ComboBox<String> cmbEspacio = new ComboBox<>();
    cmbEspacio.getItems().add("General (ningún espacio en particular)");
    cmbEspacio.getItems().addAll(mapaEspacios.keySet());
    cmbEspacio.setValue("General (ningún espacio en particular)");
    cmbEspacio.getStyleClass().add("dialog-combo");
    cmbEspacio.setMaxWidth(Double.MAX_VALUE);

    txtDescripcion = new TextArea();
    txtDescripcion.setPromptText("Describe el aviso o la incidencia...");
    txtDescripcion.setPrefRowCount(3);
    txtDescripcion.setWrapText(true);
    txtDescripcion.getStyleClass().add("dialog-textarea");

    txtComentarios = new TextArea();
    txtComentarios.setPromptText("Comentarios adicionales (opcional)...");
    txtComentarios.setPrefRowCount(2);
    txtComentarios.setWrapText(true);
    txtComentarios.getStyleClass().add("dialog-textarea");

    // NUEVOS CAMPOS: Date Picker y Text Fields para horas
    DatePicker fechaPicker = new DatePicker(LocalDate.now());
    fechaPicker.getStyleClass().add("dialog-combo");
    fechaPicker.setMaxWidth(Double.MAX_VALUE);

    TextField txtHoraInicio = new TextField();
    txtHoraInicio.setPromptText("HH:mm (ej: 08:30)");
    txtHoraInicio.getStyleClass().add("dialog-textfield");
    txtHoraInicio.setMaxWidth(Double.MAX_VALUE);

    TextField txtHoraTermino = new TextField();
    txtHoraTermino.setPromptText("HH:mm (ej: 17:00)");
    txtHoraTermino.getStyleClass().add("dialog-textfield");
    txtHoraTermino.setMaxWidth(Double.MAX_VALUE);

    // Contenedor para las horas (lado a lado)
    HBox cajasHoras = new HBox(10,
            campoDialogo("Hora de inicio *", txtHoraInicio),
            campoDialogo("Hora de término *", txtHoraTermino)
    );
    cajasHoras.setHgrow(txtHoraInicio, Priority.ALWAYS);
    cajasHoras.setHgrow(txtHoraTermino, Priority.ALWAYS);

    VBox contenido = new VBox(12,
            campoDialogo("Tipo de aviso *", cmbTipo),
            campoDialogo("Espacio relacionado", cmbEspacio),
            campoDialogo("Fecha *", fechaPicker),
            cajasHoras,
            campoDialogo("Descripción *", txtDescripcion),
            campoDialogo("Comentarios", txtComentarios)
    );
    contenido.setPadding(new Insets(6, 0, 0, 0));
    panel.setContent(contenido);

    // Validar antes de cerrar
    Button btnGuardarNode = (Button) panel.lookupButton(btnGuardarType);
    btnGuardarNode.addEventFilter(javafx.event.ActionEvent.ACTION, evento -> {
        String errorMensaje = "";

        if (cmbTipo.getValue() == null) {
            errorMensaje += "- Selecciona el tipo de aviso\n";
        }
        if (txtDescripcion.getText() == null || txtDescripcion.getText().trim().isEmpty()) {
            errorMensaje += "- Escribe una descripción\n";
        }
        if (fechaPicker.getValue() == null) {
            errorMensaje += "- Selecciona una fecha\n";
        }
        if (!esFormatoHoraValido(txtHoraInicio.getText())) {
            errorMensaje += "- Hora de inicio inválida (formato: HH:mm)\n";
        }
        if (!esFormatoHoraValido(txtHoraTermino.getText())) {
            errorMensaje += "- Hora de término inválida (formato: HH:mm)\n";
        }

        if (!errorMensaje.isEmpty()) {
            mostrarAlerta(AlertType.WARNING, "Campos incompletos o inválidos",
                    "Completa los siguientes campos:\n" + errorMensaje);
            evento.consume();
        }
    });

    dialogo.setResultConverter(boton -> {
        if (boton != btnGuardarType) {
            return null;
        }

        Aviso nuevo = new Aviso(
                0,
                fechaPicker.getValue().format(FORMATO_FECHA_UI),
                "General",
                cmbTipo.getValue(),
                txtDescripcion.getText().trim(),
                txtComentarios.getText() == null ? "" : txtComentarios.getText().trim(),
                "No leído",
                txtHoraInicio.getText().trim(),
                txtHoraTermino.getText().trim()
        );

        // Guardar horas (ajusta según tu clase Aviso)
        nuevo.setHoraInicio(txtHoraInicio.getText().trim());
        nuevo.setHoraTermino(txtHoraTermino.getText().trim());

        String espacioElegido = cmbEspacio.getValue();
        if (espacioElegido != null && mapaEspacios.containsKey(espacioElegido)) {
            nuevo.setIdEspacio(mapaEspacios.get(espacioElegido));
            nuevo.setEspacio(espacioElegido);
        } else {
            nuevo.setIdEspacio(null);
        }

        return nuevo;
    });

    return dialogo;
}

// Método auxiliar para validar formato HH:mm
private boolean esFormatoHoraValido(String hora) {
    if (hora == null || hora.trim().isEmpty()) {
        return false;
    }
    return hora.trim().matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
}

private VBox campoDialogo(String etiqueta, javafx.scene.Node campo) {
    Label lbl = new Label(etiqueta);
    lbl.getStyleClass().add("dialog-field-label");
    VBox contenedor = new VBox(5, lbl, campo);
    return contenedor;
}

    // ============================================================
    //  MARCAR COMO LEIDO
    // ============================================================

    @FXML
    private void onMarcarLeidos() {
        List<Integer> idsPendientes = new ArrayList<>();
        for (Aviso aviso : avisosFiltrados) {
            if ("No leído".equals(aviso.getEstado())) {
                idsPendientes.add(aviso.getIdAviso());
            }
        }

        if (idsPendientes.isEmpty()) {
            mostrarAlerta(AlertType.INFORMATION, "Sin pendientes",
                    "No hay avisos sin leer entre los que se están mostrando.");
            return;
        }

        boolean actualizado = avisoDAO.marcarVariosComoLeidos(idsPendientes);
        if (actualizado) {
            cargarDatos();
            aplicarFiltros();
        } else {
            mostrarAlerta(AlertType.ERROR, "No se pudo actualizar",
                    "Ocurrió un error al marcar los avisos como leídos.");
        }
    }

    private void marcarUnoComoLeido(Aviso aviso) {
        boolean actualizado = avisoDAO.marcarComoLeido(aviso.getIdAviso());
        if (actualizado) {
            aviso.setEstado("Leído");
            tblAvisos.refresh();
            actualizarResumen();
        } else {
            mostrarAlerta(AlertType.ERROR, "No se pudo actualizar",
                    "Ocurrió un error al marcar el aviso como leído.");
        }
    }

    // ============================================================
    //  ELIMINAR
    // ============================================================

    private void eliminarAviso(Aviso aviso) {
        Alert confirmacion = crearDialogoTematico(AlertType.CONFIRMATION, "🗑",
                "Eliminar aviso", "¿Eliminar este aviso?",
                "Esta acción no se puede deshacer.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean eliminado = avisoDAO.eliminar(aviso.getIdAviso());
            if (eliminado) {
                avisos.remove(aviso);
                actualizarResumen();
                actualizarContador();
            } else {
                mostrarAlerta(AlertType.ERROR, "No se pudo eliminar",
                        "Ocurrió un error al eliminar el aviso de la base de datos.");
            }
        }
    }

    // ============================================================
    //  UTILIDADES
    // ============================================================

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        String glifo = tipo == AlertType.ERROR ? "✕" : (tipo == AlertType.INFORMATION ? "ℹ" : "⚠");
        Alert alerta = crearDialogoTematico(tipo, glifo, titulo, null, mensaje);
        alerta.showAndWait();
    }

    private Alert crearDialogoTematico(AlertType tipo, String glifo, String titulo,
                                        String encabezado, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado != null ? encabezado : titulo);
        alerta.setContentText(mensaje);

        DialogPane panel = alerta.getDialogPane();
        panel.getStylesheets().add(getClass().getResource("/mx/utng/view/styles_avisos.css").toExternalForm());
        panel.getStyleClass().add("themed-dialog");
        panel.setMinWidth(440.0);
        panel.setMaxWidth(900.0);
        panel.setMaxHeight(620.0);

        Label icono = new Label(glifo);
        icono.getStyleClass().add("header-icon");
        StackPane cajaIcono = new StackPane(icono);
        cajaIcono.getStyleClass().add("header-icon-box");
        panel.setGraphic(cajaIcono);

        return alerta;
    }

    // ==================== CELDAS PERSONALIZADAS ====================

    private class TipoBadgeCell extends TableCell<Aviso, String> {
        private final Label badge = new Label();

        TipoBadgeCell() {
            badge.getStyleClass().add("tipo-badge");
        }

        @Override
        protected void updateItem(String tipo, boolean empty) {
            super.updateItem(tipo, empty);
            if (empty || tipo == null) {
                setGraphic(null);
                return;
            }
            badge.setText(tipo);
            badge.getStyleClass().removeIf(s -> s.startsWith("tipo-") && !s.equals("tipo-badge"));
            if ("Información".equals(tipo)) {
                badge.getStyleClass().add("tipo-informacion");
            } else if ("Advertencia".equals(tipo)) {
                badge.getStyleClass().add("tipo-advertencia");
            } else if ("Error".equals(tipo)) {
                badge.getStyleClass().add("tipo-error");
            } else if ("Éxito".equals(tipo)) {
                badge.getStyleClass().add("tipo-exito");
            }
            setGraphic(badge);
        }
    }

    private class EstadoBadgeCell extends TableCell<Aviso, String> {
        private final Label badge = new Label();

        EstadoBadgeCell() {
            badge.getStyleClass().add("estado-badge");
        }

        @Override
        protected void updateItem(String estado, boolean empty) {
            super.updateItem(estado, empty);
            if (empty || estado == null) {
                setGraphic(null);
                return;
            }
            badge.setText(estado);
            badge.getStyleClass().removeIf(s -> s.startsWith("estado-") && !s.equals("estado-badge"));
            if ("No leído".equals(estado)) {
                badge.getStyleClass().add("estado-no-leido");
            } else {
                badge.getStyleClass().add("estado-leido");
            }
            setGraphic(badge);
        }
    }

    private class AccionesCell extends TableCell<Aviso, Void> {
        private final Button btnLeido = new Button("✓");
        private final Button btnEliminar = new Button("🗑");
        private final HBox contenedor = new HBox(8, btnLeido, btnEliminar);

        AccionesCell() {
            btnLeido.getStyleClass().add("accion-leido-btn");
            btnEliminar.getStyleClass().add("accion-eliminar-btn");
            contenedor.setAlignment(Pos.CENTER);

            btnLeido.setOnAction(e -> {
                Aviso aviso = getTableView().getItems().get(getIndex());
                marcarUnoComoLeido(aviso);
            });
            btnEliminar.setOnAction(e -> {
                Aviso aviso = getTableView().getItems().get(getIndex());
                eliminarAviso(aviso);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                return;
            }
            Aviso aviso = getTableView().getItems().get(getIndex());
            btnLeido.setDisable("Leído".equals(aviso.getEstado()));
            setGraphic(contenedor);
        }
    }
}
