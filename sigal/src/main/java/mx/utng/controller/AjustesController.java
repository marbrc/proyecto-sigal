package mx.utng.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mx.utng.dao.UsuarioDAO;
import mx.utng.model.Usuario;
import mx.utng.util.AvatarUtil;
import mx.utng.util.ThemeManager;

public class AjustesController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /** Referencia al menú, para leer los datos reales de la sesión (nombre, rol, correo, hora de acceso). */
    private MenuController menuController;

    // ---- Perfil de usuario ----
    @FXML private Label lblUsuario;
    @FXML private Label lblRol;
    @FXML private Label lblCorreo;
    @FXML private Label lblUltimoAcceso;
    @FXML private Label lblAvatarIcono;
    @FXML private ImageView imgAvatarAjustes;

    // ---- Notificaciones ----
    @FXML private ToggleButton toggleMantenimiento;
    @FXML private ToggleButton toggleRecordatorios;
    @FXML private ToggleButton toggleReportes;
    @FXML private Label lblEstadoMantenimiento;
    @FXML private Label lblEstadoRecordatorios;
    @FXML private Label lblEstadoReportes;

    // ---- Apariencia (tema) ----
    @FXML private VBox rootAjustes;
    @FXML private VBox cardTemaAzul;
    @FXML private VBox cardTemaOscuro;
    @FXML private VBox cardTemaClaro;

    // ---- Guardar ----
    @FXML private Button btnGuardarCambios;

    @FXML
    public void initialize() {
        // El tema se aplica hasta que la pantalla ya está dentro de la
        // ventana de SIGAL (antes de eso, getScene() todavía es null).
        rootAjustes.sceneProperty().addListener((obs, escenaVieja, escenaNueva) -> {
            if (escenaNueva != null) {
                ThemeManager.apply(escenaNueva);
                marcarTarjetaTemaSeleccionada();
            }
        });
    }

    /**
     * Recibe la referencia del menú (llamada desde MenuController.cargarModulo)
     * y con ella llena la tarjeta "Perfil de usuario" con los datos reales
     * de la sesión: usuario, rol, correo y hora en la que inició sesión.
     */
    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
        cargarPerfilUsuario();
    }

    private void cargarPerfilUsuario() {
        if (menuController == null) {
            return;
        }
        lblUsuario.setText(menuController.getUsuarioActual());
        lblRol.setText(menuController.getRolActual());
        lblCorreo.setText(menuController.getCorreoActual());
        lblUltimoAcceso.setText(menuController.getHoraAccesoTexto());

        // Precarga los switches de notificaciones con lo que ya está
        // guardado en tb_usuario, en vez de arrancar siempre "Activado".
        Usuario usuario = usuarioDAO.obtenerPorId(menuController.getIdUsuarioActual());
        if (usuario != null) {
            boolean activas = Boolean.parseBoolean(usuario.getNotificaciones());
            toggleMantenimiento.setSelected(activas);
            toggleRecordatorios.setSelected(activas);
            toggleReportes.setSelected(activas);
            actualizarEstadoToggle(toggleMantenimiento, lblEstadoMantenimiento);
            actualizarEstadoToggle(toggleRecordatorios, lblEstadoRecordatorios);
            actualizarEstadoToggle(toggleReportes, lblEstadoReportes);
        }

        AvatarUtil.aplicar(imgAvatarAjustes, lblAvatarIcono, usuario != null ? usuario.getFotoPerfil() : null);
    }

    // =========================================================
    // TEMA

    @FXML
    private void seleccionarTemaAzul(MouseEvent event) {
        ThemeManager.setTema(ThemeManager.Tema.AZUL_ORIGINAL);
        aplicarTemaYMarcar();
    }

    @FXML
    private void seleccionarTemaOscuro(MouseEvent event) {
        ThemeManager.setTema(ThemeManager.Tema.OSCURO);
        aplicarTemaYMarcar();
    }

    @FXML
    private void seleccionarTemaClaro(MouseEvent event) {
        ThemeManager.setTema(ThemeManager.Tema.CLARO);
        aplicarTemaYMarcar();
    }

    private void aplicarTemaYMarcar() {
        ThemeManager.apply(rootAjustes.getScene());
        marcarTarjetaTemaSeleccionada();
    }

    private void marcarTarjetaTemaSeleccionada() {
        cardTemaAzul.getStyleClass().setAll("theme-card");
        cardTemaOscuro.getStyleClass().setAll("theme-card");
        cardTemaClaro.getStyleClass().setAll("theme-card");

        switch (ThemeManager.getTema()) {
            case AZUL_ORIGINAL -> cardTemaAzul.getStyleClass().setAll("theme-card-selected");
            case OSCURO -> cardTemaOscuro.getStyleClass().setAll("theme-card-selected");
            case CLARO -> cardTemaClaro.getStyleClass().setAll("theme-card-selected");
        }
    }

    // =========================================================
    // NOTIFICACIONES

    @FXML
    private void onToggleMantenimiento(ActionEvent event) {
        actualizarEstadoToggle(toggleMantenimiento, lblEstadoMantenimiento);
    }

    @FXML
    private void onToggleRecordatorios(ActionEvent event) {
        actualizarEstadoToggle(toggleRecordatorios, lblEstadoRecordatorios);
    }

    @FXML
    private void onToggleReportes(ActionEvent event) {
        actualizarEstadoToggle(toggleReportes, lblEstadoReportes);
    }

    private void actualizarEstadoToggle(ToggleButton toggle, Label lblEstado) {
        if (toggle.isSelected()) {
            lblEstado.setText("Activado");
            lblEstado.getStyleClass().setAll("notif-status");
        } else {
            lblEstado.setText("Desactivado");
            lblEstado.getStyleClass().setAll("notif-status-off");
        }
    }

    // =========================================================
    // GUARDAR

    @FXML
    private void guardarCambios(ActionEvent event) {
        if (menuController != null) {
            int idUsuario = menuController.getIdUsuarioActual();

            boolean temaGuardado = usuarioDAO.actualizarTema(
                    idUsuario, ThemeManager.getTema().getValorBD());

            // tb_usuario.Notificaciones es un solo interruptor general (todavía
            // no hay una columna por cada tipo de aviso), así que se guarda
            // como "activas" si al menos uno de los tres switches está prendido.
            boolean notifActivas = toggleMantenimiento.isSelected()
                    || toggleRecordatorios.isSelected()
                    || toggleReportes.isSelected();
            boolean notifGuardadas = usuarioDAO.actualizarNotificaciones(idUsuario, notifActivas);

            if (!temaGuardado || !notifGuardadas) {
                mostrarAviso("No se pudo guardar todo",
                        "Algunas preferencias no se pudieron guardar en la base de datos. Intenta de nuevo.");
                return;
            }
        }

        mostrarAviso("Cambios guardados", "Tus preferencias se guardaron correctamente.");
    }

    // =========================================================
    // AUXILIAR

    private void mostrarAviso(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

}
