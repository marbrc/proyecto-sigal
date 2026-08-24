package mx.utng.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import mx.utng.util.ThemeManager;

public class AcercaController {

    @FXML private VBox rootAcerca;

    @FXML
    public void initialize() {
        // El tema se aplica hasta que la pantalla ya está dentro de la
        // ventana de SIGAL (antes de eso, getScene() todavía es null).
        rootAcerca.sceneProperty().addListener((obs, escenaVieja, escenaNueva) -> {
            if (escenaNueva != null) {
                ThemeManager.apply(escenaNueva);
            }
        });
    }

    // ==========================
    // ABRIR MANUAL DE USUARIO
    // ==========================
    @FXML
    private void onAbrirManualUsuario() {
        abrirPdf("/docs/Manual_de_Usuario_SIGAL.pdf", "manual_usuario");
    }

    // ==========================
    // ABRIR MANUAL DE INSTALACIÓN
    // ==========================
    @FXML
    private void onAbrirManualInstalacion() {
        abrirPdf("/docs/Manual_de_Instalacion_SIGAL.pdf", "manual_instalacion");
    }

    /**
     * Copia el PDF empaquetado dentro de resources a un archivo temporal
     * y lo abre con el lector de PDF predeterminado del sistema operativo.
     * Desktop.open() necesita un File real en disco, por eso no se puede
     * abrir directamente el recurso del classpath.
     */
    private void abrirPdf(String rutaRecurso, String nombreBase) {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                mostrarError("No se pudo abrir el manual",
                        "Este equipo no permite abrir archivos externos automáticamente.");
                return;
            }

            InputStream recurso = getClass().getResourceAsStream(rutaRecurso);
            if (recurso == null) {
                mostrarError("Manual no encontrado",
                        "No se encontró el archivo " + rutaRecurso + " dentro del proyecto.");
                return;
            }

            File temporal = File.createTempFile(nombreBase, ".pdf");
            temporal.deleteOnExit();

            try (InputStream in = recurso; OutputStream out = Files.newOutputStream(temporal.toPath())) {
                in.transferTo(out);
            }

            Desktop.getDesktop().open(temporal);

        } catch (IOException e) {
            mostrarError("No se pudo abrir el manual",
                    "Ocurrió un error al intentar abrir el archivo PDF.");
            e.printStackTrace();
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}