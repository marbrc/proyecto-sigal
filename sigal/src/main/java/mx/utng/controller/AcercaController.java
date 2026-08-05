package mx.utng.controller;

import javafx.fxml.FXML;
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

}
