package mx.utng.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import mx.utng.dao.AsignacionDAO;
import mx.utng.dao.ConsultaDAO;
import mx.utng.model.Consultas;

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
    @FXML private TextField txtSolicitante;
    @FXML private ComboBox<String> cmbTipoEspacio;
    @FXML private ComboBox<String> cmbEspacio;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<String> cmbCarrera;
    @FXML private TextField txtMateria;
    @FXML private TextField txtGrupo;
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

    private static final String[] TIPOS_ESPACIO = {
            "Laboratorio de cómputo",
            "Laboratorio especializado",
            "Aula común",
            "Sala de usos múltiples"
    };

    private static final String[] ESTADOS = { "Libre", "Ocupado", "Asignado", "Cancelado" };

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final ConsultaDAO consultaDAO = new ConsultaDAO();

    // Catálogos "nombre visible -> ID real en la BD"
    private Map<String, Integer> mapaProfesores;
    private Map<String, Integer> mapaCarreras;
    private Map<String, Integer> mapaEspacios;

    @FXML
    public void initialize() {

        cargarColumnas();
        cargarCatalogos();

        // Al abrir la pantalla se muestran todas las asignaciones
        // (sin filtros), igual que en la referencia.
        buscar();
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
    private void cargarCatalogos() {

        mapaProfesores = asignacionDAO.listarProfesores();
        mapaCarreras = asignacionDAO.listarCarreras();
        mapaEspacios = asignacionDAO.listarEspacios();

        cmbProfesor.setItems(FXCollections.observableArrayList(mapaProfesores.keySet()));
        cmbCarrera.setItems(FXCollections.observableArrayList(mapaCarreras.keySet()));
        cmbEspacio.setItems(FXCollections.observableArrayList(mapaEspacios.keySet()));

        cmbTipoEspacio.setItems(FXCollections.observableArrayList(TIPOS_ESPACIO));
        cmbEstado.setItems(FXCollections.observableArrayList(ESTADOS));
    }

    // =========================================================
    // BUSCAR
    // =========================================================
    @FXML
    private void onBuscar() {
        buscar();
    }

    private void buscar() {

        Integer idProfesor = mapaProfesores.get(cmbProfesor.getValue());
        Integer idCarrera = mapaCarreras.get(cmbCarrera.getValue());
        Integer idEspacio = mapaEspacios.get(cmbEspacio.getValue());

        String solicitante = txtSolicitante.getText();
        String materia = txtMateria.getText();
        String grupo = txtGrupo.getText();
        String estado = cmbEstado.getValue();

        LocalDate fechaDesde = dtDesde.getValue();
        LocalDate fechaHasta = dtHasta.getValue();

        var resultados = consultaDAO.buscar(
                idProfesor, idCarrera, idEspacio,
                solicitante, materia, grupo, estado,
                fechaDesde, fechaHasta
        );

        tblResultados.setItems(resultados);
        lblCantidadResultados.setText(String.valueOf(resultados.size()));
    }

    // =========================================================
    // LIMPIAR FILTROS
    // =========================================================
    @FXML
    private void onLimpiarFiltros() {

        cmbProfesor.setValue(null);
        cmbCarrera.setValue(null);
        cmbEspacio.setValue(null);
        cmbTipoEspacio.setValue(null);
        cmbEstado.setValue(null);
        txtSolicitante.clear();
        txtMateria.clear();
        txtGrupo.clear();
        dtDesde.setValue(null);
        dtHasta.setValue(null);

        buscar();
    }

    // =========================================================
    // EXPORTAR
    // =========================================================
    // TODO: todavía no exporta de verdad a Excel/PDF. Cuando me
    // digas en qué formato lo quieres, lo conectamos aquí.
    @FXML
    private void onExportar() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setHeaderText(null);
        alerta.setContentText("La exportación todavía no está conectada. Dime en qué formato "
                + "quieres exportar (Excel, PDF, CSV) y lo agregamos.");
        alerta.showAndWait();
    }

}
