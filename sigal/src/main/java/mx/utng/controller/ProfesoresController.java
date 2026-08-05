package mx.utng.controller;

import java.net.URL;
import java.util.Arrays;
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
import javafx.scene.control.Label;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import mx.utng.model.Profesor;

public class ProfesoresController implements Initializable {

    // -------- Formulario "Datos del profesor" --------
    @FXML private TextField txtNombreCompleto;
    @FXML private TextField txtIdentificador;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbCarrera;
    @FXML private HBox boxOtraCarrera;
    @FXML private TextField txtOtraCarrera;

    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Button btnNuevoProfesor;

    // -------- Panel "Profesores registrados" --------
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroCarrera;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private Button btnRefrescar;

    @FXML private TableView<Profesor> tblProfesores;
    @FXML private TableColumn<Profesor, Void> colNo;
    @FXML private TableColumn<Profesor, String> colNombre;
    @FXML private TableColumn<Profesor, String> colIdentificador;
    @FXML private TableColumn<Profesor, String> colCorreo;
    @FXML private TableColumn<Profesor, String> colCarrera;
    @FXML private TableColumn<Profesor, Void> colAcciones;

    @FXML private Label lblResultados;

    // -------- Estado interno --------
    private final ObservableList<Profesor> profesores = FXCollections.observableArrayList();
    private FilteredList<Profesor> profesoresFiltrados;

    /** Profesor que se está editando actualmente (null = modo "nuevo profesor"). */
    private Profesor profesorEnEdicion;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static final String[] CARRERAS = {
            "Diseño y Animación Digital",
            "Desarrollo de Software Multiplataforma",
            "Infraestructura de Redes Digitales",
            "Entornos Virtuales y Negocios Digitales",
            "Otra"
    };

    private static final String[] ESTADOS = {
            "Activo",
            "Inactivo"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbCarrera.getItems().addAll(CARRERAS);

        cmbFiltroCarrera.getItems().add("Todas las carreras");
        cmbFiltroCarrera.getItems().addAll(CARRERAS);
        cmbFiltroCarrera.setValue("Todas las carreras");

        cmbFiltroEstado.getItems().add("Todos los estados");
        cmbFiltroEstado.getItems().addAll(ESTADOS);
        cmbFiltroEstado.setValue("Todos los estados");

        // Al elegir "Otra" se muestra el campo de texto libre (puede quedar vacío).
        cmbCarrera.valueProperty().addListener((obs, anterior, actual) -> {
            boolean esOtra = "Otra".equals(actual);
            boxOtraCarrera.setVisible(esOtra);
            boxOtraCarrera.setManaged(esOtra);
            if (!esOtra) {
                txtOtraCarrera.clear();
            }
        });

        cargarDatosIniciales();
        configurarTabla();
        configurarFiltros();

        actualizarContador();
    }

    /**
     * Punto de entrada para la carga inicial de profesores.
     * A propósito NO agrega registros de ejemplo: la aplicación debe iniciar
     * sin datos precargados.
     * TODO: sustituir por una carga real desde la base de datos, por ejemplo:
     * profesores.addAll(miDao.obtenerTodos());
     */
    private void cargarDatosIniciales() {
        // Sin datos por defecto: la tabla inicia vacía.
    }

    private void configurarTabla() {
        colNo.setCellFactory(col -> new NumeroFilaCell());

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colIdentificador.setCellValueFactory(new PropertyValueFactory<>("identificador"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colCarrera.setCellValueFactory(new PropertyValueFactory<>("carrera"));

        colAcciones.setCellFactory(col -> new AccionesCell());

        profesoresFiltrados = new FilteredList<>(profesores, p -> true);
        tblProfesores.setItems(profesoresFiltrados);
    }

    private void configurarFiltros() {
        txtBuscar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbFiltroCarrera.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((obs, oldV, newV) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String carrera = cmbFiltroCarrera.getValue();
        String estado = cmbFiltroEstado.getValue();

        profesoresFiltrados.setPredicate(prof -> {
            boolean coincideTexto = texto.isEmpty()
                    || prof.getNombreCompleto().toLowerCase().contains(texto)
                    || prof.getIdentificador().toLowerCase().contains(texto)
                    || prof.getCorreo().toLowerCase().contains(texto);
            boolean coincideCarrera = carrera == null || carrera.equals("Todas las carreras") || carrera.equals(prof.getCarrera());
            boolean coincideEstado = estado == null || estado.equals("Todos los estados") || estado.equals(prof.getEstado());
            return coincideTexto && coincideCarrera && coincideEstado;
        });

        actualizarContador();
        tblProfesores.refresh();
    }

    private void actualizarContador() {
        int mostrados = profesoresFiltrados == null ? 0 : profesoresFiltrados.size();
        int total = profesores.size();
        if (lblResultados != null) {
            if (mostrados == 0) {
                lblResultados.setText("Mostrando 0 de " + total + " profesores");
            } else {
                lblResultados.setText("Mostrando 1 a " + mostrados + " de " + total + " profesores");
            }
        }
    }

    // ==================== ACCIONES DEL FORMULARIO ====================

    @FXML
    private void onGuardar() {
        String nombre = safeTrim(txtNombreCompleto.getText());
        String identificador = safeTrim(txtIdentificador.getText());
        String correo = safeTrim(txtCorreo.getText());
        String carreraSeleccionada = cmbCarrera.getValue();

        if (nombre.isEmpty() || identificador.isEmpty() || correo.isEmpty() || carreraSeleccionada == null) {
            mostrarAlerta(AlertType.WARNING, "Campos incompletos",
                    "Por favor complete el nombre, identificador, correo y carrera antes de guardar.");
            return;
        }

        // Si eligió "Otra", se usa lo que escribió (o "Otra" si lo dejó en blanco).
        String carrera = "Otra".equals(carreraSeleccionada)
                ? (txtOtraCarrera.getText() == null || txtOtraCarrera.getText().isBlank()
                        ? "Otra" : txtOtraCarrera.getText().trim())
                : carreraSeleccionada;

        if (!EMAIL_PATTERN.matcher(correo).matches()) {
            mostrarAlerta(AlertType.WARNING, "Correo inválido",
                    "Ingrese un correo electrónico con un formato válido, por ejemplo usuario@utng.edu.mx.");
            return;
        }

        // Validar que el identificador sea único (excepto cuando se está editando ese mismo profesor)
        boolean idDuplicado = profesores.stream()
                .anyMatch(p -> p.getIdentificador().equalsIgnoreCase(identificador) && p != profesorEnEdicion);
        if (idDuplicado) {
            mostrarAlerta(AlertType.WARNING, "Identificador duplicado",
                    "Ya existe un profesor registrado con el identificador \"" + identificador + "\". Debe ser único.");
            return;
        }

        if (profesorEnEdicion == null) {
            // Modo creación
            profesores.add(new Profesor(nombre, identificador, correo, carrera, "Activo"));
        } else {
            // Modo edición: actualizar el objeto existente
            profesorEnEdicion.setNombreCompleto(nombre);
            profesorEnEdicion.setIdentificador(identificador);
            profesorEnEdicion.setCorreo(correo);
            profesorEnEdicion.setCarrera(carrera);
            tblProfesores.refresh();
        }

        // TODO: aquí va la llamada real a tu capa de datos (INSERT / UPDATE en BD)

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
    private void onNuevoProfesor() {
        limpiarFormulario();
        txtNombreCompleto.requestFocus();
    }

    @FXML
    private void onRefrescar() {
        txtBuscar.clear();
        cmbFiltroCarrera.setValue("Todas las carreras");
        cmbFiltroEstado.setValue("Todos los estados");
        // TODO: aquí volverías a consultar la base de datos.
        aplicarFiltros();
    }

    private void limpiarFormulario() {
        profesorEnEdicion = null;
        txtNombreCompleto.clear();
        txtIdentificador.clear();
        txtCorreo.clear();
        cmbCarrera.setValue(null);
        txtOtraCarrera.clear();
        boxOtraCarrera.setVisible(false);
        boxOtraCarrera.setManaged(false);
        btnGuardar.setText("💾  Guardar");
    }

    private void cargarEnFormulario(Profesor profesor) {
        profesorEnEdicion = profesor;
        txtNombreCompleto.setText(profesor.getNombreCompleto());
        txtIdentificador.setText(profesor.getIdentificador());
        txtCorreo.setText(profesor.getCorreo());

        // Si la carrera guardada no es una de las 4 conocidas, se trata como "Otra".
        String carreraGuardada = profesor.getCarrera();
        boolean esConocida = Arrays.asList(CARRERAS).contains(carreraGuardada) && !"Otra".equals(carreraGuardada);

        if (esConocida) {
            cmbCarrera.setValue(carreraGuardada);
            boxOtraCarrera.setVisible(false);
            boxOtraCarrera.setManaged(false);
            txtOtraCarrera.clear();
        } else {
            cmbCarrera.setValue("Otra");
            boxOtraCarrera.setVisible(true);
            boxOtraCarrera.setManaged(true);
            txtOtraCarrera.setText(carreraGuardada);
        }

        btnGuardar.setText("💾  Guardar cambios");
    }

    private void eliminarProfesor(Profesor profesor) {
        Alert confirmacion = crearDialogoTematico(AlertType.CONFIRMATION, "🗑",
                "Eliminar profesor", "¿Eliminar a \"" + profesor.getNombreCompleto() + "\"?",
                "Esta acción no se puede deshacer. No se puede eliminar un profesor "
                        + "que tenga asignaciones registradas.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            profesores.remove(profesor);
            // TODO: aquí va el DELETE real en la base de datos
            if (profesorEnEdicion == profesor) {
                limpiarFormulario();
            }
            actualizarContador();
            tblProfesores.refresh();
        }
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        String glifo = tipo == AlertType.ERROR ? "✕" : "⚠";
        Alert alerta = crearDialogoTematico(tipo, glifo, titulo, null, mensaje);
        alerta.showAndWait();
    }

    /**
     * Construye una ventana flotante (Alert) con la misma temática oscura /
     * azul de esta pantalla (colores y estilos de styles_profesores.css) y
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
