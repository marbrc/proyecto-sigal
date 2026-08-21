package mx.utng.controller;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
 
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import mx.utng.dao.AsignacionDAO;
import mx.utng.dao.EspacioDAO;
import mx.utng.model.AsignacionHorario;
import mx.utng.model.CeldaHorario;
import mx.utng.model.EspacioRegistro;
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
    private MenuController menuController;
    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }
    private static final String[] DIAS = {"Lunes","Martes","Miércoles","Jueves","Viernes","Sábado"};
    private static String[] HORAS_MATUTINO = {
            "8:00-8:50",
            "9:00-9:50",
            "10:00-10:50",
            "11:00-11:50",
            "11:50-12:20",
            "12:20-13:10",
            "13:15-14:05",
            "14:10-15:00",
            "15:10-16:00"
    };
    private static String[] HORAS_VESPERTINO = {
            "16:00-16:50",
            "17:00-17:50",
            "17:50-18:20",
            "18:20-19:10",
            "19:10-20:00",
            "20:05-20:55",
            "21:00-22:00"
    };
    private final List<String> todasLasHoras = new ArrayList<>();
    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("H:mm");
    private final AsignacionDAO asignacionDAO =
            new AsignacionDAO();
    private final EspacioDAO espacioDAO =
            new EspacioDAO();
    private List<EspacioRegistro> todosLosEspacios =
            new ArrayList<>();
    private Map<String, Integer> mapaEspacios =
            new LinkedHashMap<>();
    private final Map<String, CeldaHorario> celdas =
            new HashMap<>();
    private final Map<String, AsignacionHorario> celdaAsignacion =
            new HashMap<>();
    @Override
    public void initialize(
            URL location,
            ResourceBundle resources) {
        actualizarListaHoras();
        cargarDatosIniciales();
        dpFecha.setValue(LocalDate.now());
        grupoVista.selectedToggleProperty().addListener(
                (obs, old, val) -> renderizar()
        );
        cmbTipoEspacio.valueProperty().addListener(
                (obs, old, val) ->
                        filtrarComboEspacioPorTipo(val)
        );
        renderizar();
    }
    private void actualizarListaHoras() {
        todasLasHoras.clear();
        todasLasHoras.addAll(
                List.of(HORAS_MATUTINO)
        );
        todasLasHoras.addAll(
                List.of(HORAS_VESPERTINO)
        );
    }
    private void cargarDatosIniciales() {
        todosLosEspacios =
                new ArrayList<>(
                        espacioDAO.listarTodos()
                );
        mapaEspacios =
                new LinkedHashMap<>();
        for (EspacioRegistro e :
                todosLosEspacios) {
            mapaEspacios.put(
                    e.getNombre(),
                    e.getIdEspacio()
            );
        }
        List<String> tipos =
                todosLosEspacios.stream()
                        .map(EspacioRegistro::getTipo)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
        cmbTipoEspacio.getItems()
                .setAll("Todos los tipos");
        cmbTipoEspacio.getItems()
                .addAll(tipos);
        cmbTipoEspacio.setValue(
                "Todos los tipos"
        );
        filtrarComboEspacioPorTipo(
                "Todos los tipos"
        );
    }
    private void filtrarComboEspacioPorTipo(
            String tipo) {
        List<String> nombres =
                todosLosEspacios.stream()
                        .filter(e ->
                                tipo == null ||
                                tipo.equals("Todos los tipos") ||
                                tipo.equals(e.getTipo())
                        )
                        .map(EspacioRegistro::getNombre)
                        .collect(Collectors.toList());
        cmbEspacio.getItems()
                .setAll("Todos los espacios");
        cmbEspacio.getItems()
                .addAll(nombres);
        cmbEspacio.setValue(
                "Todos los espacios"
        );
    }
    private String clave(
            String dia,
            String hora) {
        return dia + "|" + hora;
    }
    private CeldaHorario getCelda(
            String dia,
            String hora) {
        return celdas.computeIfAbsent(
                clave(dia, hora),
                k -> new CeldaHorario()
        );
    }
    private LocalDate lunesDeLaSemana() {
        LocalDate base =
                dpFecha.getValue() == null
                        ? LocalDate.now()
                        : dpFecha.getValue();
        return base.minusDays(
                base.getDayOfWeek().getValue() - 1L
        );
    }
    private LocalDate fechaParaDia(
            String diaNombre) {
        int idx =
                List.of(DIAS).indexOf(
                        diaNombre
                );
        return lunesDeLaSemana()
                .plusDays(
                        Math.max(idx, 0)
                );
    }
    private String nombreDiaDeFecha(
            LocalDate fecha) {
        int idx =
                fecha.getDayOfWeek().getValue() - 1;
        return (
                idx >= 0 &&
                idx < DIAS.length
        )
                ? DIAS[idx]
                : null;
    }
    private LocalTime parseHora(
            String texto) {
        return LocalTime.parse(
                texto.trim(),
                FORMATO_HORA
        );
    }
    private boolean seSolapan(
            String slot,
            LocalTime inicioAsignacion,
            LocalTime finAsignacion) {
        String[] partes =
                slot.split("-");
        LocalTime inicioSlot =
                parseHora(partes[0]);
        LocalTime finSlot =
                parseHora(partes[1]);
        return inicioSlot.isBefore(
                finAsignacion
        )
                &&
                finSlot.isAfter(
                        inicioAsignacion
                );
    }
    private boolean debeMostrarEspacioEnDetalle() {
        String v =
                cmbEspacio.getValue();
        return v == null ||
                v.equals("Todos los espacios");
    }
    private void cargarAsignacionesDesdeBD(
            List<String> diasVisibles) {
        if (diasVisibles.isEmpty()) {
            return;
        }
        LocalDate desde =
                fechaParaDia(
                        diasVisibles.get(0)
                );
        LocalDate hasta =
                fechaParaDia(
                        diasVisibles.get(
                                diasVisibles.size() - 1
                        )
                );
        String espacioSeleccionado =
                cmbEspacio.getValue();
        Integer idEspacio =
                (
                        espacioSeleccionado == null ||
                        espacioSeleccionado.equals(
                                "Todos los espacios"
                        )
                )
                        ? null
                        : mapaEspacios.get(
                                espacioSeleccionado
                        );
        List<AsignacionHorario> asignaciones =
                asignacionDAO.listarParaHorario(
                        idEspacio,
                        desde,
                        hasta
                );
        boolean mostrarEspacio =
                debeMostrarEspacioEnDetalle();
        for (AsignacionHorario a :
                asignaciones) {
            String diaNombre =
                    nombreDiaDeFecha(
                            a.getFecha()
                    );
            if (
                    diaNombre == null ||
                    !diasVisibles.contains(
                            diaNombre
                    )
            ) {
                continue;
            }
            for (String hora :
                    todasLasHoras) {
                if (
                        !seSolapan(
                                hora,
                                a.getHoraInicio(),
                                a.getHoraTermino()
                        )
                ) {
                    continue;
                }
                StringBuilder detalle =
                        new StringBuilder();
                if (mostrarEspacio) {
                    detalle.append(
                            a.getEspacio()
                    );
                }
                String quien =
                        (
                                a.getDocente() == null ||
                                a.getDocente().isBlank()
                        )
                                ? ""
                                : a.getDocente();
                String materia =
                        (
                                a.getMateria() == null ||
                                a.getMateria().isBlank()
                        )
                                ? ""
                                : a.getMateria();
                String personaMateria =
                        quien.isEmpty()
                                ? materia
                                : (
                                        materia.isEmpty()
                                                ? quien
                                                : quien +
                                                " - " +
                                                materia
                                );
                if (!personaMateria.isEmpty()) {
                    if (detalle.length() > 0) {
                        detalle.append(": ");
                    }
                    detalle.append(
                            personaMateria
                    );
                }
                EstadoCelda estado =
                        "Ocupado".equals(
                                a.getEstado()
                        )
                                ? EstadoCelda.OCUPADO
                                : EstadoCelda.ASIGNADO;
                String clave =
                        clave(
                                diaNombre,
                                hora
                        );
                celdas.put(
                        clave,
                        new CeldaHorario(
                                estado,
                                detalle.toString()
                        )
                );
                celdaAsignacion.put(
                        clave,
                        a
                );
            }
        }
    }
    private void renderizar() {
        contenedorGrid.getChildren().clear();
        celdas.clear();
        celdaAsignacion.clear();
        actualizarListaHoras();
        if (tglMensual.isSelected()) {
            renderizarMensual();
            return;
        }
        List<String> diasVisibles =
                tglDiaria.isSelected()
                        ? diaSeleccionadoParaVistaDiaria()
                        : List.of(DIAS);
        cargarAsignacionesDesdeBD(
                diasVisibles
        );
        renderizarGrid(
                diasVisibles
        );
    }
    private List<String> diaSeleccionadoParaVistaDiaria() {
        LocalDate fecha =
                dpFecha.getValue() == null
                        ? LocalDate.now()
                        : dpFecha.getValue();
        DayOfWeek dow =
                fecha.getDayOfWeek();
        int idx =
                dow.getValue() - 1;
        if (
                idx >= 0 &&
                idx < DIAS.length
        ) {
            return List.of(
                    DIAS[idx]
            );
        }
        return List.of();
    }
    private void renderizarMensual() {
        LocalDate base =
                dpFecha.getValue() == null
                        ? LocalDate.now()
                        : dpFecha.getValue();
        LocalDate primerDiaMes = base.withDayOfMonth(1);
        LocalDate ultimoDiaMes = primerDiaMes.plusMonths(1).minusDays(1);
        LocalDate inicioCalendario =
                primerDiaMes.minusDays(
                        primerDiaMes.getDayOfWeek().getValue() - 1L
                );
        LocalDate finCalendario =
                ultimoDiaMes.plusDays(
                        7L - ultimoDiaMes.getDayOfWeek().getValue()
                );
 
        String espacioSeleccionado = cmbEspacio.getValue();
        Integer idEspacio =
                (
                        espacioSeleccionado == null ||
                        espacioSeleccionado.equals("Todos los espacios")
                )
                        ? null
                        : mapaEspacios.get(espacioSeleccionado);
 
        List<AsignacionHorario> asignacionesMes =
                asignacionDAO.listarParaHorario(idEspacio, inicioCalendario, finCalendario);
        Map<LocalDate, List<AsignacionHorario>> porDia = new HashMap<>();
        for (AsignacionHorario a : asignacionesMes) {
            porDia.computeIfAbsent(a.getFecha(), k -> new ArrayList<>()).add(a);
        }
 
        VBox contenedorMes = new VBox(10);
 
        HBox encabezadoMes = new HBox(10);
        encabezadoMes.setAlignment(Pos.CENTER_LEFT);
        Button btnMesAnterior = new Button("‹");
        btnMesAnterior.getStyleClass().add("btn-secondary");
        Button btnMesSiguiente = new Button("›");
        btnMesSiguiente.getStyleClass().add("btn-secondary");
        Label lblMes = new Label(nombreMesEs(base));
        lblMes.getStyleClass().add("empty-title");
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);
        btnMesAnterior.setOnAction(e -> {
            dpFecha.setValue(base.minusMonths(1));
            renderizar();
        });
        btnMesSiguiente.setOnAction(e -> {
            dpFecha.setValue(base.plusMonths(1));
            renderizar();
        });
        encabezadoMes.getChildren().addAll(btnMesAnterior, lblMes, btnMesSiguiente, espaciador);
 
        GridPane calendario = new GridPane();
        calendario.getStyleClass().add("horario-grid");
        calendario.setHgap(0);
        calendario.setVgap(0);
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            calendario.getColumnConstraints().add(cc);
        }
        String[] diasSemanaCorto = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        for (int i = 0; i < 7; i++) {
            calendario.add(celdaEncabezado(diasSemanaCorto[i]), i, 0);
        }
        RowConstraints rcEncabezado = new RowConstraints();
        rcEncabezado.setPrefHeight(40);
        calendario.getRowConstraints().add(rcEncabezado);
 
        LocalDate cursor = inicioCalendario;
        int fila = 1;
        while (!cursor.isAfter(finCalendario)) {
            RowConstraints rc = new RowConstraints();
            rc.setPrefHeight(90);
            rc.setVgrow(Priority.ALWAYS);
            calendario.getRowConstraints().add(rc);
            for (int col = 0; col < 7; col++) {
                boolean delMesActual = cursor.getMonth() == base.getMonth() && cursor.getYear() == base.getYear();
                List<AsignacionHorario> delDia =
                        porDia.getOrDefault(cursor, List.of()).stream()
                                .sorted(Comparator.comparing(AsignacionHorario::getHoraInicio))
                                .collect(Collectors.toList());
                calendario.add(celdaDiaMensual(cursor, delMesActual, delDia), col, fila);
                cursor = cursor.plusDays(1);
            }
            fila++;
        }
 
        contenedorMes.getChildren().addAll(encabezadoMes, calendario);
        contenedorGrid.getChildren().add(contenedorMes);
    }
    private String nombreMesEs(LocalDate fecha) {
        String mes = fecha.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "MX"));
        String mesCapitalizado = mes.substring(0, 1).toUpperCase() + mes.substring(1);
        return mesCapitalizado + " " + fecha.getYear();
    }
    private VBox celdaDiaMensual(
            LocalDate fecha,
            boolean delMesActual,
            List<AsignacionHorario> asignacionesDelDia) {
        VBox caja = new VBox(4);
        caja.setPadding(new Insets(8));
        caja.getStyleClass().add("grid-cell");
        caja.setMaxWidth(Double.MAX_VALUE);
        caja.setMaxHeight(Double.MAX_VALUE);
        if (!delMesActual) {
            caja.setOpacity(0.35);
        }
 
        Label lblNumero = new Label(String.valueOf(fecha.getDayOfMonth()));
        lblNumero.getStyleClass().add("cell-estado-text");
        boolean esHoy = fecha.equals(LocalDate.now());
        if (esHoy) {
            lblNumero.setStyle("-fx-text-fill: #4fc3ff; -fx-underline: true;");
        }
        caja.getChildren().add(lblNumero);
 
        if (!asignacionesDelDia.isEmpty()) {
            boolean hayOcupado =
                    asignacionesDelDia.stream()
                            .anyMatch(a -> "Ocupado".equals(a.getEstado()));
            caja.getStyleClass().add(hayOcupado ? "cell-ocupado" : "cell-asignado");
 
            int maxAMostrar = 3;
            for (int i = 0; i < Math.min(maxAMostrar, asignacionesDelDia.size()); i++) {
                AsignacionHorario a = asignacionesDelDia.get(i);
                String quien =
                        (a.getDocente() == null || a.getDocente().isBlank())
                                ? (a.getMateria() == null ? "" : a.getMateria())
                                : a.getDocente();
                Label lblItem = new Label(
                        a.getHoraInicio().toString().substring(0, 5) + " " + quien
                );
                lblItem.getStyleClass().add("cell-detalle-text");
                lblItem.setWrapText(true);
                caja.getChildren().add(lblItem);
            }
            if (asignacionesDelDia.size() > maxAMostrar) {
                Label lblMas = new Label(
                        "+" + (asignacionesDelDia.size() - maxAMostrar) + " más"
                );
                lblMas.getStyleClass().add("cell-detalle-text");
                caja.getChildren().add(lblMas);
            }
        } else {
            caja.getStyleClass().add("cell-libre");
        }
 
        caja.setOnMouseClicked(e -> {
            dpFecha.setValue(fecha);
            tglDiaria.setSelected(true);
            renderizar();
        });
        return caja;
    }
    private void renderizarGrid(
            List<String> dias) {
        if (dias.isEmpty()) {
            VBox aviso =
                    new VBox();
            aviso.setAlignment(
                    Pos.CENTER
            );
            aviso.setPadding(
                    new Insets(60)
            );
            Label texto =
                    new Label(
                            "No hay horario configurado para el domingo."
                    );
            texto.getStyleClass()
                    .add("empty-text");
            aviso.getChildren()
                    .add(texto);
            contenedorGrid
                    .getChildren()
                    .add(aviso);
            return;
        }
        GridPane grid =
                new GridPane();
        grid.getStyleClass()
                .add("horario-grid");
        grid.setHgap(0);
        grid.setVgap(0);
        ColumnConstraints colHora =
                new ColumnConstraints();
        colHora.setPrefWidth(110);
        colHora.setMinWidth(110);
        grid.getColumnConstraints()
                .add(colHora);
        for (int i = 0;
                i < dias.size();
                i++) {
            ColumnConstraints cc =
                    new ColumnConstraints();
            cc.setHgrow(
                    Priority.ALWAYS
            );
            cc.setFillWidth(true);
            grid.getColumnConstraints()
                    .add(cc);
        }
        int fila = 0;
        grid.add(
                celdaEncabezadoHora(),
                0,
                fila
        );
        for (int i = 0;
                i < dias.size();
                i++) {
            grid.add(
                    celdaEncabezado(
                            dias.get(i)
                    ),
                    i + 1,
                    fila
            );
        }
        fila++;
        for (String hora :
                HORAS_MATUTINO) {
            grid.add(
                    celdaHora(hora),
                    0,
                    fila
            );
            for (int i = 0;
                    i < dias.size();
                    i++) {
                grid.add(
                        celdaEstado(
                                dias.get(i),
                                hora
                        ),
                        i + 1,
                        fila
                );
            }
            fila++;
        }
        Label divisor =
                new Label(
                        "Horario vespertino"
                );
        divisor.getStyleClass()
                .add("divisor-vespertino");
        divisor.setMaxWidth(
                Double.MAX_VALUE
        );
        GridPane.setColumnSpan(
                divisor,
                dias.size() + 1
        );
        grid.add(
                divisor,
                0,
                fila
        );
        fila++;
        for (String hora :
                HORAS_VESPERTINO) {
            grid.add(
                    celdaHora(hora),
                    0,
                    fila
            );
            for (int i = 0;
                    i < dias.size();
                    i++) {
                grid.add(
                        celdaEstado(
                                dias.get(i),
                                hora
                        ),
                        i + 1,
                        fila
                );
            }
            fila++;
        }
        contenedorGrid
                .getChildren()
                .add(grid);
    }
    private Label celdaEncabezado(
            String texto) {
        Label l =
                new Label(texto);
        l.getStyleClass()
                .add("grid-header-cell");
        l.setMaxWidth(
                Double.MAX_VALUE
        );
        l.setAlignment(
                Pos.CENTER
        );
        return l;
    }
    private void alturaEncabezado(
            Region region) {
        region.setPrefHeight(40);
        region.setMinHeight(40);
        region.setMaxHeight(40);
    }
    private Label celdaHora(
            String hora) {
        Label label =
                new Label(hora);
        label.getStyleClass()
                .add("grid-hora-cell");
        label.setMaxWidth(
                Double.MAX_VALUE
        );
        label.setAlignment(
                Pos.CENTER_LEFT
        );
        label.setOnMouseClicked(
                e -> editarHora(hora)
        );
        return label;
    }
    private void editarHora(
            String actual) {
        TextInputDialog dialog =
                new TextInputDialog(
                        actual
                );
        dialog.setTitle(
                "Editar horario"
        );
        dialog.setHeaderText(
                "Modifica el horario"
        );
        dialog.setContentText(
                "Nuevo horario (ej. 8:00-8:50):"
        );
        dialog.showAndWait()
                .ifPresent(nueva -> {
                    nueva = nueva.trim();
                    if (!horarioValido(nueva)) {
                        mostrarAlerta(
                                "Horario inválido",
                                "Usa el formato 8:00-8:50."
                        );
                        return;
                    }
                    if (
                            !nueva.equals(actual) &&
                            existeHora(nueva)
                    ) {
                        mostrarAlerta(
                                "Horario duplicado",
                                "Ese horario ya existe."
                        );
                        return;
                    }
                    reemplazarHora(
                            actual,
                            nueva
                    );
                    actualizarListaHoras();
                    renderizar();
                });
    }
    private void reemplazarHora(
            String anterior,
            String nueva) {
        for (int i = 0;
                i < HORAS_MATUTINO.length;
                i++) {
            if (
                    HORAS_MATUTINO[i]
                            .equals(anterior)
            ) {
                HORAS_MATUTINO[i] =
                        nueva;
                return;
            }
        }
        for (int i = 0;
                i < HORAS_VESPERTINO.length;
                i++) {
            if (
                    HORAS_VESPERTINO[i]
                            .equals(anterior)
            ) {
                HORAS_VESPERTINO[i] =
                        nueva;
                return;
            }
        }
    }
    private boolean existeHora(
            String hora) {
        return List.of(
                HORAS_MATUTINO
        ).contains(hora)
                ||
                List.of(
                        HORAS_VESPERTINO
                ).contains(hora);
    }
    private HBox celdaEncabezadoHora() {
        Button editar =
                new Button("✎");
        editar.setTooltip(
                new Tooltip(
                        "Ajustar todos los horarios"
                )
        );
        editar.setFocusTraversable(
                false
        );
        editar.setPrefSize(
                28,
                28
        );
        editar.setMinSize(
                28,
                28
        );
        editar.setMaxSize(
                28,
                28
        );
        editar.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #4FC3F7;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 0;"
        );
        editar.setOnMouseEntered(
                e -> editar.setStyle(
                        "-fx-background-color:" +
                        "rgba(79,195,247,0.15);" +
                        "-fx-text-fill:#81D4FA;" +
                        "-fx-font-size:17px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-cursor:hand;" +
                        "-fx-padding:0;" +
                        "-fx-background-radius:6;"
                )
        );
        editar.setOnMouseExited(
                e -> editar.setStyle(
                        "-fx-background-color:transparent;" +
                        "-fx-text-fill:#4FC3F7;" +
                        "-fx-font-size:16px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-cursor:hand;" +
                        "-fx-padding:0;"
                )
        );
        editar.setOnAction(
                e -> ajustarTodasLasHoras()
        );
        Label hr =
                new Label("Hr.");
        HBox box =
                new HBox(
                        5,
                        editar,
                        hr
                );
        box.setAlignment(
                Pos.CENTER
        );
        box.setMaxWidth(
                Double.MAX_VALUE
        );
        box.getStyleClass()
                .add("grid-header-cell");
        alturaEncabezado(box);
        return box;
    }
    private void ajustarTodasLasHoras() {
        Dialog<ButtonType> dialog =
                new Dialog<>();
        dialog.setTitle(
                "Ajustar todos los horarios"
        );
        dialog.setHeaderText(
                "Modifica todas las horas"
        );
        ButtonType aplicar =
                new ButtonType(
                        "Aplicar",
                        ButtonBar.ButtonData.OK_DONE
                );
        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        aplicar,
                        ButtonType.CANCEL
                );
        ComboBox<String> accion =
                new ComboBox<>();
        accion.getItems().addAll(
                "Sumar tiempo","Disminuir tiempo"
        );
        accion.setValue(
                "Sumar tiempo"
        );
        TextField cantidad =
                new TextField();
        cantidad.setPromptText(
                "Ej. 50"
        );
        ComboBox<String> unidad =
                new ComboBox<>();
        unidad.getItems().addAll(
                "Minutos","Horas"
        );
        unidad.setValue(
                "Minutos"
        );
        GridPane form =
                new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(
                new Insets(
                        15,
                        20,
                        15,
                        20
                )
        );
        form.add(
                new Label("Acción:"),
                0,
                0
        );
        form.add(
                accion,1,0
        );
        form.add(
                new Label("Cantidad:"),0,1
        );
        form.add(
                cantidad,1,1
        );
        form.add(
                new Label("Unidad:"),0,2
        );
        form.add(
                unidad,1,2
        );
        dialog.getDialogPane()
                .setContent(form);
        dialog.setResultConverter(
                boton -> {
                    if (boton != aplicar) {
                        return boton;
                    }
                    try {
                        int valor =
                                Integer.parseInt(
                                        cantidad
                                                .getText()
                                                .trim()
                                );
                        if (valor <= 0) {
                            throw new NumberFormatException();
                        }
                        int minutos =
                                unidad.getValue()
                                        .equals("Horas")
                                        ? valor * 60
                                        : valor;
                        if (
                                accion.getValue()
                                        .equals(
                                                "Disminuir tiempo"
                                        )
                        ) {
                            minutos *= -1;
                        }
                        desplazarTodasLasHoras(
                                minutos
                        );
                    } catch (
                            NumberFormatException e
                    ) {
                        mostrarAlerta(
                                "Cantidad inválida","Escribe un número mayor que cero."
                        );
                    }
                    return boton;
                }
        );
        dialog.showAndWait();
    }
    private void desplazarTodasLasHoras(
            int minutos) {
        try {
            actualizarListaHoras();
            for (String hora :
                    todasLasHoras) {
                validarMovimiento(
                        hora,
                        minutos
                );
            }
            for (int i = 0;
                    i < HORAS_MATUTINO.length;
                    i++) {
                HORAS_MATUTINO[i] =
                        moverHorario(
                                HORAS_MATUTINO[i],
                                minutos
                        );
            }
            for (int i = 0;
                    i < HORAS_VESPERTINO.length;
                    i++) {
                HORAS_VESPERTINO[i] =
                        moverHorario(
                                HORAS_VESPERTINO[i],
                                minutos
                        );
            }
            actualizarListaHoras();
            renderizar();
        } catch (Exception e) {
            mostrarAlerta(
                    "No se pueden modificar las horas",
                    minutos < 0
                            ? "Algún horario quedaría antes de 00:00."
                            : "Algún horario quedaría después de 23:59."
            );
        }
    }
    private String moverHorario(
            String horario,
            int minutos) {
        String[] partes =
                horario.split("-");
        if (partes.length != 2) {
            throw new IllegalArgumentException();
        }
        LocalTime inicio =
                parseHora(partes[0]);
        LocalTime fin =
                parseHora(partes[1]);
        return inicio
                .plusMinutes(minutos)
                .format(FORMATO_HORA)
                + "-"
                + fin
                        .plusMinutes(minutos)
                        .format(FORMATO_HORA);
    }
    private boolean horarioValido(String horario) {
    try {
        String[] partes = horario.split("-");

        if (partes.length != 2) {
            return false;
        }

        LocalTime inicio = parseHora(partes[0]);
        LocalTime fin = parseHora(partes[1]);

        // La hora final debe ser después de la hora inicial
        if (!inicio.isBefore(fin)) {
            return false;
        }

        // La duración máxima permitida es de 50 minutos
        long minutos = java.time.Duration
                .between(inicio, fin)
                .toMinutes();

        return minutos <= 50;

    } catch (Exception e) {
        return false;
    }
}
    private void validarMovimiento(
            String horario,
            int minutos) {
        String[] partes =
                horario.split("-");
        if (partes.length != 2) {
            throw new IllegalArgumentException();
        }
        int inicio =
                parseHora(partes[0])
                        .toSecondOfDay() / 60
                        + minutos;
        int fin =
                parseHora(partes[1])
                        .toSecondOfDay() / 60
                        + minutos;
        if (
                inicio < 0 ||
                fin < 0 ||
                inicio > 1439 ||
                fin > 1439
        ) {
            throw new IllegalArgumentException();
        }
    }
    private VBox celdaEstado(
            String dia,
            String hora) {
        CeldaHorario celda =
                getCelda(
                        dia,
                        hora
                );
        Label lblEstado =
                new Label(
                        celda.getEstado()
                                .getEtiqueta()
                );
        lblEstado.getStyleClass()
                .add("cell-estado-text");
        VBox caja =
                new VBox(
                        2.0,
                        lblEstado
                );
        caja.setAlignment(
                Pos.CENTER
        );
        caja.setPadding(
                new Insets(
                        10,
                        6,
                        10,
                        6
                )
        );
        caja.setMaxWidth(
                Double.MAX_VALUE
        );
        caja.setMaxHeight(
                Double.MAX_VALUE
        );
        caja.getStyleClass()
                .add("grid-cell");
        if (
                !celda.getDetalle()
                        .isEmpty()
        ) {
            Label lblDetalle =
                    new Label(
                            celda.getDetalle()
                    );
            lblDetalle.getStyleClass()
                    .add(
                            "cell-detalle-text"
                    );
            lblDetalle.setWrapText(
                    true
            );
            lblDetalle.setAlignment(
                    Pos.CENTER
            );
            caja.getChildren()
                    .add(lblDetalle);
        }
        switch (
                celda.getEstado()
        ) {
            case LIBRE:
                caja.getStyleClass()
                        .add("cell-libre");
                break;
            case ASIGNADO:
                caja.getStyleClass()
                        .add("cell-asignado");
                break;
            case OCUPADO:
                caja.getStyleClass()
                        .add("cell-ocupado");
                break;
        }
        caja.setOnMouseClicked(
                e -> onClickCelda(
                        dia,
                        hora
                )
        );
        return caja;
    }
    private void onClickCelda(
            String dia,
            String hora) {
        CeldaHorario celda =
                getCelda(
                        dia,
                        hora
                );
        if (
                celda.getEstado()
                        == EstadoCelda.LIBRE
        ) {
            Optional<String[]> resultado =
                    mostrarDialogoAsignacion(
                            dia,
                            hora,
                            false
                    );
            resultado.ifPresent(
                    datos -> {
                        boolean guardada =
                                registrarAsignacion(
                                        datos[0],
                                        datos[1],
                                        datos[2],
                                        datos[3],
                                        datos[4],
                                        datos[5]
                                );
                        if (guardada) {
                            renderizar();
                        }
                    }
            );
        } else {
            AsignacionHorario asignacion =
                    celdaAsignacion.get(
                            clave(
                                    dia,
                                    hora
                            )
                    );
            String tituloEstado =
                    celda.getEstado()
                            == EstadoCelda.OCUPADO
                            ? "ocupada"
                            : "asignada";
            Alert confirm =
                    new Alert(
                            AlertType.CONFIRMATION,
                            "Esta celda está "
                                    + tituloEstado
                                    + " ("
                                    + celda.getDetalle()
                                    + ").\n¿Deseas liberarla?",
                            ButtonType.YES,
                            ButtonType.NO
                    );
            confirm.setHeaderText(
                    null
            );
            confirm.setTitle(
                    "Liberar espacio"
            );
            confirm.showAndWait()
                    .ifPresent(
                            resp -> {
                                if (
                                        resp != ButtonType.YES
                                ) {
                                    return;
                                }
                                if (asignacion == null) {
                                    renderizar();
                                    return;
                                }
                                boolean liberada =
                                        asignacionDAO.cancelar(
                                                asignacion
                                                        .getIdAsignacion()
                                        );
                                if (liberada) {
                                    renderizar();
                                } else {
                                    mostrarAlerta(
                                            "No se pudo liberar",
                                            "Ocurrió un problema al cancelar la asignación en la base de datos."
                                    );
                                }
                            }
                    );
        }
    }
    @FXML
    private void onNuevaAsignacion() {
        Optional<String[]> resultado =
                mostrarDialogoAsignacion(
                        null,
                        null,
                        true
                );
        resultado.ifPresent(
                datos -> {
                    String dia = datos[0];
                    String hora = datos[1];
                    if (
                            !"Todos los espacios"
                                    .equals(
                                            cmbEspacio.getValue()
                                    )
                    ) {
                        CeldaHorario celdaExistente =
                                getCelda(
                                        dia,
                                        hora
                                );
                        if (
                                celdaExistente.getEstado()
                                        != EstadoCelda.LIBRE
                        ) {
                            mostrarAlerta(
                                    "Horario ocupado",
                                    "El espacio ya está "
                                            + celdaExistente
                                                    .getEstado()
                                                    .getEtiqueta()
                                                    .toLowerCase()
                                            + " el "
                                            + dia
                                            + " en el horario "
                                            + hora
                                            + ". Elige otro horario."
                            );
                            return;
                        }
                    }
                    boolean guardada =
                            registrarAsignacion(
                                    dia,
                                    hora,
                                    datos[2],
                                    datos[3],
                                    datos[4],
                                    datos[5]
                            );
                    if (guardada) {
                        tglSemanal.setSelected(
                                true
                        );
                        renderizar();
                    }
                }
        );
    }
    private boolean registrarAsignacion(
            String dia,
            String hora,
            String espacioNombre,
            String tipoSolicitante,
            String docente,
            String materia) {
        Integer idEspacio =
                mapaEspacios.get(
                        espacioNombre
                );
        if (idEspacio == null) {
            mostrarAlerta(
                    "Espacio inválido",
                    "No se encontró el espacio seleccionado en la base de datos."
            );
            return false;
        }
        int idUsuarioActual =
                (menuController != null)
                        ? menuController
                                .getIdUsuarioActual()
                        : 0;
        if (idUsuarioActual <= 0) {
            mostrarAlerta(
                    "Sesión no encontrada",
                    "No se pudo identificar al usuario en sesión. Vuelve a iniciar sesión e inténtalo de nuevo."
            );
            return false;
        }
        LocalDate fecha =
                fechaParaDia(dia);
        String[] partes =
                hora.split("-");
        LocalTime horaInicio =
                parseHora(partes[0]);
        LocalTime horaTermino =
                parseHora(partes[1]);
        boolean guardada =
                asignacionDAO.insertarRapido(
                        idUsuarioActual,idEspacio,fecha,horaInicio,horaTermino,tipoSolicitante,docente,materia
                );
        if (!guardada) {
            mostrarAlerta(
                    "No se pudo guardar",
                    "Ocurrió un problema al guardar la asignación en la base de datos."
            );
        }
        return guardada;
    }
    private Optional<String[]> mostrarDialogoAsignacion(
            String diaFijo,
            String horaFijo,
            boolean permitirElegirDiaHora) {
        Dialog<String[]> dialog =
                new Dialog<>();
        dialog.setTitle(
                "Nueva asignación"
        );
        dialog.setHeaderText(
                permitirElegirDiaHora
                        ? "Selecciona el día, el horario y captura los datos de la asignación."
                        : "Asignar "
                                + diaFijo
                                + " · "
                                + horaFijo
        );
        ButtonType btnConfirmar =
                new ButtonType(
                        "Asignar",
                        ButtonData.OK_DONE
                );
        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnConfirmar,
                        ButtonType.CANCEL
                );
        GridPane form =
                new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(
                new Insets(
                        16,
                        16,
                        4,
                        16
                )
        );
        ComboBox<String> cmbDia =
                new ComboBox<>();
        ComboBox<String> cmbHora =
                new ComboBox<>();
        ComboBox<String> cmbEspacioDialogo =
                new ComboBox<>();
        ComboBox<String> cmbTipoSolicitante =
                new ComboBox<>();
        TextField txtDocente =
                new TextField();
        txtDocente.setPromptText(
                "Ej. Ing. Juan Pérez"
        );
        TextField txtMateria =
                new TextField();
        txtMateria.setPromptText(
                "Ej. Programación II (opcional)"
        );
        int fila = 0;
        if (permitirElegirDiaHora) {
            cmbDia.getItems()
                    .addAll(DIAS);
            cmbDia.setValue(
                    DIAS[0]
            );
            actualizarListaHoras();
            cmbHora.getItems()
                    .addAll(
                            todasLasHoras
                    );
            cmbHora.setValue(
                    todasLasHoras.get(0)
            );
            form.add(
                    new Label("Día:"),
                    0,
                    fila
            );
            form.add(
                    cmbDia,
                    1,
                    fila
            );
            fila++;
            form.add(
                    new Label("Horario:"),
                    0,
                    fila
            );
            form.add(
                    cmbHora,
                    1,
                    fila
            );
            fila++;
        }
        String espacioFiltroActual =
                cmbEspacio.getValue();
        boolean filtroTieneEspacioEspecifico =
                espacioFiltroActual != null
                        &&
                        !espacioFiltroActual.equals(
                                "Todos los espacios"
                        );
        cmbEspacioDialogo.getItems()
                .addAll(
                        mapaEspacios.keySet()
                );
        if (
                filtroTieneEspacioEspecifico
        ) {
            cmbEspacioDialogo.setValue(
                    espacioFiltroActual
            );
            cmbEspacioDialogo.setDisable(
                    true
            );
        } else if (
                !cmbEspacioDialogo
                        .getItems()
                        .isEmpty()
        ) {
            cmbEspacioDialogo.setValue(
                    cmbEspacioDialogo
                            .getItems()
                            .get(0)
            );
        }
        cmbTipoSolicitante.getItems()
                .addAll(
                        "Profesor","Administrativo","Alumno","Otro"
                );
        cmbTipoSolicitante.setValue(
                "Profesor"
        );
        form.add(
                new Label("Espacio:"),
                0,
                fila
        );
        form.add(
                cmbEspacioDialogo,
                1,
                fila
        );
        fila++;
        form.add(
                new Label(
                        "Tipo de solicitante:"
                ),
                0,
                fila
        );
        form.add(
                cmbTipoSolicitante,
                1,
                fila
        );
        fila++;
        form.add(
                new Label(
                        "Docente / Solicitante:"
                ),
                0,
                fila
        );
        form.add(
                txtDocente,
                1,
                fila
        );
        fila++;
        form.add(
                new Label("Materia:"),
                0,
                fila
        );
        form.add(
                txtMateria,
                1,
                fila
        );
        Hyperlink lnkIrAsignaciones =
                new Hyperlink(
                        "¿Necesitas más datos? Ir a Nueva asignación →"
                );
        lnkIrAsignaciones
                .getStyleClass()
                .add("dialog-link");
        lnkIrAsignaciones.setOnAction(
                e -> {
                    dialog.setResult(
                            null
                    );
                    dialog.close();
                    if (
                            menuController != null
                    ) {
                        menuController.abrirModulo(
                                "fx_asignaciones"
                        );
                    }
                }
        );
        VBox contenido =
                new VBox(
                        6,
                        form,
                        lnkIrAsignaciones
                );
        dialog.getDialogPane()
                .setContent(contenido);
        dialog.getDialogPane()
                .getStyleClass()
                .add(
                        "dialog-asignacion"
                );
        var hojaEstilos =
                getClass().getResource(
                        "/mx/utng/view/styles_horarios.css"
                );
        if (hojaEstilos != null) {
            dialog.getDialogPane()
                    .getStylesheets()
                    .add(
                            hojaEstilos
                                    .toExternalForm()
                    );
        }
        dialog.setResultConverter(
                bt -> {
                    if (
                            bt == btnConfirmar
                    ) {
                        String dia =
                                permitirElegirDiaHora
                                        ? cmbDia.getValue()
                                        : diaFijo;
                        String hora =
                                permitirElegirDiaHora
                                        ? cmbHora.getValue()
                                        : horaFijo;
                        String espacio =
                                cmbEspacioDialogo
                                        .getValue();
                        String tipoSolicitante =
                                cmbTipoSolicitante
                                        .getValue();
                        String docente =
                                txtDocente.getText()
                                        == null
                                        ? ""
                                        : txtDocente
                                                .getText()
                                                .trim();
                        String materia =
                                txtMateria.getText()
                                        == null
                                        ? ""
                                        : txtMateria
                                                .getText()
                                                .trim();
                        if (
                                espacio == null ||
                                espacio.isBlank()
                        ) {
                            mostrarAlerta(
                                    "Falta el espacio",
                                    "Selecciona el espacio que quieres asignar."
                            );
                            return null;
                        }
                        if (
                                docente.isEmpty()
                        ) {
                            mostrarAlerta(
                                    "Datos incompletos",
                                    "Captura el nombre del docente o solicitante para asignar el espacio."
                            );
                            return null;
                        }
                        return new String[]{
                                dia,hora,espacio,tipoSolicitante,docente,materia
                        };
                    }
                    return null;
                }
        );
        return dialog.showAndWait();
    }
    @FXML
    private void onBuscar() {
        renderizar();
    }
    @FXML
    private void onLimpiar() {
        cmbTipoEspacio.setValue(
                "Todos los tipos"
        );
        filtrarComboEspacioPorTipo(
                "Todos los tipos"
        );
        dpFecha.setValue(
                LocalDate.now()
        );
        tglSemanal.setSelected(
                true
        );
        renderizar();
    }
    private void mostrarAlerta(
            String titulo,
            String mensaje) {
        Alert alerta =
                new Alert(
                        AlertType.WARNING,
                        mensaje,
                        ButtonType.OK
                );
        alerta.setHeaderText(
                null
        );
        alerta.setTitle(
                titulo
        );
        alerta.showAndWait();
    }
}