package mx.utng.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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

/**
 * Controlador de la pantalla "Asignaciones" (fx_asignaciones.fxml).
 *
 * Administra el formulario para dar de alta una nueva asignación
 * (reservación de un espacio) y la tabla de asignaciones registradas.
 *
 * Este controlador es solo del CONTENIDO: el sidebar, el topbar y el
 * cierre de sesión los maneja MenuController, que es quien carga este
 * FXML dentro de su contentPane.
 */
public class AsignacionesController implements Initializable {

    // Referencia al menú, para poder abrir la pantalla de Disponibilidad completa
    private MenuController menuController;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    // --------------------------- Formulario ---------------------------
    @FXML private ComboBox<String> cmbSolicitante;
    @FXML private TextField txtNombreSolicitante;
    @FXML private ComboBox<String> cmbProfesor;
    @FXML private CheckBox chkSinProfesor;

    @FXML private ComboBox<String> cmbEspacio;
    @FXML private ComboBox<String> cmbCarrera;
    @FXML private CheckBox chkOtraCarrera;
    @FXML private TextField txtOtraCarrera;
    @FXML private TextField txtMateria;
    @FXML private TextField txtGrupo;

    @FXML private TextField txtNumAlumnos;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHoraInicio;
    @FXML private TextField txtHoraTermino;

    @FXML private TextArea txtActividad;

    @FXML private Button btnLimpiar;
    @FXML private Button btnGuardar;

    // ------------------------- Disponibilidad -------------------------
    @FXML private Label lblDisponibles;
    @FXML private Label lblOcupados;
    @FXML private Label lblMantenimiento;
    @FXML private Label lblCancelados;

    // ----------------------------- Tabla -----------------------------
    @FXML private TextField txtBuscar;
    @FXML private Button btnFiltros;
    @FXML private TableView<Asignaciones> tablaAsignaciones;
    @FXML private TableColumn<Asignaciones, String> colId;
    @FXML private TableColumn<Asignaciones, String> colFecha;
    @FXML private TableColumn<Asignaciones, String> colHora;
    @FXML private TableColumn<Asignaciones, String> colEspacio;
    @FXML private TableColumn<Asignaciones, String> colSolicitante;
    @FXML private TableColumn<Asignaciones, String> colProfesor;
    @FXML private TableColumn<Asignaciones, String> colMateria;
    @FXML private TableColumn<Asignaciones, String> colGrupo;
    @FXML private TableColumn<Asignaciones, String> colActividad;
    @FXML private TableColumn<Asignaciones, String> colEstado;
    @FXML private TableColumn<Asignaciones, Void> colAcciones;

    @FXML private Pagination paginacion;

    private final ObservableList<Asignaciones> listaAsignaciones = FXCollections.observableArrayList();

    private final AsignacionDAO asignacionDAO = new AsignacionDAO();

    // Texto que ve el usuario en cada combo -> ID real que se guarda en la BD
    private Map<String, Integer> mapaEspacios = new LinkedHashMap<>();
    private Map<String, Integer> mapaProfesores = new LinkedHashMap<>();
    private Map<String, Integer> mapaCarreras = new LinkedHashMap<>();

    /** Asignación que se está editando (null = el formulario va a crear una nueva). */
    private Asignaciones asignacionEnEdicion = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarCombos();
        configurarColumnas();
        configurarToggleOtraCarrera();
        cargarDatosReales();
        tablaAsignaciones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaAsignaciones.setItems(listaAsignaciones);
    }

    /**
     * Cuando se marca "Otra carrera", esconde el combo de carreras
     * catalogadas y muestra un campo de texto libre (y viceversa).
     */
    private void configurarToggleOtraCarrera() {
        chkOtraCarrera.selectedProperty().addListener((obs, antes, marcado) -> {
            txtOtraCarrera.setVisible(marcado);
            txtOtraCarrera.setManaged(marcado);
            cmbCarrera.setDisable(marcado);
            if (marcado) {
                cmbCarrera.setValue(null);
            } else {
                txtOtraCarrera.clear();
            }
        });
    }

    // ============================================================
    //  Combos del formulario (cargados desde la base de datos)
    // ============================================================

    private void cargarCombos() {
        if (cmbSolicitante != null) {
            cmbSolicitante.setItems(FXCollections.observableArrayList(
                    "Profesor", "Administrativo", "Alumno", "Otro"));
        }

        mapaProfesores = asignacionDAO.listarProfesores();
        if (cmbProfesor != null) {
            cmbProfesor.setItems(FXCollections.observableArrayList(new ArrayList<>(mapaProfesores.keySet())));
        }

        mapaEspacios = asignacionDAO.listarEspacios();
        if (cmbEspacio != null) {
            cmbEspacio.setItems(FXCollections.observableArrayList(new ArrayList<>(mapaEspacios.keySet())));
        }

        mapaCarreras = asignacionDAO.listarCarreras();
        if (cmbCarrera != null) {
            cmbCarrera.setItems(FXCollections.observableArrayList(new ArrayList<>(mapaCarreras.keySet())));
        }
    }

    /**
     * Trae de tb_asignacion (con sus catálogos) las asignaciones reales
     * y refresca la tabla. Se llama al abrir la pantalla y después de
     * guardar o eliminar, para que la tabla siempre refleje la BD.
     */
    private void cargarDatosReales() {
        listaAsignaciones.setAll(asignacionDAO.listarTodas());
    }

    // ============================================================
    //  Tabla de asignaciones
    // ============================================================

    private void configurarColumnas() {
        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colFecha.setCellValueFactory(data -> data.getValue().fechaProperty());
        colHora.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getHora()));
        colEspacio.setCellValueFactory(data -> data.getValue().espacioProperty());
        colProfesor.setCellValueFactory(data -> data.getValue().profesorProperty());
        colMateria.setCellValueFactory(data -> data.getValue().materiaProperty());
        colGrupo.setCellValueFactory(data -> data.getValue().grupoProperty());
        colActividad.setCellValueFactory(data -> data.getValue().actividadProperty());

        // Columna "Solicitante": texto con color según tipoSolicitante
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
                getStyleClass().removeAll("tag-profesor", "tag-administrativo", "tag-alumno", "tag-otro");
                switch (tipo) {
                    case "Profesor" -> getStyleClass().add("tag-profesor");
                    case "Administrativo" -> getStyleClass().add("tag-administrativo");
                    case "Alumno" -> getStyleClass().add("tag-alumno");
                    default -> getStyleClass().add("tag-otro");
                }
            }
        });

        // Columna "Estado": badge de color
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
                // Estado en tb_asignacion es ENUM('Libre','Ocupado','Reservado','Cancelado')
                badge.getStyleClass().removeAll("badge-confirmada", "badge-pendiente", "badge-cancelada");
                switch (estado) {
                    case "Ocupado" -> badge.getStyleClass().add("badge-confirmada");
                    case "Reservado" -> badge.getStyleClass().add("badge-pendiente");
                    case "Libre" -> badge.getStyleClass().add("badge-confirmada");
                    default -> badge.getStyleClass().add("badge-cancelada"); // Cancelado
                }
                setGraphic(badge);
            }
        });

        // Columna "Acciones": ver / editar / eliminar
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
        txtNombreSolicitante.setText(asignacion.getNombreSolicitante());

        boolean sinProfesor = "—".equals(asignacion.getProfesor());
        chkSinProfesor.setSelected(sinProfesor);
        cmbProfesor.setValue(sinProfesor ? null : asignacion.getProfesor());

        cmbEspacio.setValue(asignacion.getEspacio());

        boolean carreraReconocida = mapaCarreras.containsKey(asignacion.getCarrera());
        chkOtraCarrera.setSelected(!carreraReconocida);
        cmbCarrera.setValue(carreraReconocida ? asignacion.getCarrera() : null);
        cmbCarrera.setDisable(!carreraReconocida);
        txtOtraCarrera.setText(carreraReconocida ? "" : asignacion.getCarrera());
        txtOtraCarrera.setVisible(!carreraReconocida);
        txtOtraCarrera.setManaged(!carreraReconocida);

        txtMateria.setText(asignacion.getMateria());
        txtGrupo.setText(asignacion.getGrupo());
        txtNumAlumnos.setText(asignacion.getNumAlumnos());

        dpFecha.setValue(LocalDate.parse(asignacion.getFecha(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtHoraInicio.setText(asignacion.getHoraInicio());
        txtHoraTermino.setText(asignacion.getHoraTermino());
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
                    listaAsignaciones.remove(asignacion);
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
        if (cmbSolicitante.getValue() == null || txtNombreSolicitante.getText().isBlank()
                || cmbEspacio.getValue() == null || txtMateria.getText().isBlank()
                || txtGrupo.getText().isBlank() || txtNumAlumnos.getText().isBlank()
                || dpFecha.getValue() == null || txtHoraInicio.getText().isBlank()
                || txtHoraTermino.getText().isBlank() || txtActividad.getText().isBlank()) {
            mostrarAlerta(AlertType.WARNING, "Campos incompletos",
                    "Por favor llena todos los campos obligatorios (*) antes de guardar.");
            return;
        }

        if (!txtNumAlumnos.getText().matches("\\d+")) {
            mostrarAlerta(AlertType.WARNING, "Número de alumnos inválido",
                    "Escribe solo números en \"Número de alumnos\".");
            return;
        }

        int idEspacio = mapaEspacios.getOrDefault(cmbEspacio.getValue(), -1);
        if (idEspacio == -1) {
            mostrarAlerta(AlertType.ERROR, "Espacio inválido",
                    "No se encontró el espacio seleccionado en la base de datos.");
            return;
        }

        Integer idProfesor = chkSinProfesor.isSelected()
                ? null
                : mapaProfesores.get(cmbProfesor.getValue());

        if (chkOtraCarrera.isSelected() && txtOtraCarrera.getText().isBlank()) {
            mostrarAlerta(AlertType.WARNING, "Falta la carrera",
                    "Escribe el nombre de la carrera en \"Otra carrera\".");
            return;
        }

        Integer idCarrera = chkOtraCarrera.isSelected()
                ? null
                : mapaCarreras.get(cmbCarrera.getValue());
        String otraCarreraTexto = chkOtraCarrera.isSelected() ? txtOtraCarrera.getText().trim() : null;

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
                txtHoraInicio.getText(),
                txtHoraTermino.getText(),
                cmbEspacio.getValue(),
                cmbSolicitante.getValue(),
                txtNombreSolicitante.getText(),
                chkSinProfesor.isSelected() ? "—" : safe(cmbProfesor.getValue()),
                chkOtraCarrera.isSelected() ? otraCarreraTexto : safe(cmbCarrera.getValue()),
                txtMateria.getText(),
                txtGrupo.getText(),
                txtNumAlumnos.getText(),
                txtActividad.getText(),
                "Reservado"
        );

        boolean guardada;
        String mensajeExito;

        if (asignacionEnEdicion != null) {
            guardada = asignacionDAO.actualizar(asignacionEnEdicion.getIdAsignacion(),
                    datosFormulario, idUsuarioActual, idEspacio, idProfesor, idCarrera, otraCarreraTexto);
            datosFormulario.setId(asignacionEnEdicion.getId());
            mensajeExito = "La asignación " + datosFormulario.getId() + " se actualizó correctamente.";
        } else {
            guardada = asignacionDAO.insertar(
                    datosFormulario, idUsuarioActual, idEspacio, idProfesor, idCarrera, otraCarreraTexto);
            mensajeExito = "La asignación " + datosFormulario.getId() + " se registró correctamente.";
        }

        if (!guardada) {
            mostrarAlerta(AlertType.ERROR, "No se pudo guardar",
                    "Ocurrió un problema al guardar la asignación en la base de datos. "
                            + "Revisa la consola para más detalle.");
            return;
        }

        cargarDatosReales();
        onLimpiar(null);
        mostrarAlerta(AlertType.INFORMATION, "Asignación guardada", mensajeExito);
    }

    private String safe(String valor) {
        return valor == null ? "—" : valor;
    }

    @FXML
    private void onLimpiar(ActionEvent event) {
        cmbSolicitante.setValue(null);
        txtNombreSolicitante.clear();
        cmbProfesor.setValue(null);
        chkSinProfesor.setSelected(false);
        cmbEspacio.setValue(null);
        cmbCarrera.setValue(null);
        chkOtraCarrera.setSelected(false);
        txtOtraCarrera.clear();
        txtOtraCarrera.setVisible(false);
        txtOtraCarrera.setManaged(false);
        cmbCarrera.setDisable(false);
        txtMateria.clear();
        txtGrupo.clear();
        txtNumAlumnos.clear();
        dpFecha.setValue(null);
        txtHoraInicio.clear();
        txtHoraTermino.clear();
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

    @FXML
    private void onVerMasDisponibilidad(javafx.scene.input.MouseEvent event) {
        if (menuController != null) {
            menuController.abrirModulo("fx_disponibilidad");
        }
    }

}
 