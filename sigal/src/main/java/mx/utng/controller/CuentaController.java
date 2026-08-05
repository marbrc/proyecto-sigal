package mx.utng.controller;

import java.net.URL;
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
import mx.utng.dao.UsuarioDAO;
import mx.utng.model.Usuario;

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

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
