package mx.utng.controller;

import java.io.IOException;
import java.time.LocalDateTime;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import mx.utng.dao.UsuarioDAO;
import mx.utng.model.Usuario;
import mx.utng.util.ThemeManager;

public class LoginController {

    //==========================
    // COMPONENTES DEL FXML
    //==========================

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField pwdContrasena;

    @FXML
    private TextField txtContrasenaVisible;

    @FXML
    private CheckBox chkRecordarme;

    @FXML
    private Button btnTogglePassword;

    @FXML
    private Button btnIniciarSesion;

    @FXML
    private StackPane rootLogin;

  

    //==========================
    // INICIALIZACIÓN
    //==========================

    @FXML
    public void initialize() {

        txtUsuario.requestFocus();

        txtContrasenaVisible.setManaged(false);
        txtContrasenaVisible.setVisible(false);

        pwdContrasena.setManaged(true);
        pwdContrasena.setVisible(true);

        txtContrasenaVisible.textProperty().bindBidirectional(pwdContrasena.textProperty());

        // El login ahora también respeta el último tema elegido (Azul/Oscuro/Claro),
        // igual que el resto del sistema. Antes solo se aplicaba después de
        // iniciar sesión, así que la pantalla de login siempre se veía azul.
        if (rootLogin != null) {
            rootLogin.sceneProperty().addListener((obs, escenaVieja, escenaNueva) -> {
                if (escenaNueva != null) {
                    ThemeManager.apply(escenaNueva);
                }
            });
        }

    }

    //==========================
    // MOSTRAR / OCULTAR CONTRASEÑA
    //==========================

    @FXML
    private void onTogglePassword() {

        if (pwdContrasena.isVisible()) {

            txtContrasenaVisible.setText(pwdContrasena.getText());

            pwdContrasena.setVisible(false);
            pwdContrasena.setManaged(false);

            txtContrasenaVisible.setVisible(true);
            txtContrasenaVisible.setManaged(true);


        } else {

            pwdContrasena.setText(txtContrasenaVisible.getText());

            txtContrasenaVisible.setVisible(false);
            txtContrasenaVisible.setManaged(false);

            pwdContrasena.setVisible(true);
            pwdContrasena.setManaged(true);


        }

    }

    //==========================
    // BOTÓN INICIAR SESIÓN
    //==========================

    @FXML
    private void onIniciarSesion() {

        String usuario = txtUsuario.getText().trim();

        String password;

        if (pwdContrasena.isVisible()) {
            password = pwdContrasena.getText();
        } else {
            password = txtContrasenaVisible.getText();
        }

        if (!validarCampos(usuario, password)) {
            return;
        }
        
        UsuarioDAO dao = new UsuarioDAO();
        Usuario usuarioLogueado = dao.validar(usuario, password);
        if (usuarioLogueado != null) {
            abrirMenu(usuarioLogueado);
        } else {
            mostrarError("Acceso denegado", "Usuario o contraseña incorrectos.");

}

    }

    //==========================
    // OLVIDÉ MI CONTRASEÑA
    //==========================

    @FXML
    private void onOlvidasteContrasena() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/mx/utng/view/fx_recuperar_contrasena.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) btnIniciarSesion.getScene().getWindow();
            Scene escena = new Scene(root);
            stage.setScene(escena);
            stage.setTitle("SIGAL - Recuperar contraseña");
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.show();

        } catch (IOException e) {
            mostrarError("Error del sistema", "No fue posible abrir la recuperación de contraseña.");
            e.printStackTrace();
        }
    }

    //==========================
    // REGRESAR A BIENVENIDA
    //==========================

    @FXML
    private void onRegresarBienvenida() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/mx/utng/view/fx_bienvenida.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) btnIniciarSesion.getScene().getWindow();
            Scene escena = new Scene(root);
            stage.setScene(escena);
            stage.setTitle("SIGAL - Sistema Integral de Gestión y Asignación de Laboratorios");
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.show();

        } catch (IOException e) {
            mostrarError("Error del sistema", "No fue posible regresar a la pantalla de bienvenida.");
            e.printStackTrace();
        }
    }

    //==========================
    // VALIDAR CAMPOS
    //==========================

    private boolean validarCampos(String usuario, String password) {

        if (usuario.isBlank()) {

            mostrarAdvertencia(
                    "Campo obligatorio",
                    "Ingrese su usuario institucional."
            );

            txtUsuario.requestFocus();
            return false;

        }

        if (password.isBlank()) {

            mostrarAdvertencia(
                    "Campo obligatorio",
                    "Ingrese su contraseña."
            );

            if (pwdContrasena.isVisible()) {
                pwdContrasena.requestFocus();
            } else {
                txtContrasenaVisible.requestFocus();
            }

            return false;

        }

        return true;

    }

        //==========================
    // MENSAJES
    //==========================

    private void mostrarAdvertencia(String titulo, String mensaje) {

        Alert alert = new Alert(Alert.AlertType.WARNING);

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        alert.showAndWait();

    }

    private void mostrarInformacion(String titulo, String mensaje) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        alert.showAndWait();

    }

    private void mostrarError(String titulo, String mensaje) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        alert.showAndWait();

    }

    //==========================
    // ABRIR MENÚ
    //==========================

    private void abrirMenu(Usuario usuario) {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/mx/utng/view/fx_menu.fxml"));

            Parent root = loader.load();
            MenuController controller = loader.getController();
            controller.setUsuarioActual(usuario.getIdUsuario(), usuario.getNombre(), usuario.getRol());
            controller.setSesionExtra(usuario.getCorreoElectronico(), LocalDateTime.now());
            controller.setFotoPerfilSesion(usuario.getFotoPerfil());

            Stage stage = (Stage) btnIniciarSesion.getScene().getWindow();

            Scene escena = new Scene(root);
            stage.setScene(escena);
            stage.setTitle("SIGAL - Sistema Integral de Gestión y Asignación de Laboratorios");
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.show();

            // El tema (Azul/Oscuro/Claro) que el usuario guardó la última vez
            // en Ajustes se aplica hasta aquí, ya que la Scene recién se creó.
            ThemeManager.setTema(ThemeManager.Tema.desdeValorBD(usuario.getTema()));
            ThemeManager.apply(escena);

        } catch (IOException e) {

            mostrarError(
                    "Error del sistema",
                    "No fue posible abrir el menú principal."
            );

            e.printStackTrace();

        }

    }

}