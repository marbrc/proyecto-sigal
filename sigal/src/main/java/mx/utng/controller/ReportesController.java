package mx.utng.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import mx.utng.dao.ReporteDAO;
import mx.utng.model.Consultas;
import mx.utng.model.Reporte;
import mx.utng.model.ResultadoReporte;
import mx.utng.util.ReporteExportador;

/**
 * Controller de fx_reportes.fxml (pantalla "Reportes" de SIGAL).
 *
 * Arma un reporte de ocupación de espacios: elige un periodo
 * (Mensual/Semanal/Anual), calcula el rango de fechas, le pide a
 * ReporteDAO las horas ocupadas reales (tb_asignacion) contra las
 * horas disponibles estimadas, y pinta 4 tarjetas resumen + una
 * tabla + una gráfica apilada.
 */
public class ReportesController {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    // ---- Filtros ----
    @FXML private ComboBox<String> cmbPeriodo;
    @FXML private ComboBox<String> cmbMes;
    @FXML private ComboBox<String> cmbAnio;
    @FXML private ComboBox<String> cmbEspacio;
    @FXML private Button btnGenerar;

    // ---- Tarjetas resumen ----
    @FXML private Label lblTotalEspacios;
    @FXML private Label lblEspaciosConAsignaciones;
    @FXML private Label lblPromedioAsignaciones;
    @FXML private Label lblEspacioMasAsignado;
    @FXML private Label lblRangoPeriodo;
        @FXML private Label lblFechaGeneracion;

    // ---- Tabla ----
    @FXML private TableView<Reporte> tablaReporte;
    @FXML private TableColumn<Reporte, String> colEspacio;
    @FXML private TableColumn<Reporte, Double> colHorasDisponibles;
    @FXML private TableColumn<Reporte, Double> colHorasOcupadas;
    @FXML private TableColumn<Reporte, String> colPorcentaje;

    // ---- Gráfica ----
    @FXML private StackedBarChart<String, Number> graficaOcupacion;

    @FXML private Button btnExportarPDF;
    @FXML private Button btnExportarExcel;
    @FXML private Button btnExportarWord;
    @FXML
    private TableView<Reporte> tablaDetalle;


 


    /** Lo último que se generó, para poder exportarlo sin volver a consultar la BD. */
    private ResultadoReporte resultadoActual;
    private String rangoTextoActual = "";
    private ObservableList<Reporte> detalleActual =
        FXCollections.observableArrayList();


    private static final String[] MESES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    /** Texto del combo "Espacio" (nombre) -> ID_Espacio real en la BD. */
    private Map<String, Integer> mapaEspacios;

    @FXML
    private DatePicker dtDia;

    @FXML
    public void initialize() {

        cmbPeriodo.setItems(FXCollections.observableArrayList("Mensual", "Semanal", "Anual"));
        cmbPeriodo.setValue("Mensual");

        cmbMes.setItems(FXCollections.observableArrayList(MESES));
        cmbMes.setValue(MESES[LocalDate.now().getMonthValue() - 1]);

        int anioActual = LocalDate.now().getYear();
        cmbAnio.setItems(FXCollections.observableArrayList(
                String.valueOf(anioActual - 1), String.valueOf(anioActual), String.valueOf(anioActual + 1)));
        cmbAnio.setValue(String.valueOf(anioActual));

        // El combo "Mes" solo aplica cuando el periodo es Mensual.
        cmbPeriodo.valueProperty().addListener((obs, anterior, actual) ->
                cmbMes.setDisable(!"Mensual".equals(actual)));

        mapaEspacios = reporteDAO.listarEspacios();
        cmbEspacio.getItems().add("Todos");
        cmbEspacio.getItems().addAll(mapaEspacios.keySet());
        cmbEspacio.setValue("Todos");

        colEspacio.setCellValueFactory(new PropertyValueFactory<>("espacio"));
        colHorasDisponibles.setCellValueFactory(new PropertyValueFactory<>("horasDisponibles"));
        colHorasOcupadas.setCellValueFactory(new PropertyValueFactory<>("horasOcupadas"));
        colPorcentaje.setCellValueFactory(datosFila ->
                new SimpleStringProperty(datosFila.getValue().getPorcentajeTexto()));

        generarReporte(null);
    }

    @FXML
    private void onGenerarReporte(ActionEvent event) {
    String espacioSeleccionado = cmbEspacio.getValue();

    Integer idEspacio = null;

    if (espacioSeleccionado != null
            && !"Todos".equals(espacioSeleccionado)) {

        idEspacio = mapaEspacios.get(espacioSeleccionado);
    }

    generarReporte(idEspacio);
}

 private void generarReporte(Integer idEspacioFiltro) {
    LocalDate[] rango = calcularRango();

    LocalDate desde = rango[0];
    LocalDate hasta = rango[1];

    ResultadoReporte resultado =
            reporteDAO.generar(
                    desde,
                    hasta,
                    idEspacioFiltro
            );

    lblTotalEspacios.setText(
            String.valueOf(resultado.getTotalEspacios())
    );;

    lblEspaciosConAsignaciones.setText(
            String.valueOf(
                    resultado.getEspaciosConAsignaciones()
            )
    );

    lblPromedioAsignaciones.setText(
            String.format(
                    "%.1f",
                    resultado.getPromedioAsignacionesPorEspacio()
            )
    );

    lblEspacioMasAsignado.setText(
            resultado.getEspacioMasAsignado()
    );

    tablaReporte.setItems(resultado.getFilas());

    /*
     * Fecha en la que se generó el reporte
     */
    DateTimeFormatter formatoFechaHora =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm"
            );

    actualizarGrafica(resultado);

    if (lblRangoPeriodo != null) {
        rangoTextoActual =
                "Del "
                        + formatoCorto(desde)
                        + " al "
                        + formatoCorto(hasta);

        lblRangoPeriodo.setText(rangoTextoActual);
    }
    this.resultadoActual = resultado;
    detalleActual = reporteDAO.listarDetalle(
        desde,
        hasta,
        idEspacioFiltro
    );

    tablaReporte.setItems(detalleActual);
}

private LocalDate[] calcularRango() {
    String periodo = cmbPeriodo.getValue();

    if (periodo == null || periodo.isBlank()) {
        periodo = "Mensual";
    }

    if ("Diario".equals(periodo)) {
        LocalDate diaSeleccionado = dtDia.getValue();

        if (diaSeleccionado == null) {
            diaSeleccionado = LocalDate.now();
        }

        return new LocalDate[] {
                diaSeleccionado,
                diaSeleccionado
        };
    }

    if ("Semanal".equals(periodo)) {
        LocalDate hoy = LocalDate.now();

        return new LocalDate[] {
                hoy.minusDays(6),
                hoy
        };
    }

    // Mensual
    int anio = Integer.parseInt(cmbAnio.getValue());

    int mesIndice =
            Arrays.asList(MESES)
                    .indexOf(cmbMes.getValue()) + 1;

    YearMonth ym = YearMonth.of(anio, mesIndice);

    return new LocalDate[] {
            ym.atDay(1),
            ym.atEndOfMonth()
    };
}


    private void actualizarGrafica(ResultadoReporte resultado) {
        graficaOcupacion.getData().clear();

        XYChart.Series<String, Number> serieOcupadas = new XYChart.Series<>();
        serieOcupadas.setName("Horas ocupadas");

        XYChart.Series<String, Number> serieDisponibles = new XYChart.Series<>();
        serieDisponibles.setName("Horas disponibles restantes");

        for (Reporte fila : resultado.getFilas()) {
            serieOcupadas.getData().add(new XYChart.Data<>(fila.getEspacio(), fila.getHorasOcupadas()));
            double restante = Math.max(fila.getHorasDisponibles() - fila.getHorasOcupadas(), 0);
            serieDisponibles.getData().add(new XYChart.Data<>(fila.getEspacio(), restante));
        }

        graficaOcupacion.getData().addAll(serieOcupadas, serieDisponibles);
    }

    private String formatoCorto(LocalDate fecha) {
        return String.format("%02d/%02d/%d", fecha.getDayOfMonth(), fecha.getMonthValue(), fecha.getYear());
    }

    // ============================================================
    //  EXPORTAR
    // ============================================================

 @FXML
    private void onExportarPDF(ActionEvent event) {
        exportar(
                "PDF (*.pdf)",
                "*.pdf",
                "reporte_sigal.pdf",
                destino -> ReporteExportador.exportarPDF(
                        resultadoActual,
                        detalleActual,
                        rangoTextoActual,
                        destino
                ),
                event
        );
    }


    @FXML
    private void onExportarExcel(ActionEvent event) {
        exportar(
                "Excel (*.xlsx)",
                "*.xlsx",
                "reporte_sigal.xlsx",
                destino -> ReporteExportador.exportarExcel(
                        resultadoActual,
                        detalleActual,
                        rangoTextoActual,
                        destino
                ),
                event
        );
    }


    @FXML
private void onExportarWord(ActionEvent event) {
    exportar(
            "Word (*.docx)",
            "*.docx",
            "reporte_sigal.docx",
            destino -> ReporteExportador.exportarWord(
                    resultadoActual,
                    detalleActual,
                    rangoTextoActual,
                    destino
            ),
            event
    );
}



    /** Pequeña interfaz para no repetir el mismo try/catch 3 veces (PDF/Excel/Word). */
    @FunctionalInterface
    private interface AccionExportar {
        void exportar(File destino) throws IOException;
    }

    private void exportar(String descripcionFiltro, String patronFiltro, String nombreSugerido,
                           AccionExportar accion, ActionEvent event) {

        if (resultadoActual == null || resultadoActual.getFilas().isEmpty()) {
            mostrarAviso(AlertType.WARNING, "Nada que exportar",
                    "Primero genera un reporte con datos antes de exportarlo.");
            return;
        }

        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar reporte");
        selector.setInitialFileName(nombreSugerido);
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter(descripcionFiltro, patronFiltro));

        Window ventana = ((Node) event.getSource()).getScene().getWindow();
        File destino = selector.showSaveDialog(ventana);
        if (destino == null) {
            return; // el usuario le dio Cancelar
        }

        try {
            accion.exportar(destino);
            mostrarAviso(AlertType.INFORMATION, "Reporte exportado",
                    "Se guardó correctamente en:\n" + destino.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAviso(AlertType.ERROR, "No se pudo exportar",
                    "Ocurrió un problema generando el archivo. Revisa que no esté abierto en otro programa.");
        }
    }

    private void mostrarAviso(AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
