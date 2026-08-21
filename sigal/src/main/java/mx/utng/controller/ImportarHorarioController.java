package mx.utng.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import mx.utng.service.HorarioImportService;
import mx.utng.service.HorarioImportService.ResultadoValidacion;

/**
 * Controller de la ventana emergente "Importar horario desde Excel".
 * Se abre como Stage modal desde la pantalla de Horarios (botón "Importar horario").
 */
public class ImportarHorarioController {

    @FXML private Button btnCerrar;
    @FXML private Button btnDescargarPlantilla;
    @FXML private Button btnSeleccionarArchivo;
    @FXML private Button btnCancelar;
    @FXML private Button btnValidarImportar;
    @FXML private StackPane dropArea;
    @FXML private Label lblArchivoSeleccionado;
    @FXML private Label lblError;

    /** Ruta de la plantilla vacía empaquetada con la app (dentro de resources). */
    private static final String RUTA_PLANTILLA_RECURSO = "/plantillas/plantilla_importar_horario.xlsx";

    private final HorarioImportService importService = new HorarioImportService();

    private File archivoSeleccionado;
    private Stage stage;

    // ------------------------------------------------------------------
    // Apertura de la ventana modal
    // ------------------------------------------------------------------

    /** Abre esta pantalla como modal sobre la ventana dueña indicada. */
    public static void abrir(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ImportarHorarioController.class.getResource("/fxml/ImportarHorarioView.fxml"));
            Parent root = loader.load();

            ImportarHorarioController controller = loader.getController();

            Stage stage = new Stage();
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            controller.stage = stage;

            Scene scene = new Scene(root);
            scene.setFill(null);
            scene.getStylesheets().add(
                    ImportarHorarioController.class.getResource("/css/importar-horario.css").toExternalForm());

            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo abrir la ventana de importación de horario", e);
        }
    }

    @FXML
    private void initialize() {
        dropArea.setOnDragOver(this::onDragOver);
        dropArea.setOnDragDropped(this::onDragDropped);
    }

    // ------------------------------------------------------------------
    // Paso 1: descargar plantilla
    // ------------------------------------------------------------------

    @FXML
    private void onDescargarPlantilla(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar plantilla de horario");
        chooser.setInitialFileName("plantilla_importar_horario.xlsx");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));

        File destino = chooser.showSaveDialog(stage);
        if (destino == null) {
            return; // el usuario canceló el diálogo
        }

        try (var in = getClass().getResourceAsStream(RUTA_PLANTILLA_RECURSO)) {
            if (in == null) {
                mostrarError("No se encontró la plantilla dentro de la aplicación.");
                return;
            }
            Files.copy(in, destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            ocultarError();
        } catch (IOException e) {
            mostrarError("No se pudo guardar la plantilla: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Paso 2: seleccionar / arrastrar archivo
    // ------------------------------------------------------------------

    @FXML
    private void onSeleccionarArchivo(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar horario a importar");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));

        File archivo = chooser.showOpenDialog(stage);
        if (archivo != null) {
            establecerArchivoSeleccionado(archivo);
        }
    }

    private void onDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles() && db.getFiles().stream().anyMatch(f -> f.getName().toLowerCase().endsWith(".xlsx"))) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void onDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean exito = false;
        if (db.hasFiles()) {
            List<File> archivos = db.getFiles();
            File xlsx = archivos.stream()
                    .filter(f -> f.getName().toLowerCase().endsWith(".xlsx"))
                    .findFirst()
                    .orElse(null);
            if (xlsx != null) {
                establecerArchivoSeleccionado(xlsx);
                exito = true;
            } else {
                mostrarError("Solo se aceptan archivos .xlsx");
            }
        }
        event.setDropCompleted(exito);
        event.consume();
    }

    private void establecerArchivoSeleccionado(File archivo) {
        this.archivoSeleccionado = archivo;
        lblArchivoSeleccionado.setText(archivo.getName());
        lblArchivoSeleccionado.setVisible(true);
        lblArchivoSeleccionado.setManaged(true);
        btnValidarImportar.setDisable(false);
        ocultarError();
    }

    // ------------------------------------------------------------------
    // Validar e importar
    // ------------------------------------------------------------------

    @FXML
    private void onValidarEImportar(ActionEvent event) {
        if (archivoSeleccionado == null) {
            mostrarError("Selecciona un archivo antes de continuar.");
            return;
        }

        try {
            ResultadoValidacion resultado = importService.validar(archivoSeleccionado);
            // Abre la pantalla de resultado (filas válidas / con conflicto).
            // Al estar en el mismo paquete (mx.utng.controller) no requiere import aparte.
            ResultadoImportacionController.abrir(stage, resultado);
        } catch (Exception e) {
            mostrarError("No se pudo leer el archivo: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelar(ActionEvent event) {
        stage.close();
    }

    @FXML
    private void onCerrar(ActionEvent event) {
        stage.close();
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}
