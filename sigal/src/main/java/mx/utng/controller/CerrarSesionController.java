package mx.utng.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;


public class CerrarSesionController {

    private Stage stage;

    private Runnable onConfirmar;

    private Runnable onCancelar;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOnConfirmar(Runnable accion) {
        this.onConfirmar = accion;
    }

    public void setOnCancelar(Runnable accion) {
        this.onCancelar = accion;
    }

    @FXML
    private void manejarCancelar() {
        if (onCancelar != null) {
            onCancelar.run();
        }
        cerrarVentana();
    }

    @FXML
    private void manejarConfirmar() {
        if (onConfirmar != null) {
            onConfirmar.run();
        }
        cerrarVentana();
    }

    private void cerrarVentana() {
        if (stage != null) {
            stage.close();
        }
    }
}
