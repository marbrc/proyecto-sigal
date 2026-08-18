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

import mx.utng.dao.AreaAcademicaDAO;
import mx.utng.dao.CarreraDAO;
import mx.utng.model.Carrera;

/**
 * Controlador de la pantalla "Carreras" (fx_carreras.fxml).
 *
 * Usa EXACTAMENTE las columnas reales de tb_carrera (NombreCarrera,
 * ID_Area) a través de CarreraDAO. El formulario pide el área
 * académica a la que pertenece la carrera mediante un ComboBox
 * (cmbArea), igual que ProfesoresController pide el usuario vinculado.
 */
public class CarrerasController implements Initializable {

    private final CarreraDAO carreraDAO = new CarreraDAO();
    private final AreaAcademicaDAO areaDAO = new AreaAcademicaDAO();

    private MenuController menuController;

    // -------- Formulario "Datos de la carrera" --------
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbArea;

    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnNuevaCarrera;

    // -------- Panel "Carreras registradas" --------
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroArea;
    @FXML private Button btnRefrescar;

    @FXML private TableView<Carrera> tblCarreras;
    @FXML private TableColumn<Carrera, Void> colNo;
    @FXML private TableColumn<Carrera, String> colNombre;
    @FXML private TableColumn<Carrera, String> colArea;
    @FXML private TableColumn<Carrera, Void> colAcciones;

    @FXML private Label lblResultados;

    // -------- Estado interno --------
    private final ObservableList<Carrera> carreras = FXCollections.observableArrayList();
    private FilteredList<Carrera> carrerasFiltradas;

    /** Carrera que se está editando actualmente (null = modo "nueva carrera"). */
    private Carrera carreraEnEdicion;

    /** Texto mostrado en los combos de área -> ID_Area real (tb_area_academica.ID_Area). */
    private Map<String, Integer> mapaAreas = new LinkedHashMap<>();

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarAreasDisponibles();

        cmbFiltroArea.getItems().add("Todas las áreas");
        cmbFiltroArea.getItems().addAll(mapaAreas.keySet());
        cmbFiltroArea.setValue("Todas las áreas");

        cargarDatosIniciales();
        configurarTabla();
        configurarFiltros();
        actualizarContador();
    }

    /** Llena cmbArea/cmbFiltroArea con las áreas reales de tb_area_academica. */
    private void cargarAreasDisponibles() {
        mapaAreas = areaDAO.listarAreasParaVincular();
        cmbArea.setItems(FXCollections.observableArrayList(mapaAreas.keySet()));
    }

    private void cargarDatosIniciales() {
        carreras.setAll(carreraDAO.cargarTabla());
    }

    private void configurarTabla() {
        colNo.setCellFactory(col -> new NumeroFilaCell());
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCarrera"));
        colArea.setCellValueFactory(new PropertyValueFactory<>("nombreAreaVinculada"));
        colAcciones.setCellFactory(col -> new AccionesCell());

        carrerasFiltradas = new FilteredList<>(carreras, c -> true);
        tblCarreras.setItems(carrerasFiltradas);
    }

    private void configurarFiltros() {
        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbFiltroArea.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String area = cmbFiltroArea.getValue();

        carrerasFiltradas.setPredicate(carrera -> {
            boolean coincideTexto = texto.isEmpty()
                    || carrera.getNombreCarrera().toLowerCase().contains(texto);
            boolean coincideArea = area == null || area.equals("Todas las áreas")
                    || area.equals(carrera.getNombreAreaVinculada());
            return coincideTexto && coincideArea;
        });

        actualizarContador();
        tblCarreras.refresh();
    }

    private void actualizarContador() {
        int mostrados = carrerasFiltradas == null ? 0 : carrerasFiltradas.size();
        int total = carreras.size();
        if (lblResultados != null) {
            lblResultados.setText("Mostrando " + mostrados + " de " + total + " carreras");
        }
    }

    // ==================== ACCIONES DEL FORMULARIO ====================

    @FXML
    private void onGuardar() {
        String nombre = safeTrim(txtNombre.getText());
        String areaSeleccionada = cmbArea.getValue();

        if (nombre.isEmpty() || areaSeleccionada == null) {
            mostrarAlerta(AlertType.WARNING, "Campos incompletos",
                    "Por favor completa el nombre de la carrera y el área académica antes de guardar.");
            return;
        }

        Integer idArea = mapaAreas.get(areaSeleccionada);
        if (idArea == null) {
            mostrarAlerta(AlertType.ERROR, "Área no válida",
                    "El área académica seleccionada ya no está disponible. Actualiza la lista e inténtalo de nuevo.");
            return;
        }

        Integer idEnEdicion = (carreraEnEdicion == null) ? null : carreraEnEdicion.getIdCarrera();
        if (carreraDAO.existeNombre(nombre, idEnEdicion)) {
            mostrarAlerta(AlertType.WARNING, "Carrera duplicada",
                    "Ya existe una carrera registrada con el nombre \"" + nombre + "\".");
            return;
        }

        if (carreraEnEdicion == null) {
            Carrera nueva = new Carrera(nombre, null);
            boolean guardado = carreraDAO.insertarCarrera(nueva, idArea);
            if (!guardado) {
                mostrarAlerta(AlertType.ERROR, "No se pudo guardar",
                        "Ocurrió un error al registrar la carrera en la base de datos. Intenta de nuevo.");
                return;
            }
        } else {
            Carrera datosActualizados = new Carrera(nombre, null);
            boolean actualizado = carreraDAO.actualizarCarrera(carreraEnEdicion.getIdCarrera(), datosActualizados, idArea);
            if (!actualizado) {
                mostrarAlerta(AlertType.ERROR, "No se pudo actualizar",
                        "Ocurrió un error al actualizar la carrera en la base de datos. Intenta de nuevo.");
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
    private void onNuevaCarrera() {
        limpiarFormulario();
        txtNombre.requestFocus();
    }

    @FXML
    private void onRefrescar() {
        txtBuscar.clear();
        cmbFiltroArea.setValue("Todas las áreas");
        cargarAreasDisponibles();
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
        carreraEnEdicion = null;
        txtNombre.clear();
        cmbArea.setValue(null);
        btnGuardar.setText("💾  Guardar");
    }

    private void cargarEnFormulario(Carrera carrera) {
        carreraEnEdicion = carrera;
        txtNombre.setText(carrera.getNombreCarrera());

        String textoArea = mapaAreas.entrySet().stream()
                .filter(entry -> carrera.getIdArea() != null && entry.getValue().equals(carrera.getIdArea()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        cmbArea.setValue(textoArea);

        btnGuardar.setText("💾  Guardar cambios");
    }

    private void eliminarCarrera(Carrera carrera) {
        Alert confirmacion = crearDialogoTematico(AlertType.CONFIRMATION, "🗑",
                "Eliminar carrera", "¿Eliminar \"" + carrera.getNombreCarrera() + "\"?",
                "Esta acción no se puede deshacer. No se puede eliminar una carrera que "
                        + "todavía tenga grupos, materias o asignaciones registradas.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean eliminada = carreraDAO.eliminarCarrera(carrera.getIdCarrera());
            if (!eliminada) {
                mostrarAlerta(AlertType.WARNING, "No se pudo eliminar",
                        "Esta carrera tiene grupos, materias o asignaciones relacionadas, "
                                + "así que no se puede eliminar mientras existan.");
                return;
            }
            carreras.remove(carrera);
            if (carreraEnEdicion == carrera) {
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

    private static class NumeroFilaCell extends TableCell<Carrera, Void> {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : String.valueOf(getIndex() + 1));
            setAlignment(Pos.CENTER_LEFT);
        }
    }

    private class AccionesCell extends TableCell<Carrera, Void> {
        private final Button btnEditar = new Button("✎");
        private final Button btnEliminar = new Button("🗑");
        private final HBox contenedor = new HBox(8, btnEditar, btnEliminar);

        AccionesCell() {
            btnEditar.getStyleClass().add("accion-editar-btn");
            btnEliminar.getStyleClass().add("accion-eliminar-btn");
            contenedor.setAlignment(Pos.CENTER);

            btnEditar.setOnAction(e -> {
                Carrera carrera = getTableView().getItems().get(getIndex());
                cargarEnFormulario(carrera);
            });
            btnEliminar.setOnAction(e -> {
                Carrera carrera = getTableView().getItems().get(getIndex());
                eliminarCarrera(carrera);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : contenedor);
        }
    }
}
