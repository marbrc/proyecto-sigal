package mx.utng.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.nio.file.Files;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import mx.utng.dao.UsuarioDAO;
import mx.utng.model.Usuario;
import mx.utng.util.AvatarUtil;

/**
 * Controlador de la pantalla "Mi cuenta" (fx_cuenta.fxml).
 *
 * Permite al usuario en sesión actualizar su nombre, apellidos,
 * nombre de usuario y correo, además de cambiar su contraseña.
 *
 * Igual que AsignacionesController, este es un módulo que
 * MenuController carga dentro de su contentPane, así que necesita
 * la referencia al menú para saber quién es el usuario en sesión.
 */
public class CuentaController implements Initializable {

    private MenuController menuController;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
        cargarDatosUsuario();
    }

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private Usuario usuarioActual;

    // --------------------------- Tarjeta de identidad ---------------------------
    @FXML private Label lblCuentaNombreCompleto;
    @FXML private Label lblCuentaIniciales;
    @FXML private Label lblCuentaCorreo;
    @FXML private Label lblCuentaRolBadge;
    @FXML private Circle avatarCirculo;
    @FXML private ImageView imgAvatarCuenta;
    @FXML private Button btnCambiarFoto;
    @FXML private Button btnQuitarFoto;

    // --------------------------- Datos personales ---------------------------
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidoPaterno;
    @FXML private TextField txtApellidoMaterno;
    @FXML private TextField txtNombreUsuario;
    @FXML private TextField txtCorreo;
    @FXML private Button btnGuardarDatos;
    @FXML private Button btnCancelarDatos;

    // --------------------------- Cambiar contraseña ---------------------------
    @FXML private PasswordField txtContrasenaActual;
    @FXML private PasswordField txtContrasenaNueva;
    @FXML private PasswordField txtConfirmarContrasena;
    @FXML private Button btnGuardarContrasena;
    
    @FXML private VBox panelAdminUsuarios;
    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, String> colUsuarioNombre;
    @FXML private TableColumn<Usuario, String> colUsuarioNombreUsuario;
    @FXML private TableColumn<Usuario, String> colUsuarioCorreo;
    @FXML private TableColumn<Usuario, String> colUsuarioRol;
    @FXML private TableColumn<Usuario, String> colUsuarioEstado;
    @FXML private TableColumn<Usuario, Void> colUsuarioAcciones;
    @FXML private Button btnToggleUsuarios;
    @FXML private StackPane contenedorTablaUsuarios;

    private boolean tablaUsuariosVisible = false;
    private static final double ALTURA_TABLA_USUARIOS = 260.0;
    private boolean tablaUsuariosConfigurada = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Los datos reales se cargan hasta que MenuController llama
        // setMenuController(...), porque ahí es cuando sabemos el
        // ID_Usuario en sesión.
    }

    /**
     * Trae de tb_usuario los datos del usuario en sesión y los
     * muestra tanto en la tarjeta de identidad como en el formulario.
     */
    private void cargarDatosUsuario() {
        if (menuController == null) return;

        int idUsuario = menuController.getIdUsuarioActual();
        if (idUsuario <= 0) return;

        usuarioActual = usuarioDAO.obtenerPorId(idUsuario);
        if (usuarioActual == null) {
            mostrarAlerta(AlertType.ERROR, "No se pudo cargar tu cuenta",
                    "No se encontraron los datos de tu usuario en la base de datos.");
            return;
        }

        

        txtNombre.setText(usuarioActual.getNombre());
        txtApellidoPaterno.setText(usuarioActual.getApellidoPaterno());
        txtApellidoMaterno.setText(usuarioActual.getApellidoMaterno());
        txtNombreUsuario.setText(usuarioActual.getNombreUsuario());

        String nombreCompleto = (usuarioActual.getNombre() + " " + usuarioActual.getApellidoPaterno()).trim();
        lblCuentaNombreCompleto.setText(nombreCompleto.isBlank() ? usuarioActual.getNombreUsuario() : nombreCompleto);
        lblCuentaIniciales.setText(obtenerIniciales(usuarioActual.getNombre(), usuarioActual.getApellidoPaterno()));
        lblCuentaCorreo.setText(usuarioActual.getCorreoElectronico());
        lblCuentaRolBadge.setText(usuarioActual.getRol());

        AvatarUtil.aplicar(imgAvatarCuenta, lblCuentaIniciales, usuarioActual.getFotoPerfil());

        configurarPanelAdmin();
    }

     /**
     * Devuelve las iniciales (nombre + apellido) para mostrar en el
     * avatar circular de la cuenta, por ejemplo "Mar Rodriguez" -> "MR".
     */
    private String obtenerIniciales(String nombre, String apellido) {
        String i1 = (nombre != null && !nombre.isBlank()) ? nombre.trim().substring(0, 1) : "";
        String i2 = (apellido != null && !apellido.isBlank()) ? apellido.trim().substring(0, 1) : "";
        String iniciales = (i1 + i2).toUpperCase();
        return iniciales.isBlank() ? "?" : iniciales;
    }

    // ============================================================
    //  DATOS PERSONALES
    // ============================================================

    @FXML
    private void onGuardarDatos(ActionEvent event) {
        if (usuarioActual == null) return;

        if (txtNombre.getText().isBlank() || txtApellidoPaterno.getText().isBlank()
                || txtNombreUsuario.getText().isBlank() || txtCorreo.getText().isBlank()) {
            mostrarAlerta(AlertType.WARNING, "Campos incompletos",
                    "Llena todos los campos obligatorios (*) antes de guardar.");
            return;
        }

        if (!txtCorreo.getText().matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")) {
            mostrarAlerta(AlertType.WARNING, "Correo inválido",
                    "Escribe un correo electrónico válido.");
            return;
        }

        String nuevoNombreUsuario = txtNombreUsuario.getText().trim();

        if (usuarioDAO.existeNombreUsuario(nuevoNombreUsuario, usuarioActual.getIdUsuario())) {
            mostrarAlerta(AlertType.WARNING, "Nombre de usuario en uso",
                    "Ya existe otra cuenta con ese nombre de usuario. Elige otro.");
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION,
                    "¿Seguro que quieres guardar estos cambios en tu cuenta?");
        confirmacion.setTitle("Confirmar cambios");
        confirmacion.setHeaderText(null);
        if (confirmacion.showAndWait().filter(boton -> boton == javafx.scene.control.ButtonType.OK).isEmpty()) {
            return;
        }

        boolean actualizado = usuarioDAO.actualizarDatos(
                usuarioActual.getIdUsuario(),
                txtNombre.getText().trim(),
                txtApellidoPaterno.getText().trim(),
                txtApellidoMaterno.getText().trim(),
                nuevoNombreUsuario,
                txtCorreo.getText().trim()
        );

        if (!actualizado) {
            mostrarAlerta(AlertType.ERROR, "No se pudo guardar",
                    "Ocurrió un problema al actualizar tus datos en la base de datos.");
            return;
        }

        // Refresca el objeto en memoria y el nombre que se muestra en el topbar del menú
        usuarioActual.setNombre(txtNombre.getText().trim());
        usuarioActual.setApellidoPaterno(txtApellidoPaterno.getText().trim());
        usuarioActual.setApellidoMaterno(txtApellidoMaterno.getText().trim());
        usuarioActual.setNombreUsuario(nuevoNombreUsuario);
        usuarioActual.setCorreoElectronico(txtCorreo.getText().trim());

        if (menuController != null) {
            menuController.actualizarNombreUsuarioEnSesion(usuarioActual.getNombre());
        }

        cargarDatosUsuario();
        mostrarAlerta(AlertType.INFORMATION, "Datos actualizados",
                "Tu información se guardó correctamente.");
    }

    @FXML
    private void onCancelarDatos(ActionEvent event) {
        cargarDatosUsuario();
    }

    // ============================================================
    //  CAMBIAR CONTRASEÑA
    // ============================================================

    @FXML
    private void onCambiarContrasena(ActionEvent event) {
        if (usuarioActual == null) return;

        String actual = txtContrasenaActual.getText();
        String nueva = txtContrasenaNueva.getText();
        String confirmar = txtConfirmarContrasena.getText();

        if (actual.isBlank() || nueva.isBlank() || confirmar.isBlank()) {
            mostrarAlerta(AlertType.WARNING, "Campos incompletos",
                    "Llena tu contraseña actual, la nueva y su confirmación.");
            return;
        }

        if (nueva.length() < 6) {
            mostrarAlerta(AlertType.WARNING, "Contraseña muy corta",
                    "La nueva contraseña debe tener al menos 6 caracteres.");
            return;
        }

        if (!nueva.equals(confirmar)) {
            mostrarAlerta(AlertType.WARNING, "Las contraseñas no coinciden",
                    "La nueva contraseña y su confirmación deben ser iguales.");
            return;
        }

        if (!usuarioDAO.validarContrasenaActual(usuarioActual.getIdUsuario(), actual)) {
            mostrarAlerta(AlertType.ERROR, "Contraseña incorrecta",
                    "La contraseña actual que escribiste no es correcta.");
            return;
        }

        boolean actualizada = usuarioDAO.actualizarContrasena(usuarioActual.getIdUsuario(), nueva);

        if (!actualizada) {
            mostrarAlerta(AlertType.ERROR, "No se pudo cambiar",
                    "Ocurrió un problema al actualizar tu contraseña en la base de datos.");
            return;
        }

        txtContrasenaActual.clear();
        txtContrasenaNueva.clear();
        txtConfirmarContrasena.clear();

        mostrarAlerta(AlertType.INFORMATION, "Contraseña actualizada",
                "Tu contraseña se cambió correctamente.");
    }

    // ============================================================
    //  FOTO DE PERFIL
    // ============================================================

    /** 3 MB: suficiente para una foto de perfil sin dejar crecer demasiado la base de datos. */
    private static final long TAMANO_MAXIMO_FOTO_BYTES = 3L * 1024 * 1024;

    @FXML
    private void onCambiarFoto(ActionEvent event) {
        if (usuarioActual == null) return;

        FileChooser selector = new FileChooser();
        selector.setTitle("Elige tu foto de perfil");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes (JPG, PNG)", "*.jpg", "*.jpeg", "*.png"));

        Window ventana = btnCambiarFoto.getScene() != null ? btnCambiarFoto.getScene().getWindow() : null;
        File archivo = selector.showOpenDialog(ventana);
        if (archivo == null) {
            return; // el usuario cerró el selector sin elegir nada
        }

        if (archivo.length() > TAMANO_MAXIMO_FOTO_BYTES) {
            mostrarAlerta(AlertType.WARNING, "Imagen muy pesada",
                    "Elige una imagen de máximo 3 MB.");
            return;
        }

        try {
            byte[] fotoBytes = Files.readAllBytes(archivo.toPath());
            guardarFoto(fotoBytes);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(AlertType.ERROR, "No se pudo leer la imagen",
                    "Ocurrió un problema al abrir el archivo que elegiste.");
        }
    }

    @FXML
    private void onQuitarFoto(ActionEvent event) {
        if (usuarioActual == null || usuarioActual.getFotoPerfil() == null) {
            return; // ya no tiene foto, nada que quitar
        }
        guardarFoto(null);
    }

    /** Guarda la foto (o null para quitarla) en la BD y la refleja en esta pantalla y en la barra lateral. */
    private void guardarFoto(byte[] fotoBytes) {
        boolean guardado = usuarioDAO.actualizarFotoPerfil(usuarioActual.getIdUsuario(), fotoBytes);

        if (!guardado) {
            mostrarAlerta(AlertType.ERROR, "No se pudo guardar la foto",
                    "Ocurrió un problema al guardar tu foto de perfil en la base de datos.");
            return;
        }

        usuarioActual.setFotoPerfil(fotoBytes);
        AvatarUtil.aplicar(imgAvatarCuenta, lblCuentaIniciales, fotoBytes);

        if (menuController != null) {
            menuController.setFotoPerfilSesion(fotoBytes);
        }

        mostrarAlerta(AlertType.INFORMATION,
                fotoBytes == null ? "Foto eliminada" : "Foto actualizada",
                fotoBytes == null ? "Tu foto de perfil se quitó correctamente."
                                   : "Tu foto de perfil se actualizó correctamente.");
    }

        // ============================================================
    //  ADMINISTRACIÓN DE USUARIOS
    // ============================================================

    private void configurarPanelAdmin() {
        panelAdminUsuarios.setVisible(true);
        panelAdminUsuarios.setManaged(true);

        if (!tablaUsuariosConfigurada) {
            colUsuarioNombre.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            (data.getValue().getNombre() + " " + data.getValue().getApellidoPaterno()).trim()));
            colUsuarioNombreUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
            colUsuarioCorreo.setCellValueFactory(new PropertyValueFactory<>("correoElectronico"));
            colUsuarioRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
            colUsuarioEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

            colUsuarioAcciones.setCellFactory(col -> new TableCell<Usuario, Void>() {
                private final Button btn = new Button();
                {
                    btn.setOnAction(e -> onCambiarEstadoUsuario(getTableView().getItems().get(getIndex())));
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                        return;
                    }
                    Usuario u = getTableView().getItems().get(getIndex());
                    boolean activo = "Activo".equalsIgnoreCase(u.getEstado());
                    btn.setText(activo ? "Desactivar" : "Reactivar");
                    btn.getStyleClass().setAll(activo ? "btn-danger-compact" : "btn-primary-compact");
                    setGraphic(btn);
                }
            });

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(contenedorTablaUsuarios.widthProperty());
            clip.heightProperty().bind(contenedorTablaUsuarios.heightProperty());
            contenedorTablaUsuarios.setClip(clip);

            tablaUsuariosConfigurada = true;
        }

        cargarListaUsuarios();
    }

    private void cargarListaUsuarios() {
        tblUsuarios.setItems(FXCollections.observableArrayList(
                usuarioDAO.listarUsuarios(usuarioActual.getIdUsuario())));
    }

    private void onCambiarEstadoUsuario(Usuario u) {
        boolean activo = "Activo".equalsIgnoreCase(u.getEstado());
        String accion = activo ? "desactivar" : "reactivar";

        Alert confirmacion = new Alert(AlertType.CONFIRMATION,
                "¿Seguro que quieres " + accion + " la cuenta de "
                        + u.getNombre() + " " + u.getApellidoPaterno() + "?");
        confirmacion.setTitle(activo ? "Desactivar cuenta" : "Reactivar cuenta");
        confirmacion.setHeaderText(null);
        if (confirmacion.showAndWait().filter(b -> b == javafx.scene.control.ButtonType.OK).isEmpty()) {
            return;
        }

        boolean ok = activo
                ? usuarioDAO.desactivarUsuario(u.getIdUsuario())
                : usuarioDAO.reactivarUsuario(u.getIdUsuario());

        if (!ok) {
            mostrarAlerta(AlertType.ERROR, "No se pudo " + accion,
                    "Ocurrió un problema al actualizar el estado de esa cuenta.");
            return;
        }

        cargarListaUsuarios();
        mostrarAlerta(AlertType.INFORMATION, "Listo",
                "La cuenta se " + (activo ? "desactivó" : "reactivó") + " correctamente.");
    }

    @FXML
    private void onToggleTablaUsuarios(ActionEvent event) {
        tablaUsuariosVisible = !tablaUsuariosVisible;
        double destino = tablaUsuariosVisible ? ALTURA_TABLA_USUARIOS : 0.0;

        Timeline animacion = new Timeline(
                new KeyFrame(Duration.millis(260),
                        new KeyValue(contenedorTablaUsuarios.prefHeightProperty(), destino, Interpolator.EASE_BOTH))
        );
        animacion.play();

        btnToggleUsuarios.setText(tablaUsuariosVisible ? "Ocultar usuarios ▴" : "Ver usuarios ▾");
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
