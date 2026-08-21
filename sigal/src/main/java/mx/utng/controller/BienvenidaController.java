package mx.utng.controller;
 
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
 
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import mx.utng.dao.AsignacionDAO;
import mx.utng.dao.EspacioDAO;
import mx.utng.model.Asignaciones;
 
/**
 * ============================================================
 * SIGAL
 * Sistema Integral de Gestión y Asignación de Laboratorios
 *
 * Controlador de la pantalla de Bienvenida
 * ============================================================
 */
public class BienvenidaController implements Initializable {
 
    // =========================================================
    // COMPONENTES DEL FXML
    // =========================================================
 
    @FXML
    private BorderPane rootLayout;
 
    @FXML
    private Rectangle overlay;
 
    @FXML
    private ImageView imgBackground;
 
    @FXML
    private ImageView imgLogo;
 
    @FXML
    private Label lblHora;
 
    @FXML
    private Label lblFecha;
 
    @FXML
    private Label lblSaludo;
 
    @FXML
    private Button btnIniciarSesion;
 
    @FXML
    private Button btnRegistrarse;
 
    // ---- Calendario ----
    @FXML
    private Label lblMesCalendario;
 
    @FXML
    private Button btnMesAnterior;
 
    @FXML
    private Button btnMesSiguiente;
 
    @FXML
    private VBox calGridDias;
 
    // ---- Próximas asignaciones / Laboratorios disponibles ----
    @FXML
    private VBox panelProximasAsignaciones;
 
    @FXML
    private Label lblLabsDisponibles;
 
    // =========================================================
    // VARIABLES
    // =========================================================
 
    private Timeline reloj;
 
    private static final Locale LOCALE =
            new Locale("es", "MX");
 
    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("hh:mm:ss a");
 
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern(
                    "EEEE, dd 'de' MMMM yyyy",
                    LOCALE);
 
    private static final DateTimeFormatter FORMATO_MES =
            DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE);
 
    private final String[] fondos = {
        "/mx/utng/view/assets/background.jpg",
        "/mx/utng/view/assets/background1.png",
        "/mx/utng/view/assets/background2.png",
        "/mx/utng/view/assets/background3.png"
    };
 
    private int fondoActual = 0;
 
    /** Acceso a datos reales para el calendario y los paneles laterales. */
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final EspacioDAO espacioDAO = new EspacioDAO();
 
    /** Mes que se está mostrando actualmente en el mini-calendario. */
    private YearMonth mesActual = YearMonth.now();
 
 
    // =========================================================
    // MÉTODO PRINCIPAL
    // =========================================================
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
 
        iniciarReloj();
 
        configurarSaludo();
 
        iniciarAnimaciones();
 
        iniciarCarrusel();
 
        construirCalendario();
 
        cargarProximasAsignaciones();
 
        cargarLaboratoriosDisponibles();
 
        // Hace que el contenido se adapte al tamaño de la ventana
        imgBackground.fitWidthProperty().bind(rootLayout.widthProperty());
        imgBackground.fitHeightProperty().bind(rootLayout.heightProperty());
 
        overlay.widthProperty().bind(rootLayout.widthProperty());
        overlay.heightProperty().bind(rootLayout.heightProperty());
    }
 
    // =========================================================
    // RELOJ
    // =========================================================
 
    /**
     * Inicia el reloj en tiempo real.
     */
    private void iniciarReloj() {
 
        actualizarFechaHora();
 
        reloj = new Timeline(
 
                new KeyFrame(
                        Duration.seconds(1),
                        e -> actualizarFechaHora()
                )
        );
 
        reloj.setCycleCount(Timeline.INDEFINITE);
 
        reloj.play();
    }
 
    /**
     * Actualiza la fecha y hora.
     */
    private void actualizarFechaHora() {
 
        LocalTime hora = LocalTime.now();
 
        lblHora.setText(
                hora.format(FORMATO_HORA)
        );
 
        LocalDate fecha = LocalDate.now();
 
        lblFecha.setText(
                capitalize(
                        fecha.format(FORMATO_FECHA)
                )
        );
    }
 
    // =========================================================
    // SALUDO
    // =========================================================
 
    /**
     * Cambia el saludo dependiendo de la hora.
     */
    private void configurarSaludo() {
 
        int hora = LocalTime.now().getHour();
 
        if (hora >= 6 && hora < 12) {
 
            lblSaludo.setText("🌞 ¡Buenos días!");
 
        } else if (hora >= 12 && hora < 19) {
 
            lblSaludo.setText("🌤 ¡Buenas tardes!");
 
        } else {
 
            lblSaludo.setText("🌙 ¡Buenas noches!");
        }
    }
 
    // =========================================================
    // CALENDARIO (conectado a datos reales)
    // =========================================================
 
    @FXML
    private void onMesAnterior(ActionEvent event) {
        mesActual = mesActual.minusMonths(1);
        construirCalendario();
    }
 
    @FXML
    private void onMesSiguiente(ActionEvent event) {
        mesActual = mesActual.plusMonths(1);
        construirCalendario();
    }
 
    /**
     * Reconstruye la cuadrícula de días del mes visible, marcando el
     * día de hoy y con un punto los días que ya tienen asignaciones
     * activas (datos reales de tb_asignacion).
     */
    private void construirCalendario() {
        if (calGridDias == null) return;
 
        lblMesCalendario.setText(capitalize(mesActual.format(FORMATO_MES)));
 
        Set<Integer> diasConAsignaciones = asignacionDAO.diasConAsignacionesEnMes(mesActual);
        LocalDate hoy = LocalDate.now();
 
        LocalDate primerDiaMes = mesActual.atDay(1);
        int retroceso = primerDiaMes.getDayOfWeek().getValue() - 1;
        LocalDate cursor = primerDiaMes.minusDays(retroceso);
 
        VBox nuevoContenido = new VBox(6.0);
 
        for (int fila = 0; fila < 6; fila++) {
            HBox filaSemana = new HBox(2.0);
            for (int col = 0; col < 7; col++) {
                LocalDate fecha = cursor;
                boolean delMesActual = YearMonth.from(fecha).equals(mesActual);
                boolean esHoy = fecha.equals(hoy);
                boolean tieneAsignaciones = delMesActual && diasConAsignaciones.contains(fecha.getDayOfMonth());
 
                filaSemana.getChildren().add(crearCeldaDia(fecha, delMesActual, esHoy, tieneAsignaciones));
                cursor = cursor.plusDays(1);
            }
            nuevoContenido.getChildren().add(filaSemana);
        }
 
        FadeTransition salida = new FadeTransition(Duration.millis(180), calGridDias);
        salida.setFromValue(1.0);
        salida.setToValue(0.0);
        salida.setOnFinished(evento -> {
            calGridDias.getChildren().setAll(nuevoContenido.getChildren());
            FadeTransition entrada = new FadeTransition(Duration.millis(180), calGridDias);
            entrada.setFromValue(0.0);
            entrada.setToValue(1.0);
            entrada.play();
        });
        salida.play();
    }
 
    private VBox crearCeldaDia(LocalDate fecha, boolean delMesActual, boolean esHoy, boolean tieneAsignaciones) {
        Label numero = new Label(String.valueOf(fecha.getDayOfMonth()));
        numero.setMaxWidth(Double.MAX_VALUE);
        numero.setAlignment(Pos.CENTER);
 
        if (!delMesActual) {
            numero.getStyleClass().add("cal-day-muted");
        } else if (esHoy) {
            numero.getStyleClass().add("cal-day-today");
        } else {
            numero.getStyleClass().add("cal-day");
        }
 
        Circle punto = new Circle(2.6);
        if (tieneAsignaciones && delMesActual) {
            punto.getStyleClass().add("cal-dot");
        } else {
            punto.setOpacity(0.0);
        }
 
        VBox celda = new VBox(3.0, numero, punto);
        celda.setAlignment(Pos.CENTER);
        HBox.setHgrow(celda, Priority.ALWAYS);
 
        return celda;
    }
 
    // =========================================================
    // PRÓXIMAS ASIGNACIONES (panel lateral, datos reales)
    // =========================================================
 
    private void cargarProximasAsignaciones() {
        if (panelProximasAsignaciones == null) return;
 
        ObservableList<Asignaciones> proximas = asignacionDAO.listarProximas(4);
 
        panelProximasAsignaciones.getChildren().clear();
 
        if (proximas.isEmpty()) {
            Label vacio = new Label("No hay próximas asignaciones.");
            vacio.getStyleClass().add("placeholder-text");
            panelProximasAsignaciones.getChildren().add(vacio);
            return;
        }
 
        for (Asignaciones a : proximas) {
            panelProximasAsignaciones.getChildren().add(crearFilaAsignacion(a));
        }
    }
 
    private VBox crearFilaAsignacion(Asignaciones a) {
        boolean ocupado = a.getEstado() != null && a.getEstado().toLowerCase().contains("ocup");
 
        VBox fila = new VBox(2.0);
        fila.getStyleClass().add("asignacion-row");
 
        Label nombre = new Label(a.getEspacio());
        nombre.getStyleClass().add("asignacion-name");
 
        HBox info = new HBox(8.0);
        info.setAlignment(Pos.CENTER_LEFT);
 
        Label horario = new Label(a.getHoraInicio() + " - " + a.getHoraTermino());
        horario.getStyleClass().add("asignacion-time");
 
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);
 
        Label badge = new Label(a.getEstado());
        badge.getStyleClass().add(ocupado ? "badge-pending" : "badge-confirmed");
 
        info.getChildren().addAll(horario, espaciador, badge);
        fila.getChildren().addAll(nombre, info);
 
        fila.setOpacity(0.0);
        FadeTransition entrada = new FadeTransition(Duration.millis(220), fila);
        entrada.setFromValue(0.0);
        entrada.setToValue(1.0);
        entrada.play();
 
        return fila;
    }
 
    // =========================================================
    // LABORATORIOS DISPONIBLES (dato real)
    // =========================================================
 
    private void cargarLaboratoriosDisponibles() {
        if (lblLabsDisponibles == null) return;
 
        int disponibles = espacioDAO.contarDisponiblesAhora();
        lblLabsDisponibles.setText(disponibles + " Laboratorios disponibles");
    }
 
    // =========================================================
    // ANIMACIONES
    // =========================================================
 
    /**
     * Inicializa las animaciones de la pantalla.
     */
    private void iniciarAnimaciones() {
 
        // -----------------------------
        // Animación del logo
        // -----------------------------
 
        FadeTransition fadeLogo = new FadeTransition(
                Duration.seconds(1.5),
                imgLogo);
 
        fadeLogo.setFromValue(0);
 
        fadeLogo.setToValue(1);
 
        fadeLogo.play();
 
        ScaleTransition scaleLogo = new ScaleTransition(
                Duration.seconds(2),
                imgLogo);
 
        scaleLogo.setFromX(0.90);
 
        scaleLogo.setFromY(0.90);
 
        scaleLogo.setToX(1);
 
        scaleLogo.setToY(1);
 
        scaleLogo.play();
 
        // -----------------------------
        // Botón Iniciar Sesión
        // -----------------------------
 
        FadeTransition fadeLogin = new FadeTransition(
                Duration.seconds(1.6),
                btnIniciarSesion);
 
        fadeLogin.setFromValue(0);
 
        fadeLogin.setToValue(1);
 
        fadeLogin.play();
 
        // -----------------------------
        // Botón Registrarse
        // -----------------------------
 
        FadeTransition fadeRegistro = new FadeTransition(
                Duration.seconds(1.8),
                btnRegistrarse);
 
        fadeRegistro.setFromValue(0);
 
        fadeRegistro.setToValue(1);
 
        fadeRegistro.play();
 
    }
 
    // =========================================================
    // BOTONES
    // =========================================================
 
    /**
     * Evento del botón Iniciar Sesión
     */
    @FXML
    private void iniciarSesion(ActionEvent event) {
 
        try {
 
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/mx/utng/view/fx_login.fxml"));
 
        Parent root = loader.load();
 
        Stage stage = (Stage) btnIniciarSesion.getScene().getWindow();
 
        Scene scene = new Scene(root);
 
        stage.setScene(scene);
        stage.setTitle("SIGAL - Inicio de sesión");
        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.show();
 
    } catch (IOException e) {
 
        e.printStackTrace();
 
    }
 
}
 
    /**
     * Evento del botón Registrarse
     */
    @FXML
    private void registrarse(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/mx/utng/view/fx_registro.fxml"));
 
        Parent root = loader.load();
 
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                .getScene()
                .getWindow();
 
        stage.setScene(new Scene(root));
        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.show();
 
    } catch (IOException e) {
        e.printStackTrace();
    }
 
 
}
 
    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================
 
 
    private String capitalize(String texto) {
 
        if (texto == null || texto.isEmpty()) {
 
            return texto;
 
        }
 
        return texto.substring(0, 1).toUpperCase()
                + texto.substring(1);
 
    }
 
 
 
    private void iniciarCarrusel() {
 
    Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(6), e -> cambiarFondo())
    );
 
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
    }
 
private void cambiarFondo() {
 
    FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), imgBackground);
    fadeOut.setFromValue(1);
    fadeOut.setToValue(0);
 
    fadeOut.setOnFinished(event -> {
 
        fondoActual++;
 
        if (fondoActual >= fondos.length) {
            fondoActual = 0;
        }
 
        imgBackground.setImage(new Image(getClass().getResourceAsStream(fondos[fondoActual])));
 
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), imgBackground);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
 
    });
 
    fadeOut.play();
}
 
}
 