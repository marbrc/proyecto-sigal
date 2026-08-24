package mx.utng.controller;
 
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
 
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
    @FXML private ComboBox<String> cmbCuatrimestre;
    @FXML private ComboBox<String> cmbCarrera;
    @FXML private ComboBox<String> cmbMateria;
    @FXML private ComboBox<String> cmbGrupo;
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
 
    private final ObservableList<Asignaciones> listaAsignaciones = FXCollections.observableArrayList();
    private FilteredList<Asignaciones> asignacionesFiltradas;
 
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private Map<String, Integer> mapaEspacios = new LinkedHashMap<>();
    private Map<String, Integer> mapaCarreras = new LinkedHashMap<>();
    private Asignaciones asignacionEnEdicion = null;
 
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnas();
        cargarCombos();
        configurarBusqueda();
        cargarDatosReales();
    }
 
    private void cargarCombos() {
        cmbSolicitante.setItems(FXCollections.observableArrayList("Profesor", "Administrativo", "Alumno", "Otro"));
 
        cmbNombreSolicitante.setEditable(true);
        cmbNombreSolicitante.getStyleClass().add("combo-box-editable");
 
        cmbNombreSolicitante.setItems(
            FXCollections.observableArrayList(asignacionDAO.listarNombresSolicitantes())
        );
 
        cmbSolicitante.valueProperty().addListener((obs, valorAnterior, tipo) -> {
            cmbNombreSolicitante.getSelectionModel().clearSelection();
            cmbNombreSolicitante.setValue(null);
 
            if (cmbNombreSolicitante.isEditable()) {
                cmbNombreSolicitante.getEditor().clear();
            }
 
            if ("Profesor".equals(tipo) || "Administrativo".equals(tipo)) {
                cmbNombreSolicitante.setItems(
                    FXCollections.observableArrayList(asignacionDAO.listarNombresPersonalPorTipo(tipo))
                );
            } else {
                cmbNombreSolicitante.setItems(
                    FXCollections.observableArrayList(asignacionDAO.listarNombresSolicitantes())
                );
            }
        });
 
        // Cadena dependiente: Carrera -> Cuatrimestre -> Materia -> Grupo
        mapaCarreras = asignacionDAO.listarCarreras();
        cmbCarrera.setItems(FXCollections.observableArrayList(mapaCarreras.keySet()));
        cmbCarrera.setDisable(mapaCarreras.isEmpty());

        cmbCuatrimestre.setItems(FXCollections.observableArrayList(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
        cmbCuatrimestre.setDisable(true);

        cmbMateria.getItems().clear();
        cmbMateria.setDisable(true);
        cmbGrupo.getItems().clear();
        cmbGrupo.setDisable(true);

        cmbCarrera.valueProperty().addListener((obs, oldVal, newVal) -> actualizarCuatrimestrePorCarrera());
        cmbCuatrimestre.valueProperty().addListener((obs, oldVal, newVal) -> actualizarMateriasPorCarreraYCuatri());
        cmbMateria.valueProperty().addListener((obs, oldVal, newVal) -> actualizarGruposPorCarreraYCuatri());

        // Mismos extremos horarios que utiliza el módulo Horarios.
        ObservableList<String> horas = generarHoras();
        cmbHoraInicio.setItems(FXCollections.observableArrayList(horas));
        cmbHoraTermino.setItems(FXCollections.observableArrayList(horas));

        cmbTipoEspacio.setItems(FXCollections.observableArrayList(
            "Lab. de cómputo", "Aula común", "Especializado", "Sala múltiple"
        ));
 
        cmbEspacio.setDisable(true);
        cmbEspacio.getItems().clear();
 
        cmbTipoEspacio.valueProperty().addListener((obs, valorAnterior, tipo) -> {
            cmbEspacio.getSelectionModel().clearSelection();
            cmbEspacio.getItems().clear();
            cmbEspacio.setValue(null);
 
            if (cmbEspacio.isEditable()) {
                cmbEspacio.getEditor().clear();
            }
 
            if (tipo == null || tipo.trim().isEmpty()) {
                cmbEspacio.setDisable(true);
                return;
            }
 
            mapaEspacios = asignacionDAO.listarEspaciosPorTipo(tipo);
            cmbEspacio.setItems(FXCollections.observableArrayList(mapaEspacios.keySet()));
            cmbEspacio.setDisable(mapaEspacios.isEmpty());
        });
    }
 
    private void actualizarCuatrimestrePorCarrera() {
        String carreraSel = cmbCarrera.getValue();

        cmbCuatrimestre.getSelectionModel().clearSelection();
        cmbCuatrimestre.setValue(null);
        cmbMateria.getSelectionModel().clearSelection();
        cmbMateria.setValue(null);
        cmbGrupo.getSelectionModel().clearSelection();
        cmbGrupo.setValue(null);

        cmbMateria.getItems().clear();
        cmbGrupo.getItems().clear();
        cmbMateria.setDisable(true);
        cmbGrupo.setDisable(true);

        boolean carreraValida = carreraSel != null
                && !carreraSel.isBlank()
                && mapaCarreras.containsKey(carreraSel);
        cmbCuatrimestre.setDisable(!carreraValida);
    }

    private void actualizarMateriasPorCarreraYCuatri() {
        String carreraSel = cmbCarrera.getValue();
        String cuatriSel = cmbCuatrimestre.getValue();

        cmbMateria.getSelectionModel().clearSelection();
        cmbMateria.setValue(null);
        cmbGrupo.getSelectionModel().clearSelection();
        cmbGrupo.setValue(null);
        cmbGrupo.getItems().clear();
        cmbGrupo.setDisable(true);

        if (carreraSel == null || carreraSel.isBlank()
                || cuatriSel == null || cuatriSel.isBlank()) {
            cmbMateria.getItems().clear();
            cmbMateria.setDisable(true);
            return;
        }

        Integer idCarrera = mapaCarreras.get(carreraSel);
        if (idCarrera == null) {
            cmbMateria.getItems().clear();
            cmbMateria.setDisable(true);
            return;
        }

        int cuatri = Integer.parseInt(cuatriSel);
        List<String> materias = asignacionDAO.listarMateriasPorCuatrimestreYCarrera(cuatri, idCarrera);
        cmbMateria.setItems(FXCollections.observableArrayList(materias));
        cmbMateria.setDisable(materias.isEmpty());
    }

    private void actualizarGruposPorCarreraYCuatri() {
        String carreraSel = cmbCarrera.getValue();
        String cuatriSel = cmbCuatrimestre.getValue();
        String materiaSel = cmbMateria.getValue();

        cmbGrupo.getSelectionModel().clearSelection();
        cmbGrupo.setValue(null);
        cmbGrupo.getItems().clear();

        if (materiaSel == null || materiaSel.isBlank()
                || carreraSel == null || carreraSel.isBlank()
                || cuatriSel == null || cuatriSel.isBlank()) {
            cmbGrupo.setDisable(true);
            return;
        }

        Integer idCarrera = mapaCarreras.get(carreraSel);
        if (idCarrera == null) {
            cmbGrupo.setDisable(true);
            return;
        }

        int cuatri = Integer.parseInt(cuatriSel);
        List<String> grupos = asignacionDAO.listarGruposPorCuatrimestreYCarrera(cuatri, idCarrera);
        cmbGrupo.setItems(FXCollections.observableArrayList(grupos));
        cmbGrupo.setDisable(grupos.isEmpty());
    }

    private ObservableList<String> generarHoras() {
        // Mismos extremos horarios que utiliza HorarioController.
        return FXCollections.observableArrayList(
                "8:00", "8:50", "9:00", "9:50", "10:00", "10:50",
                "11:00", "11:50", "12:20", "13:10", "13:15", "14:05",
                "14:10", "15:00", "15:10", "16:00", "16:50", "17:00", "17:50",
                "18:20", "19:10", "20:00", "20:05", "20:55", "21:00", "22:00"
        );
    }

 
private void cargarDatosReales() {
    listaAsignaciones.setAll(asignacionDAO.listarTodas());
    actualizarPaginacion();
    tablaAsignaciones.refresh();  // fuerza el repintado de todas las celdas visibles
}
 
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
            // Pagination solo muestra los controles. La TableView permanece en su
            // contenedor original; devolver la misma TableView desde pageFactory
            // hacía que JavaFX la reparentara y por eso desaparecía.
            paginacion.setPageFactory(pageIndex -> new javafx.scene.layout.Region());
            paginacion.currentPageIndexProperty().addListener(
                    (obs, anterior, actual) -> mostrarPagina(actual.intValue()));
        }
    }

    private void actualizarPaginacion() {
        if (paginacion == null) {
            tablaAsignaciones.setItems(asignacionesFiltradas);
            return;
        }

        int numPaginas = Math.max(1,
                (int) Math.ceil((double) asignacionesFiltradas.size() / FILAS_POR_PAGINA));
        paginacion.setPageCount(numPaginas);

        int paginaActual = Math.min(
                paginacion.getCurrentPageIndex(),
                numPaginas - 1
        );
        if (paginaActual < 0) paginaActual = 0;

        if (paginacion.getCurrentPageIndex() != paginaActual) {
            paginacion.setCurrentPageIndex(paginaActual);
        }

        mostrarPagina(paginaActual);
    }

    private void mostrarPagina(int pageIndex) {
        if (asignacionesFiltradas == null) return;

        int inicio = Math.max(0, pageIndex * FILAS_POR_PAGINA);
        int fin = Math.min(inicio + FILAS_POR_PAGINA, asignacionesFiltradas.size());

        if (inicio >= asignacionesFiltradas.size()) {
            tablaAsignaciones.setItems(FXCollections.observableArrayList());
            return;
        }

        tablaAsignaciones.setItems(FXCollections.observableArrayList(
                asignacionesFiltradas.subList(inicio, fin)));
    }

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
                    case "Maestro", "Profesor" -> getStyleClass().add("tag-profesor");
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
 
private void onEditarAsignacion(Asignaciones asignacionFila) {
    if (asignacionFila == null) return;
 
    // Siempre recargamos desde la BD por si el objeto en memoria está desactualizado
    Asignaciones asignacion = asignacionDAO.buscarPorId(asignacionFila.getIdAsignacion());
    if (asignacion == null) asignacion = asignacionFila;
 
    cmbSolicitante.setValue(asignacion.getTipoSolicitante());
        cmbNombreSolicitante.setValue(asignacion.getNombreSolicitante());
 
        String tipoEspacio = asignacionDAO.obtenerTipoEspacio(asignacion.getEspacio());
        cmbTipoEspacio.setValue(tipoEspacio);
        cmbEspacio.setValue(asignacion.getEspacio());
 
        // Precargar en el mismo orden de dependencia del formulario:
        // Carrera -> Cuatrimestre -> Materia -> Grupo.
        cmbCarrera.setValue(asignacion.getCarrera());
        if (asignacion.getCuatrimestre() != null && !asignacion.getCuatrimestre().isBlank()) {
            cmbCuatrimestre.setValue(asignacion.getCuatrimestre());
        }
        cmbMateria.setValue(asignacion.getMateria());
        cmbGrupo.setValue(asignacion.getGrupo());
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
 
@FXML
private void onGuardarAsignacion(ActionEvent event) {
    if (cmbSolicitante.getValue() == null
            || cmbNombreSolicitante.getValue() == null || cmbNombreSolicitante.getValue().isBlank()
            || cmbTipoEspacio.getValue() == null
            || cmbEspacio.getValue() == null
            || cmbCuatrimestre.getValue() == null
            || cmbCarrera.getValue() == null
            || cmbMateria.getValue() == null || cmbMateria.getValue().isBlank()
            || cmbGrupo.getValue() == null
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
 
    // --- Validar que el espacio tenga capacidad suficiente ---
    int idEspacioParaCapacidad = mapaEspacios.getOrDefault(cmbEspacio.getValue(), -1);
    if (idEspacioParaCapacidad == -1) {
        mostrarAlerta(AlertType.ERROR, "Espacio inválido",
                "No se encontró el espacio seleccionado en la base de datos.");
        return;
    }
 
    Integer capacidadMaxima = asignacionDAO.obtenerCapacidadEspacio(idEspacioParaCapacidad);
    int numAlumnos;
    try {
        numAlumnos = Integer.parseInt(txtNumAlumnos.getText().trim());
    } catch (NumberFormatException ex) {
        mostrarAlerta(AlertType.WARNING, "Número de alumnos inválido",
                "La cantidad de alumnos no es válida.");
        return;
    }

    if (capacidadMaxima == null) {
        mostrarAlerta(AlertType.ERROR, "Capacidad no disponible",
                "No se pudo consultar la capacidad máxima del espacio seleccionado. "
                        + "La asignación no puede guardarse hasta verificar ese dato.");
        return;
    }

    if (numAlumnos > capacidadMaxima) {
        mostrarAlerta(AlertType.WARNING, "Capacidad excedida",
                "El espacio \"" + cmbEspacio.getValue() + "\" tiene una capacidad máxima de "
                        + capacidadMaxima + " personas, pero capturaste " + numAlumnos + ". "
                        + "Elige un espacio con mayor capacidad o reduce el número de alumnos.");
        return;
    }
 
    LocalTime horaInicio = LocalTime.parse(
            cmbHoraInicio.getValue(), DateTimeFormatter.ofPattern("H:mm"));
    LocalTime horaTermino = LocalTime.parse(
            cmbHoraTermino.getValue(), DateTimeFormatter.ofPattern("H:mm"));

    if (!horaInicio.isBefore(horaTermino)) {
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
            "",                              // profesor: sin usar por ahora
            cmbCarrera.getValue(),
            cmbMateria.getValue(),
            cmbGrupo.getValue(),
            txtNumAlumnos.getText(),
            txtActividad.getText(),
            "Asignado"
    );
    datosFormulario.setCuatrimestre(cmbCuatrimestre.getValue());
 
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
        cmbCuatrimestre.setValue(null);
        cmbCuatrimestre.setDisable(true);
        cmbMateria.setValue(null);
        cmbMateria.getItems().clear();
        cmbMateria.setDisable(true);
        cmbGrupo.setValue(null);
        cmbGrupo.getItems().clear();
        cmbGrupo.setDisable(true);
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