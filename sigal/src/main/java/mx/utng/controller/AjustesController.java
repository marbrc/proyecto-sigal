package mx.utng.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mx.utng.dao.UsuarioDAO;
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
            boolean guardado = usuarioDAO.actualizarTema(
                    menuController.getIdUsuarioActual(),
                    ThemeManager.getTema().getValorBD());

            if (!guardado) {
                mostrarAviso("No se pudo guardar",
                        "Tus preferencias de notificaciones se aplicaron, pero no se pudo guardar el tema. Intenta de nuevo.");
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
