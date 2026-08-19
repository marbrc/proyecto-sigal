package mx.utng.controller;
 
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
 
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import mx.utng.dao.EspacioDAO;
import mx.utng.model.EspacioRegistro;
 
public class EspaciosController implements Initializable {
 
    private final EspacioDAO espacioDAO = new EspacioDAO();
 
    // -------- Formulario "Datos del espacio" --------
    @FXML private TextField txtClave;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtCapacidad;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TextArea txtDescripcion;
 
    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnNuevoEspacio;
 
    // -------- Panel "Espacios registrados" --------
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroTipo;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private Button btnRefrescar;
 
    @FXML private TableView<EspacioRegistro> tblEspacios;
    @FXML private TableColumn<EspacioRegistro, String> colClave;
    @FXML private TableColumn<EspacioRegistro, String> colNombre;
    @FXML private TableColumn<EspacioRegistro, String> colTipo;
    @FXML private TableColumn<EspacioRegistro, Integer> colCapacidad;
    @FXML private TableColumn<EspacioRegistro, String> colEstado;
    @FXML private TableColumn<EspacioRegistro, Void> colAcciones;
 
    @FXML private Label lblResultados;
 
    // -------- Estado interno --------
    private final ObservableList<EspacioRegistro> espacios = FXCollections.observableArrayList();
    private FilteredList<EspacioRegistro> espaciosFiltrados;
 
    /** EspacioRegistro que se está editando actualmente (null = modo "nuevo espacio"). */
    private EspacioRegistro espacioEnEdicion;
 
    private static final String[] TIPOS = {
            "Aula común",
            "Lab. de cómputo",
            "Especializado",
            "Sala múltiple"
    };
 
    private static final String[] ESTADOS = {
            "Disponible",
            "En mantenimiento",
            "Fuera de servicio"
    };
 
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbTipo.getItems().addAll(TIPOS);
        cmbEstado.getItems().addAll(ESTADOS);
 
        cmbFiltroTipo.getItems().add("Todos los tipos");
        cmbFiltroTipo.getItems().addAll(TIPOS);
        cmbFiltroTipo.setValue("Todos los tipos");
 
        cmbFiltroEstado.getItems().add("Todos los estados");
        cmbFiltroEstado.getItems().addAll(ESTADOS);
        cmbFiltroEstado.setValue("Todos los estados");
 
        cargarDatosIniciales();
        configurarTabla();
        configurarFiltros();
 
        actualizarContador();
    }
 
    /**
     * Carga los espacios registrados desde tb_espado.
     */
    private void cargarDatosIniciales() {
        espacios.setAll(espacioDAO.listarTodos());
    }
 
    private void configurarTabla() {
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCapacidad.setCellValueFactory(new PropertyValueFactory<>("capacidad"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
 
        // Columna "Estado" con badge de color
        colEstado.setCellFactory(col -> new EstadoBadgeCell());
 
        // Columna "Acciones" con botones editar / eliminar
        colAcciones.setCellFactory(col -> new AccionesCell());
 
        espaciosFiltrados = new FilteredList<>(espacios, e -> true);
        tblEspacios.setItems(espaciosFiltrados);
    }
 
    private void configurarFiltros() {
        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbFiltroTipo.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
    }
 
    private void aplicarFiltros() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String tipo = cmbFiltroTipo.getValue();
        String estado = cmbFiltroEstado.getValue();
 
        espaciosFiltrados.setPredicate(esp -> {
            boolean coincideTexto = texto.isEmpty()
                    || esp.getClave().toLowerCase().contains(texto)
                    || esp.getNombre().toLowerCase().contains(texto);
            boolean coincideTipo = tipo == null || tipo.equals("Todos los tipos") || tipo.equals(esp.getTipo());
            boolean coincideEstado = estado == null || estado.equals("Todos los estados") || estado.equals(esp.getEstado());
            return coincideTexto && coincideTipo && coincideEstado;
        });
 
        actualizarContador();
    }
 
    private void actualizarContador() {
        int mostrados = espaciosFiltrados == null ? 0 : espaciosFiltrados.size();
        int total = espacios.size();
        if (lblResultados != null) {
            lblResultados.setText("Mostrando " + mostrados + " de " + total + " espacios");
        }
    }
 
    // ==================== ACCIONES DEL FORMULARIO ====================
 
    @FXML
    private void onGuardar() {
        String clave = safeTrim(txtClave.getText());
        String nombre = safeTrim(txtNombre.getText());
        String tipo = cmbTipo.getValue();
        String capacidadTxt = safeTrim(txtCapacidad.getText());
        String estado = cmbEstado.getValue();
        String descripcion = safeTrim(txtDescripcion.getText());
 
        if (clave.isEmpty() || nombre.isEmpty() || tipo == null || capacidadTxt.isEmpty() || estado == null) {
            mostrarAlerta(AlertType.WARNING, "Campos incompletos",
                    "Por favor complete todos los campos obligatorios (*) antes de guardar.");
            return;
        }
 
        int capacidad;
        try {
            capacidad = Integer.parseInt(capacidadTxt);
            if (capacidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            mostrarAlerta(AlertType.WARNING, "Capacidad inválida",
                    "La capacidad máxima debe ser un número entero mayor a 0.");
            return;
        }
 
        // Validar que la clave sea única (excepto cuando se está editando ese mismo espacio)
        Integer idEnEdicion = (espacioEnEdicion == null) ? null : espacioEnEdicion.getIdEspacio();
        if (espacioDAO.existeClave(clave, idEnEdicion)) {
            mostrarAlerta(AlertType.WARNING, "Clave duplicada",
                    "Ya existe un espacio registrado con la clave \"" + clave + "\". La clave debe ser única.");
            return;
        }
 
        if (espacioEnEdicion == null) {
            // Modo creación
            EspacioRegistro nuevo = new EspacioRegistro(clave, nombre, tipo, capacidad, estado, descripcion);
            boolean guardado = espacioDAO.insertar(nuevo);
            if (!guardado) {
                mostrarAlerta(AlertType.ERROR, "No se pudo guardar",
                        "Ocurrió un error al registrar el espacio en la base de datos. Intenta de nuevo.");
                return;
            }
            espacios.add(nuevo);
        } else {
            // Modo edición: actualizar primero en BD y, si funciona, en el objeto en memoria
            boolean actualizado = espacioDAO.actualizar(espacioEnEdicion.getIdEspacio(),
                    new EspacioRegistro(clave, nombre, tipo, capacidad, estado, descripcion));
            if (!actualizado) {
                mostrarAlerta(AlertType.ERROR, "No se pudo actualizar",
                        "Ocurrió un error al actualizar el espacio en la base de datos. Intenta de nuevo.");
                return;
            }
            espacioEnEdicion.setClave(clave);
            espacioEnEdicion.setNombre(nombre);
            espacioEnEdicion.setTipo(tipo);
            espacioEnEdicion.setCapacidad(capacidad);
            espacioEnEdicion.setEstado(estado);
            espacioEnEdicion.setDescripcion(descripcion);
            tblEspacios.refresh();
        }
 
        limpiarFormulario();
        actualizarContador();
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
    private void onNuevoEspacio() {
        limpiarFormulario();
        txtClave.requestFocus();
    }
 
    @FXML
    private void onRefrescar() {
        txtBuscar.clear();
        cmbFiltroTipo.setValue("Todos los tipos");
        cmbFiltroEstado.setValue("Todos los estados");
        cargarDatosIniciales();
        aplicarFiltros();
    }
 
    private void limpiarFormulario() {
        espacioEnEdicion = null;
        txtClave.clear();
        txtNombre.clear();
        cmbTipo.setValue(null);
        txtCapacidad.clear();
        cmbEstado.setValue(null);
        txtDescripcion.clear();
        btnGuardar.setText("💾  Guardar");
    }
 
    private void cargarEnFormulario(EspacioRegistro espacio) {
        espacioEnEdicion = espacio;
        txtClave.setText(espacio.getClave());
        txtNombre.setText(espacio.getNombre());
        cmbTipo.setValue(espacio.getTipo());
        txtCapacidad.setText(String.valueOf(espacio.getCapacidad()));
        cmbEstado.setValue(espacio.getEstado());
        txtDescripcion.setText(espacio.getDescripcion());
        btnGuardar.setText("💾  Guardar cambios");
    }
 
    private void eliminarEspacio(EspacioRegistro espacio) {
        Alert confirmacion = crearDialogoTematico(AlertType.CONFIRMATION, "🗑",
                "Eliminar espacio", "¿Eliminar \"" + espacio.getNombre() + "\"?",
                "Esta acción no se puede deshacer. No se puede eliminar un espacio "
                        + "que tenga asignaciones registradas.");
 
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean eliminado = espacioDAO.eliminar(espacio.getIdEspacio());
            if (!eliminado) {
                mostrarAlerta(AlertType.WARNING, "No se pudo eliminar",
                        "Este espacio tiene asignaciones (u otros registros) relacionados, "
                                + "así que no se puede eliminar mientras existan.");
                return;
            }
            espacios.remove(espacio);
            if (espacioEnEdicion == espacio) {
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
     * morada de esta pantalla (colores y estilos de styles_espacios.css) y
     * con las mismas medidas máximas usadas para las ventanas flotantes del
     * resto del sistema (ver el modal de fx_inicio.fxml: maxWidth 900 /
     * maxHeight 620), para que todas las ventanas emergentes de la
     * aplicación luzcan y midan igual entre sí.
     */
    private Alert crearDialogoTematico(AlertType tipo, String glifo, String titulo,
                                        String encabezado, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado != null ? encabezado : titulo);
        alerta.setContentText(mensaje);
 
        DialogPane panel = alerta.getDialogPane();
        panel.getStylesheets().add(getClass().getResource("/mx/utng/view/styles_espacios.css").toExternalForm());
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
 
    /** Pinta el estado como una "pastilla" de color, igual que en el diseño. */
    private class EstadoBadgeCell extends javafx.scene.control.TableCell<EspacioRegistro, String> {
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
            badge.getStyleClass().removeIf(s -> s.startsWith("estado-"));
            badge.getStyleClass().add("estado-badge");
            if ("Disponible".equals(estado)) {
                badge.getStyleClass().add("estado-disponible");
            } else if ("Ocupado".equals(estado)) {
                badge.getStyleClass().add("estado-ocupado");
            } else if ("En mantenimiento".equals(estado)) {
                badge.getStyleClass().add("estado-mantenimiento");
            } else if ("Fuera de servicio".equals(estado)) {
                badge.getStyleClass().add("estado-fuera");
            }
            setGraphic(badge);
        }
    }
 
    private class AccionesCell extends javafx.scene.control.TableCell<EspacioRegistro, Void> {
        private final Button btnEditar = new Button("✎");
        private final Button btnEliminar = new Button("🗑");
        private final HBox contenedor = new HBox(8, btnEditar, btnEliminar);
 
        AccionesCell() {
            btnEditar.getStyleClass().add("accion-editar-btn");
            btnEliminar.getStyleClass().add("accion-eliminar-btn");
            contenedor.setAlignment(javafx.geometry.Pos.CENTER);
 
            btnEditar.setOnAction(e -> {
                EspacioRegistro espacio = getTableView().getItems().get(getIndex());
                cargarEnFormulario(espacio);
            });
            btnEliminar.setOnAction(e -> {
                EspacioRegistro espacio = getTableView().getItems().get(getIndex());
                eliminarEspacio(espacio);
            });
        }
 
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : contenedor);
        }
    }
}