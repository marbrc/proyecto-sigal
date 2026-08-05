package mx.utng.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

// Controller de fx_admin.fxml (la mini pantalla emergente "Acceso de Administrador").
public class AdminController {

    private boolean autorizado = false;

    @FXML
    private TextField txtUsuario;

    // Contraseña (campo oculto + campo visible que se van intercambiando)
    @FXML
    private PasswordField pwdContrasena;
    @FXML
    private TextField txtContrasenaVisible;
    @FXML
    private Button btnToggleContrasena;
    

    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnIngresar;

    private boolean contrasenaVisible = false;

    // =========================================================
    // MOSTRAR / OCULTAR CONTRASEÑA (mismo patrón que en el registro)
    // =========================================================
    @FXML
    private void onToggleContrasena() {

        contrasenaVisible = !contrasenaVisible;

        if (contrasenaVisible) {
            txtContrasenaVisible.setText(pwdContrasena.getText());
            txtContrasenaVisible.setVisible(true);
            txtContrasenaVisible.setManaged(true);
            pwdContrasena.setVisible(false);
            pwdContrasena.setManaged(false);
        } else {
            pwdContrasena.setText(txtContrasenaVisible.getText());
            pwdContrasena.setVisible(true);
            pwdContrasena.setManaged(true);
            txtContrasenaVisible.setVisible(false);
            txtContrasenaVisible.setManaged(false);
        }

    }

    private String obtenerContrasena() {
        return contrasenaVisible ? txtContrasenaVisible.getText() : pwdContrasena.getText();
    }

    // =========================================================
    // CANCELAR: cierra la ventana sin hacer nada más
    // =========================================================
    @FXML
    private void onCancelar() {
        cerrarVentana();
    }

    // =========================================================
    // INGRESAR
    // =========================================================
    // IMPORTANTE: todavía no está conectado a la base de datos.
    // Aquí solo se valida que los campos no estén vacíos.
    // Cuando me pases el UsuarioDAO de SIGAL (o me digas cómo
    // identificar el rol Administrador en tb_usuario), completamos
    // esta parte para validar de verdad usuario/contraseña.
    @FXML
    private void onIngresar() {

        String usuario = txtUsuario.getText();
        String contrasena = obtenerContrasena();

        if (usuario.isBlank() || contrasena.isBlank()) {
            mostrarMensaje(Alert.AlertType.ERROR, "Complete usuario y contraseña.");
            return;
        }

        autorizado = true;
        cerrarVentana();

        // TODO: aquí falta llamar al DAO real, algo como:
        // if (usuarioDAO.validarAdministrador(usuario, contrasena)) {
        //     cerrarVentana();
        //     ... abrir el panel de administrador ...
        // } else {
        //     mostrarMensaje(Alert.AlertType.ERROR, "Usuario o contraseña incorrectos");
        // }

        mostrarMensaje(Alert.AlertType.INFORMATION,
                "Los datos son válidos. Falta conectar esto con la base de datos de SIGAL.");
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarMensaje(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.show();
    }

    // =========================================================
    // Método de ayuda para abrir esta mini pantalla como ventana
    // emergente desde cualquier otro controller. Ejemplo de uso,
    // desde otro botón: AdminController.abrirVentana(miBoton.getScene().getWindow());
    // =========================================================
    public static void abrirVentana(Window ventanaPropietaria) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    AdminController.class.getResource("/mx/utng/view/fx_admin.fxml"));

            Parent root = loader.load();

            Stage ventana = new Stage();
            ventana.initOwner(ventanaPropietaria);
            ventana.initModality(Modality.WINDOW_MODAL);
            ventana.initStyle(StageStyle.TRANSPARENT);

            Scene escena = new Scene(root);
            escena.setFill(null);

            ventana.setScene(escena);
            ventana.centerOnScreen();
            ventana.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAutorizado() {
    return autorizado;
}

}

