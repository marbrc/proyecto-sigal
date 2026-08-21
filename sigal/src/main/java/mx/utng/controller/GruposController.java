package mx.utng.controller;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import mx.utng.dao.CarreraDAO;
import mx.utng.dao.GrupoDAO;
import mx.utng.model.Grupo;

/**
 * Controlador de la pantalla "Grupos" (fx_grupos.fxml).
 *
 * Usa EXACTAMENTE las columnas reales de tb_grupo (NombreGrupo,
 * Capacidad, Cuatrimestre, Turno, ID_Carrera) a través de GrupoDAO.
 * Turno respeta el enum('Matutino','Vespertino') real de la BD.
 */
public class GruposController implements Initializable {

    private final GrupoDAO grupoDAO = new GrupoDAO();
    private final CarreraDAO carreraDAO = new CarreraDAO();

    private MenuController menuController;

    /** Cuatrimestres válidos (carreras UTNG van de 1º a 9º cuatrimestre). */
    private static final Integer[] CUATRIMESTRES = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    /** Turnos reales de tb_grupo (columna Turno: enum('Matutino','Vespertino')). */
    private static final String[] TURNOS = {"Matutino", "Vespertino"};

    // -------- Formulario "Datos del grupo" --------
    @FXML private TextField txtNombre;
    @FXML private TextField txtCapacidad;
    @FXML private ComboBox<Integer> cmbCuatrimestre;
    @FXML private ComboBox<String> cmbTurno;
    @FXML private ComboBox<String> cmbCarrera;

    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnNuevoGrupo;

    // -------- Panel "Grupos registrados" --------
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroCarrera;
    @FXML private ComboBox<String> cmbFiltroTurno;
    @FXML private Button btnRefrescar;

    @FXML private TableView<Grupo> tblGrupos;
    @FXML private TableColumn<Grupo, Void> colNo;
    @FXML private TableColumn<Grupo, String> colNombre;
    @FXML private TableColumn<Grupo, Number> colCapacidad;
    @FXML private TableColumn<Grupo, Number> colCuatrimestre;
    @FXML private TableColumn<Grupo, String> colTurno;
    @FXML private TableColumn<Grupo, String> colCarrera;
    @FXML private TableColumn<Grupo, Void> colAcciones;

    @FXML private Label lblResultados;

    // -------- Estado interno --------
    private final ObservableList<Grupo> grupos = FXCollections.observableArrayList();
    private FilteredList<Grupo> gruposFiltrados;

    /** Grupo que se está editando actualmente (null = modo "nuevo grupo"). */
    private Grupo grupoEnEdicion;

    /** Texto mostrado en los combos de carrera -> ID_Carrera real (tb_carrera.ID_Carrera). */
    private Map<String, Integer> mapaCarreras = new LinkedHashMap<>();

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbCuatrimestre.setItems(FXCollections.observableArrayList(CUATRIMESTRES));
        cmbTurno.setItems(FXCollections.observableArrayList(TURNOS));

        cargarCarrerasDisponibles();

        cmbFiltroCarrera.getItems().add("Todas las carreras");
        cmbFiltroCarrera.getItems().addAll(mapaCarreras.keySet());
        cmbFiltroCarrera.setValue("Todas las carreras");

        cmbFiltroTurno.getItems().add("Todos los turnos");
        cmbFiltroTurno.getItems().addAll(TURNOS);
        cmbFiltroTurno.setValue("Todos los turnos");

        cargarDatosIniciales();
        configurarTabla();
        configurarFiltros();
        actualizarContador();
    }

    /** Llena los combos de carrera con las carreras reales de tb_carrera. */
    private void cargarCarrerasDisponibles() {
        mapaCarreras = carreraDAO.listarCarrerasParaVincular();
        cmbCarrera.setItems(FXCollections.observableArrayList(mapaCarreras.keySet()));
    }

    private void cargarDatosIniciales() {
        grupos.setAll(grupoDAO.cargarTabla());
    }

    private void configurarTabla() {
        colNo.setCellFactory(col -> new NumeroFilaCell());
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreGrupo"));
        colCapacidad.setCellValueFactory(new PropertyValueFactory<>("capacidad"));
        colCuatrimestre.setCellValueFactory(new PropertyValueFactory<>("cuatrimestre"));
        colTurno.setCellValueFactory(new PropertyValueFactory<>("turno"));
        colTurno.setCellFactory(col -> new TurnoBadgeCell());
        colCarrera.setCellValueFactory(new PropertyValueFactory<>("nombreCarreraVinculada"));
        colAcciones.setCellFactory(col -> new AccionesCell());

        gruposFiltrados = new FilteredList<>(grupos, g -> true);
        tblGrupos.setItems(gruposFiltrados);
    }

    private void configurarFiltros() {
        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbFiltroCarrera.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbFiltroTurno.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String carrera = cmbFiltroCarrera.getValue();
        String turno = cmbFiltroTurno.getValue();

        gruposFiltrados.setPredicate(grupo -> {
            boolean coincideTexto = texto.isEmpty()
                    || grupo.getNombreGrupo().toLowerCase().contains(texto);
            boolean coincideCarrera = carrera == null || carrera.equals("Todas las carreras")
                    || carrera.equals(grupo.getNombreCarreraVinculada());
            boolean coincideTurno = turno == null || turno.equals("Todos los turnos")
                    || turno.equals(grupo.getTurno());
            return coincideTexto && coincideCarrera && coincideTurno;
        });

        actualizarContador();
        tblGrupos.refresh();
    }

    private void actualizarContador() {
        int mostrados = gruposFiltrados == null ? 0 : gruposFiltrados.size();
        int total = grupos.size();
        if (lblResultados != null) {
            lblResultados.setText("Mostrando " + mostrados + " de " + total + " grupos");
        }
    }

    // ==================== ACCIONES DEL FORMULARIO ====================

    @FXML
    private void onGuardar() {
        String nombre = safeTrim(txtNombre.getText());
        String capacidadTexto = safeTrim(txtCapacidad.getText());
        Integer cuatrimestre = cmbCuatrimestre.getValue();
        String turno = cmbTurno.getValue();
        String carreraSeleccionada = cmbCarrera.getValue();

        if (nombre.isEmpty() || capacidadTexto.isEmpty() || cuatrimestre == null
                || turno == null || carreraSeleccionada == null) {
            mostrarAlerta(AlertType.WARNING, "Campos incompletos",
                    "Por favor completa el nombre, la capacidad, el cuatrimestre, el turno "
                            + "y la carrera antes de guardar.");
            return;
        }

        int capacidad;
        try {
            capacidad = Integer.parseInt(capacidadTexto);
        } catch (NumberFormatException ex) {
            mostrarAlerta(AlertType.WARNING, "Capacidad inválida",
                    "La capacidad debe ser un número entero, por ejemplo 30.");
            return;
        }
        if (capacidad <= 0) {
            mostrarAlerta(AlertType.WARNING, "Capacidad inválida",
                    "La capacidad debe ser mayor que cero.");
            return;
        }

        Integer idCarrera = mapaCarreras.get(carreraSeleccionada);
        if (idCarrera == null) {
            mostrarAlerta(AlertType.ERROR, "Carrera no válida",
                    "La carrera seleccionada ya no está disponible. Actualiza la lista e inténtalo de nuevo.");
            return;
        }

        if (grupoEnEdicion == null) {
            Grupo nuevo = new Grupo(nombre, capacidad, cuatrimestre, turno, null);
            boolean guardado = grupoDAO.insertarGrupo(nuevo, idCarrera);
            if (!guardado) {
                mostrarAlerta(AlertType.ERROR, "No se pudo guardar",
                        "Ocurrió un error al registrar el grupo en la base de datos. Intenta de nuevo.");
                return;
            }
        } else {
            Grupo datosActualizados = new Grupo(nombre, capacidad, cuatrimestre, turno, null);
            boolean actualizado = grupoDAO.actualizarGrupo(grupoEnEdicion.getIdGrupo(), datosActualizados, idCarrera);
            if (!actualizado) {
                mostrarAlerta(AlertType.ERROR, "No se pudo actualizar",
                        "Ocurrió un error al actualizar el grupo en la base de datos. Intenta de nuevo.");
                return;
            }
        }

        cargarDatosIniciales();
        limpiarFormulario();
        aplicarFiltros();
    }

    @FXML
    private void onLimpiar() {
        limpiarFormulario();
    }

    @FXML
    private void onCancelar() {
        limpiarFormulario();
    }

    @FXML
    private void onNuevoGrupo() {
        limpiarFormulario();
        txtNombre.requestFocus();
    }

    @FXML
    private void onRefrescar() {
        txtBuscar.clear();
        cmbFiltroCarrera.setValue("Todas las carreras");
        cmbFiltroTurno.setValue("Todos los turnos");
        cargarCarrerasDisponibles();
        cargarDatosIniciales();
        aplicarFiltros();
    }

    @FXML
    private void onVolverCatalogo() {
        if (menuController != null) {
            menuController.abrirModulo("fx_catalogo_academico");
        }
    }

    private void limpiarFormulario() {
        grupoEnEdicion = null;
        txtNombre.clear();
        txtCapacidad.clear();
        cmbCuatrimestre.setValue(null);
        cmbTurno.setValue(null);
        cmbCarrera.setValue(null);
        btnGuardar.setText("💾  Guardar");
    }

    private void cargarEnFormulario(Grupo grupo) {
        grupoEnEdicion = grupo;
        txtNombre.setText(grupo.getNombreGrupo());
        txtCapacidad.setText(String.valueOf(grupo.getCapacidad()));
        cmbCuatrimestre.setValue(grupo.getCuatrimestre());
        cmbTurno.setValue(grupo.getTurno());

        String textoCarrera = mapaCarreras.entrySet().stream()
                .filter(entry -> entry.getValue() == grupo.getIdCarrera())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        cmbCarrera.setValue(textoCarrera);

        btnGuardar.setText("💾  Guardar cambios");
    }

    private void eliminarGrupo(Grupo grupo) {
        Alert confirmacion = crearDialogoTematico(AlertType.CONFIRMATION, "🗑",
                "Eliminar grupo", "¿Eliminar \"" + grupo.getNombreGrupo() + "\"?",
                "Esta acción no se puede deshacer. No se puede eliminar un grupo que "
                        + "tenga asignaciones registradas.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean eliminado = grupoDAO.eliminarGrupo(grupo.getIdGrupo());
            if (!eliminado) {
                mostrarAlerta(AlertType.WARNING, "No se pudo eliminar",
                        "Este grupo tiene asignaciones relacionadas, así que no se puede "
                                + "eliminar mientras existan.");
                return;
            }
            grupos.remove(grupo);
            if (grupoEnEdicion == grupo) {
                limpiarFormulario();
            }
            actualizarContador();
        }
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        String glifo = tipo == AlertType.ERROR ? "✕" : "⚠";
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
        panel.getStylesheets().add(getClass().getResource("/mx/utng/view/styles_catalogo_crud.css").toExternalForm());
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

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    // ==================== CELDAS PERSONALIZADAS ====================

    private static class NumeroFilaCell extends TableCell<Grupo, Void> {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : String.valueOf(getIndex() + 1));
            setAlignment(Pos.CENTER_LEFT);
        }
    }

    /** Pinta el Turno como una "pastilla" de color (Matutino / Vespertino). */
    private static class TurnoBadgeCell extends TableCell<Grupo, String> {
        private final Label badge = new Label();

        TurnoBadgeCell() {
            badge.getStyleClass().add("estado-badge");
        }

        @Override
        protected void updateItem(String turno, boolean empty) {
            super.updateItem(turno, empty);
            if (empty || turno == null || turno.isBlank()) {
                setGraphic(null);
                return;
            }
            badge.setText(turno);
            badge.getStyleClass().removeIf(s -> s.startsWith("estado-") && !s.equals("estado-badge"));
            if ("Matutino".equals(turno)) {
                badge.getStyleClass().add("estado-matutino");
            } else {
                badge.getStyleClass().add("estado-vespertino");
            }
            setGraphic(badge);
        }
    }

    private class AccionesCell extends TableCell<Grupo, Void> {
        private final Button btnEditar = new Button("✎");
        private final Button btnEliminar = new Button("🗑");
        private final HBox contenedor = new HBox(8, btnEditar, btnEliminar);

        AccionesCell() {
            btnEditar.getStyleClass().add("accion-editar-btn");
            btnEliminar.getStyleClass().add("accion-eliminar-btn");
            contenedor.setAlignment(Pos.CENTER);

            btnEditar.setOnAction(e -> {
                Grupo grupo = getTableView().getItems().get(getIndex());
                cargarEnFormulario(grupo);
            });
            btnEliminar.setOnAction(e -> {
                Grupo grupo = getTableView().getItems().get(getIndex());
                eliminarGrupo(grupo);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : contenedor);
        }
    }
}
