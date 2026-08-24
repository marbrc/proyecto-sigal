package mx.utng.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
import javafx.scene.layout.VBox;
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
    @FXML private TextField txtContrasenaNuevaVisible;
    @FXML private TextField txtConfirmarContrasenaVisible;
    @FXML private Button btnGuardarContrasena;
    @FXML private Button btnVerContrasenaNueva;
    @FXML private Button btnVerConfirmarContrasena;

    // --------------------------- Opciones de la cuenta ---------------------------
    @FXML private VBox panelOpcionesCuenta;
    @FXML private Label lblEstadoCuentaActual;
    @FXML private Button btnEliminarMiCuenta;

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

        configurarPanelCuenta();
    }

    // ============================================================
    //  MOSTRAR / OCULTAR CONTRASEÑA
    // ============================================================

    @FXML
private void onToggleVerContrasenaNueva() {

    if (txtContrasenaNueva.isVisible()) {

        txtContrasenaNuevaVisible.setText(txtContrasenaNueva.getText());

        txtContrasenaNueva.setVisible(false);
        txtContrasenaNueva.setManaged(false);

        txtContrasenaNuevaVisible.setVisible(true);
        txtContrasenaNuevaVisible.setManaged(true);


    } else {

        txtContrasenaNueva.setText(txtContrasenaNuevaVisible.getText());

        txtContrasenaNuevaVisible.setVisible(false);
        txtContrasenaNuevaVisible.setManaged(false);

        txtContrasenaNueva.setVisible(true);
        txtContrasenaNueva.setManaged(true);


    }

}

@FXML
private void onToggleVerConfirmarContrasena() {

    if (txtConfirmarContrasena.isVisible()) {

        txtConfirmarContrasenaVisible.setText(txtConfirmarContrasena.getText());

        txtConfirmarContrasena.setVisible(false);
        txtConfirmarContrasena.setManaged(false);

        txtConfirmarContrasenaVisible.setVisible(true);
        txtConfirmarContrasenaVisible.setManaged(true);


    } else {

        txtConfirmarContrasena.setText(txtConfirmarContrasenaVisible.getText());

        txtConfirmarContrasenaVisible.setVisible(false);
        txtConfirmarContrasenaVisible.setManaged(false);

        txtConfirmarContrasena.setVisible(true);
        txtConfirmarContrasena.setManaged(true);


    }

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

        // Si el usuario deja un campo vacío, se conserva el valor que ya tenía.
        String nombre = txtNombre.getText().isBlank() ? usuarioActual.getNombre() : txtNombre.getText().trim();
        String apellidoPaterno = txtApellidoPaterno.getText().isBlank() ? usuarioActual.getApellidoPaterno() : txtApellidoPaterno.getText().trim();
        String apellidoMaterno = txtApellidoMaterno.getText().isBlank() ? usuarioActual.getApellidoMaterno() : txtApellidoMaterno.getText().trim();
        String nombreUsuario = txtNombreUsuario.getText().isBlank() ? usuarioActual.getNombreUsuario() : txtNombreUsuario.getText().trim();
        String correo = txtCorreo.getText().isBlank() ? usuarioActual.getCorreoElectronico() : txtCorreo.getText().trim();

        if (!correo.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")) {
            mostrarAlerta(AlertType.WARNING, "Correo inválido",
                    "Escribe un correo electrónico válido.");
            return;
        }

        if (!nombreUsuario.equals(usuarioActual.getNombreUsuario())
                && usuarioDAO.existeNombreUsuario(nombreUsuario, usuarioActual.getIdUsuario())) {
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
                nombre, apellidoPaterno, apellidoMaterno, nombreUsuario, correo
        );

        if (!actualizado) {
            mostrarAlerta(AlertType.ERROR, "No se pudo guardar",
                    "Ocurrió un problema al actualizar tus datos en la base de datos.");
            return;
        }

        usuarioActual.setNombre(nombre);
        usuarioActual.setApellidoPaterno(apellidoPaterno);
        usuarioActual.setApellidoMaterno(apellidoMaterno);
        usuarioActual.setNombreUsuario(nombreUsuario);
        usuarioActual.setCorreoElectronico(correo);

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
    //  OPCIONES DE LA CUENTA (solo la propia; ya no se ven otros usuarios)
    // ============================================================

    private void configurarPanelCuenta() {
        lblEstadoCuentaActual.setText("Estado actual: " + usuarioActual.getEstado());
    }

    @FXML
    private void onEliminarMiCuenta(ActionEvent event) {
        if (usuarioActual == null) return;

        Alert confirmacion = new Alert(AlertType.CONFIRMATION,
                "Esto elimina tu cuenta de forma permanente y no se puede deshacer. "
                        + "¿Seguro que quieres continuar?");
        confirmacion.setTitle("Eliminar mi cuenta");
        confirmacion.setHeaderText(null);
        if (confirmacion.showAndWait().filter(b -> b == javafx.scene.control.ButtonType.OK).isEmpty()) {
            return;
        }

        boolean ok = usuarioDAO.eliminarUsuario(usuarioActual.getIdUsuario());
        if (!ok) {
            mostrarAlerta(AlertType.ERROR, "No se pudo eliminar",
                    "No se pudo eliminar tu cuenta. Es posible que tenga asignaciones registradas asociadas; "
                            + "si es así, usa \"Desactivar mi cuenta\" en su lugar.");
            return;
        }

        mostrarAlerta(AlertType.INFORMATION, "Cuenta eliminada",
                "Tu cuenta se eliminó correctamente. Se cerrará la sesión.");

        if (menuController != null) {
            menuController.cerrarSesionSinConfirmar();
        }
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}