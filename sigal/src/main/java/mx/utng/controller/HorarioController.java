package mx.utng.controller;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import mx.utng.model.CeldaHorario;
import mx.utng.model.EstadoCelda;

public class HorarioController implements javafx.fxml.Initializable {

    @FXML private ComboBox<String> cmbTipoEspacio;
    @FXML private ComboBox<String> cmbEspacio;
    @FXML private DatePicker dpFecha;

    @FXML private ToggleGroup grupoVista;
    @FXML private ToggleButton tglDiaria;
    @FXML private ToggleButton tglSemanal;
    @FXML private ToggleButton tglMensual;

    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnNuevaAsignacion;

    @FXML private ScrollPane scrollHorario;
    @FXML private VBox contenedorGrid;

    /** Referencia al menú, para poder mandar a la persona a "Asignaciones" desde el diálogo. */
    private MenuController menuController;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    private static final String[] DIAS = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};

    private static final String[] HORAS_MATUTINO = {
            "8:00-8:50", "9:00-9:50", "10:00-10:50", "11:00-11:50",
            "11:50-12:20", "12:20-13:10", "13:15-14:05", "14:10-15:00", "15:10-16:00"
    };

    private static final String[] HORAS_VESPERTINO = {
            "16:00-16:50", "17:00-17:50", "17:50-18:20", "18:20-19:10",
            "19:10-20:00", "20:05-20:55", "21:00-22:00"
    };

    private static final String[] TIPOS = {
            "Laboratorio de cómputo", "Laboratorio especializado", "Aula común", "Sala de usos múltiples"
    };

    private static final String[] ESPACIOS = {
            "LAB-T11", "LAB-T12", "LAB-RED", "LAB-SEG", "AUL-T11", "AUL-T12", "SAL-US1", "SAL-US2", "SAL-US3"
    };

    /** Clave = "dia|hora" -> celda con su estado y detalle (docente - materia) */
    private final Map<String, CeldaHorario> celdas = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbTipoEspacio.getItems().add("Todos los tipos");
        cmbTipoEspacio.getItems().addAll(TIPOS);
        cmbTipoEspacio.setValue("Todos los tipos");

        cmbEspacio.getItems().add("Todos los espacios");
        cmbEspacio.getItems().addAll(ESPACIOS);
        cmbEspacio.setValue("Todos los espacios");

        dpFecha.setValue(LocalDate.now());

        cargarDatosIniciales();

        grupoVista.selectedToggleProperty().addListener((obs, old, val) -> renderizar());

        renderizar();
    }

    private void cargarDatosIniciales() {
        // Todos los espacios inician como "Libre". El mapa "celdas" se va llenando
        // sobre la marcha conforme el usuario reserva u ocupa horarios desde la UI
        // (ver onClickCelda y onNuevaAsignacion). No se precarga ningún estado.
    }

    private void marcar(String dia, String hora, EstadoCelda estado, String detalle) {
        celdas.put(clave(dia, hora), new CeldaHorario(estado, detalle));
    }

    private String clave(String dia, String hora) {
        return dia + "|" + hora;
    }

    private CeldaHorario getCelda(String dia, String hora) {
        return celdas.computeIfAbsent(clave(dia, hora), k -> new CeldaHorario());
    }

    // ==================== RENDERIZADO ====================

    private void renderizar() {
        contenedorGrid.getChildren().clear();

        if (tglMensual.isSelected()) {
            renderizarMensual();
        } else if (tglDiaria.isSelected()) {
            renderizarGrid(diaSeleccionadoParaVistaDiaria());
        } else {
            renderizarGrid(List.of(DIAS));
        }
    }

    private List<String> diaSeleccionadoParaVistaDiaria() {
        LocalDate fecha = dpFecha.getValue() == null ? LocalDate.now() : dpFecha.getValue();
        DayOfWeek dow = fecha.getDayOfWeek();
        int idx = dow.getValue() - 1; // Lunes = 0 ... Domingo = 6
        if (idx >= 0 && idx < DIAS.length) {
            return List.of(DIAS[idx]);
        }
        return List.of(); // Domingo: no hay columnas que mostrar
    }

    private void renderizarMensual() {
        VBox aviso = new VBox();
        aviso.setAlignment(Pos.CENTER);
        aviso.setSpacing(8.0);
        aviso.setPadding(new Insets(60));
        Label icono = new Label("🗓");
        icono.getStyleClass().add("empty-icon");
        Label titulo = new Label("Vista mensual");
        titulo.getStyleClass().add("empty-title");
        Label texto = new Label("La vista mensual no está disponible en este preview. Usa \"Diaria\" o \"Semanal\".");
        texto.getStyleClass().add("empty-text");
        aviso.getChildren().addAll(icono, titulo, texto);
        contenedorGrid.getChildren().add(aviso);
    }

    private void renderizarGrid(List<String> dias) {
        if (dias.isEmpty()) {
            VBox aviso = new VBox();
            aviso.setAlignment(Pos.CENTER);
            aviso.setPadding(new Insets(60));
            Label texto = new Label("No hay horario configurado para el domingo.");
            texto.getStyleClass().add("empty-text");
            aviso.getChildren().add(texto);
            contenedorGrid.getChildren().add(aviso);
            return;
        }

        GridPane grid = new GridPane();
        grid.getStyleClass().add("horario-grid");
        grid.setHgap(0);
        grid.setVgap(0);

        ColumnConstraints colHora = new ColumnConstraints();
        colHora.setPrefWidth(110);
        colHora.setMinWidth(110);
        grid.getColumnConstraints().add(colHora);

        for (int i = 0; i < dias.size(); i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            grid.getColumnConstraints().add(cc);
        }

        int fila = 0;

        // Encabezado
        grid.add(celdaEncabezado("Hr."), 0, fila);
        for (int i = 0; i < dias.size(); i++) {
            grid.add(celdaEncabezado(dias.get(i)), i + 1, fila);
        }
        fila++;

        for (String hora : HORAS_MATUTINO) {
            grid.add(celdaHora(hora), 0, fila);
            for (int i = 0; i < dias.size(); i++) {
                grid.add(celdaEstado(dias.get(i), hora), i + 1, fila);
            }
            fila++;
        }

        Label divisor = new Label("Horario vespertino");
        divisor.getStyleClass().add("divisor-vespertino");
        divisor.setMaxWidth(Double.MAX_VALUE);
        GridPane.setColumnSpan(divisor, dias.size() + 1);
        grid.add(divisor, 0, fila);
        fila++;

        for (String hora : HORAS_VESPERTINO) {
            grid.add(celdaHora(hora), 0, fila);
            for (int i = 0; i < dias.size(); i++) {
                grid.add(celdaEstado(dias.get(i), hora), i + 1, fila);
            }
            fila++;
        }

        contenedorGrid.getChildren().add(grid);
    }

    private Label celdaEncabezado(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("grid-header-cell");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private Label celdaHora(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("grid-hora-cell");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER_LEFT);
        return l;
    }

    private VBox celdaEstado(String dia, String hora) {
        CeldaHorario celda = getCelda(dia, hora);

        Label lblEstado = new Label(celda.getEstado().getEtiqueta());
        lblEstado.getStyleClass().add("cell-estado-text");

        VBox caja = new VBox(2.0, lblEstado);
        caja.setAlignment(Pos.CENTER);
        caja.setPadding(new Insets(10, 6, 10, 6));
        caja.setMaxWidth(Double.MAX_VALUE);
        caja.setMaxHeight(Double.MAX_VALUE);
        caja.getStyleClass().add("grid-cell");

        if (!celda.getDetalle().isEmpty()) {
            Label lblDetalle = new Label(celda.getDetalle());
            lblDetalle.getStyleClass().add("cell-detalle-text");
            lblDetalle.setWrapText(true);
            lblDetalle.setAlignment(Pos.CENTER);
            caja.getChildren().add(lblDetalle);
        }

        switch (celda.getEstado()) {
            case LIBRE:
                caja.getStyleClass().add("cell-libre");
                break;
            case RESERVADO:
                caja.getStyleClass().add("cell-reservado");
                break;
            case OCUPADO:
                caja.getStyleClass().add("cell-ocupado");
                break;
        }

        caja.setOnMouseClicked(e -> onClickCelda(dia, hora));
        return caja;
    }

    // ==================== INTERACCIÓN ====================

    private void onClickCelda(String dia, String hora) {
        CeldaHorario celda = getCelda(dia, hora);

        if (celda.getEstado() == EstadoCelda.LIBRE) {
            Optional<String[]> resultado = mostrarDialogoReserva(dia, hora, false);
            resultado.ifPresent(datos -> {
                celda.setEstado(EstadoCelda.RESERVADO);
                celda.setDetalle(datos[2] + " - " + datos[3]);
                renderizar();
            });
        } else {
            String tituloEstado = celda.getEstado() == EstadoCelda.OCUPADO ? "ocupada" : "reservada";
            Alert confirm = new Alert(AlertType.CONFIRMATION,
                    "Esta celda está " + tituloEstado + " (" + celda.getDetalle() + ").\n¿Deseas liberarla?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.setTitle("Liberar espacio");
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.YES) {
                    celda.setEstado(EstadoCelda.LIBRE);
                    celda.setDetalle("");
                    renderizar();
                }
            });
        }
    }

    @FXML
    private void onNuevaAsignacion() {
        Optional<String[]> resultado = mostrarDialogoReserva(null, null, true);
        resultado.ifPresent(datos -> {
            String dia = datos[0];
            String hora = datos[1];
            CeldaHorario celda = getCelda(dia, hora);
            if (celda.getEstado() != EstadoCelda.LIBRE) {
                mostrarAlerta("Horario ocupado", "El espacio ya está " + celda.getEstado().getEtiqueta().toLowerCase()
                        + " el " + dia + " en el horario " + hora + ". Elige otro horario.");
                return;
            }
            celda.setEstado(EstadoCelda.RESERVADO);
            celda.setDetalle(datos[2] + " - " + datos[3]);

            tglSemanal.setSelected(true);
            renderizar();
        });
    }

    /**
     * Muestra un diálogo para capturar una nueva reserva.
     * Si permitirElegirDiaHora es true, se incluyen combos de Día y Horario (usado por "Nueva Asignación").
     * Si es false, día y hora ya se conocen (clic directo sobre una celda libre).
     *
     * @return arreglo [dia, hora, docente, materia] o vacío si se cancela
     */
    private Optional<String[]> mostrarDialogoReserva(String diaFijo, String horaFijo, boolean permitirElegirDiaHora) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Nueva reservación");
        dialog.setHeaderText(permitirElegirDiaHora
                ? "Selecciona el día, el horario y captura los datos de la asignación."
                : "Reservar " + diaFijo + " · " + horaFijo);

        ButtonType btnConfirmar = new ButtonType("Asignar", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirmar, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(new Insets(16, 16, 4, 16));

        ComboBox<String> cmbDia = new ComboBox<>();
        ComboBox<String> cmbHora = new ComboBox<>();
        TextField txtDocente = new TextField();
        txtDocente.setPromptText("Ej. Ing. Juan Pérez");
        TextField txtMateria = new TextField();
        txtMateria.setPromptText("Ej. Programación II");

        int fila = 0;
        if (permitirElegirDiaHora) {
            cmbDia.getItems().addAll(DIAS);
            cmbDia.setValue(DIAS[0]);

            cmbHora.getItems().addAll(HORAS_MATUTINO);
            cmbHora.getItems().addAll(HORAS_VESPERTINO);
            cmbHora.setValue(HORAS_MATUTINO[0]);

            form.add(new Label("Día:"), 0, fila);
            form.add(cmbDia, 1, fila);
            fila++;
            form.add(new Label("Horario:"), 0, fila);
            form.add(cmbHora, 1, fila);
            fila++;
        }

        form.add(new Label("Docente:"), 0, fila);
        form.add(txtDocente, 1, fila);
        fila++;
        form.add(new Label("Materia:"), 0, fila);
        form.add(txtMateria, 1, fila);

        // Acceso directo al formulario completo de Asignaciones, por si no le
        // alcanza con Día/Horario/Docente/Materia (ej. necesita elegir espacio,
        // carrera, grupo, número de alumnos, etc.).
        Hyperlink lnkIrAsignaciones = new Hyperlink("¿Necesitas más datos? Ir a Nueva asignación →");
        lnkIrAsignaciones.getStyleClass().add("dialog-link");
        lnkIrAsignaciones.setOnAction(e -> {
            dialog.setResult(null);
            dialog.close();
            if (menuController != null) {
                menuController.abrirModulo("fx_asignaciones");
            }
        });

        VBox contenido = new VBox(6, form, lnkIrAsignaciones);
        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().getStyleClass().add("dialog-reserva");

        dialog.setResultConverter(bt -> {
            if (bt == btnConfirmar) {
                String dia = permitirElegirDiaHora ? cmbDia.getValue() : diaFijo;
                String hora = permitirElegirDiaHora ? cmbHora.getValue() : horaFijo;
                String docente = txtDocente.getText() == null ? "" : txtDocente.getText().trim();
                String materia = txtMateria.getText() == null ? "" : txtMateria.getText().trim();

                if (docente.isEmpty() || materia.isEmpty()) {
                    mostrarAlerta("Datos incompletos", "Captura el docente y la materia para reservar el espacio.");
                    return null;
                }
                return new String[]{dia, hora, docente, materia};
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // ==================== FILTROS ====================

    @FXML
    private void onBuscar() {
        renderizar();
    }

    @FXML
    private void onLimpiar() {
        cmbTipoEspacio.setValue("Todos los tipos");
        cmbEspacio.setValue("Todos los espacios");
        dpFecha.setValue(LocalDate.now());
        tglSemanal.setSelected(true);
        renderizar();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.setTitle(titulo);
        alerta.showAndWait();
    }
}
