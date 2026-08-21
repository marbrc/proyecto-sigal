package mx.utng.controller;
 
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
 
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import mx.utng.dao.UsuarioDAO;
import mx.utng.util.EmailUtil;
 
public class RegistroController {
 
    // ================= PASO 1: FORMULARIO =================
    @FXML private VBox paneFormulario;
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
    @FXML private Button btnRegistrarse;
 
    // ================= PASO 2: VERIFICAR CORREO =================
    @FXML private VBox paneVerificacion;
    @FXML private Label lblCorreoEnviado;
    @FXML private TextField txtCodigoVerificacion;
    @FXML private Label lblTimerRegistro;
    @FXML private Hyperlink lnkReenviarCodigoRegistro;
 
    @FXML private Button btnRegresarBienvenida;
    private final UsuarioDAO dao = new UsuarioDAO();
    private boolean contrasenaVisible = false;
    private boolean confirmarVisible = false;
 
    // Datos ya validados del paso 1, esperando confirmación del correo
    private String nombrePendiente;
    private String apPaternoPendiente;
    private String apMaternoPendiente;
    private String usuarioPendiente;
    private String correoPendiente;
    private String contrasenaPendiente;
 
    private String codigoGenerado;
    private LocalDateTime codigoExpira;
 
    private Timeline timelineExpiracion;
    private Timeline timelineReenvio;
 
    private static final int MINUTOS_VIGENCIA = 10;
    private static final int SEGUNDOS_REENVIO = 60;
 
    // ==========================================================
    // PASO 1: VALIDAR FORMULARIO Y ENVIAR CÓDIGO
    // ==========================================================
 
    @FXML
    public void onRegistrarse(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String apPaterno = txtApellidoPaterno.getText().trim();
        String apMaterno = txtApellidoMaterno.getText().trim();
        String nombreUsuario = txtNombreUsuario.getText().trim();
        String correo = txtCorreo.getText().trim();
 
        String contrasena = contrasenaVisible ? txtContrasenaVisible.getText() : pwdContrasena.getText();
        String confirmar = confirmarVisible ? txtConfirmarContrasenaVisible.getText() : pwdConfirmarContrasena.getText();
 
        if (nombre.isEmpty() || apPaterno.isEmpty() || nombreUsuario.isEmpty()
                || correo.isEmpty() || contrasena.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Completa todos los campos obligatorios.");
            return;
        }
 
        if (!correo.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            mostrarAlerta(Alert.AlertType.WARNING, "Ingresa un correo electrónico válido.");
            return;
        }
 
        if (!contrasena.equals(confirmar)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Las contraseñas no coinciden.");
            return;
        }
 
        if (dao.existeNombreUsuario(nombreUsuario, -1)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Ese nombre de usuario ya está en uso.");
            return;
        }
 
        if (dao.existeCorreo(correo)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Ya existe una cuenta con ese correo.");
            return;
        }
 
        nombrePendiente = nombre;
        apPaternoPendiente = apPaterno;
        apMaternoPendiente = apMaterno;
        usuarioPendiente = nombreUsuario;
        correoPendiente = correo;
        contrasenaPendiente = contrasena;
 
        enviarCodigo(true);
    }
 
    private String generarCodigo() {
        int numero = new java.security.SecureRandom().nextInt(900000) + 100000;
        return String.valueOf(numero);
    }
 
    private void enviarCodigo(boolean esPrimerEnvio) {
 
        codigoGenerado = generarCodigo();
        codigoExpira = LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA);
 
        String textoOriginal = btnRegistrarse.getText();
 
        if (esPrimerEnvio) {
            btnRegistrarse.setDisable(true);
            btnRegistrarse.setText("Enviando...");
        } else {
            lnkReenviarCodigoRegistro.setDisable(true);
        }
 
        Task<Void> tareaEnvio = new Task<>() {
            @Override
            protected Void call() throws Exception {
                EmailUtil.enviarCodigoRegistro(correoPendiente, nombrePendiente, codigoGenerado);
                return null;
            }
        };
 
        tareaEnvio.setOnSucceeded(e -> Platform.runLater(() -> {
            if (esPrimerEnvio) {
                btnRegistrarse.setDisable(false);
                btnRegistrarse.setText(textoOriginal);
                mostrarPasoVerificacion();
            } else {
                iniciarTemporizadorExpiracion();
                iniciarBloqueoReenvio();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Enviamos un nuevo código a tu correo.");
            }
        }));
 
        tareaEnvio.setOnFailed(e -> Platform.runLater(() -> {
            if (esPrimerEnvio) {
                btnRegistrarse.setDisable(false);
                btnRegistrarse.setText(textoOriginal);
            } else {
                lnkReenviarCodigoRegistro.setDisable(false);
            }
            mostrarAlerta(Alert.AlertType.ERROR,
                    "No se pudo enviar el correo. Revisa tu conexión a internet o la configuración de correo del sistema (EmailUtil.java).");
            tareaEnvio.getException().printStackTrace();
        }));
 
        new Thread(tareaEnvio, "hilo-envio-codigo-registro").start();
    }
 
    // ==========================================================
    // PASO 2: VERIFICAR CÓDIGO Y CREAR LA CUENTA
    // ==========================================================
 
    private void mostrarPasoVerificacion() {
 
        lblCorreoEnviado.setText("Enviamos un código de 6 dígitos a " + ofuscarCorreo(correoPendiente));
        txtCodigoVerificacion.clear();
 
        paneFormulario.setVisible(false);
        paneFormulario.setManaged(false);
        paneVerificacion.setVisible(true);
        paneVerificacion.setManaged(true);
 
        iniciarTemporizadorExpiracion();
        iniciarBloqueoReenvio();
    }
 
    private void iniciarTemporizadorExpiracion() {
 
        if (timelineExpiracion != null) {
            timelineExpiracion.stop();
        }
 
        timelineExpiracion = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
 
            long segundosRestantes = ChronoUnit.SECONDS.between(LocalDateTime.now(), codigoExpira);
 
            if (segundosRestantes <= 0) {
                lblTimerRegistro.setText("El código venció, solicita uno nuevo");
                timelineExpiracion.stop();
                return;
            }
 
            long min = segundosRestantes / 60;
            long seg = segundosRestantes % 60;
            lblTimerRegistro.setText(String.format("El código vence en %02d:%02d", min, seg));
 
        }));
 
        timelineExpiracion.setCycleCount(Timeline.INDEFINITE);
        timelineExpiracion.play();
    }
 
    private void iniciarBloqueoReenvio() {
 
        if (timelineReenvio != null) {
            timelineReenvio.stop();
        }
 
        final int[] restante = { SEGUNDOS_REENVIO };
        lnkReenviarCodigoRegistro.setDisable(true);
        lnkReenviarCodigoRegistro.setText("Reenviar código (" + restante[0] + "s)");
 
        timelineReenvio = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
 
            restante[0]--;
 
            if (restante[0] <= 0) {
                lnkReenviarCodigoRegistro.setText("Reenviar código");
                lnkReenviarCodigoRegistro.setDisable(false);
                timelineReenvio.stop();
            } else {
                lnkReenviarCodigoRegistro.setText("Reenviar código (" + restante[0] + "s)");
            }
 
        }));
 
        timelineReenvio.setCycleCount(Timeline.INDEFINITE);
        timelineReenvio.play();
    }
 
    @FXML
    public void onReenviarCodigoRegistro(ActionEvent event) {
        if (correoPendiente == null) {
            return;
        }
        enviarCodigo(false);
    }
 
    @FXML
    public void onRegresarFormulario(ActionEvent event) {
        detenerTemporizadores();
        paneVerificacion.setVisible(false);
        paneVerificacion.setManaged(false);
        paneFormulario.setVisible(true);
        paneFormulario.setManaged(true);
    }
 
    @FXML
    public void onVerificarCodigoRegistro(ActionEvent event) {
 
        String ingresado = txtCodigoVerificacion.getText() == null ? "" : txtCodigoVerificacion.getText().trim();
 
        if (ingresado.isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Ingresa el código que enviamos a tu correo.");
            return;
        }
 
        if (codigoExpira != null && LocalDateTime.now().isAfter(codigoExpira)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Tu código venció, solicita uno nuevo con \"Reenviar código\".");
            return;
        }
 
        if (!ingresado.equals(codigoGenerado)) {
            mostrarAlerta(Alert.AlertType.WARNING, "El código ingresado no es válido.");
            return;
        }
 
        detenerTemporizadores();
 
        boolean ok = dao.registrar(nombrePendiente, apPaternoPendiente, apMaternoPendiente,
                usuarioPendiente, correoPendiente, contrasenaPendiente);
 
        if (ok) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Cuenta creada exitosamente. Ya puedes iniciar sesión.");
            onVolverLogin(event);
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo registrar. Intenta de nuevo.");
        }
    }
 
    // ==========================================================
    // MOSTRAR / OCULTAR CONTRASEÑAS
    // ==========================================================
 
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
 
    // ==========================================================
    // NAVEGACIÓN
    // ==========================================================
 
    @FXML
    public void onVolverLogin(ActionEvent event) {
        detenerTemporizadores();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/mx/utng/view/fx_login.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SIGAL - Iniciar sesion");
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error al cargar la vista de inicio de sesión.");
        }
    }
 
    @FXML
    public void onRegresarBienvenida(ActionEvent event) {
        detenerTemporizadores();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/mx/utng/view/fx_bienvenida.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SIGAL - Sistema Integral de Gestión y Asignación de Laboratorios");
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error al cargar la vista de bienvenida.");
        }
    }
 
    // ==========================================================
    // UTILIDADES
    // ==========================================================
 
    private void detenerTemporizadores() {
        if (timelineExpiracion != null) {
            timelineExpiracion.stop();
        }
        if (timelineReenvio != null) {
            timelineReenvio.stop();
        }
    }
 
    private String ofuscarCorreo(String correo) {
 
        if (correo == null || !correo.contains("@")) {
            return correo;
        }
 
        String[] partes = correo.split("@");
        String usuario = partes[0];
        String dominio = partes[1];
 
        if (usuario.length() <= 2) {
            return usuario.charAt(0) + "***@" + dominio;
        }
 
        return usuario.substring(0, 2) + "***@" + dominio;
    }
 
    private void mostrarAlerta(Alert.AlertType tipo, String msg) {
        Alert alert = new Alert(tipo, msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
 