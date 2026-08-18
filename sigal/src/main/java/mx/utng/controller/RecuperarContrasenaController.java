package mx.utng.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import mx.utng.model.Usuario;
import mx.utng.util.EmailUtil;

public class RecuperarContrasenaController {

    // ================= ENCABEZADO =================
    @FXML private Label lblTituloPaso;
    @FXML private Label lblSubtituloPaso;

    // ================= PASO 1 =================
    @FXML private VBox paneSolicitar;
    @FXML private TextField txtUsuarioRec;
    @FXML private TextField txtCorreoRec;
    @FXML private Button btnEnviarCodigo;

    // ================= PASO 2 =================
    @FXML private VBox paneCodigo;
    @FXML private Label lblCorreoEnviado;
    @FXML private TextField txtCodigo;
    @FXML private Label lblTimer;
    @FXML private Hyperlink lnkReenviar;

    // ================= PASO 3 =================
    @FXML private VBox paneNueva;
    @FXML private PasswordField pwdNueva;
    @FXML private TextField txtNuevaVisible;
    @FXML private PasswordField pwdConfirmar;
    @FXML private TextField txtConfirmarVisible;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private Usuario usuarioEncontrado;
    private String codigoGenerado;
    private LocalDateTime codigoExpira;

    private Timeline timelineExpiracion;
    private Timeline timelineReenvio;

    private static final int MINUTOS_VIGENCIA = 10;
    private static final int SEGUNDOS_REENVIO = 60;

    // ==========================================================
    // PASO 1: SOLICITAR CÓDIGO
    // ==========================================================

    @FXML
    private void onEnviarCodigo() {

        String usuario = txtUsuarioRec.getText() == null ? "" : txtUsuarioRec.getText().trim();
        String correo = txtCorreoRec.getText() == null ? "" : txtCorreoRec.getText().trim();

        if (usuario.isBlank() || correo.isBlank()) {
            mostrarAdvertencia("Campos obligatorios", "Ingresa tu usuario y tu correo electrónico.");
            return;
        }

        Usuario encontrado = usuarioDAO.buscarPorUsuarioYCorreo(usuario, correo);

        if (encontrado == null) {
            mostrarAdvertencia("Datos no encontrados",
                    "No encontramos una cuenta activa con ese usuario y ese correo.");
            return;
        }

        this.usuarioEncontrado = encontrado;

        enviarCodigo(true);
    }

    private String generarCodigo() {
        int numero = new java.security.SecureRandom().nextInt(900000) + 100000;
        return String.valueOf(numero);
    }

    /** Genera y envía el código. Si esPrimerEnvio es true, cambia a la pantalla del paso 2 al terminar. */
    private void enviarCodigo(boolean esPrimerEnvio) {

        codigoGenerado = generarCodigo();
        codigoExpira = LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA);

        String textoOriginal = btnEnviarCodigo.getText();

        if (esPrimerEnvio) {
            btnEnviarCodigo.setDisable(true);
            btnEnviarCodigo.setText("Enviando...");
        } else {
            lnkReenviar.setDisable(true);
        }

        Task<Void> tareaEnvio = new Task<>() {
            @Override
            protected Void call() throws Exception {
                EmailUtil.enviarCodigoRecuperacion(
                        usuarioEncontrado.getCorreoElectronico(),
                        usuarioEncontrado.getNombre(),
                        codigoGenerado
                );
                return null;
            }
        };

        tareaEnvio.setOnSucceeded(e -> Platform.runLater(() -> {
            if (esPrimerEnvio) {
                btnEnviarCodigo.setDisable(false);
                btnEnviarCodigo.setText(textoOriginal);
                mostrarPaso2();
            } else {
                iniciarTemporizadorExpiracion();
                iniciarBloqueoReenvio();
                mostrarInformacion("Código reenviado", "Enviamos un nuevo código a tu correo.");
            }
        }));

        tareaEnvio.setOnFailed(e -> Platform.runLater(() -> {
            if (esPrimerEnvio) {
                btnEnviarCodigo.setDisable(false);
                btnEnviarCodigo.setText(textoOriginal);
            } else {
                lnkReenviar.setDisable(false);
            }
            mostrarError("No se pudo enviar el correo",
                    "Revisa tu conexión a internet o la configuración de correo del sistema (EmailUtil.java).");
            tareaEnvio.getException().printStackTrace();
        }));

        new Thread(tareaEnvio, "hilo-envio-codigo").start();
    }

    // ==========================================================
    // PASO 2: VERIFICAR CÓDIGO
    // ==========================================================

    private void mostrarPaso2() {

        lblCorreoEnviado.setText("Enviamos un código de 6 dígitos a " + ofuscarCorreo(usuarioEncontrado.getCorreoElectronico()));
        txtCodigo.clear();

        cambiarPaso(paneCodigo, "Verifica tu código", "Revisa tu bandeja de entrada (y spam)");

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
                lblTimer.setText("El código venció, solicita uno nuevo");
                timelineExpiracion.stop();
                return;
            }

            long min = segundosRestantes / 60;
            long seg = segundosRestantes % 60;
            lblTimer.setText(String.format("El código vence en %02d:%02d", min, seg));

        }));

        timelineExpiracion.setCycleCount(Timeline.INDEFINITE);
        timelineExpiracion.play();
    }

    private void iniciarBloqueoReenvio() {

        if (timelineReenvio != null) {
            timelineReenvio.stop();
        }

        final int[] restante = { SEGUNDOS_REENVIO };
        lnkReenviar.setDisable(true);
        lnkReenviar.setText("Reenviar código (" + restante[0] + "s)");

        timelineReenvio = new Timeline(new KeyFrame(Duration.seconds(1), e -> {

            restante[0]--;

            if (restante[0] <= 0) {
                lnkReenviar.setText("Reenviar código");
                lnkReenviar.setDisable(false);
                timelineReenvio.stop();
            } else {
                lnkReenviar.setText("Reenviar código (" + restante[0] + "s)");
            }

        }));

        timelineReenvio.setCycleCount(Timeline.INDEFINITE);
        timelineReenvio.play();
    }

    @FXML
    private void onReenviarCodigo() {
        if (usuarioEncontrado == null) {
            return;
        }
        enviarCodigo(false);
    }

    @FXML
    private void onVerificarCodigo() {

        String ingresado = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();

        if (ingresado.isBlank()) {
            mostrarAdvertencia("Campo obligatorio", "Ingresa el código que enviamos a tu correo.");
            return;
        }

        if (codigoExpira != null && LocalDateTime.now().isAfter(codigoExpira)) {
            mostrarAdvertencia("Código vencido", "Tu código venció, solicita uno nuevo con \"Reenviar código\".");
            return;
        }

        if (!ingresado.equals(codigoGenerado)) {
            mostrarAdvertencia("Código incorrecto", "El código ingresado no es válido.");
            return;
        }

        detenerTemporizadores();
        mostrarPaso3();
    }

    @FXML
    private void onRegresarPaso1() {
        detenerTemporizadores();
        cambiarPaso(paneSolicitar, "Recuperar contraseña", "Ingresa tus datos para continuar");
    }

    // ==========================================================
    // PASO 3: NUEVA CONTRASEÑA
    // ==========================================================

    private void mostrarPaso3() {
        pwdNueva.clear();
        txtNuevaVisible.clear();
        pwdConfirmar.clear();
        txtConfirmarVisible.clear();
        cambiarPaso(paneNueva, "Crea tu nueva contraseña", "Ya casi terminas");
    }

    @FXML
    private void onToggleNueva() {
        alternarVisibilidad(pwdNueva, txtNuevaVisible);
    }

    @FXML
    private void onToggleConfirmar() {
        alternarVisibilidad(pwdConfirmar, txtConfirmarVisible);
    }

    private void alternarVisibilidad(PasswordField campoOculto, TextField campoVisible) {

        if (campoOculto.isVisible()) {
            campoVisible.setText(campoOculto.getText());
            campoOculto.setVisible(false);
            campoOculto.setManaged(false);
            campoVisible.setVisible(true);
            campoVisible.setManaged(true);
        } else {
            campoOculto.setText(campoVisible.getText());
            campoVisible.setVisible(false);
            campoVisible.setManaged(false);
            campoOculto.setVisible(true);
            campoOculto.setManaged(true);
        }
    }

    @FXML
    private void onGuardarContrasena() {

        String nueva = pwdNueva.isVisible() ? pwdNueva.getText() : txtNuevaVisible.getText();
        String confirmar = pwdConfirmar.isVisible() ? pwdConfirmar.getText() : txtConfirmarVisible.getText();

        if (nueva == null || nueva.isBlank() || confirmar == null || confirmar.isBlank()) {
            mostrarAdvertencia("Campos obligatorios", "Ingresa y confirma tu nueva contraseña.");
            return;
        }

        if (nueva.length() < 6) {
            mostrarAdvertencia("Contraseña muy corta", "Tu nueva contraseña debe tener al menos 6 caracteres.");
            return;
        }

        if (!nueva.equals(confirmar)) {
            mostrarAdvertencia("Las contraseñas no coinciden", "Verifica que ambas contraseñas sean iguales.");
            return;
        }

        boolean actualizado = usuarioDAO.actualizarContrasena(usuarioEncontrado.getIdUsuario(), nueva);

        if (!actualizado) {
            mostrarError("Error del sistema", "No fue posible actualizar tu contraseña. Intenta de nuevo.");
            return;
        }

        mostrarInformacion("Contraseña actualizada",
                "Tu contraseña se actualizó correctamente. Ahora puedes iniciar sesión con tu nueva contraseña.");

        // Regresa a login. Si en vez de esto prefieres entrar directo a la cuenta,
        // aquí es donde lo cambiarías por la lógica de abrirMenu() de LoginController.
        onRegresarLogin();
    }

    // ==========================================================
    // NAVEGACIÓN / UTILIDADES
    // ==========================================================

    @FXML
    private void onRegresarLogin() {

        detenerTemporizadores();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/utng/view/fx_login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) paneSolicitar.getScene().getWindow();

            Scene escena = new Scene(root);
            stage.setScene(escena);
            stage.setTitle("SIGAL - Inicio de sesión");
            javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());    
            stage.show();

        } catch (IOException e) {
            mostrarError("Error del sistema", "No fue posible regresar a inicio de sesión.");
            e.printStackTrace();
        }
    }

    private void detenerTemporizadores() {
        if (timelineExpiracion != null) {
            timelineExpiracion.stop();
        }
        if (timelineReenvio != null) {
            timelineReenvio.stop();
        }
    }

    private void cambiarPaso(VBox paneDestino, String titulo, String subtitulo) {

        paneSolicitar.setVisible(paneDestino == paneSolicitar);
        paneSolicitar.setManaged(paneDestino == paneSolicitar);

        paneCodigo.setVisible(paneDestino == paneCodigo);
        paneCodigo.setManaged(paneDestino == paneCodigo);

        paneNueva.setVisible(paneDestino == paneNueva);
        paneNueva.setManaged(paneDestino == paneNueva);

        lblTituloPaso.setText(titulo);
        lblSubtituloPaso.setText(subtitulo);
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

    // ==========================================================
    // MENSAJES
    // ==========================================================

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
}
