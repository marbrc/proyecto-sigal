package mx.utng.controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import mx.utng.dao.UsuarioDAO;

public class RegistroController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidoPaterno;
    @FXML private TextField txtApellidoMaterno;
    @FXML private TextField txtNombreUsuario;
    @FXML private TextField txtCorreo;
    @FXML private PasswordField pwdContrasena;
    @FXML private TextField txtContrasenaVisible;
    @FXML private PasswordField pwdConfirmarContrasena;
    @FXML private TextField txtConfirmarContrasenaVisible;
    @FXML private Button btnToggleContrasena;
    @FXML private Button btnToggleConfirmarContrasena;

    private final UsuarioDAO dao = new UsuarioDAO();
    private boolean contrasenaVisible = false;
    private boolean confirmarVisible = false;

    @FXML
    public void onRegistrarse(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String apPaterno = txtApellidoPaterno.getText().trim();
        String apMaterno = txtApellidoMaterno.getText().trim();
        String nombreUsuario = txtNombreUsuario.getText().trim();
        String correo = txtCorreo.getText().trim();
        
        String contrasena = contrasenaVisible ? txtContrasenaVisible.getText() : pwdContrasena.getText();
        String confirmar = confirmarVisible ? txtConfirmarContrasenaVisible.getText() : pwdConfirmarContrasena.getText();

        // Validar campos obligatorios
        if (nombre.isEmpty() || apPaterno.isEmpty() || nombreUsuario.isEmpty()
                || correo.isEmpty() || contrasena.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Completa todos los campos obligatorios.");
            return;
        }

        // Validar que las contraseñas coincidan
        if (!contrasena.equals(confirmar)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Las contraseñas no coinciden.");
            return;
        }

        // Validar que no exista el nombre de usuario
     if (dao.existeNombreUsuario(nombreUsuario, -1)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Ese nombre de usuario ya está en uso.");
            return;
        }

        // Validar que no exista el correo
        if (dao.existeCorreo(correo)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Ya existe una cuenta con ese correo.");
            return;
        }

        // Guardar usuario en BD
        boolean ok = dao.registrar(nombre, apPaterno, apMaterno, nombreUsuario, correo, contrasena);
        if (ok) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Cuenta creada exitosamente. Ya puedes iniciar sesión.");
            onVolverLogin(event);
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo registrar. Intenta de nuevo.");
        }
    }

    @FXML
    public void onToggleContrasena(ActionEvent event) {
        contrasenaVisible = !contrasenaVisible;
        if (contrasenaVisible) {
            txtContrasenaVisible.setText(pwdContrasena.getText());
        } else {
            pwdContrasena.setText(txtContrasenaVisible.getText());
        }
        pwdContrasena.setVisible(!contrasenaVisible);
        pwdContrasena.setManaged(!contrasenaVisible);
        txtContrasenaVisible.setVisible(contrasenaVisible);
        txtContrasenaVisible.setManaged(contrasenaVisible);
    }

    @FXML
    public void onToggleConfirmarContrasena(ActionEvent event) {
        confirmarVisible = !confirmarVisible;
        if (confirmarVisible) {
            txtConfirmarContrasenaVisible.setText(pwdConfirmarContrasena.getText());
        } else {
            pwdConfirmarContrasena.setText(txtConfirmarContrasenaVisible.getText());
        }
        pwdConfirmarContrasena.setVisible(!confirmarVisible);
        pwdConfirmarContrasena.setManaged(!confirmarVisible);
        txtConfirmarContrasenaVisible.setVisible(confirmarVisible);
        txtConfirmarContrasenaVisible.setManaged(confirmarVisible);
    }

    @FXML
    public void onVolverLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/mx/utng/view/fx_login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error al cargar la vista de inicio de sesión.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String msg) {
        Alert alert = new Alert(tipo, msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
