package mx.utng.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import mx.utng.dao.AsignacionDAO;
import mx.utng.model.Asignaciones;

public class AsignacionesController implements Initializable {

    private MenuController menuController;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    // --------------------------- Formulario ---------------------------
    @FXML private ComboBox<String> cmbSolicitante;
    @FXML private ComboBox<String> cmbNombreSolicitante;
    @FXML private ComboBox<String> cmbTipoEspacio;
    @FXML private ComboBox<String> cmbEspacio;
    @FXML private ComboBox<String> cmbCarrera;
    @FXML private TextField txtMateria;
    @FXML private TextField txtGrupo;
    @FXML private TextField txtNumAlumnos;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cmbHoraInicio;
    @FXML private ComboBox<String> cmbHoraTermino;
    @FXML private TextArea txtActividad;

    @FXML private Button btnLimpiar;
    @FXML private Button btnGuardar;

    // ----------------------------- Tabla -----------------------------
    @FXML private TextField txtBuscar;
    @FXML private TableView<Asignaciones> tablaAsignaciones;
    @FXML private TableColumn<Asignaciones, String> colId;
    @FXML private TableColumn<Asignaciones, String> colFecha;
    @FXML private TableColumn<Asignaciones, String> colHora;
    @FXML private TableColumn<Asignaciones, String> colEspacio;
    @FXML private TableColumn<Asignaciones, String> colSolicitante;
    @FXML private TableColumn<Asignaciones, String> colNombreSolicitante;
    @FXML private TableColumn<Asignaciones, String> colMateria;
    @FXML private TableColumn<Asignaciones, String> colGrupo;
    @FXML private TableColumn<Asignaciones, String> colActividad;
    @FXML private TableColumn<Asignaciones, String> colEstado;
    @FXML private TableColumn<Asignaciones, Void> colAcciones;

    @FXML private Pagination paginacion;

    private static final int FILAS_POR_PAGINA = 10;

    private static final String[] CARRERAS = {
        "Licenciatura en Ingeniería en Tecnologías de la Información e Innovación Digital – Desarrollo de Software Multiplataforma",
        "Licenciatura en Ingeniería en Tecnologías de la Información e Innovación Digital – Entornos Virtuales y Negocios Digitales",
        "Redes Digitales",
        "Diseño Gráfico"
    };

    private final ObservableList<Asignaciones> listaAsignaciones = FXCollections.observableArrayList();
    private FilteredList<Asignaciones> asignacionesFiltradas;

    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private Map<String, Integer> mapaEspacios = new LinkedHashMap<>();

    private Asignaciones asignacionEnEdicion = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarCombos();
        configurarColumnas();
        configurarBusqueda();
        cargarDatosReales();
        tablaAsignaciones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ============================================================
    //  Combos y carga de datos
    // ============================================================

    private void cargarCombos() {
        cmbNombreSolicitante.setEditable(true);
        cmbNombreSolicitante.setEditable(true);
cmbNombreSolicitante.getStyleClass().add("combo-box-editable");
        cmbNombreSolicitante.setItems(FXCollections.observableArrayList(asignacionDAO.listarNombresSolicitantes()));
        cmbSolicitante.valueProperty().addListener((obs, antes, tipo) -> {
    cmbNombreSolicitante.setValue(null);
    if ("Maestro".equals(tipo)) {
        cmbNombreSolicitante.setItems(FXCollections.observableArrayList(asignacionDAO.listarNombresProfesores()));
    } else {
        cmbNombreSolicitante.setItems(FXCollections.observableArrayList(asignacionDAO.listarNombresSolicitantes()));
    }
});
        cmbSolicitante.setItems(FXCollections.observableArrayList("Maestro", "Administrativo", "Alumno"));

        cmbCarrera.setItems(FXCollections.observableArrayList(CARRERAS));

        ObservableList<String> horas = generarHoras();
        cmbHoraInicio.setItems(horas);
        cmbHoraTermino.setItems(FXCollections.observableArrayList(horas));

        cmbTipoEspacio.setItems(FXCollections.observableArrayList("Laboratorio", "Aula", "Sala"));
        cmbEspacio.setDisable(true);

        cmbTipoEspacio.valueProperty().addListener((obs, antes, tipo) -> {
            cmbEspacio.setValue(null);
            if (tipo == null) {
                cmbEspacio.setItems(FXCollections.observableArrayList());
                cmbEspacio.setDisable(true);
                return;
            }
            mapaEspacios = asignacionDAO.listarEspaciosPorTipo(tipo);
            cmbEspacio.setItems(FXCollections.observableArrayList(new ArrayList<>(mapaEspacios.keySet())));
            cmbEspacio.setDisable(false);
        });
    }

    private ObservableList<String> generarHoras() {
        ObservableList<String> horas = FXCollections.observableArrayList();
        LocalTime hora = LocalTime.of(7, 0);
        LocalTime fin = LocalTime.of(21, 0);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm");
        while (!hora.isAfter(fin)) {
            horas.add(hora.format(formato));
            hora = hora.plusMinutes(30);
        }
        return horas;
    }

    private void cargarDatosReales() {
        listaAsignaciones.setAll(asignacionDAO.listarTodas());
        actualizarPaginacion();
    }

    // ============================================================
    //  Búsqueda y Paginación
    // ============================================================

    private void configurarBusqueda() {
        asignacionesFiltradas = new FilteredList<>(listaAsignaciones, a -> true);

        txtBuscar.textProperty().addListener((obs, antes, texto) -> {
            String filtro = texto == null ? "" : texto.trim().toLowerCase();
            asignacionesFiltradas.setPredicate(asignacion -> {
                if (filtro.isEmpty()) return true;
                boolean coincideId = asignacion.getId() != null
                        && asignacion.getId().toLowerCase().contains(filtro);
                boolean coincideNombre = asignacion.getNombreSolicitante() != null
                        && asignacion.getNombreSolicitante().toLowerCase().contains(filtro);
                return coincideId || coincideNombre;
            });
            actualizarPaginacion();
        });

        if (paginacion != null) {
            paginacion.setPageFactory(this::crearPagina);
        }
    }

    private void actualizarPaginacion() {
        if (paginacion != null) {
            int numPaginas = (int) Math.ceil((double) asignacionesFiltradas.size() / FILAS_POR_PAGINA);
            paginacion.setPageCount(numPaginas == 0 ? 1 : numPaginas);
            paginacion.setCurrentPageIndex(0);
            paginacion.setPageFactory(this::crearPagina);
        } else {
            tablaAsignaciones.setItems(asignacionesFiltradas);
        }
    }

    private Node crearPagina(int pageIndex) {
        int deIndex = pageIndex * FILAS_POR_PAGINA;
        int paraIndex = Math.min(deIndex + FILAS_POR_PAGINA, asignacionesFiltradas.size());

        if (deIndex > asignacionesFiltradas.size()) {
            tablaAsignaciones.setItems(FXCollections.observableArrayList());
        } else {
            tablaAsignaciones.setItems(FXCollections.observableArrayList(
                    asignacionesFiltradas.subList(deIndex, paraIndex)));
        }
        return tablaAsignaciones;
    }

    // ============================================================
    //  Tabla y Columnas
    // ============================================================

    private void configurarColumnas() {
        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colFecha.setCellValueFactory(data -> data.getValue().fechaProperty());
        colHora.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getHora()));
        colEspacio.setCellValueFactory(data -> data.getValue().espacioProperty());
        colNombreSolicitante.setCellValueFactory(data -> data.getValue().nombreSolicitanteProperty());
        colMateria.setCellValueFactory(data -> data.getValue().materiaProperty());
        colGrupo.setCellValueFactory(data -> data.getValue().grupoProperty());
        colActividad.setCellValueFactory(data -> data.getValue().actividadProperty());

        colSolicitante.setCellValueFactory(data -> data.getValue().tipoSolicitanteProperty());
        colSolicitante.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);
                if (empty || tipo == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(tipo);
                getStyleClass().removeAll("tag-profesor", "tag-administrativo", "tag-alumno");
                switch (tipo) {
                    case "Maestro" -> getStyleClass().add("tag-profesor");
                    case "Administrativo" -> getStyleClass().add("tag-administrativo");
                    case "Alumno" -> getStyleClass().add("tag-alumno");
                    default -> getStyleClass().add("tag-administrativo");
                }
            }
        });

        colEstado.setCellValueFactory(data -> data.getValue().estadoProperty());
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText(estado);
                badge.getStyleClass().removeAll("badge-confirmada", "badge-pendiente", "badge-cancelada");
                switch (estado) {
                    case "Ocupado", "Libre" -> badge.getStyleClass().add("badge-confirmada");
                    case "Asignado" -> badge.getStyleClass().add("badge-pendiente");
                    default -> badge.getStyleClass().add("badge-cancelada");
                }
                setGraphic(badge);
            }
        });

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("👁");
            private final Button btnEditar = new Button("✎");
            private final Button btnEliminar = new Button("🗑");
            private final HBox contenedor = new HBox(4, btnVer, btnEditar, btnEliminar);

            {
                btnVer.getStyleClass().add("table-action-btn");
                btnEditar.getStyleClass().add("table-action-btn");
                btnEliminar.getStyleClass().addAll("table-action-btn", "table-action-btn-danger");
                contenedor.setAlignment(Pos.CENTER_LEFT);

                btnVer.setOnAction(e -> onVerAsignacion(getTableRow().getItem()));
                btnEditar.setOnAction(e -> onEditarAsignacion(getTableRow().getItem()));
                btnEliminar.setOnAction(e -> onEliminarAsignacion(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    private void onVerAsignacion(Asignaciones asignacion) {
        if (asignacion == null) return;
        mostrarAlerta(AlertType.INFORMATION, "Detalle de asignación",
                asignacion.getId() + " · " + asignacion.getEspacio() + " · " + asignacion.getFecha());
    }

    private void onEditarAsignacion(Asignaciones asignacion) {
        if (asignacion == null) return;

        cmbSolicitante.setValue(asignacion.getTipoSolicitante());
        cmbNombreSolicitante.setValue(asignacion.getNombreSolicitante());

        String tipoEspacio = asignacionDAO.obtenerTipoEspacio(asignacion.getEspacio());
        cmbTipoEspacio.setValue(tipoEspacio);
        cmbEspacio.setValue(asignacion.getEspacio());

        cmbCarrera.setValue(asignacion.getCarrera());
        txtMateria.setText(asignacion.getMateria());
        txtGrupo.setText(asignacion.getGrupo());
        txtNumAlumnos.setText(asignacion.getNumAlumnos());

        try {
            dpFecha.setValue(LocalDate.parse(asignacion.getFecha(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } catch (Exception e) {
            dpFecha.setValue(LocalDate.now());
        }

        cmbHoraInicio.setValue(asignacion.getHoraInicio());
        cmbHoraTermino.setValue(asignacion.getHoraTermino());
        txtActividad.setText(asignacion.getActividad());

        asignacionEnEdicion = asignacion;
        btnGuardar.setText("✎  Actualizar asignación");
    }

    private void onEliminarAsignacion(Asignaciones asignacion) {
        if (asignacion == null) return;
        Alert confirm = new Alert(AlertType.CONFIRMATION,
                "¿Eliminar la asignación " + asignacion.getId() + "?");
        confirm.showAndWait().ifPresent(respuesta -> {
            if (respuesta.getButtonData().isDefaultButton()) {
                boolean eliminada = asignacionDAO.eliminar(asignacion.getIdAsignacion());
                if (eliminada) {
                    cargarDatosReales();
                } else {
                    mostrarAlerta(AlertType.ERROR, "No se pudo eliminar",
                            "Ocurrió un problema al eliminar la asignación " + asignacion.getId()
                                    + " de la base de datos.");
                }
            }
        });
    }

    // ============================================================
    //  Acciones del formulario
    // ============================================================

    @FXML
    private void onGuardarAsignacion(ActionEvent event) {
        if (cmbSolicitante.getValue() == null
                || cmbNombreSolicitante.getValue() == null || cmbNombreSolicitante.getValue().isBlank()
                || cmbTipoEspacio.getValue() == null
                || cmbEspacio.getValue() == null
                || cmbCarrera.getValue() == null
                || txtMateria.getText().isBlank()
                || txtGrupo.getText().isBlank()
                || txtNumAlumnos.getText().isBlank()
                || dpFecha.getValue() == null
                || cmbHoraInicio.getValue() == null
                || cmbHoraTermino.getValue() == null
                || txtActividad.getText().isBlank()) {
            mostrarAlerta(AlertType.WARNING, "Campos incompletos",
                    "Por favor llena todos los campos obligatorios (*) antes de guardar.");
            return;
        }

        if (!txtNumAlumnos.getText().matches("\\d+")) {
            mostrarAlerta(AlertType.WARNING, "Número de alumnos inválido",
                    "Escribe solo números en \"Número de alumnos\".");
            return;
        }

        if (cmbHoraTermino.getValue().compareTo(cmbHoraInicio.getValue()) <= 0) {
            mostrarAlerta(AlertType.WARNING, "Horario inválido",
                    "La hora de término debe ser posterior a la hora de inicio.");
            return;
        }
        int idEspacioParaValidar = mapaEspacios.getOrDefault(cmbEspacio.getValue(), -1);
int idAsignacionExcluir = (asignacionEnEdicion != null) ? asignacionEnEdicion.getIdAsignacion() : -1;

boolean hayConflicto = asignacionDAO.existeConflictoHorario(
        idEspacioParaValidar, dpFecha.getValue(),
        cmbHoraInicio.getValue(), cmbHoraTermino.getValue(),
        idAsignacionExcluir);

if (hayConflicto) {
    mostrarAlerta(AlertType.WARNING, "Horario ocupado",
            "El espacio \"" + cmbEspacio.getValue() + "\" ya está asignado ese día en un horario "
                    + "que se cruza con el que capturaste. Elige otro horario o espacio.");
    return;
}

        int idEspacio = mapaEspacios.getOrDefault(cmbEspacio.getValue(), -1);
        if (idEspacio == -1) {
            mostrarAlerta(AlertType.ERROR, "Espacio inválido",
                    "No se encontró el espacio seleccionado en la base de datos.");
            return;
        }

        int idUsuarioActual = (menuController != null) ? menuController.getIdUsuarioActual() : 0;
        if (idUsuarioActual <= 0) {
            mostrarAlerta(AlertType.ERROR, "Sesión no encontrada",
                    "No se pudo identificar al usuario en sesión. Vuelve a iniciar sesión e inténtalo de nuevo.");
            return;
        }

        LocalDate fecha = dpFecha.getValue();
        String fechaTexto = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        Asignaciones datosFormulario = new Asignaciones(
                "",
                fechaTexto,
                cmbHoraInicio.getValue(),
                cmbHoraTermino.getValue(),
                cmbEspacio.getValue(),
                cmbSolicitante.getValue(),
                cmbNombreSolicitante.getValue(),
                "—",
                cmbCarrera.getValue(),
                txtMateria.getText(),
                txtGrupo.getText(),
                txtNumAlumnos.getText(),
                txtActividad.getText(),
                "Asignado"
        );

        boolean guardada;
        String mensajeExito;

        if (asignacionEnEdicion != null) {
            guardada = asignacionDAO.actualizar(asignacionEnEdicion.getIdAsignacion(),
                    datosFormulario, idUsuarioActual, idEspacio);
            datosFormulario.setId(asignacionEnEdicion.getId());
            mensajeExito = "La asignación " + datosFormulario.getId() + " se actualizó correctamente.";
        } else {
            guardada = asignacionDAO.insertar(datosFormulario, idUsuarioActual, idEspacio);
            mensajeExito = "La asignación se registró correctamente.";
        }

        if (!guardada) {
            mostrarAlerta(AlertType.ERROR, "No se pudo guardar",
                    "Ocurrió un problema al guardar la asignación en la base de datos.");
            return;
        }

        cargarDatosReales();
        onLimpiar(null);
        mostrarAlerta(AlertType.INFORMATION, "Asignación guardada", mensajeExito);
    }

    @FXML
    private void onLimpiar(ActionEvent event) {
        cmbSolicitante.setValue(null);
        cmbNombreSolicitante.setValue(null);
        cmbTipoEspacio.setValue(null);
        cmbEspacio.setValue(null);
        cmbEspacio.setDisable(true);
        cmbCarrera.setValue(null);
        txtMateria.clear();
        txtGrupo.clear();
        txtNumAlumnos.clear();
        dpFecha.setValue(null);
        cmbHoraInicio.setValue(null);
        cmbHoraTermino.setValue(null);
        txtActividad.clear();

        asignacionEnEdicion = null;
        btnGuardar.setText("✓  Guardar asignación");
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
