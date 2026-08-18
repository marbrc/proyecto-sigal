package mx.utng.controller;

import java.net.URL;
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
import mx.utng.model.AreaAcademica;

/**
 * Controlador de la pantalla "Área Académica" (fx_area_academica.fxml).
 *
 * Usa EXACTAMENTE las columnas reales de tb_area_academica (NombreArea)
 * a través de AreaAcademicaDAO. Sigue el mismo patrón visual y de flujo
 * (TableView + FilteredList, Guardar / Editar / Eliminar / Limpiar /
 * Buscar) que ProfesoresController.
 */
public class AreaAcademicaController implements Initializable {

    private final AreaAcademicaDAO areaDAO = new AreaAcademicaDAO();

    private MenuController menuController;

    // -------- Formulario "Datos del área académica" --------
    @FXML private TextField txtNombre;

    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnNuevaArea;

    // -------- Panel "Áreas académicas registradas" --------
    @FXML private TextField txtBuscar;
    @FXML private Button btnRefrescar;

    @FXML private TableView<AreaAcademica> tblAreas;
    @FXML private TableColumn<AreaAcademica, Void> colNo;
    @FXML private TableColumn<AreaAcademica, String> colNombre;
    @FXML private TableColumn<AreaAcademica, Number> colTotalCarreras;
    @FXML private TableColumn<AreaAcademica, Void> colAcciones;

    @FXML private Label lblResultados;

    // -------- Estado interno --------
    private final ObservableList<AreaAcademica> areas = FXCollections.observableArrayList();
    private FilteredList<AreaAcademica> areasFiltradas;

    /** Área que se está editando actualmente (null = modo "nueva área"). */
    private AreaAcademica areaEnEdicion;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarDatosIniciales();
        configurarTabla();
        configurarFiltros();
        actualizarContador();
    }

    private void cargarDatosIniciales() {
        areas.setAll(areaDAO.cargarTabla());
    }

    private void configurarTabla() {
        colNo.setCellFactory(col -> new NumeroFilaCell());
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreArea"));
        colTotalCarreras.setCellValueFactory(new PropertyValueFactory<>("totalCarreras"));
        colTotalCarreras.setCellFactory(col -> new TotalCarrerasCell());
        colAcciones.setCellFactory(col -> new AccionesCell());

        areasFiltradas = new FilteredList<>(areas, a -> true);
        tblAreas.setItems(areasFiltradas);
    }

    private void configurarFiltros() {
        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();

        areasFiltradas.setPredicate(area ->
                texto.isEmpty() || area.getNombreArea().toLowerCase().contains(texto));

        actualizarContador();
        tblAreas.refresh();
    }

    private void actualizarContador() {
        int mostrados = areasFiltradas == null ? 0 : areasFiltradas.size();
        int total = areas.size();
        if (lblResultados != null) {
            lblResultados.setText("Mostrando " + mostrados + " de " + total + " áreas académicas");
        }
    }

    // ==================== ACCIONES DEL FORMULARIO ====================

    @FXML
    private void onGuardar() {
        String nombre = safeTrim(txtNombre.getText());

        if (nombre.isEmpty()) {
            mostrarAlerta(AlertType.WARNING, "Campo incompleto",
                    "Por favor escribe el nombre del área académica antes de guardar.");
            return;
        }

        Integer idEnEdicion = (areaEnEdicion == null) ? null : areaEnEdicion.getIdArea();
        if (areaDAO.existeNombre(nombre, idEnEdicion)) {
            mostrarAlerta(AlertType.WARNING, "Área duplicada",
                    "Ya existe un área académica registrada con el nombre \"" + nombre + "\". Debe ser único.");
            return;
        }

        if (areaEnEdicion == null) {
            AreaAcademica nueva = new AreaAcademica(nombre);
            boolean guardado = areaDAO.insertarArea(nueva);
            if (!guardado) {
                mostrarAlerta(AlertType.ERROR, "No se pudo guardar",
                        "Ocurrió un error al registrar el área académica en la base de datos. Intenta de nuevo.");
                return;
            }
        } else {
            AreaAcademica datosActualizados = new AreaAcademica(nombre);
            boolean actualizado = areaDAO.actualizarArea(areaEnEdicion.getIdArea(), datosActualizados);
            if (!actualizado) {
                mostrarAlerta(AlertType.ERROR, "No se pudo actualizar",
                        "Ocurrió un error al actualizar el área académica en la base de datos. Intenta de nuevo.");
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
    private void onNuevaArea() {
        limpiarFormulario();
        txtNombre.requestFocus();
    }

    @FXML
    private void onRefrescar() {
        txtBuscar.clear();
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
        areaEnEdicion = null;
        txtNombre.clear();
        btnGuardar.setText("💾  Guardar");
    }

    private void cargarEnFormulario(AreaAcademica area) {
        areaEnEdicion = area;
        txtNombre.setText(area.getNombreArea());
        btnGuardar.setText("💾  Guardar cambios");
    }

    private void eliminarArea(AreaAcademica area) {
        Alert confirmacion = crearDialogoTematico(AlertType.CONFIRMATION, "🗑",
                "Eliminar área académica", "¿Eliminar \"" + area.getNombreArea() + "\"?",
                "Esta acción no se puede deshacer. No se puede eliminar un área académica "
                        + "que todavía tenga carreras registradas.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean eliminada = areaDAO.eliminarArea(area.getIdArea());
            if (!eliminada) {
                mostrarAlerta(AlertType.WARNING, "No se pudo eliminar",
                        "Esta área académica tiene carreras registradas, así que no se puede "
                                + "eliminar mientras existan.");
                return;
            }
            areas.remove(area);
            if (areaEnEdicion == area) {
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

    private static class NumeroFilaCell extends TableCell<AreaAcademica, Void> {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : String.valueOf(getIndex() + 1));
            setAlignment(Pos.CENTER_LEFT);
        }
    }

    /** Pastilla con el número de carreras que agrupa cada área. */
    private static class TotalCarrerasCell extends TableCell<AreaAcademica, Number> {
        private final Label badge = new Label();

        TotalCarrerasCell() {
            badge.getStyleClass().addAll("estado-badge", "estado-usuario");
        }

        @Override
        protected void updateItem(Number total, boolean empty) {
            super.updateItem(total, empty);
            if (empty || total == null) {
                setGraphic(null);
                return;
            }
            int n = total.intValue();
            badge.setText(n + (n == 1 ? " carrera" : " carreras"));
            setGraphic(badge);
        }
    }

    private class AccionesCell extends TableCell<AreaAcademica, Void> {
        private final Button btnEditar = new Button("✎");
        private final Button btnEliminar = new Button("🗑");
        private final HBox contenedor = new HBox(8, btnEditar, btnEliminar);

        AccionesCell() {
            btnEditar.getStyleClass().add("accion-editar-btn");
            btnEliminar.getStyleClass().add("accion-eliminar-btn");
            contenedor.setAlignment(Pos.CENTER);

            btnEditar.setOnAction(e -> {
                AreaAcademica area = getTableView().getItems().get(getIndex());
                cargarEnFormulario(area);
            });
            btnEliminar.setOnAction(e -> {
                AreaAcademica area = getTableView().getItems().get(getIndex());
                eliminarArea(area);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : contenedor);
        }
    }
}
