package mx.utng.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import mx.utng.model.Espacio;

/**
 * Controlador de la pantalla "Disponibilidad completa"
 * (fx_disponibilidad.fxml).
 *
 * Se abre desde el link "Ver más →" de la tarjeta "Disponibilidad de
 * espacios" en la pantalla de Asignaciones. Igual que
 * AsignacionesController, este controlador es solo del CONTENIDO:
 * el sidebar y el topbar los sigue manejando MenuController, que es
 * quien carga este FXML dentro de su contentPane.
 */
public class DisponibilidadController implements Initializable {

    // Referencia al menú para poder "regresar" a Asignaciones
    private MenuController menuController;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    // --------------------------- Resumen ---------------------------
    @FXML private Label lblDisponibles;
    @FXML private Label lblOcupados;
    @FXML private Label lblMantenimiento;
    @FXML private Label lblCancelados;

    // ----------------------------- Tabla -----------------------------
    @FXML private TextField txtBuscar;
    @FXML private TableView<Espacio> tablaEspacios;
    @FXML private TableColumn<Espacio, String> colNombre;
    @FXML private TableColumn<Espacio, String> colTipo;
    @FXML private TableColumn<Espacio, String> colCapacidad;
    @FXML private TableColumn<Espacio, String> colEstado;
    @FXML private TableColumn<Espacio, String> colOcupacion;
    @FXML private TableColumn<Espacio, String> colProximoHorario;
    @FXML private TableColumn<Espacio, String> colEncargado;

    private final ObservableList<Espacio> listaEspacios = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnas();
        cargarDatosDePrueba();
        tablaEspacios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaEspacios.setItems(listaEspacios);
        actualizarResumen();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> filtrar(nuevo));
        }
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(data -> data.getValue().nombreProperty());
        colTipo.setCellValueFactory(data -> data.getValue().tipoProperty());
        colCapacidad.setCellValueFactory(data -> data.getValue().capacidadProperty());
        colOcupacion.setCellValueFactory(data -> data.getValue().ocupacionActualProperty());
        colProximoHorario.setCellValueFactory(data -> data.getValue().proximoHorarioProperty());
        colEncargado.setCellValueFactory(data -> data.getValue().encargadoProperty());

        colEstado.setCellValueFactory(data -> data.getValue().estadoProperty());
        colEstado.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setGraphic(null);
                    return;
                }
                badge.setText(estado);
                badge.getStyleClass().removeAll(
                        "badge-disponible", "badge-ocupado", "badge-mantenimiento", "badge-cancelado");
                switch (estado) {
                    case "Disponible" -> badge.getStyleClass().add("badge-disponible");
                    case "Ocupado" -> badge.getStyleClass().add("badge-ocupado");
                    case "Mantenimiento" -> badge.getStyleClass().add("badge-mantenimiento");
                    default -> badge.getStyleClass().add("badge-cancelado");
                }
                setGraphic(badge);
            }
        });
    }

    private void cargarDatosDePrueba() {
        listaEspacios.addAll(
                new Espacio("Lab. Cómputo 1", "Laboratorio de cómputo", "35", "Ocupado",
                        "Programación II · 5° A", "Libre a partir de las 09:30", "María González"),
                new Espacio("Lab. Cómputo 2", "Laboratorio de cómputo", "35", "Disponible",
                        "—", "Todo el día libre", "María González"),
                new Espacio("Lab. WAN", "Laboratorio de redes", "30", "Ocupado",
                        "Redes de Computadoras · 6° B", "Libre a partir de las 11:30", "Juan Pérez"),
                new Espacio("Sala Audiovisual", "Sala multimedia", "40", "Ocupado",
                        "Capacitación administrativa", "Libre a partir de las 13:30", "Administración"),
                new Espacio("Lab. Fotografía", "Laboratorio especializado", "20", "Ocupado",
                        "Fotografía Digital · 3° A", "Libre a partir de las 16:30", "Carla López"),
                new Espacio("Lab. Soporte", "Laboratorio de mantenimiento", "15", "Mantenimiento",
                        "Fuera de servicio", "Vuelve el 02/08/2026", "Roberto Sánchez"),
                new Espacio("Lab. Electrónica", "Laboratorio de mecatrónica", "25", "Mantenimiento",
                        "Revisión de equipo", "Vuelve el 01/08/2026", "Roberto Sánchez"),
                new Espacio("Aula 101", "Aula teórica", "45", "Disponible",
                        "—", "Todo el día libre", "—"),
                new Espacio("Aula 102", "Aula teórica", "45", "Cancelado",
                        "Reservación cancelada hoy", "Disponible desde ahora", "—"),
                new Espacio("Sala de Juntas", "Sala administrativa", "12", "Disponible",
                        "—", "Todo el día libre", "Administración")
        );
    }

    private void actualizarResumen() {
        long disponibles = listaEspacios.stream().filter(e -> "Disponible".equals(e.getEstado())).count();
        long ocupados = listaEspacios.stream().filter(e -> "Ocupado".equals(e.getEstado())).count();
        long mantenimiento = listaEspacios.stream().filter(e -> "Mantenimiento".equals(e.getEstado())).count();
        long cancelados = listaEspacios.stream().filter(e -> "Cancelado".equals(e.getEstado())).count();

        if (lblDisponibles != null) lblDisponibles.setText(String.valueOf(disponibles));
        if (lblOcupados != null) lblOcupados.setText(String.valueOf(ocupados));
        if (lblMantenimiento != null) lblMantenimiento.setText(String.valueOf(mantenimiento));
        if (lblCancelados != null) lblCancelados.setText(String.valueOf(cancelados));
    }

    private void filtrar(String texto) {
        if (texto == null || texto.isBlank()) {
            tablaEspacios.setItems(listaEspacios);
            return;
        }
        String t = texto.toLowerCase();
        ObservableList<Espacio> filtrados = FXCollections.observableArrayList();
        for (Espacio e : listaEspacios) {
            if (e.getNombre().toLowerCase().contains(t)
                    || e.getTipo().toLowerCase().contains(t)
                    || e.getEstado().toLowerCase().contains(t)
                    || e.getEncargado().toLowerCase().contains(t)) {
                filtrados.add(e);
            }
        }
        tablaEspacios.setItems(filtrados);
    }

    @FXML
    private void onVolver(ActionEvent event) {
        if (menuController != null) {
            menuController.abrirModulo("fx_asignaciones");
        }
    }
}

