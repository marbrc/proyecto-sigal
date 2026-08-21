package mx.utng.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setHeaderText(null);
        alerta.setContentText("La exportación todavía no está conectada. Dime en qué formato "
                + "quieres exportar (Excel, PDF, CSV) y lo agregamos.");
        alerta.showAndWait();
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
