package mx.utng.util;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import mx.utng.controller.CerrarSesionController;


public final class CerrarSesionDialog {

    private CerrarSesionDialog() {
    }

    /**
     * Abre el modal de "Cerrar sesión" centrado sobre la ventana de origen.
     *
     * @param ventanaOrigen      Stage desde el que se invoca (puede ser null;
     *                           en ese caso el modal se centra en la pantalla).
     * @param accionCerrarSesion Código que se ejecuta SOLO si el usuario
     *                           confirma. Puede ser null si sólo se quiere
     *                           mostrar el aviso sin ejecutar nada extra.
     */
    public static void mostrar(Stage ventanaOrigen, Runnable accionCerrarSesion) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CerrarSesionDialog.class.getResource("/mx/utng/view/fx_cerrar_sesion.fxml"));
            Parent raiz = loader.load();

            CerrarSesionController controlador = loader.getController();

            Stage ventanaDialogo = new Stage();
            ventanaDialogo.initStyle(StageStyle.TRANSPARENT);
            ventanaDialogo.initModality(Modality.APPLICATION_MODAL);

            if (ventanaOrigen != null) {
                ventanaDialogo.initOwner(ventanaOrigen);
                // Cubre toda la ventana de origen para lograr el efecto de
                // fondo oscurecido detrás de la tarjeta (ver .logout-overlay).
                ventanaDialogo.setX(ventanaOrigen.getX());
                ventanaDialogo.setY(ventanaOrigen.getY());
                ventanaDialogo.setWidth(ventanaOrigen.getWidth());
                ventanaDialogo.setHeight(ventanaOrigen.getHeight());
            } else {
                ventanaDialogo.setWidth(900);
                ventanaDialogo.setHeight(620);
                ventanaDialogo.centerOnScreen();
            }

            Scene escena = new Scene(raiz);
            escena.setFill(Color.TRANSPARENT);
            ventanaDialogo.setScene(escena);

            controlador.setStage(ventanaDialogo);
            controlador.setOnConfirmar(accionCerrarSesion);

            ventanaDialogo.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo abrir la pantalla de cerrar sesión.", e);
        }
    }
}
