package mx.utng.controller;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mx.utng.dao.AsignacionDAO;
import mx.utng.dao.ConsultaDAO;
import mx.utng.model.Consultas;
import mx.utng.model.Reporte;
import mx.utng.util.ReporteExportador;


/**
 * Controller de fx_consultas.fxml.
 *
 * Reutiliza los catálogos (Profesor / Carrera / Espacio) que ya
 * arma AsignacionDAO, y usa ConsultaDAO para la búsqueda filtrada
 * de asignaciones registradas.
 */
public class ConsultaController {

    // ---- Filtros ----
    @FXML private ComboBox<String> cmbProfesor;
    @FXML private ComboBox<String> cmbSolicitante;
    @FXML private ComboBox<String> cmbTipoEspacio;
    @FXML private ComboBox<String> cmbEspacio;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<String> cmbCarrera;
    @FXML private ComboBox<String> cmbMateria;
    @FXML private ComboBox<String> cmbGrupo;
    @FXML private DatePicker dtDesde;
    @FXML private DatePicker dtHasta;

    @FXML private Button btnLimpiar;
    @FXML private Button btnBuscar;
    @FXML private Button btnExportar;

    // ---- Resultados ----
    @FXML private Label lblCantidadResultados;
    @FXML private TableView<Consultas> tblResultados;
    @FXML private TableColumn<Consultas, String> colHorario;
    @FXML private TableColumn<Consultas, String> colEspacio;
    @FXML private TableColumn<Consultas, String> colSolicitante;
    @FXML private TableColumn<Consultas, String> colGrupo;
    @FXML private TableColumn<Consultas, String> colEstado;
    @FXML private TableColumn<Consultas, String> colMotivo;
    @FXML private TableColumn<Consultas, String> colCarrera;
    @FXML private TableColumn<Consultas, String> colMateria;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final ConsultaDAO consultaDAO = new ConsultaDAO();
        private final ObservableList<Consultas> listaResultados =
            FXCollections.observableArrayList();

    // Catálogos "nombre visible -> ID real en la BD"
     private Map<String, Integer> mapaEspacios;

    @FXML
    public void initialize() {

        cargarColumnas();
        cargarCombos();
        tblResultados.setItems(listaResultados);

        lblCantidadResultados.setText("Resultados: 0");

        // Al abrir la pantalla se muestran todas las asignaciones
        // (sin filtros), igual que en la referencia.
    }

    // =========================================================
    // COLUMNAS DE LA TABLA
    // =========================================================
    private void cargarColumnas() {
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horario"));
        colEspacio.setCellValueFactory(new PropertyValueFactory<>("espacio"));
        colSolicitante.setCellValueFactory(new PropertyValueFactory<>("solicitante"));
        colGrupo.setCellValueFactory(new PropertyValueFactory<>("grupo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colCarrera.setCellValueFactory(new PropertyValueFactory<>("carrera"));
        colMateria.setCellValueFactory(new PropertyValueFactory<>("materia"));
    }

    // =========================================================
    // CATÁLOGOS DE LOS COMBOS
    // =========================================================
      private void cargarCombos() {

    // Combo de solicitantes
   cmbSolicitante.setItems(FXCollections.observableArrayList("Profesor", "Administrativo", "Alumno", "Otro"));

    cmbSolicitante.setEditable(true);
    cmbSolicitante.getStyleClass().add("combo-box-editable");

    // Valores iniciales
    cmbSolicitante.setItems(
        FXCollections.observableArrayList(
            asignacionDAO.listarNombresSolicitantes()
        )
    );

    cmbSolicitante.valueProperty().addListener((obs, valorAnterior, tipo) -> {

        // Limpiar selección y texto escrito
        cmbSolicitante.getSelectionModel().clearSelection();
        cmbSolicitante.setValue(null);

        if (cmbSolicitante.isEditable()) {
            cmbSolicitante.getEditor().clear();
        }

        if ("Maestro".equals(tipo)) {
            cmbSolicitante.setItems(
                FXCollections.observableArrayList(
                    asignacionDAO.listarNombresProfesores()
                )
            );
        } else {
            cmbSolicitante.setItems(
                FXCollections.observableArrayList(
                    asignacionDAO.listarNombresSolicitantes()
                )
            );
        }
    });

    // Otros combos
   cmbCarrera.setItems(FXCollections.observableArrayList(
    asignacionDAO.listarCarreras().keySet()
    ));
    cmbMateria.setItems(FXCollections.observableArrayList(
        asignacionDAO.listarMaterias()
    ));

    cmbGrupo.setItems(
        FXCollections.observableArrayList(
            asignacionDAO.listarGrupos()
        )
    );


    // Combo de tipos de espacio
    cmbTipoEspacio.setItems(
        FXCollections.observableArrayList(
            "Lab. de cómputo",
            "Aula común",
            "Especializado",
            "Sala múltiple"
        )
    );

    
    // El combo dependiente inicia deshabilitado
    cmbEspacio.setDisable(true);
    cmbEspacio.getItems().clear();

    cmbTipoEspacio.valueProperty().addListener((obs, valorAnterior, tipo) -> {

        // Limpiar combo dependiente
        cmbEspacio.getSelectionModel().clearSelection();
        cmbEspacio.getItems().clear();
        cmbEspacio.setValue(null);

        if (cmbEspacio.isEditable()) {
            cmbEspacio.getEditor().clear();
        }

        // Si no hay tipo seleccionado, permanece deshabilitado
        if (tipo == null || tipo.trim().isEmpty()) {
            cmbEspacio.setDisable(true);
            return;
        }

        // Consultar espacios según el tipo seleccionado
        mapaEspacios = asignacionDAO.listarEspaciosPorTipo(tipo);

        cmbEspacio.setItems(
            FXCollections.observableArrayList(
                mapaEspacios.keySet()
            )
        );

        cmbEspacio.setDisable(mapaEspacios.isEmpty());
    });
}

    // =========================================================
    // BUSCAR

    @FXML
    private void onBuscar() {
        this.buscar();
    }
    @FXML
    private void onLimpiarFiltros() {
        this.limpiar();
    }

    @FXML
private void buscar() {
    String solicitante = cmbSolicitante.getValue();
    String tipoEspacio = cmbTipoEspacio.getValue();
    String espacio = cmbEspacio.getValue();
    String estado = cmbEstado.getValue();
    String carrera = cmbCarrera.getValue();
    String materia = cmbMateria.getValue();
    String grupo = cmbGrupo.getValue();

    LocalDate fechaDesde = dtDesde.getValue();
    LocalDate fechaHasta = dtHasta.getValue();

    if (fechaDesde != null
            && fechaHasta != null
            && fechaDesde.isAfter(fechaHasta)) {

        mostrarAlerta(
                "La fecha desde no puede ser posterior "
                + "a la fecha hasta."
        );
        return;
    }

    ObservableList<Consultas> resultados =
            asignacionDAO.buscarConFiltros(
                    solicitante,
                    tipoEspacio,
                    espacio,
                    estado,
                    carrera,
                    materia,
                    grupo,
                    fechaDesde,
                    fechaHasta
            );

    tblResultados.setItems(resultados);

    lblCantidadResultados.setText(
            "Resultados: " + resultados.size()
    );
}


     @FXML
private void limpiar() {
    cmbSolicitante.setValue(null);
    cmbTipoEspacio.setValue(null);
    cmbEspacio.setValue(null);
    cmbEstado.setValue(null);
    cmbCarrera.setValue(null);
    cmbMateria.setValue(null);
    cmbGrupo.setValue(null);

    dtDesde.setValue(null);
    dtHasta.setValue(null);

    tblResultados.getItems().clear();

    lblCantidadResultados.setText("Resultados: 0");
}

    // =========================================================
    // EXPORTAR
    // =========================================================
    // TODO: todavía no exporta de verdad a Excel/PDF. Cuando me
    // digas en qué formato lo quieres, lo conectamos aquí.
@FXML
private void onExportar() {

    if (tblResultados.getItems() == null || tblResultados.getItems().isEmpty()) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setHeaderText(null);
        alerta.setContentText("No hay datos para exportar.");
        alerta.showAndWait();
        return;
    }

    List<String> opciones = List.of("PDF", "Excel", "Word");

    ChoiceDialog<String> dialogo = new ChoiceDialog<>("PDF", opciones);
    dialogo.setTitle("Exportar reporte");
    dialogo.setHeaderText(null);
    dialogo.setContentText("Selecciona el formato de exportación:");

    Optional<String> resultado = dialogo.showAndWait();
    if (resultado.isEmpty()) {
        return;
    }

    String formato = resultado.get();

    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialFileName("reporte_ocupacion");

    switch (formato) {
        case "PDF" -> fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        case "Excel" -> fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Libro de Excel", "*.xlsx"));
        case "Word" -> fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documento de Word", "*.docx"));
    }

    Stage stage = (Stage) btnExportar.getScene().getWindow();
    File destino = fileChooser.showSaveDialog(stage);
    if (destino == null) {
        return;
    }

    try {
        String rangoTexto = "Desde: "
                + (dtDesde.getValue() == null ? "-" : dtDesde.getValue().format(FORMATO_FECHA))
                + " Hasta: "
                + (dtHasta.getValue() == null ? "-" : dtHasta.getValue().format(FORMATO_FECHA));

        ObservableList<Reporte> datos = convertirAReporte(tblResultados.getItems());

        switch (formato) {
            case "PDF" -> ReporteExportador.exportarPDF(null, datos, rangoTexto, destino);
            case "Excel" -> ReporteExportador.exportarExcel(null, datos, rangoTexto, destino);
            case "Word" -> ReporteExportador.exportarWord(null, datos, rangoTexto, destino);
        }

        Alert exito = new Alert(Alert.AlertType.INFORMATION);
        exito.setHeaderText(null);
        exito.setContentText("Reporte exportado correctamente en formato " + formato + ".");
        exito.showAndWait();

    } catch (Exception e) {
        e.printStackTrace();
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setHeaderText(null);
        error.setContentText("Ocurrió un error al exportar el reporte: " + e.getMessage());
        error.showAndWait();
    }
}

private ObservableList<Reporte> convertirAReporte(ObservableList<Consultas> datos) {
    ObservableList<Reporte> convertido = FXCollections.observableArrayList();

    for (Consultas c : datos) {
        Reporte r = new Reporte(
        "",
        c.getHorario(),
        c.getEspacio(),
        c.getSolicitante(),
        c.getGrupo(),
        c.getEstado(),
        c.getMotivo(),
        c.getCarrera(),
        c.getMateria()
);
convertido.add(r);
    }

    return convertido;
}


    // =========================================================
    // UTILIDADES
    // =========================================================
    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

}
