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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
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
import mx.utng.dao.CarreraDAO;
import mx.utng.dao.MateriaCarreraDAO;
import mx.utng.dao.MateriaDAO;
import mx.utng.model.Materia;
import mx.utng.model.MateriaCarrera;

/**
 * Controlador de la pantalla "Materias" (fx_materias.fxml).
 *
 * tb_materia SÓLO tiene Nombre y Descripcion: el cuatrimestre y la
 * carrera de cada materia en realidad viven en tb_materia_carrera
 * (relación materia-carrera-cuatrimestre-profesor). Por eso esta
 * pantalla administra tb_materia_carrera a través de
 * MateriaCarreraDAO, con filtros por carrera y cuatrimestre para
 * ubicar fácilmente cualquier materia entre las que ya existen.
 *
 * El catálogo base de materias (tb_materia, vía MateriaDAO) se
 * administra desde los botones "+" (nueva materia) y "✎" (editar
 * nombre/descripción) junto al combo de materia, con una ventana
 * flotante propia.
 */
public class MateriasController implements Initializable {

    private final MateriaCarreraDAO materiaCarreraDAO = new MateriaCarreraDAO();
    private final MateriaDAO materiaDAO = new MateriaDAO();
    private final CarreraDAO carreraDAO = new CarreraDAO();

    private MenuController menuController;

    /** Cuatrimestres válidos (carreras UTNG van de 1º a 9º cuatrimestre). */
    private static final Integer[] CUATRIMESTRES = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    private static final String SIN_PROFESOR = "Sin profesor asignado";

    // -------- Formulario "Materia por carrera" --------
    @FXML private ComboBox<String> cmbMateria;
    @FXML private Button btnNuevaMateriaBase;
    @FXML private Button btnEditarMateriaBase;
    @FXML private ComboBox<String> cmbCarrera;
    @FXML private ComboBox<Integer> cmbCuatrimestre;
    @FXML private ComboBox<String> cmbProfesor;

    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnNuevaRelacion;

    // -------- Panel "Materias registradas" --------
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroCarrera;
    @FXML private ComboBox<Integer> cmbFiltroCuatrimestre;
    @FXML private Button btnRefrescar;

    @FXML private TableView<MateriaCarrera> tblMaterias;
    @FXML private TableColumn<MateriaCarrera, Void> colNo;
    @FXML private TableColumn<MateriaCarrera, String> colMateria;
    @FXML private TableColumn<MateriaCarrera, String> colCarrera;
    @FXML private TableColumn<MateriaCarrera, Number> colCuatrimestre;
    @FXML private TableColumn<MateriaCarrera, String> colProfesor;
    @FXML private TableColumn<MateriaCarrera, Void> colAcciones;

    @FXML private Label lblResultados;

    // -------- Estado interno --------
    private final ObservableList<MateriaCarrera> relaciones = FXCollections.observableArrayList();
    private FilteredList<MateriaCarrera> relacionesFiltradas;

    /** Relación que se está editando actualmente (null = modo "nueva relación"). */
    private MateriaCarrera relacionEnEdicion;

    /** Texto mostrado en los combos -> ID real (tb_materia / tb_carrera / tb_profesor). */
    private Map<String, Integer> mapaMaterias = new LinkedHashMap<>();
    private Map<String, Integer> mapaCarreras = new LinkedHashMap<>();
    private Map<String, Integer> mapaProfesores = new LinkedHashMap<>();

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbCuatrimestre.setItems(FXCollections.observableArrayList(CUATRIMESTRES));

        cargarMateriasDisponibles();
        cargarCarrerasDisponibles();
        cargarProfesoresDisponibles();

        cmbFiltroCarrera.getItems().add("Todas las carreras");
        cmbFiltroCarrera.getItems().addAll(mapaCarreras.keySet());
        cmbFiltroCarrera.setValue("Todas las carreras");

        cmbFiltroCuatrimestre.getItems().add(null);
        cmbFiltroCuatrimestre.getItems().addAll(CUATRIMESTRES);
        cmbFiltroCuatrimestre.setValue(null);
        cmbFiltroCuatrimestre.setPromptText("Todos los cuatrimestres");

        cargarDatosIniciales();
        configurarTabla();
        configurarFiltros();
        actualizarContador();
    }

    private void cargarMateriasDisponibles() {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (Materia m : materiaDAO.listarMaterias()) {
            mapa.put(m.getNombre(), m.getIdMateria());
        }
        mapaMaterias = mapa;
        cmbMateria.setItems(FXCollections.observableArrayList(mapaMaterias.keySet()));
    }

    private void cargarCarrerasDisponibles() {
        mapaCarreras = carreraDAO.listarCarrerasParaVincular();
        cmbCarrera.setItems(FXCollections.observableArrayList(mapaCarreras.keySet()));
    }

    private void cargarProfesoresDisponibles() {
        mapaProfesores = materiaCarreraDAO.listarProfesoresParaVincular();
        cmbProfesor.setItems(FXCollections.observableArrayList());
        cmbProfesor.getItems().add(SIN_PROFESOR);
        cmbProfesor.getItems().addAll(mapaProfesores.keySet());
    }

    private void cargarDatosIniciales() {
        relaciones.setAll(materiaCarreraDAO.cargarTabla());
    }

    private void configurarTabla() {
        colNo.setCellFactory(col -> new NumeroFilaCell());
        colMateria.setCellValueFactory(new PropertyValueFactory<>("nombreMateria"));
        colCarrera.setCellValueFactory(new PropertyValueFactory<>("nombreCarrera"));
        colCuatrimestre.setCellValueFactory(new PropertyValueFactory<>("cuatrimestre"));
        colCuatrimestre.setCellFactory(col -> new CuatrimestreBadgeCell());
        colProfesor.setCellValueFactory(new PropertyValueFactory<>("nombreProfesor"));
        colAcciones.setCellFactory(col -> new AccionesCell());

        relacionesFiltradas = new FilteredList<>(relaciones, r -> true);
        tblMaterias.setItems(relacionesFiltradas);
    }

    private void configurarFiltros() {
        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbFiltroCarrera.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbFiltroCuatrimestre.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String carrera = cmbFiltroCarrera.getValue();
        Integer cuatrimestre = cmbFiltroCuatrimestre.getValue();

        relacionesFiltradas.setPredicate(rel -> {
            boolean coincideTexto = texto.isEmpty()
                    || rel.getNombreMateria().toLowerCase().contains(texto);
            boolean coincideCarrera = carrera == null || carrera.equals("Todas las carreras")
                    || carrera.equals(rel.getNombreCarrera());
            boolean coincideCuatrimestre = cuatrimestre == null || cuatrimestre == rel.getCuatrimestre();
            return coincideTexto && coincideCarrera && coincideCuatrimestre;
        });

        actualizarContador();
        tblMaterias.refresh();
    }

    private void actualizarContador() {
        int mostrados = relacionesFiltradas == null ? 0 : relacionesFiltradas.size();
        int total = relaciones.size();
        if (lblResultados != null) {
            lblResultados.setText("Mostrando " + mostrados + " de " + total + " materias");
        }
    }

    // ==================== ACCIONES DEL FORMULARIO ====================

    @FXML
    private void onGuardar() {
        String materiaSeleccionada = cmbMateria.getValue();
        String carreraSeleccionada = cmbCarrera.getValue();
        Integer cuatrimestre = cmbCuatrimestre.getValue();
        String profesorSeleccionado = cmbProfesor.getValue();

        if (materiaSeleccionada == null || carreraSeleccionada == null || cuatrimestre == null) {
            mostrarAlerta(AlertType.WARNING, "Campos incompletos",
                    "Por favor selecciona la materia, la carrera y el cuatrimestre antes de guardar.");
            return;
        }

        Integer idMateria = mapaMaterias.get(materiaSeleccionada);
        Integer idCarrera = mapaCarreras.get(carreraSeleccionada);
        if (idMateria == null || idCarrera == null) {
            mostrarAlerta(AlertType.ERROR, "Selección no válida",
                    "La materia o la carrera seleccionada ya no está disponible. Actualiza la lista e inténtalo de nuevo.");
            return;
        }

        Integer idProfesor = (profesorSeleccionado == null || SIN_PROFESOR.equals(profesorSeleccionado))
                ? null
                : mapaProfesores.get(profesorSeleccionado);

        Integer idEnEdicion = (relacionEnEdicion == null) ? null : relacionEnEdicion.getIdMateriaCarrera();
        if (materiaCarreraDAO.existeRelacion(idMateria, idCarrera, idEnEdicion)) {
            mostrarAlerta(AlertType.WARNING, "Materia duplicada en esa carrera",
                    "\"" + materiaSeleccionada + "\" ya está registrada en \"" + carreraSeleccionada + "\". "
                            + "Edita esa relación en vez de crear otra.");
            return;
        }

        boolean exito;
        if (relacionEnEdicion == null) {
            exito = materiaCarreraDAO.insertarRelacion(idMateria, idCarrera, cuatrimestre, idProfesor);
        } else {
            exito = materiaCarreraDAO.actualizarRelacion(
                    relacionEnEdicion.getIdMateriaCarrera(), idMateria, idCarrera, cuatrimestre, idProfesor);
        }

        if (!exito) {
            mostrarAlerta(AlertType.ERROR, "No se pudo guardar",
                    "Ocurrió un error al guardar en la base de datos. Intenta de nuevo.");
            return;
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
    private void onNuevaRelacion() {
        limpiarFormulario();
        cmbMateria.requestFocus();
    }

    @FXML
    private void onRefrescar() {
        txtBuscar.clear();
        cmbFiltroCarrera.setValue("Todas las carreras");
        cmbFiltroCuatrimestre.setValue(null);
        cargarMateriasDisponibles();
        cargarCarrerasDisponibles();
        cargarProfesoresDisponibles();
        cargarDatosIniciales();
        aplicarFiltros();
    }

    @FXML
    private void onVolverCatalogo() {
        if (menuController != null) {
            menuController.abrirModulo("fx_catalogo_academico");
        }
    }

    // ==================== CATÁLOGO BASE DE MATERIAS (tb_materia) ====================

    @FXML
    private void onNuevaMateriaBase() {
        Optional<Materia> resultado = mostrarDialogoMateria(null);
        resultado.ifPresent(datos -> {
            if (materiaDAO.existeNombre(datos.getNombre(), null)) {
                mostrarAlerta(AlertType.WARNING, "Materia duplicada",
                        "Ya existe una materia registrada con el nombre \"" + datos.getNombre() + "\".");
                return;
            }
            boolean guardado = materiaDAO.insertarMateria(datos);
            if (!guardado) {
                mostrarAlerta(AlertType.ERROR, "No se pudo guardar",
                        "Ocurrió un error al registrar la materia en la base de datos. Intenta de nuevo.");
                return;
            }
            cargarMateriasDisponibles();
            cmbMateria.setValue(datos.getNombre());
        });
    }

    @FXML
    private void onEditarMateriaBase() {
        String materiaSeleccionada = cmbMateria.getValue();
        if (materiaSeleccionada == null) {
            mostrarAlerta(AlertType.WARNING, "Ninguna materia seleccionada",
                    "Selecciona primero una materia del catálogo para poder editarla.");
            return;
        }
        Integer idMateria = mapaMaterias.get(materiaSeleccionada);
        if (idMateria == null) {
            return;
        }
        Materia actual = null;
        for (Materia m : materiaDAO.listarMaterias()) {
            if (m.getIdMateria() == idMateria) {
                actual = m;
                break;
            }
        }
        if (actual == null) {
            return;
        }

        final Materia actualFinal = actual;
        Optional<Materia> resultado = mostrarDialogoMateria(actual);
        resultado.ifPresent(datos -> {
            if (!datos.getNombre().equalsIgnoreCase(actualFinal.getNombre())
                    && materiaDAO.existeNombre(datos.getNombre(), idMateria)) {
                mostrarAlerta(AlertType.WARNING, "Materia duplicada",
                        "Ya existe una materia registrada con el nombre \"" + datos.getNombre() + "\".");
                return;
            }
            boolean actualizado = materiaDAO.actualizarMateria(idMateria, datos);
            if (!actualizado) {
                mostrarAlerta(AlertType.ERROR, "No se pudo actualizar",
                        "Ocurrió un error al actualizar la materia en la base de datos. Intenta de nuevo.");
                return;
            }
            cargarMateriasDisponibles();
            cmbMateria.setValue(datos.getNombre());
        });
    }

    /**
     * Ventana flotante para crear o editar el Nombre/Descripción base
     * de una materia (tb_materia), con la misma temática visual del
     * resto de SIGAL. materiaActual == null -> modo "nueva materia".
     */
    private Optional<Materia> mostrarDialogoMateria(Materia materiaActual) {
        Dialog<Materia> dialogo = new Dialog<>();
        dialogo.setTitle(materiaActual == null ? "Nueva materia" : "Editar materia");
        dialogo.setHeaderText(materiaActual == null
                ? "Registrar una materia nueva en el catálogo"
                : "Editar \"" + materiaActual.getNombre() + "\"");

        DialogPane panel = dialogo.getDialogPane();
        panel.getStylesheets().add(getClass().getResource("/mx/utng/view/styles_catalogo_crud.css").toExternalForm());
        panel.getStyleClass().add("themed-dialog");
        panel.setMinWidth(420.0);
        panel.setMaxWidth(480.0);

        Label icono = new Label(materiaActual == null ? "＋" : "✎");
        icono.getStyleClass().add("header-icon");
        StackPane cajaIcono = new StackPane(icono);
        cajaIcono.getStyleClass().add("header-icon-box");
        panel.setGraphic(cajaIcono);

        Label lblNombre = new Label("Nombre de la materia *");
        lblNombre.getStyleClass().add("field-label");
        TextField txtNombreDialogo = new TextField();
        txtNombreDialogo.getStyleClass().add("input-field");
        txtNombreDialogo.setPromptText("Ej. Bases de Datos");
        HBox cajaNombre = new HBox(8, txtNombreDialogo);
        cajaNombre.getStyleClass().add("input-box");
        HBox.setHgrow(txtNombreDialogo, Priority.ALWAYS);

        Label lblDescripcion = new Label("Descripción");
        lblDescripcion.getStyleClass().add("field-label");
        TextArea txtDescripcionDialogo = new TextArea();
        txtDescripcionDialogo.getStyleClass().add("input-textarea");
        txtDescripcionDialogo.setPromptText("Descripción breve (opcional)");
        txtDescripcionDialogo.setPrefRowCount(3);
        txtDescripcionDialogo.setWrapText(true);
        VBox cajaDescripcion = new VBox(txtDescripcionDialogo);
        cajaDescripcion.getStyleClass().add("input-box");

        if (materiaActual != null) {
            txtNombreDialogo.setText(materiaActual.getNombre());
            txtDescripcionDialogo.setText(materiaActual.getDescripcion());
        }

        VBox contenido = new VBox(12, lblNombre, cajaNombre, lblDescripcion, cajaDescripcion);
        contenido.setPadding(new Insets(6, 0, 0, 0));
        panel.setContent(contenido);

        ButtonType btnGuardarTipo = new ButtonType("💾  Guardar", ButtonBar.ButtonData.OK_DONE);
        panel.getButtonTypes().addAll(btnGuardarTipo, ButtonType.CANCEL);

        dialogo.setResultConverter(boton -> {
            if (boton == btnGuardarTipo) {
                String nombre = txtNombreDialogo.getText() == null ? "" : txtNombreDialogo.getText().trim();
                String descripcion = txtDescripcionDialogo.getText() == null ? "" : txtDescripcionDialogo.getText().trim();
                if (nombre.isEmpty()) {
                    mostrarAlerta(AlertType.WARNING, "Campo incompleto",
                            "Por favor escribe el nombre de la materia antes de guardar.");
                    return null;
                }
                return new Materia(nombre, descripcion);
            }
            return null;
        });

        return dialogo.showAndWait();
    }

    // ==================== FORMULARIO / TABLA ====================

    private void limpiarFormulario() {
        relacionEnEdicion = null;
        cmbMateria.setValue(null);
        cmbCarrera.setValue(null);
        cmbCuatrimestre.setValue(null);
        cmbProfesor.setValue(null);
        btnGuardar.setText("💾  Guardar");
    }

    private void cargarEnFormulario(MateriaCarrera relacion) {
        relacionEnEdicion = relacion;
        cmbMateria.setValue(relacion.getNombreMateria());
        cmbCarrera.setValue(relacion.getNombreCarrera());
        cmbCuatrimestre.setValue(relacion.getCuatrimestre());
        cmbProfesor.setValue(relacion.getIdProfesor() == null ? SIN_PROFESOR : relacion.getNombreProfesor());
        btnGuardar.setText("💾  Guardar cambios");
    }

    private void eliminarRelacion(MateriaCarrera relacion) {
        Alert confirmacion = crearDialogoTematico(AlertType.CONFIRMATION, "🗑",
                "Quitar materia de la carrera",
                "¿Quitar \"" + relacion.getNombreMateria() + "\" de \"" + relacion.getNombreCarrera() + "\"?",
                "Esta acción no se puede deshacer. La materia seguirá existiendo en el catálogo, "
                        + "sólo se quitará su relación con esta carrera y cuatrimestre.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean eliminada = materiaCarreraDAO.eliminarRelacion(relacion.getIdMateriaCarrera());
            if (!eliminada) {
                mostrarAlerta(AlertType.ERROR, "No se pudo eliminar",
                        "Ocurrió un error al eliminar la relación. Intenta de nuevo.");
                return;
            }
            relaciones.remove(relacion);
            if (relacionEnEdicion == relacion) {
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

    // ==================== CELDAS PERSONALIZADAS ====================

    private static class NumeroFilaCell extends TableCell<MateriaCarrera, Void> {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : String.valueOf(getIndex() + 1));
            setAlignment(Pos.CENTER_LEFT);
        }
    }

    /** Pastilla con el número de cuatrimestre. */
    private static class CuatrimestreBadgeCell extends TableCell<MateriaCarrera, Number> {
        private final Label badge = new Label();

        CuatrimestreBadgeCell() {
            badge.getStyleClass().addAll("estado-badge", "estado-usuario");
        }

        @Override
        protected void updateItem(Number cuatrimestre, boolean empty) {
            super.updateItem(cuatrimestre, empty);
            if (empty || cuatrimestre == null) {
                setGraphic(null);
                return;
            }
            badge.setText(cuatrimestre.intValue() + "º");
            setGraphic(badge);
        }
    }

    private class AccionesCell extends TableCell<MateriaCarrera, Void> {
        private final Button btnEditar = new Button("✎");
        private final Button btnEliminar = new Button("🗑");
        private final HBox contenedor = new HBox(8, btnEditar, btnEliminar);

        AccionesCell() {
            btnEditar.getStyleClass().add("accion-editar-btn");
            btnEliminar.getStyleClass().add("accion-eliminar-btn");
            contenedor.setAlignment(Pos.CENTER);

            btnEditar.setOnAction(e -> {
                MateriaCarrera relacion = getTableView().getItems().get(getIndex());
                cargarEnFormulario(relacion);
            });
            btnEliminar.setOnAction(e -> {
                MateriaCarrera relacion = getTableView().getItems().get(getIndex());
                eliminarRelacion(relacion);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : contenedor);
        }
    }
}
