package mx.utng.controller;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

// Controller de fx_registro.fxml (pantalla "Crear cuenta" de SIGAL).
public class RegistroController {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellidoPaterno;
    @FXML
    private TextField txtApellidoMaterno;
    @FXML
    private TextField txtCorreo;

    // Contraseña (campo oculto + campo visible que se van intercambiando)
    @FXML
    private PasswordField pwdContrasena;
    @FXML
    private TextField txtContrasenaVisible;
    @FXML
    private Button btnToggleContrasena;
    
    // Confirmar contraseña (mismo patrón)
    @FXML
    private PasswordField pwdConfirmarContrasena;
    @FXML
    private TextField txtConfirmarContrasenaVisible;
    @FXML
    private Button btnToggleConfirmarContrasena;
    
    @FXML
    private ComboBox<String> cmbRol;

    @FXML
    private Button btnRegistrarse;
    @FXML
    private Button btnVolverLogin;

    // Guardan si la contraseña se está mostrando en texto plano o no
    private boolean contrasenaVisible = false;
    private boolean confirmarContrasenaVisible = false;

    // Se ejecuta al abrir la pantalla
    @FXML
    public void initialize() {

    cmbRol.setItems(FXCollections.observableArrayList(
            "Usuario",
            "🔒 Administrador"));

    cmbRol.setOnAction(e -> {

        if ("🔒 Administrador".equals(cmbRol.getValue())) {

            AdminController.abrirVentana(
                    cmbRol.getScene().getWindow()
            );

        }

    });

}
    // =========================================================
    // MOSTRAR / OCULTAR CONTRASEÑA
    // =========================================================
    // La idea: el PasswordField (pwdContrasena) y el TextField (txtContrasenaVisible)
    // están encimados en el mismo lugar (un StackPane). Solo se ve uno a la vez.
    // Al picarle al ojito, se intercambian y se copia el texto de uno al otro.

    @FXML
    private void onToggleContrasena() {

        contrasenaVisible = !contrasenaVisible;

        if (contrasenaVisible) {
            // Copiamos lo que hay escrito al campo visible, y lo mostramos
            txtContrasenaVisible.setText(pwdContrasena.getText());
            txtContrasenaVisible.setVisible(true);
            txtContrasenaVisible.setManaged(true);
            pwdContrasena.setVisible(false);
            pwdContrasena.setManaged(false);
        } else {
            // Regresamos lo escrito al campo oculto, y ocultamos el texto
            pwdContrasena.setText(txtContrasenaVisible.getText());
            pwdContrasena.setVisible(true);
            pwdContrasena.setManaged(true);
            txtContrasenaVisible.setVisible(false);
            txtContrasenaVisible.setManaged(false);
        }

        // La rayita (Line) sobre el ojo indica "contraseña visible" cuando se muestra
    }

    @FXML
    private void onToggleConfirmarContrasena() {

        confirmarContrasenaVisible = !confirmarContrasenaVisible;

        if (confirmarContrasenaVisible) {
            txtConfirmarContrasenaVisible.setText(pwdConfirmarContrasena.getText());
            txtConfirmarContrasenaVisible.setVisible(true);
            txtConfirmarContrasenaVisible.setManaged(true);
            pwdConfirmarContrasena.setVisible(false);
            pwdConfirmarContrasena.setManaged(false);
        } else {
            pwdConfirmarContrasena.setText(txtConfirmarContrasenaVisible.getText());
            pwdConfirmarContrasena.setVisible(true);
            pwdConfirmarContrasena.setManaged(true);
            txtConfirmarContrasenaVisible.setVisible(false);
            txtConfirmarContrasenaVisible.setManaged(false);
        }

    }

    // Trae la contraseña actual sin importar cuál de los dos campos
    // (oculto o visible) es el que se está mostrando ahorita
    private String obtenerContrasena() {
        return contrasenaVisible ? txtContrasenaVisible.getText() : pwdContrasena.getText();
    }

    private String obtenerConfirmarContrasena() {
        return confirmarContrasenaVisible ? txtConfirmarContrasenaVisible.getText() : pwdConfirmarContrasena.getText();
    }

    // =========================================================
    // REGISTRARSE
    // =========================================================
    // IMPORTANTE: todavía no está conectado a la base de datos.
    // Aquí solo se validan los campos. Cuando me pases el
    // UsuarioDAO / modelo Usuario de SIGAL (con los nombres
    // reales de columnas de tb_usuario), completamos esta parte
    // para que sí inserte el registro de verdad.
    @FXML
    private void onRegistrarse() {

        String nombre = txtNombre.getText();
        String apellidoPaterno = txtApellidoPaterno.getText();
        String apellidoMaterno = txtApellidoMaterno.getText();
        String correo = txtCorreo.getText();
        String contrasena = obtenerContrasena();
        String confirmar = obtenerConfirmarContrasena();
        String rol = cmbRol.getValue();

        if (nombre.isBlank() || apellidoPaterno.isBlank() || apellidoMaterno.isBlank()
                || correo.isBlank() || contrasena.isBlank() || confirmar.isBlank() || rol == null) {
            mostrarMensaje(Alert.AlertType.ERROR, "Complete todos los campos.");
            return;
        }

        if (!contrasena.equals(confirmar)) {
            mostrarMensaje(Alert.AlertType.ERROR, "Las contraseñas no coinciden.");
            return;
        }

        // TODO: aquí falta llamar al DAO real, algo como:
        // Usuario usuario = new Usuario();
        // usuario.setNombre(nombre);
        // ... etc
        // if (dao.guardar(usuario)) { ... }

        mostrarMensaje(Alert.AlertType.INFORMATION,
                "Los datos son válidos. Falta conectar esto con la base de datos de SIGAL.");
    }

    // =========================================================
    // VOLVER AL LOGIN
    // =========================================================
    @FXML
    private void onVolverLogin() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/mx/utng/view/fx_login.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) btnVolverLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarMensaje(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.show();
    }

}

