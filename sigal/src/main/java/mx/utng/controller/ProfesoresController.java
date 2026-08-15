package mx.utng.controller;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

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

import mx.utng.dao.ProfesorDAO;
import mx.utng.model.Profesor;

/**
 * Controlador de la pantalla "Profesores" (fx_profesores.fxml).
 *
 * Usa EXACTAMENTE las columnas reales de tb_profesor (Nombre,
 * ApellidoPaterno, ApellidoMaterno, CorreoElectronico, ID_Usuario) a
 * través de ProfesorDAO. Como ID_Usuario es obligatorio (llave
 * foránea hacia tb_usuario), el formulario pide elegir el usuario al
 * que se vincula el profesor en vez de pedir datos que no existen en
 * la tabla (como "Identificador" o "Carrera").
 *
 * Sigue el mismo patrón que EspaciosController: TableView +
 * FilteredList en memoria, tarjetas con el mismo estilo visual
 * (styles_profesores.css, calcado de styles_espacios.css) y el mismo
 * flujo de Guardar / Editar / Eliminar / Limpiar / Buscar.
 */
public class ProfesoresController implements Initializable {

    private final ProfesorDAO profesorDAO = new ProfesorDAO();

    // -------- Formulario "Datos del profesor" --------
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidoPaterno;
    @FXML private TextField txtApellidoMaterno;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbUsuario;

    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnNuevoProfesor;

    // -------- Panel "Profesores registrados" --------
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroUsuario;
    @FXML private ComboBox<String> cmbFiltroRol;
    @FXML private Button btnRefrescar;

    @FXML private TableView<Profesor> tblProfesores;
    @FXML private TableColumn<Profesor, Void> colNo;
    @FXML private TableColumn<Profesor, String> colNombre;
    @FXML private TableColumn<Profesor, String> colCorreo;
    @FXML private TableColumn<Profesor, String> colUsuario;
    @FXML private TableColumn<Profesor, String> colRol;
    @FXML private TableColumn<Profesor, Void> colAcciones;

    @FXML private Label lblResultados;

    // -------- Estado interno --------
    private final ObservableList<Profesor> profesores = FXCollections.observableArrayList();
    private FilteredList<Profesor> profesoresFiltrados;

    /** Profesor que se está editando actualmente (null = modo "nuevo profesor"). */
    private Profesor profesorEnEdicion;

    /** Texto mostrado en cmbUsuario -> ID_Usuario real (tb_usuario.ID_Usuario). */
    private Map<String, Integer> mapaUsuarios = new LinkedHashMap<>();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Roles reales de tb_usuario (columna Rol: enum('Administrador','Usuario')). */
    private static final String[] ROLES = {
            "Administrador",
            "Usuario"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarUsuariosDisponibles();

        cmbFiltroUsuario.getItems().add("Todos los usuarios");
        cmbFiltroUsuario.getItems().addAll(mapaUsuarios.keySet());
        cmbFiltroUsuario.setValue("Todos los usuarios");

        cmbFiltroRol.getItems().add("Todos los roles");
        cmbFiltroRol.getItems().addAll(ROLES);
        cmbFiltroRol.setValue("Todos los roles");

        cargarDatosIniciales();
        configurarTabla();
        configurarFiltros();

        actualizarContador();
    }

    /** Llena cmbUsuario con los usuarios reales de tb_usuario (JOIN usado por ProfesorDAO). */
    private void cargarUsuariosDisponibles() {
        mapaUsuarios = profesorDAO.listarUsuariosParaVincular();
        cmbUsuario.setItems(FXCollections.observableArrayList(mapaUsuarios.keySet()));
    }

    /**
     * Carga los profesores registrados desde tb_profesor (con su
     * usuario vinculado) usando ProfesorDAO.cargarTabla().
     */
    private void cargarDatosIniciales() {
        profesores.setAll(profesorDAO.cargarTabla());
    }

    private void configurarTabla() {
        colNo.setCellFactory(col -> new NumeroFilaCell());

        colNombre.setCellFactory(col -> new NombreCompletoCell());
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correoElectronico"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuarioVinculado"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rolUsuarioVinculado"));

        // Columna "Rol" con badge de color (mismo estilo visual que el estado en Espacios)
        colRol.setCellFactory(col -> new RolBadgeCell());

        colAcciones.setCellFactory(col -> new AccionesCell());

        profesoresFiltrados = new FilteredList<>(profesores, p -> true);
        tblProfesores.setItems(profesoresFiltrados);
    }

    private void configurarFiltros() {
        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbFiltroUsuario.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbFiltroRol.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String usuario = cmbFiltroUsuario.getValue();
        String rol = cmbFiltroRol.getValue();

        profesoresFiltrados.setPredicate(prof -> {
            boolean coincideTexto = texto.isEmpty()
                    || prof.getNombreCompleto().toLowerCase().contains(texto)
                    || (prof.getCorreoElectronico() != null && prof.getCorreoElectronico().toLowerCase().contains(texto));
            boolean coincideUsuario = usuario == null || usuario.equals("Todos los usuarios")
                    || usuario.equals(prof.getNombreUsuarioVinculado());
            boolean coincideRol = rol == null || rol.equals("Todos los roles")
                    || rol.equals(prof.getRolUsuarioVinculado());
            return coincideTexto && coincideUsuario && coincideRol;
        });

        actualizarContador();
        tblProfesores.refresh();
    }

    private void actualizarContador() {
        int mostrados = profesoresFiltrados == null ? 0 : profesoresFiltrados.size();
        int total = profesores.size();
        if (lblResultados != null) {
            lblResultados.setText("Mostrando " + mostrados + " de " + total + " profesores");
        }
    }

    // ==================== ACCIONES DEL FORMULARIO ====================

    @FXML
    private void onGuardar() {
        String nombre = safeTrim(txtNombre.getText());
        String apellidoPaterno = safeTrim(txtApellidoPaterno.getText());
        String apellidoMaterno = safeTrim(txtApellidoMaterno.getText());
        String correo = safeTrim(txtCorreo.getText());
        String usuarioSeleccionado = cmbUsuario.getValue();

        if (nombre.isEmpty() || apellidoPaterno.isEmpty() || usuarioSeleccionado == null) {
            mostrarAlerta(AlertType.WARNING, "Campos incompletos",
                    "Por favor complete el nombre, el apellido paterno y el usuario vinculado antes de guardar.");
            return;
        }

        if (!correo.isEmpty() && !EMAIL_PATTERN.matcher(correo).matches()) {
            mostrarAlerta(AlertType.WARNING, "Correo inválido",
                    "Ingrese un correo electrónico con un formato válido, por ejemplo usuario@utng.edu.mx.");
            return;
        }

        Integer idUsuario = mapaUsuarios.get(usuarioSeleccionado);
        if (idUsuario == null) {
            mostrarAlerta(AlertType.ERROR, "Usuario no válido",
                    "El usuario seleccionado ya no está disponible. Actualiza la lista e inténtalo de nuevo.");
            return;
        }

        // Validar que el correo sea único (excepto cuando se está editando ese mismo profesor)
        Integer idEnEdicion = (profesorEnEdicion == null) ? null : profesorEnEdicion.getIdProfesor();
        if (!correo.isEmpty() && profesorDAO.existeCorreo(correo, idEnEdicion)) {
            mostrarAlerta(AlertType.WARNING, "Correo duplicado",
                    "Ya existe un profesor registrado con el correo \"" + correo + "\". Debe ser único.");
            return;
        }

        if (profesorEnEdicion == null) {
            // Modo creación
            Profesor nuevo = new Profesor(nombre, apellidoPaterno, apellidoMaterno, correo, null, null);
            boolean guardado = profesorDAO.insertarProfesor(nuevo, idUsuario);
            if (!guardado) {
                mostrarAlerta(AlertType.ERROR, "No se pudo guardar",
                        "Ocurrió un error al registrar el profesor en la base de datos. Intenta de nuevo.");
                return;
            }
        } else {
            // Modo edición
            Profesor datosActualizados = new Profesor(nombre, apellidoPaterno, apellidoMaterno, correo, null, null);
            boolean actualizado = profesorDAO.actualizarProfesor(profesorEnEdicion.getIdProfesor(), datosActualizados, idUsuario);
            if (!actualizado) {
                mostrarAlerta(AlertType.ERROR, "No se pudo actualizar",
                        "Ocurrió un error al actualizar el profesor en la base de datos. Intenta de nuevo.");
                return;
            }
        }

        // Volvemos a cargar desde la BD para reflejar el JOIN con tb_usuario (NombreUsuario / Rol) correctamente.
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
    private void onNuevoProfesor() {
        limpiarFormulario();
        txtNombre.requestFocus();
    }

    @FXML
    private void onRefrescar() {
        txtBuscar.clear();
        cmbFiltroUsuario.setValue("Todos los usuarios");
        cmbFiltroRol.setValue("Todos los roles");
        cargarUsuariosDisponibles();
        cargarDatosIniciales();
        aplicarFiltros();
    }

    private void limpiarFormulario() {
        profesorEnEdicion = null;
        txtNombre.clear();
        txtApellidoPaterno.clear();
        txtApellidoMaterno.clear();
        txtCorreo.clear();
        cmbUsuario.setValue(null);
        btnGuardar.setText("💾  Guardar");
    }

    private void cargarEnFormulario(Profesor profesor) {
        profesorEnEdicion = profesor;
        txtNombre.setText(profesor.getNombre());
        txtApellidoPaterno.setText(profesor.getApellidoPaterno());
        txtApellidoMaterno.setText(profesor.getApellidoMaterno());
        txtCorreo.setText(profesor.getCorreoElectronico());

        // Selecciona en el combo la entrada cuyo ID_Usuario coincide con el del profesor.
        String textoUsuario = mapaUsuarios.entrySet().stream()
                .filter(entry -> entry.getValue() == profesor.getIdUsuario())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        cmbUsuario.setValue(textoUsuario);

        btnGuardar.setText("💾  Guardar cambios");
    }

    private void eliminarProfesor(Profesor profesor) {
        Alert confirmacion = crearDialogoTematico(AlertType.CONFIRMATION, "🗑",
                "Eliminar profesor", "¿Eliminar a \"" + profesor.getNombreCompleto() + "\"?",
                "Esta acción no se puede deshacer. No se puede eliminar un profesor "
                        + "que tenga asignaciones registradas.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean eliminado = profesorDAO.eliminarProfesor(profesor.getIdProfesor());
            if (!eliminado) {
                mostrarAlerta(AlertType.WARNING, "No se pudo eliminar",
                        "Este profesor tiene asignaciones (u otros registros) relacionados, "
                                + "así que no se puede eliminar mientras existan.");
                return;
            }
            profesores.remove(profesor);
            if (profesorEnEdicion == profesor) {
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

    /**
     * Construye una ventana flotante (Alert) con la misma temática oscura /
     * azul-morada de esta pantalla (colores y estilos de
     * styles_profesores.css, calcados de styles_espacios.css) y con las
     * mismas medidas máximas usadas para las ventanas flotantes del resto
     * del sistema.
     */
    private Alert crearDialogoTematico(AlertType tipo, String glifo, String titulo,
                                        String encabezado, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado != null ? encabezado : titulo);
        alerta.setContentText(mensaje);

        DialogPane panel = alerta.getDialogPane();
        panel.getStylesheets().add(getClass().getResource("/mx/utng/view/styles_profesores.css").toExternalForm());
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

    /** Columna "No." con el número de fila (1, 2, 3...), se recalcula al filtrar/ordenar. */
    private static class NumeroFilaCell extends TableCell<Profesor, Void> {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : String.valueOf(getIndex() + 1));
            setAlignment(Pos.CENTER_LEFT);
        }
    }

    /** Columna "Nombre completo": concatena Nombre + ApellidoPaterno + ApellidoMaterno. */
    private static class NombreCompletoCell extends TableCell<Profesor, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                setText(null);
                return;
            }
            Profesor profesor = getTableView().getItems().get(getIndex());
            setText(profesor == null ? null : profesor.getNombreCompleto());
        }
    }

    /** Pinta el Rol del usuario vinculado como una "pastilla" de color. */
    private class RolBadgeCell extends TableCell<Profesor, String> {
        private final Label badge = new Label();

        RolBadgeCell() {
            badge.getStyleClass().add("estado-badge");
        }

        @Override
        protected void updateItem(String rol, boolean empty) {
            super.updateItem(rol, empty);
            if (empty || rol == null || rol.isBlank()) {
                setGraphic(null);
                return;
            }
            badge.setText(rol);
            badge.getStyleClass().removeIf(s -> s.startsWith("estado-") && !s.equals("estado-badge"));
            if ("Administrador".equals(rol)) {
                badge.getStyleClass().add("estado-administrador");
            } else {
                badge.getStyleClass().add("estado-usuario");
            }
            setGraphic(badge);
        }
    }

    private class AccionesCell extends TableCell<Profesor, Void> {
        private final Button btnEditar = new Button("✎");
        private final Button btnEliminar = new Button("🗑");
        private final HBox contenedor = new HBox(8, btnEditar, btnEliminar);

        AccionesCell() {
            btnEditar.getStyleClass().add("accion-editar-btn");
            btnEliminar.getStyleClass().add("accion-eliminar-btn");
            contenedor.setAlignment(Pos.CENTER);

            btnEditar.setOnAction(e -> {
                Profesor profesor = getTableView().getItems().get(getIndex());
                cargarEnFormulario(profesor);
            });
            btnEliminar.setOnAction(e -> {
                Profesor profesor = getTableView().getItems().get(getIndex());
                eliminarProfesor(profesor);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : contenedor);
        }
    }
}
