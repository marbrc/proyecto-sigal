package mx.utng.controller;
 
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
import mx.utng.dao.AsignacionDAO;
import mx.utng.dao.EspacioDAO;
import mx.utng.model.AsignacionHorario;
import mx.utng.model.CeldaHorario;
import mx.utng.model.EspacioRegistro;
import mx.utng.model.EstadoCelda;
 
/**
 * Controlador de la pantalla "Horarios" (fx_horarios.fxml).
 *
 * Pinta la cuadrícula de disponibilidad y permite asignar/liberar celdas.
 * NO usa una tabla propia: se conecta directo a tb_asignacion (la misma
 * tabla que usa la pantalla de Asignaciones) a través de AsignacionDAO,
 * y a tb_espacio a través de EspacioDAO para llenar los combos de Tipo
 * y Espacio con datos reales.
 */
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
 
    /** Referencia al menú, para poder mandar a la persona a "Asignaciones" y para saber quién tiene la sesión. */
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
 
    private static final List<String> TODAS_LAS_HORAS;
    static {
        List<String> t = new ArrayList<>();
        t.addAll(List.of(HORAS_MATUTINO));
        t.addAll(List.of(HORAS_VESPERTINO));
        TODAS_LAS_HORAS = List.copyOf(t);
    }
 
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("H:mm");
 
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final EspacioDAO espacioDAO = new EspacioDAO();
 
    /** Todos los espacios reales (tb_espacio), para poder filtrar el combo "Espacio" según el "Tipo" elegido. */
    private List<EspacioRegistro> todosLosEspacios = new ArrayList<>();
 
    /** Nombre de espacio (como se ve en los combos) -> ID_Espacio real en la BD. */
    private Map<String, Integer> mapaEspacios = new LinkedHashMap<>();
 
    /** Clave = "dia|hora" -> celda con su estado y detalle (docente - materia), calculada a partir de la BD. */
    private final Map<String, CeldaHorario> celdas = new HashMap<>();
 
    /** Clave = "dia|hora" -> la asignación real de tb_asignacion que ocupa esa celda (null si sigue libre). */
    private final Map<String, AsignacionHorario> celdaAsignacion = new HashMap<>();
 
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarDatosIniciales();
 
        dpFecha.setValue(LocalDate.now());
 
        grupoVista.selectedToggleProperty().addListener((obs, old, val) -> renderizar());
        cmbTipoEspacio.valueProperty().addListener((obs, old, val) -> filtrarComboEspacioPorTipo(val));
 
        renderizar();
    }
 
    // ==================== CARGA DE CATÁLOGOS (BD REAL) ====================
 
    private void cargarDatosIniciales() {
        todosLosEspacios = new ArrayList<>(espacioDAO.listarTodos());
 
        mapaEspacios = new LinkedHashMap<>();
        for (EspacioRegistro e : todosLosEspacios) {
            mapaEspacios.put(e.getNombre(), e.getIdEspacio());
        }
 
        List<String> tipos = todosLosEspacios.stream()
                .map(EspacioRegistro::getTipo)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
 
        cmbTipoEspacio.getItems().setAll("Todos los tipos");
        cmbTipoEspacio.getItems().addAll(tipos);
        cmbTipoEspacio.setValue("Todos los tipos");
 
        filtrarComboEspacioPorTipo("Todos los tipos");
    }
 
    private void filtrarComboEspacioPorTipo(String tipo) {
        List<String> nombres = todosLosEspacios.stream()
                .filter(e -> tipo == null || tipo.equals("Todos los tipos") || tipo.equals(e.getTipo()))
                .map(EspacioRegistro::getNombre)
                .collect(Collectors.toList());
 
        cmbEspacio.getItems().setAll("Todos los espacios");
        cmbEspacio.getItems().addAll(nombres);
        cmbEspacio.setValue("Todos los espacios");
    }
 
    // ==================== HELPERS DE FECHA / CELDA ====================
 
    private String clave(String dia, String hora) {
        return dia + "|" + hora;
    }
 
    private CeldaHorario getCelda(String dia, String hora) {
        return celdas.computeIfAbsent(clave(dia, hora), k -> new CeldaHorario());
    }
 
    /** Lunes de la semana que contiene la fecha elegida en el DatePicker (o hoy, si no hay ninguna). */
    private LocalDate lunesDeLaSemana() {
        LocalDate base = dpFecha.getValue() == null ? LocalDate.now() : dpFecha.getValue();
        return base.minusDays(base.getDayOfWeek().getValue() - 1L);
    }
 
    /** Convierte "Lunes".."Sábado" a la fecha real dentro de la semana visible actualmente. */
    private LocalDate fechaParaDia(String diaNombre) {
        int idx = List.of(DIAS).indexOf(diaNombre);
        return lunesDeLaSemana().plusDays(Math.max(idx, 0));
    }
 
    private String nombreDiaDeFecha(LocalDate fecha) {
        int idx = fecha.getDayOfWeek().getValue() - 1; // Lunes = 0 ... Domingo = 6
        return (idx >= 0 && idx < DIAS.length) ? DIAS[idx] : null;
    }
 
    private LocalTime parseHora(String texto) {
        return LocalTime.parse(texto.trim(), FORMATO_HORA);
    }
 
    private boolean seSolapan(String slot, LocalTime inicioAsignacion, LocalTime finAsignacion) {
        String[] partes = slot.split("-");
        LocalTime inicioSlot = parseHora(partes[0]);
        LocalTime finSlot = parseHora(partes[1]);
        return inicioSlot.isBefore(finAsignacion) && finSlot.isAfter(inicioAsignacion);
    }
 
    /** Si el filtro superior está en "Todos los espacios", conviene mostrar de qué espacio se trata en el detalle. */
    private boolean debeMostrarEspacioEnDetalle() {
        String v = cmbEspacio.getValue();
        return v == null || v.equals("Todos los espacios");
    }
 
    // ==================== CARGA DE ASIGNACIONES DESDE LA BD ====================
 
    private void cargarAsignacionesDesdeBD(List<String> diasVisibles) {
        if (diasVisibles.isEmpty()) {
            return;
        }
 
        LocalDate desde = fechaParaDia(diasVisibles.get(0));
        LocalDate hasta = fechaParaDia(diasVisibles.get(diasVisibles.size() - 1));
 
        String espacioSeleccionado = cmbEspacio.getValue();
        Integer idEspacio = (espacioSeleccionado == null || espacioSeleccionado.equals("Todos los espacios"))
                ? null
                : mapaEspacios.get(espacioSeleccionado);
 
        List<AsignacionHorario> asignaciones = asignacionDAO.listarParaHorario(idEspacio, desde, hasta);
        boolean mostrarEspacio = debeMostrarEspacioEnDetalle();
 
        for (AsignacionHorario a : asignaciones) {
            String diaNombre = nombreDiaDeFecha(a.getFecha());
            if (diaNombre == null || !diasVisibles.contains(diaNombre)) {
                continue; // fuera de la vista actual (p.ej. domingo, o no aplica a la vista diaria)
            }
 
            for (String hora : TODAS_LAS_HORAS) {
                if (!seSolapan(hora, a.getHoraInicio(), a.getHoraTermino())) {
                    continue;
                }
 
                StringBuilder detalle = new StringBuilder();
                if (mostrarEspacio) {
                    detalle.append(a.getEspacio());
                }
                String quien = (a.getDocente() == null || a.getDocente().isBlank()) ? "" : a.getDocente();
                String materia = (a.getMateria() == null || a.getMateria().isBlank()) ? "" : a.getMateria();
                String personaMateria = quien.isEmpty() ? materia : (materia.isEmpty() ? quien : quien + " - " + materia);
                if (!personaMateria.isEmpty()) {
                    if (detalle.length() > 0) detalle.append(": ");
                    detalle.append(personaMateria);
                }
 
                EstadoCelda estado = "Ocupado".equals(a.getEstado()) ? EstadoCelda.OCUPADO : EstadoCelda.ASIGNADO;
                String clave = clave(diaNombre, hora);
                celdas.put(clave, new CeldaHorario(estado, detalle.toString()));
                celdaAsignacion.put(clave, a);
            }
        }
    }
 
    // ==================== RENDERIZADO ====================
 
    private void renderizar() {
        contenedorGrid.getChildren().clear();
        celdas.clear();
        celdaAsignacion.clear();
 
        if (tglMensual.isSelected()) {
            renderizarMensual();
            return;
        }
 
        List<String> diasVisibles = tglDiaria.isSelected() ? diaSeleccionadoParaVistaDiaria() : List.of(DIAS);
        cargarAsignacionesDesdeBD(diasVisibles);
        renderizarGrid(diasVisibles);
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
            case ASIGNADO:
                caja.getStyleClass().add("cell-asignado");
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
            Optional<String[]> resultado = mostrarDialogoAsignacion(dia, hora, false);
            resultado.ifPresent(datos -> {
                boolean guardada = registrarAsignacion(datos[0], datos[1], datos[2], datos[3], datos[4], datos[5]);
                if (guardada) {
                    renderizar();
                }
            });
        } else {
            AsignacionHorario asignacion = celdaAsignacion.get(clave(dia, hora));
            String tituloEstado = celda.getEstado() == EstadoCelda.OCUPADO ? "ocupada" : "asignada";
            Alert confirm = new Alert(AlertType.CONFIRMATION,
                    "Esta celda está " + tituloEstado + " (" + celda.getDetalle() + ").\n¿Deseas liberarla?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.setTitle("Liberar espacio");
            confirm.showAndWait().ifPresent(resp -> {
                if (resp != ButtonType.YES) return;
 
                if (asignacion == null) {
                    // No debería pasar (toda celda no-libre viene de una asignación real),
                    // pero por si acaso no se rompe la UI.
                    renderizar();
                    return;
                }
 
                boolean liberada = asignacionDAO.cancelar(asignacion.getIdAsignacion());
                if (liberada) {
                    renderizar();
                } else {
                    mostrarAlerta("No se pudo liberar",
                            "Ocurrió un problema al cancelar la asignación en la base de datos.");
                }
            });
        }
    }
 
    @FXML
    private void onNuevaAsignacion() {
        Optional<String[]> resultado = mostrarDialogoAsignacion(null, null, true);
        resultado.ifPresent(datos -> {
            String dia = datos[0];
            String hora = datos[1];
 
            // Si el filtro de arriba ya trae un espacio específico, avisamos si esa celda
            // ya está ocupada según lo que se ve ahorita en pantalla.
            if (!"Todos los espacios".equals(cmbEspacio.getValue())) {
                CeldaHorario celdaExistente = getCelda(dia, hora);
                if (celdaExistente.getEstado() != EstadoCelda.LIBRE) {
                    mostrarAlerta("Horario ocupado", "El espacio ya está "
                            + celdaExistente.getEstado().getEtiqueta().toLowerCase()
                            + " el " + dia + " en el horario " + hora + ". Elige otro horario.");
                    return;
                }
            }
 
            boolean guardada = registrarAsignacion(dia, hora, datos[2], datos[3], datos[4], datos[5]);
            if (guardada) {
                tglSemanal.setSelected(true);
                renderizar();
            }
        });
    }
 
    /**
     * Inserta la asignación en tb_asignacion (vía AsignacionDAO.insertarRapido).
     * @return true si se guardó correctamente
     */
    private boolean registrarAsignacion(String dia, String hora, String espacioNombre,
                                         String tipoSolicitante, String docente, String materia) {
        Integer idEspacio = mapaEspacios.get(espacioNombre);
        if (idEspacio == null) {
            mostrarAlerta("Espacio inválido", "No se encontró el espacio seleccionado en la base de datos.");
            return false;
        }
 
        int idUsuarioActual = (menuController != null) ? menuController.getIdUsuarioActual() : 0;
        if (idUsuarioActual <= 0) {
            mostrarAlerta("Sesión no encontrada",
                    "No se pudo identificar al usuario en sesión. Vuelve a iniciar sesión e inténtalo de nuevo.");
            return false;
        }
 
        LocalDate fecha = fechaParaDia(dia);
        String[] partes = hora.split("-");
        LocalTime horaInicio = parseHora(partes[0]);
        LocalTime horaTermino = parseHora(partes[1]);
 
        boolean guardada = asignacionDAO.insertarRapido(idUsuarioActual, idEspacio, fecha,
                horaInicio, horaTermino, tipoSolicitante, docente, materia);
 
        if (!guardada) {
            mostrarAlerta("No se pudo guardar",
                    "Ocurrió un problema al guardar la asignación en la base de datos.");
        }
        return guardada;
    }
 
    /**
     * Muestra un diálogo para capturar una nueva asignación. Pide solo lo indispensable
     * para que la asignación quede válida en tb_asignacion:
     * Espacio (obligatorio, precargado/deshabilitado si ya venía filtrado), Tipo de
     * solicitante, Docente/Solicitante (obligatorio) y Materia (opcional).
     *
     * Si permitirElegirDiaHora es true, se incluyen combos de Día y Horario (usado
     * por "Nueva Asignación"). Si es false, día y hora ya se conocen (clic directo
     * sobre una celda libre).
     *
     * @return arreglo [dia, hora, espacio, tipoSolicitante, docente, materia] o vacío si se cancela
     */
    private Optional<String[]> mostrarDialogoAsignacion(String diaFijo, String horaFijo, boolean permitirElegirDiaHora) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Nueva asignación");
        dialog.setHeaderText(permitirElegirDiaHora
                ? "Selecciona el día, el horario y captura los datos de la asignación."
                : "Asignar " + diaFijo + " · " + horaFijo);
 
        ButtonType btnConfirmar = new ButtonType("Asignar", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirmar, ButtonType.CANCEL);
 
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(new Insets(16, 16, 4, 16));
 
        ComboBox<String> cmbDia = new ComboBox<>();
        ComboBox<String> cmbHora = new ComboBox<>();
        ComboBox<String> cmbEspacioDialogo = new ComboBox<>();
        ComboBox<String> cmbTipoSolicitante = new ComboBox<>();
        TextField txtDocente = new TextField();
        txtDocente.setPromptText("Ej. Ing. Juan Pérez");
        TextField txtMateria = new TextField();
        txtMateria.setPromptText("Ej. Programación II (opcional)");
 
        int fila = 0;
        if (permitirElegirDiaHora) {
            cmbDia.getItems().addAll(DIAS);
            cmbDia.setValue(DIAS[0]);
 
            cmbHora.getItems().addAll(TODAS_LAS_HORAS);
            cmbHora.setValue(TODAS_LAS_HORAS.get(0));
 
            form.add(new Label("Día:"), 0, fila);
            form.add(cmbDia, 1, fila);
            fila++;
            form.add(new Label("Horario:"), 0, fila);
            form.add(cmbHora, 1, fila);
            fila++;
        }
 
        // Espacio: si el filtro de arriba ya trae uno específico, lo precargamos y
        // bloqueamos (para no asignar por error un espacio distinto al que se está
        // viendo); si el filtro está en "Todos los espacios", hay que elegir uno.
        String espacioFiltroActual = cmbEspacio.getValue();
        boolean filtroTieneEspacioEspecifico = espacioFiltroActual != null
                && !espacioFiltroActual.equals("Todos los espacios");
 
        cmbEspacioDialogo.getItems().addAll(mapaEspacios.keySet());
        if (filtroTieneEspacioEspecifico) {
            cmbEspacioDialogo.setValue(espacioFiltroActual);
            cmbEspacioDialogo.setDisable(true);
        } else if (!cmbEspacioDialogo.getItems().isEmpty()) {
            cmbEspacioDialogo.setValue(cmbEspacioDialogo.getItems().get(0));
        }
 
        cmbTipoSolicitante.getItems().addAll("Profesor", "Administrativo", "Alumno", "Otro");
        cmbTipoSolicitante.setValue("Profesor");
 
        form.add(new Label("Espacio:"), 0, fila);
        form.add(cmbEspacioDialogo, 1, fila);
        fila++;
        form.add(new Label("Tipo de solicitante:"), 0, fila);
        form.add(cmbTipoSolicitante, 1, fila);
        fila++;
        form.add(new Label("Docente / Solicitante:"), 0, fila);
        form.add(txtDocente, 1, fila);
        fila++;
        form.add(new Label("Materia:"), 0, fila);
        form.add(txtMateria, 1, fila);
 
        // Acceso directo al formulario completo de Asignaciones, por si no le
        // alcanza con estos campos (ej. necesita elegir carrera, grupo, número
        // de alumnos, profesor catalogado, etc.).
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
        dialog.getDialogPane().getStyleClass().add("dialog-asignacion");
 
        // Un Dialog abre su propia ventana/Scene: NO hereda el stylesheet del
        // VBox principal de fx_horarios.fxml, hay que cargárselo aparte o
        // se ve con el estilo blanco por default de Windows.
        var hojaEstilos = getClass().getResource("/mx/utng/view/styles_horarios.css");
        if (hojaEstilos != null) {
            dialog.getDialogPane().getStylesheets().add(hojaEstilos.toExternalForm());
        }
 
        dialog.setResultConverter(bt -> {
            if (bt == btnConfirmar) {
                String dia = permitirElegirDiaHora ? cmbDia.getValue() : diaFijo;
                String hora = permitirElegirDiaHora ? cmbHora.getValue() : horaFijo;
                String espacio = cmbEspacioDialogo.getValue();
                String tipoSolicitante = cmbTipoSolicitante.getValue();
                String docente = txtDocente.getText() == null ? "" : txtDocente.getText().trim();
                String materia = txtMateria.getText() == null ? "" : txtMateria.getText().trim();
 
                if (espacio == null || espacio.isBlank()) {
                    mostrarAlerta("Falta el espacio", "Selecciona el espacio que quieres asignar.");
                    return null;
                }
                if (docente.isEmpty()) {
                    mostrarAlerta("Datos incompletos",
                            "Captura el nombre del docente o solicitante para asignar el espacio.");
                    return null;
                }
                return new String[]{dia, hora, espacio, tipoSolicitante, docente, materia};
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
        filtrarComboEspacioPorTipo("Todos los tipos");
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
 