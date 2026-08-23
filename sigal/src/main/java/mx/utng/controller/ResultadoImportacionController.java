package mx.utng.controller;
 
import java.io.IOException;

import javafx.application.Platform;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import mx.utng.service.HorarioImportService;
import mx.utng.service.HorarioImportService.FilaConError;
import mx.utng.service.HorarioImportService.FilaHorario;
import mx.utng.service.HorarioImportService.ResultadoImportacion;
import mx.utng.service.HorarioImportService.ResultadoValidacion;
 
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
    @FXML private Label lblMotivoSeleccionado;
 
    private Stage stage;
    private ResultadoValidacion resultado;
    private int idUsuarioActual;
    private Runnable onExito;
    private final HorarioImportService importService = new HorarioImportService();
 
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

        colErrMotivo.setCellFactory(col -> new TableCell<FilaConError, String>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(String motivo, boolean vacio) {
                super.updateItem(motivo, vacio);
                if (vacio || motivo == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(motivo);
                    tooltip.setText(motivo);
                    tooltip.setWrapText(true);
                    tooltip.setMaxWidth(360);
                    setTooltip(tooltip);
                }
            }
        });

        tablaErrores.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> {
            if (seleccionada != null) {
                lblMotivoSeleccionado.setText(seleccionada.motivo());
                // La ventana no es redimensionable y su alto se calculo al
                // abrirse (con el texto corto de "Selecciona una fila..."),
                // asi que si el motivo ocupa mas lineas se ve cortado. Con
                // sizeToScene() se ajusta la ventana al nuevo contenido.
                Platform.runLater(() -> {
                    if (stage != null) {
                        stage.sizeToScene();
                    }
                });
            }
        });
    }
 
    private void cargarDatos() {
        tablaValidas.getItems().setAll(resultado.validas());
        tablaErrores.getItems().setAll(resultado.conError());
 
        lblResumen.setText(String.format(
                "%d filas leídas · %d se importarán · %d con error o conflicto",
                resultado.totalFilas(), resultado.validas().size(), resultado.conError().size()));
 
        btnConfirmarImportacion.setDisable(resultado.validas().isEmpty());
    }
 
    @FXML
    private void onConfirmarImportacion() {
        btnConfirmarImportacion.setDisable(true);

        ResultadoImportacion resultadoImportacion = importService.importar(
                resultado.validas(), resultado.inicioCuatrimestre(), resultado.finCuatrimestre(), idUsuarioActual);

        AlertType tipo = resultadoImportacion.fallidas() == 0 ? AlertType.INFORMATION : AlertType.WARNING;
        Alert alert = new Alert(tipo);
        alert.initOwner(stage);
        alert.setTitle("Importación terminada");
        alert.setHeaderText(null);
        alert.setContentText(
                "Se crearon " + resultadoImportacion.insertadas() + " asignación(es) en el horario."
                        + (resultadoImportacion.fallidas() > 0
                                ? "\n" + resultadoImportacion.fallidas() + " no se pudieron guardar (revisa la consola)."
                                : ""));
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