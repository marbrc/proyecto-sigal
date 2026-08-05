package mx.utng.controller;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import mx.utng.util.CerrarSesionDialog;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * =================================================================
 * MenuController
 * -----------------------------------------------------------------
 * Controlador principal
 *
 * EL controlador es el "shell" permanente de la aplicacion:
 * ycontiene el sidebar, el topbar y todo el Dashboard (tarjetas,
 * calendario, reservaciones, avisos, modal de detalle)
 * =================================================================
 */
public class MenuController {

    //====================================================
    // COMPONENTES FXML
    //====================================================

    // ---- Layout general ----
    @FXML private javafx.scene.layout.BorderPane mainLayout;

    // ---- Panel central para cargar modulos (ver nota de la clase y td eso) ----
    // Si FXML todavia no tiene este AnchorPane en este campo
    //  queda en null y cargarModulo() lo detecta y avisa
    @FXML private StackPane contentPane;

    // ---- Sidebar: navegacion ----
    @FXML private Button navInicio;
    @FXML private Button navAsignaciones;
    @FXML private Button navRegistroEspacios;
    @FXML private Button navProfesores;
    @FXML private Button navHorarios;
    @FXML private Button navConsultas;
    @FXML private Button navReportes;
    @FXML private Button navAvisos;
    @FXML private Button navAjustes;
    @FXML private Button navCuenta;
    @FXML private Button navAcerca;
    @FXML private Button btnCerrarSesion;

    // ---- Topbar ----
    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private Label lblFechaLarga;
    @FXML private Label lblDiaSemana;
    @FXML private Label lblHora;
    @FXML private Label lblTemp;
    @FXML private Label lblClima;

    // ---- Menu desplegable de cuenta ----
    @FXML private HBox chipCuenta;
    @FXML private StackPane scrimCuenta;
    @FXML private StackPane panelCuenta;
    @FXML private Label lblCuentaNombre;
    @FXML private Label lblCuentaRol;


    //====================================================
    // VARIABLES GLOBALES
    //====================================================

    /** Localizacion usada para formatear fecha/hora en español */
    private static final Locale LOCALE_ES = new Locale("es", "MX");

    /** Formato de hora tipo "08:45 PM */
    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("hh:mm a", LOCALE_ES);

    /** Ruta base donde viven todas las vistas FXML */
    private static final String RUTA_VISTAS = "/mx/utng/view/";


    /** Timeline del reloj en vivo time reeeal */
    private Timeline timelineReloj;

    /** Nombre de usuario actualmente en sesion (simulado por ahora) */
    private String usuarioActual = "Usuario";

    /** Rol del usuario actualmente en sesion (simulado por ahora) */
    private String rolActual = "Usuario";

    /** ID_Usuario real (de tb_usuario) del usuario en sesion, para guardar asignaciones/avisos */
    private int idUsuarioActual = 0;

    /** Correo electrónico del usuario en sesion (para la tarjeta de Ajustes) */
    private String correoActual = "";

    /** Momento en el que inició esta sesión (para mostrar "último acceso" en Ajustes) */
    private java.time.LocalDateTime horaInicioSesion;



//====================================================
// INITIALIZE
//====================================================

    @FXML
    public void initialize() {

        iniciarReloj();
        cargarClima();
        cargarUsuario();
        cargarModulo("fx_inicio");

    }


      /**
     * Carga los datos del usuario en sesion (nombre y rol) en el topbar
     *
     * TODO: sustituir usuarioActual/rolActual por los datos reales que
     * vengan de la sesion/login (por ejemplo, un objeto Usuario que se
     * le pase a este controller al hacer el cambio de escena desde
     * LoginController)
     */
    private void cargarUsuario() {
        lblBienvenida.setText("¡Bienvenido(a), " + usuarioActual + "!");
        lblRol.setText("Rol: " + rolActual);
        if (lblCuentaNombre != null) lblCuentaNombre.setText(usuarioActual);
        if (lblCuentaRol != null) lblCuentaRol.setText("Rol: " + rolActual);
    }

    /**
     * Recibe los datos del usuario autenticado desde LoginController
     * y actualiza la información mostrada en la barra superior.
     */
    public void setUsuarioActual(int idUsuario, String nombreUsuario, String rol) {
        this.idUsuarioActual = idUsuario;
        this.usuarioActual = nombreUsuario;
        this.rolActual = rol;
        this.horaInicioSesion = LocalDateTime.now();
        cargarUsuario();
    }

    /**
     * Momento en el que inició esta sesión. SIGAL todavía no guarda un
     * "último acceso" histórico en la base de datos, así que esto
     * representa el inicio de la sesión actual.
     */
    public java.time.LocalDateTime getHoraInicioSesion() {
        return horaInicioSesion;
    }

    /**
     * Datos adicionales de la sesión que setUsuarioActual(...) no carga
     * (correo y hora de acceso). LoginController la llama justo después
     * de setUsuarioActual(...) al iniciar sesión.
     */
    public void setSesionExtra(String correo, java.time.LocalDateTime horaAcceso) {
        this.correoActual = correo;
        this.horaInicioSesion = horaAcceso;
    }

    /** Nombre del usuario en sesión (para la tarjeta "Perfil de usuario" de Ajustes). */
    public String getUsuarioActual() {
        return usuarioActual;
    }

    /** Rol del usuario en sesión (para la tarjeta "Perfil de usuario" de Ajustes). */
    public String getRolActual() {
        return rolActual;
    }

    /** Correo del usuario en sesión (para la tarjeta "Perfil de usuario" de Ajustes). */
    public String getCorreoActual() {
        return correoActual;
    }

    /**
     * Hora de acceso ya formateada como texto, lista para mostrarse en
     * la tarjeta "Perfil de usuario" de Ajustes.
     */
    public String getHoraAccesoTexto() {
        if (horaInicioSesion == null) {
            return "—";
        }
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
        return horaInicioSesion.format(formato);
    }

    /**
     * ID_Usuario real de tb_usuario para el usuario en sesion. Lo usan
     * los módulos hijos (por ejemplo AsignacionesController) para saber
     * quién está guardando una asignación o un aviso.
     */
    public int getIdUsuarioActual() {
        return idUsuarioActual;
    }

    /**
     * Refresca el nombre mostrado en el topbar y en el menú de cuenta
     * cuando el usuario lo cambia desde la pantalla "Mi cuenta", sin
     * necesidad de volver a iniciar sesión.
     */
    public void actualizarNombreUsuarioEnSesion(String nuevoNombre) {
        this.usuarioActual = nuevoNombre;
        cargarUsuario();
    }




    //====================================================
    // FECHA Y HORA
    //====================================================

    /**
     * Arranca el Timeline que mantiene la hora y la fecha del topbar
     * siempre tiene que estar actualizadas, sin congelar la interfaz
     */
    private void iniciarReloj() {
        actualizarFechaHora();

        timelineReloj = new Timeline(
                new KeyFrame(Duration.seconds(1), evento -> actualizarFechaHora())
        );
        timelineReloj.setCycleCount(Timeline.INDEFINITE);
        timelineReloj.play();
    }

    /**
     * Refresca los labels de hora, fecha larga y dia de la semana
     * Se llama una vez por segundo desde el Timeline del reloj
     */
    private void actualizarFechaHora() {
        LocalDateTime ahora = LocalDateTime.now();

        lblHora.setText(ahora.format(FORMATO_HORA));

        String diaSemana = capitalizar(
                ahora.getDayOfWeek().getDisplayName(TextStyle.FULL, LOCALE_ES));
        String mes = capitalizar(
                ahora.getMonth().getDisplayName(TextStyle.FULL, LOCALE_ES));
        String fechaLarga = String.format("%d de %s de %d",
                ahora.getDayOfMonth(), mes, ahora.getYear());

        lblFechaLarga.setText(fechaLarga);
        lblDiaSemana.setText(diaSemana);
    }


    //====================================================
    // CLIMA
    //====================================================

    /**
     * Carga la informacion del clima en el topbar
     *
     * TODO: reemplazar estos valores fijos por una llamada real a una
     * API de clima (ej. OpenWeatherMap) cuando tenga la API key
     * La firma del metodo ya no tendria que cambiar solo su interior
     */
    private void cargarClima() {
        lblTemp.setText("24°C");
        lblClima.setText("Parcialmente nublado");
    }




    //====================================================
    // NAVEGACION
    //====================================================

    /**
     * Accion del boton "Inicio". Como el Dashboard ya es la pantalla
     * de Inicio, aqui solo refrescamos los datos y marcamos el boton
     * como activo (por si el usuario venia de otro modulo)
     */
    @FXML
    private void onNavInicio(ActionEvent event) {

        marcarNavActivo(navInicio);

        cargarModulo("fx_inicio");

    }

    /**
     * Manejador unico para el resto de los botones del sidebar
     * (Asignaciones, Registro de Espacios, Profesores, Horarios,
     * Consultas, Reportes, Avisos, Ajustes, Acerca del Sistema)
     *
     * Todos estos botones apuntan a este mismo metodo en el FXML
     * (onAction="#onNavGenerico"), asi que aqui identificamos cual
     * fue presionado usando su id (fx:id tambien asigna el id del
     * nodo automaticamente) y decidimos que modulo cargar.
     */
    @FXML
    private void onNavGenerico(ActionEvent event) {
        Button origen = (Button) event.getSource();
        marcarNavActivo(origen);

        String idBoton = origen.getId();
        if (idBoton == null) {
            System.out.println("Boton de navegacion sin id: " + origen.getText());
            return;
        }

        switch (idBoton) {
            case "navAsignaciones":
                cargarModulo("fx_asignaciones");
                break;
            case "navRegistroEspacios":
                cargarModulo("fx_espacios");
                break;
            case "navProfesores":
                cargarModulo("fx_profesores");
                break;
            case "navHorarios":
                cargarModulo("fx_horarios");
                break;
            case "navConsultas":
                cargarModulo("fx_consultas");
                break;
            case "navReportes":
                cargarModulo("fx_reportes");
                break;
            case "navAvisos":
                cargarModulo("fx_avisos");
                break;
            case "navAjustes":
                cargarModulo("fx_ajustes");
                break;
            case "navCuenta":
                cargarModulo("fx_cuenta");
                break;
            case "navAcerca":
                cargarModulo("fx_acerca");
                break;
            default:
                System.out.println("Navegacion no reconocida: " + idBoton);

                 
        }
    }

    /**
     * Quitaré la clase "nav-item-active" de todos los botones del
     * sidebar y se la asigna unicamente al boton que se acaba de
     * presionar, para que el resaltado visual siga siempre al
     * modulo actual
     */
    private void marcarNavActivo(Button botonActivo) {
        Button[] botonesNav = {
                navInicio, navAsignaciones, navRegistroEspacios, navProfesores,
                navHorarios, navConsultas, navReportes, navAvisos, navAjustes, navCuenta, navAcerca
        };

        for (Button boton : botonesNav) {
            if (boton == null) continue;
            boton.getStyleClass().remove("nav-item-active");
            if (!boton.getStyleClass().contains("nav-item")) {
                boton.getStyleClass().add("nav-item");
            }
        }

        botonActivo.getStyleClass().remove("nav-item");
        if (!botonActivo.getStyleClass().contains("nav-item-active")) {
            botonActivo.getStyleClass().add("nav-item-active");
        }
    }

    //====================================================
    // METODOS AUXILIARES
    //====================================================


    /**
     * Muestra una notificacion simple al usuario mediante un Alert
     * nativo de JavaFX
     *
     * Por ahorita es la forma mas rapida y confiable de avisar algo sin
     * modificar el FXML (no depende de ningun nodo adicional) Si mas
     * adelante se pone un "toast" visual dentro del propio Dashboard
     * se puede reemplazar el interior de este metodo sin cambiar su
     * firma en el resto del controller
     */
    private void mostrarNotificacion(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }


    //====================================================
    // CARGA DE MODULOS
    //====================================================

    /**
     * Carga un modulo (FXML) dentro del panel central de la
     * aplicacion, sin recargar ni el sidebar ni el topbar
     *
     * Requiere que el FXML de esta pantalla tenga un AnchorPane con
     * fx:id="panelContenido" en la zona donde quieres que aparezcan
     * los modulos. Si todavia no lo agregé, este metodo lo detecta
     * y solo te avisa por consola -- no rompe el códgo
     *
     * @param nombreFxml nombre del archivo, sin extension ni ruta,
     *                    por ejemplo "fx_asignaciones". Se busca
     *                    automaticamente dentro de RUTA_VISTAS
     */
    /**
     * Version publica de cargarModulo(), para que otros controladores
     * (por ejemplo AsignacionesController, cuando el usuario da clic en
     * "Ver más →") puedan pedirle al menú que cambie de pantalla.
     */
    public void abrirModulo(String nombreFxml) {
        cargarModulo(nombreFxml);
    }

    private void cargarModulo(String nombreFxml) {
        if (contentPane == null) {            System.out.println(
                    "[SIGAL] No se pudo cargar el modulo '" + nombreFxml + "': "
                            + "No se encontró el StackPane con "
                            + "fx:id=\"contentPane\".");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(RUTA_VISTAS + nombreFxml + ".fxml"));

            Parent vista = loader.load();

            // Si el controlador de la pantalla que se acaba de cargar necesita
            // regresar a este menú (por ejemplo para abrir otro módulo desde
            // un link interno), le pasamos la referencia.
            Object controlador = loader.getController();
            if (controlador instanceof AsignacionesController asignacionesController) {
                asignacionesController.setMenuController(this);
            } else if (controlador instanceof DisponibilidadController disponibilidadController) {
                disponibilidadController.setMenuController(this);
            } else if (controlador instanceof CuentaController cuentaController) {
                cuentaController.setMenuController(this);
            } else if (controlador instanceof AjustesController ajustesController) {
                ajustesController.setMenuController(this);
            }

            contentPane.getChildren().clear();
            contentPane.getChildren().add(vista);
           

        } catch (IOException e) {
            System.err.println("[SIGAL] Error al cargar el modulo '" + nombreFxml + "':");
            e.printStackTrace();
            mostrarNotificacion(
                    "No se pudo abrir esa seccion",
                    "Ocurrio un problema cargando '" + nombreFxml + ".fxml'. "
                            + "Revisa que el archivo exista en " + RUTA_VISTAS,
                    Alert.AlertType.ERROR);
        }
    }

    private void abrirVentana(String nombreFxml) {
    try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(RUTA_VISTAS + nombreFxml + ".fxml"));

         Parent root = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}








    

        //====================================================
        // EFECTOS
        //====================================================

        /**
         * Le da un pequeño realce de sombra a los botones del sidebar
         * cuando el usuario pasa el mouse encima.
         */
    private void configurarEfectosSidebar() {
        Button[] botonesNav = {
            navInicio, navAsignaciones, navRegistroEspacios, navProfesores,
            navHorarios, navConsultas, navReportes, navAvisos, navAjustes, navCuenta, navAcerca
        };

        for (Button boton : botonesNav) {
            if (boton == null) continue;

                boton.setOnMouseEntered(evento -> {
                    DropShadow sombra = new DropShadow(10, javafx.scene.paint.Color.rgb(79, 163, 255, 0.35));
                    boton.setEffect(sombra);
            });

            boton.setOnMouseExited(evento -> boton.setEffect(null));
        }
    }

    //====================================================
    // MENU DESPLEGABLE DE CUENTA
    //====================================================

    /**
     * Abre/cierra el menú de cuenta (nombre, rol, ajustes, cerrar sesión)
     * que cuelga del chip del avatar en el topbar. Vive aquí, en el
     * shell del menú, así que se puede abrir desde cualquier pantalla.
     */
    @FXML
    private void onToggleMenuCuenta(MouseEvent event) {
        if (panelCuenta.isVisible()) {
            cerrarMenuCuenta();
        } else {
            abrirMenuCuenta();
        }
    }

    private void abrirMenuCuenta() {
        panelCuenta.setVisible(true);
        panelCuenta.setMouseTransparent(false);
        panelCuenta.setOpacity(1.0);

        scrimCuenta.setVisible(true);
        scrimCuenta.setMouseTransparent(false);
    }

    private void cerrarMenuCuenta() {
        panelCuenta.setVisible(false);
        panelCuenta.setMouseTransparent(true);
        panelCuenta.setOpacity(0.0);

        scrimCuenta.setVisible(false);
        scrimCuenta.setMouseTransparent(true);
    }

    /** Clic fuera del menú de cuenta (en el overlay invisible): lo cierra. */
    @FXML
    private void onCerrarMenuCuenta(MouseEvent event) {
        cerrarMenuCuenta();
    }

    @FXML
    private void onIrCuenta(ActionEvent event) {
        cerrarMenuCuenta();
        marcarNavActivo(navCuenta);
        abrirModulo("fx_cuenta");
    }

    /**
     * "Cambiar de cuenta" desde el menú desplegable: cierra la sesión
     * actual y regresa a la pantalla de login para que otro usuario
     * pueda entrar, igual que "Cerrar sesión" pero pensado para el
     * caso de compartir el equipo entre varios usuarios.
     */
    @FXML
    private void onCambiarCuenta(ActionEvent event) {
        cerrarMenuCuenta();
        onCerrarSesion(event);
    }

    @FXML
    private void onIrAjustes(ActionEvent event) {
        cerrarMenuCuenta();
        marcarNavActivo(navAjustes);
        abrirModulo("fx_ajustes");
    }

    @FXML
    private void onIrAcerca(ActionEvent event) {
        cerrarMenuCuenta();
        marcarNavActivo(navAcerca);
        abrirModulo("fx_acerca");
    }

    /** "Cerrar sesión" desde el menú de cuenta: reusa la misma lógica del botón del sidebar. */
    @FXML
    private void onCerrarSesionDesdeMenu(ActionEvent event) {
        cerrarMenuCuenta();
        onCerrarSesion(event);
    }

    //====================================================
    // CERRAR SESION
    //====================================================

    /**
     * Cierra la sesion actual y regresa a la pantalla de login
     * reemplazando por completo la escena de la ventana (no solo el
     * panel central), tal como corresponde a un logout real
     *
     * TODO: cuando tengamos el  DAO de sesion, aqui tambien deberia
     * invalidar el token/sesion activa antes de cambiar de pantalla
     */
    @FXML
    private void onCerrarSesion(ActionEvent event) {
        javafx.stage.Stage stage =
                (javafx.stage.Stage) btnCerrarSesion.getScene().getWindow();

        CerrarSesionDialog.mostrar(stage, () -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(RUTA_VISTAS + "fx_login.fxml"));

                Parent raizLogin = loader.load();

                stage.setScene(new javafx.scene.Scene(raizLogin));
                stage.setTitle("SIGAL - Iniciar sesion");
                stage.centerOnScreen();
                stage.show();

                detenerReloj();

            } catch (IOException e) {
                System.err.println("[SIGAL] Error al cerrar sesion / cargar fx_login.fxml:");
                e.printStackTrace();
                mostrarNotificacion(
                        "No se pudo cerrar sesion",
                        "No se encontro " + RUTA_VISTAS + "fx_login.fxml",
                        Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Detiene el Timeline del reloj para no dejarlo corriendo en
     * segundo plano despues de salir del Dashboard
     */
    private void detenerReloj() {
        if (timelineReloj != null) {
            timelineReloj.stop();
        }
    }


    //====================================================
    // METODOS AUXILIARES
    //====================================================

    /**
     * Pone en mayuscula la primera letra de un texto (los nombres de
     * dia/mes en español que da java.time vienen en minusculas)
     */
    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase(LOCALE_ES) + texto.substring(1);
    }

    

    
}
 