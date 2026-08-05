package mx.utng;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        Parent root = loadFXML("fx_bienvenida");

        scene = new Scene(root);

        // DEBUG TEMPORAL: para ver qué resolución está detectando Java
        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        System.out.println("[SIGAL] Pantalla detectada: " + bounds.getWidth() + " x " + bounds.getHeight());

        stage.setTitle("SIGAL - Sistema Integral de Gestión y Asignación de Laboratorios");

        // Pantalla completa
        stage.setMaximized(true);

        // Si queremos cambiar el tamaño fijo, comentamos la línea anterior
        // y usamos estas:
        // stage.setWidth(1536);
        // stage.setHeight(1024);

        stage.setScene(scene);

        stage.show();
    }

    /**
     * Cambia de pantalla.
     */
    public static void setRoot(String fxml) throws IOException {

        scene.setRoot(loadFXML(fxml));

    }

    /**
     * Carga un archivo FXML.
     */
    private static Parent loadFXML(String fxml) throws IOException {

        FXMLLoader loader = new FXMLLoader(

                App.class.getResource(
                        "/mx/utng/view/" + fxml + ".fxml"
                )

        );

        return loader.load();

    }

    public static void main(String[] args) {

        launch();

    }

}