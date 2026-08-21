package mx.utng.controller;
 
import java.io.IOException;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import mx.utng.service.HorarioImportService.FilaConError;
import mx.utng.service.HorarioImportService.FilaHorario;
import mx.utng.service.HorarioImportService.ResultadoValidacion;
 
/**
 * Controller de la ventana emergente "Resultado de la importación".
 * Se abre desde ImportarHorarioController una vez que el Excel fue validado:
 * muestra qué filas se importarán y cuáles quedaron fuera por error o conflicto.
 *
 * NOTA: "Confirmar importación" todavía NO inserta en la base de datos real
 * (AsignacionDAO / EspacioDAO). Se deja marcado con TODO; el resto de la
 * pantalla (tablas, resumen, callback de éxito hacia HorarioController) ya
 * está completo y navegable.
 */
public class ResultadoImportacionController {
 
    @FXML private Button btnCerrar;
    @FXML private Button btnCancelar;
    @FXML private Button btnConfirmarImportacion;
    @FXML private Label lblResumen;
 
    @FXML private TableView<FilaHorario> tablaValidas;
    @FXML private TableColumn<FilaHorario, String> colValTipo;
    @FXML private TableColumn<FilaHorario, String> colValEspacio;
    @FXML private TableColumn<FilaHorario, String> colValDia;
    @FXML private TableColumn<FilaHorario, String> colValInicio;
    @FXML private TableColumn<FilaHorario, String> colValFin;
    @FXML private TableColumn<FilaHorario, String> colValGrupo;
 
    @FXML private TableView<FilaConError> tablaErrores;
    @FXML private TableColumn<FilaConError, Number> colErrFila;
    @FXML private TableColumn<FilaConError, String> colErrEspacio;
    @FXML private TableColumn<FilaConError, String> colErrDia;
    @FXML private TableColumn<FilaConError, String> colErrHorario;
    @FXML private TableColumn<FilaConError, String> colErrMotivo;
 
    private Stage stage;
    private ResultadoValidacion resultado;
    private int idUsuarioActual;
    private Runnable onExito;
 
    // ------------------------------------------------------------------
    // Apertura de la ventana modal
    // ------------------------------------------------------------------
 
    /**
     * Abre esta pantalla como modal sobre la ventana dueña indicada.
     * onExito se dispara cuando el usuario confirma la importación (para que
     * ImportarHorarioController pueda cerrar su propia ventana y avisar a
     * HorarioController que debe refrescar la cuadrícula).
     */
    public static void abrir(Window owner, ResultadoValidacion resultado, int idUsuarioActual, Runnable onExito) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ResultadoImportacionController.class.getResource("/mx/utng/view/ResultadoImportacionView.fxml"));
            Parent root = loader.load();
 
            ResultadoImportacionController controller = loader.getController();
            controller.resultado = resultado;
            controller.idUsuarioActual = idUsuarioActual;
            controller.onExito = onExito;
 
            Stage stage = new Stage();
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            controller.stage = stage;
 
            Scene scene = new Scene(root);
            scene.setFill(null);
            scene.getStylesheets().add(
                    ResultadoImportacionController.class.getResource("/mx/utng/view/importar-horario.css").toExternalForm());
 
            stage.setScene(scene);
            stage.setResizable(false);
 
            controller.cargarDatos();
 
            stage.showAndWait();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo abrir la ventana de resultado de importación", e);
        }
    }
 
    @FXML
    private void initialize() {
        colValTipo.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().tipoEspacio()));
        colValEspacio.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().nombreEspacio()));
        colValDia.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().dia()));
        colValInicio.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().horaInicio()));
        colValFin.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().horaFin()));
        colValGrupo.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().grupo()));
 
        colErrFila.setCellValueFactory(d -> new ReadOnlyIntegerWrapper(d.getValue().numeroFila()));
        colErrEspacio.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().fila().nombreEspacio()));
        colErrDia.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().fila().dia()));
        colErrHorario.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                d.getValue().fila().horaInicio() + " - " + d.getValue().fila().horaFin()));
        colErrMotivo.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().motivo()));
    }
 
    private void cargarDatos() {
        tablaValidas.getItems().setAll(resultado.validas());
        tablaErrores.getItems().setAll(resultado.conError());
 
        lblResumen.setText(String.format(
                "%d filas leídas · %d se importarán · %d con error o conflicto",
                resultado.totalFilas(), resultado.validas().size(), resultado.conError().size()));
 
        btnConfirmarImportacion.setDisable(resultado.validas().isEmpty());
    }
 
    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------
 
    @FXML
    private void onConfirmarImportacion() {
        // TODO (siguiente paso): reemplazar este aviso por la inserción real de
        // resultado.validas() usando AsignacionDAO / EspacioDAO, asociando
        // idUsuarioActual como quien hizo la importación.
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle("Importación lista");
        alert.setHeaderText(null);
        alert.setContentText(
                resultado.validas().size()
                        + " fila(s) están validadas y listas para insertarse en el horario. "
                        + "La escritura en la base de datos se conecta en el siguiente paso.");
        alert.showAndWait();
 
        if (onExito != null) {
            onExito.run();
        }
        stage.close();
    }
 
    @FXML
    private void onCancelar() {
        stage.close();
    }
 
    @FXML
    private void onCerrar() {
        stage.close();
    }
}