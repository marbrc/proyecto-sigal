package mx.utng.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
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

public class ImportarHorarioController {

    @FXML private Button btnCerrar;
    @FXML private Button btnDescargarPlantilla;
    @FXML private Button btnSeleccionarArchivo;
    @FXML private Button btnCancelar;
    @FXML private Button btnValidarImportar;
    @FXML private StackPane dropArea;
    @FXML private DatePicker dpInicioCuatrimestre;
    @FXML private DatePicker dpFinCuatrimestre;
    @FXML private Label lblArchivoSeleccionado;
    @FXML private Label lblError;

    private static final String RUTA_PLANTILLA_RECURSO = "/plantillas/plantilla_importar_horario.xlsx";

    private final HorarioImportService importService = new HorarioImportService();

    private File archivoSeleccionado;
    private Stage stage;
    private int idUsuarioActual;
    private Runnable alTerminarConExito;

    public static void abrir(Window owner, int idUsuarioActual, Runnable alTerminarConExito) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ImportarHorarioController.class.getResource("/mx/utng/view/ImportarHorarioView.fxml"));
            Parent root = loader.load();

            ImportarHorarioController controller = loader.getController();

            Stage stage = new Stage();
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            controller.stage = stage;
            controller.idUsuarioActual = idUsuarioActual;
            controller.alTerminarConExito = alTerminarConExito;

            Scene scene = new Scene(root);
            scene.setFill(null);
            scene.getStylesheets().add(
                    ImportarHorarioController.class.getResource("/mx/utng/view/importar-horario.css").toExternalForm());

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

        LocalDate hoy = LocalDate.now();
        LocalDate lunesDeEstaSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1L);
        dpInicioCuatrimestre.setValue(lunesDeEstaSemana);
        dpFinCuatrimestre.setValue(lunesDeEstaSemana.plusWeeks(16).minusDays(2));
    }

    @FXML
    private void onDescargarPlantilla(ActionEvent event) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Guardar plantilla de horario");
            chooser.setInitialFileName("plantilla_importar_horario.xlsx");
            chooser.setInitialDirectory(carpetaInicialValida());
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));

            File destino = chooser.showSaveDialog(stage);
            if (destino == null) return;

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
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir la ventana para guardar el archivo: " + e.getMessage());
        }
    }

    @FXML
    private void onSeleccionarArchivo(ActionEvent event) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Seleccionar horario a importar");
            chooser.setInitialDirectory(carpetaInicialValida());
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"));

            File archivo = chooser.showOpenDialog(stage);
            if (archivo != null) establecerArchivoSeleccionado(archivo);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir la ventana para seleccionar el archivo: " + e.getMessage());
        }
    }

    /**
     * Carpeta con la que arranca el explorador de archivos del FileChooser.
     * Si no se le da una carpeta valida (que exista de verdad en el disco),
     * algunos sistemas Windows truenan con "Location is not set." al abrir
     * el dialogo. Aqui probamos la carpeta de Documentos y, si no existe,
     * caemos a la carpeta de usuario (que siempre existe).
     */
    private File carpetaInicialValida() {
        File documentos = new File(System.getProperty("user.home", "."), "Documents");
        if (documentos.isDirectory()) {
            return documentos;
        }
        File home = new File(System.getProperty("user.home", "."));
        return home.isDirectory() ? home : new File(".");
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
            File xlsx = archivos.stream().filter(f -> f.getName().toLowerCase().endsWith(".xlsx")).findFirst().orElse(null);
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

    @FXML
    private void onValidarEImportar(ActionEvent event) {
        if (archivoSeleccionado == null) {
            mostrarError("Selecciona un archivo antes de continuar.");
            return;
        }
        LocalDate inicio = dpInicioCuatrimestre.getValue();
        LocalDate fin = dpFinCuatrimestre.getValue();
        if (inicio == null || fin == null) {
            mostrarError("Indica el inicio y el fin del cuatrimestre.");
            return;
        }

        try {
            ResultadoValidacion resultado = importService.validar(archivoSeleccionado, inicio, fin);
            ResultadoImportacionController.abrir(stage, resultado, idUsuarioActual, () -> {
                if (alTerminarConExito != null) alTerminarConExito.run();
                stage.close();
            });
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
            mostrarError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo leer el archivo: " + e.getClass().getSimpleName()
                    + (e.getMessage() != null ? ": " + e.getMessage() : ""));
        }
    }

    @FXML
    private void onCancelar(ActionEvent event) { stage.close(); }

    @FXML
    private void onCerrar(ActionEvent event) { stage.close(); }

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