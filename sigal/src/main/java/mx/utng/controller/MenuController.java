package mx.utng.controller;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import mx.utng.util.AvatarUtil;
import mx.utng.util.CerrarSesionDialog;

/**
 * =================================================================
 * MenuController
 * -----------------------------------------------------------------
 * Controlador principal
 *
 * EL controlador es el "shell" permanente de la aplicacion:
 * ycontiene el sidebar, el topbar y todo el Dashboard (tarjetas,
 * calendario, asignaciones, avisos, modal de detalle)
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
    @FXML private VBox sidebarRoot;
    @FXML private Button btnToggleSidebar;
    @FXML private Node iconoToggleSidebar;
    @FXML private ImageView imgLogoSidebar;
    @FXML private Button navInicio;
    @FXML private Button navAsignaciones;
    @FXML private Button navRegistroEspacios;
    @FXML private Button navProfesores;
    @FXML private Button navCatalogoAcademico;
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

    // ---- Menu desplegable de cuenta ----
    @FXML private HBox chipCuenta;
    @FXML private StackPane scrimCuenta;
    @FXML private StackPane panelCuenta;
    @FXML private Label lblCuentaNombre;
    @FXML private Label lblCuentaRol;
    @FXML private Node iconoAvatarChip;
    @FXML private ImageView imgAvatarChip;
    @FXML private Node iconoAvatarPanel;
    @FXML private ImageView imgAvatarPanel;


    //====================================================
    // VARIABLES GLOBALES
    //====================================================

    /** Localizacion usada para formatear fecha/hora en español */
    private static final Locale LOCALE_ES = new Locale("es", "MX");

    /** Formato de hora tipo "20:45" (24 horas) */
    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm", LOCALE_ES);

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

    /** Foto de perfil del usuario en sesión (bytes tal cual vienen de tb_usuario.FotoPerfil). Puede ser null. */
    private byte[] fotoPerfilActual;

    /** true cuando el sidebar esta colapsado (solo iconos) */
    private boolean sidebarColapsado = false;

    /** Ancho del sidebar expandido (con etiquetas de texto) */
    private static final double SIDEBAR_ANCHO_EXPANDIDO = 252.0;

    /** Ancho del sidebar colapsado (solo iconos) */
    private static final double SIDEBAR_ANCHO_COLAPSADO = 76.0;

    /** Tamaño del logo con el sidebar expandido / colapsado */
    private static final double LOGO_TAMANO_EXPANDIDO = 96.0;
    private static final double LOGO_TAMANO_COLAPSADO = 40.0;



//====================================================
// INITIALIZE
//====================================================

    @FXML
    public void initialize() {

        iniciarReloj();
        cargarUsuario();
        configurarEfectosSidebar();
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
     * Actualiza el encabezado del topbar (lblBienvenida / lblRol) segun
     * el modulo que se acaba de cargar en el contentPane.
     *
     * En "fx_inicio" se mantiene el saludo de bienvenida + rol de
     * siempre. En cualquier otro modulo, esos mismos labels se
     * reutilizan para mostrar el titulo de la pantalla y una breve
     * descripcion de que hace, en vez del saludo.
     */
    private void actualizarEncabezado(String nombreFxml) {
        if ("fx_inicio".equals(nombreFxml)) {
            cargarUsuario();
            return;
        }

        String titulo;
        String descripcion;

        switch (nombreFxml) {
            case "fx_asignaciones":
                titulo = "Asignaciones";
                descripcion = "Gestiona las asignaciones de espacios y horarios";
                break;
            case "fx_espacios":
                titulo = "Registro de Espacios";
                descripcion = "Administra los espacios disponibles en el Edificio F";
                break;
            case "fx_profesores":
                titulo = "Profesores";
                descripcion = "Consulta y administra el catálogo de profesores";
                break;
            case "fx_horarios":
                titulo = "Horarios";
                descripcion = "Visualiza y organiza los horarios por espacio";
                break;
            case "fx_catalogo_academico":
                titulo = "Catálogo Académico";
                descripcion = "Administra materias, grupos, carreras y áreas académicas";
                break;
            case "fx_materias":
                titulo = "Materias";
                descripcion = "Catálogo Académico › Materias";
                break;
            case "fx_grupos":
                titulo = "Grupos";
                descripcion = "Catálogo Académico › Grupos";
                break;
            case "fx_carreras":
                titulo = "Carreras";
                descripcion = "Catálogo Académico › Carreras";
                break;
            case "fx_area_academica":
                titulo = "Área Académica";
                descripcion = "Catálogo Académico › Área Académica";
                break;
            case "fx_consultas":
                titulo = "Consultas";
                descripcion = "Consulta el historial de reservaciones";
                break;
            case "fx_reportes":
                titulo = "Reportes";
                descripcion = "Genera reportes de ocupación y uso";
                break;
            case "fx_avisos":
                titulo = "Avisos";
                descripcion = "Publica y revisa avisos e incidencias";
                break;
            case "fx_ajustes":
                titulo = "Ajustes";
                descripcion = "Configura tu perfil, notificaciones y apariencia";
                break;
            case "fx_cuenta":
                titulo = "Cuenta";
                descripcion = "Administra los datos de tu cuenta";
                break;
            case "fx_acerca":
                titulo = "Acerca del Sistema";
                descripcion = "Información sobre el sistema SIGAL";
                break;
            default:
                titulo = "";
                descripcion = "";
        }

        lblBienvenida.setText(titulo);
        lblRol.setText(descripcion);
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

    /**
     * Guarda la foto de perfil (bytes) de la sesión actual y la refleja
     * de inmediato en el chip de la topbar y en el panel desplegable de
     * cuenta. LoginController la llama al iniciar sesión; CuentaController
     * la vuelve a llamar cada vez que el usuario sube una foto nueva, para
     * que no haga falta cerrar sesión para verla reflejada.
     */
    public void setFotoPerfilSesion(byte[] fotoBytes) {
        this.fotoPerfilActual = fotoBytes;
        AvatarUtil.aplicar(imgAvatarChip, iconoAvatarChip, fotoBytes);
        AvatarUtil.aplicar(imgAvatarPanel, iconoAvatarPanel, fotoBytes);
    }

    /** Foto de perfil de la sesión actual (para precargarla en Ajustes/Cuenta sin volver a consultar la BD). */
    public byte[] getFotoPerfilActual() {
        return fotoPerfilActual;
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
            case "navCatalogoAcademico":
                cargarModulo("fx_catalogo_academico");
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
        Button[] botonesNav = obtenerBotonesNav();

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

    /** Lista de los botones de navegacion del sidebar (sin Cerrar Sesion). */
    private Button[] obtenerBotonesNav() {
        return new Button[]{
                navInicio, navAsignaciones, navRegistroEspacios, navProfesores, navCatalogoAcademico,
                navHorarios, navConsultas, navReportes, navAvisos, navAjustes, navCuenta, navAcerca
        };
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
            } else if (controlador instanceof HorarioController horarioController) {
                horarioController.setMenuController(this);
            } else if (controlador instanceof CatalogoAcademicoController catalogoController) {
                catalogoController.setMenuController(this);
            } else if (controlador instanceof MateriasController materiasController) {
                materiasController.setMenuController(this);
            } else if (controlador instanceof GruposController gruposController) {
                gruposController.setMenuController(this);
            } else if (controlador instanceof CarrerasController carrerasController) {
                carrerasController.setMenuController(this);
            } else if (controlador instanceof AreaAcademicaController areaAcademicaController) {
                areaAcademicaController.setMenuController(this);
            } else if (controlador instanceof PlaceholderController placeholderController) {
                placeholderController.configurar(nombreFxml, this);
            }

            contentPane.getChildren().clear();
            contentPane.getChildren().add(vista);

            actualizarEncabezado(nombreFxml);

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
         * cuando el usuario pasa el mouse encima, y les instala un
         * tooltip con el nombre de la pantalla (aparece tras 2 seg de
         * dejar el cursor encima, util sobre todo cuando el sidebar
         * esta colapsado y solo se ven los iconos).
         */
    private void configurarEfectosSidebar() {
        Button[] botonesNav = obtenerBotonesNav();
        Button[] botonesConLogout = java.util.Arrays.copyOf(botonesNav, botonesNav.length + 1);
        botonesConLogout[botonesNav.length] = btnCerrarSesion;

        for (Button boton : botonesConLogout) {
            if (boton == null) continue;

                boton.setOnMouseEntered(evento -> {
                    DropShadow sombra = new DropShadow(10, javafx.scene.paint.Color.rgb(79, 163, 255, 0.35));
                    boton.setEffect(sombra);
            });

            boton.setOnMouseExited(evento -> boton.setEffect(null));

            Tooltip tooltip = new Tooltip(boton.getText());
            tooltip.setShowDelay(Duration.seconds(2));
            tooltip.setHideDelay(Duration.millis(80));
            Tooltip.install(boton, tooltip);
        }
    }

    //====================================================
    // SIDEBAR COLAPSABLE
    //====================================================

    /**
     * Boton "barrita" del sidebar: alterna entre el modo expandido
     * (icono + texto) y el modo colapsado (solo iconos). En ambos
     * modos los botones siguen navegando exactamente igual; lo unico
     * que cambia es que, colapsado, el nombre de la pantalla se ve
     * como tooltip al dejar el cursor encima por 2 segundos.
     */
    @FXML
    private void onToggleSidebar(ActionEvent event) {
        sidebarColapsado = !sidebarColapsado;
        aplicarAnchoSidebar(sidebarColapsado);
        aplicarVisibilidadEtiquetasSidebar(!sidebarColapsado);
        aplicarAlineacionBotonesSidebar(sidebarColapsado);
        aplicarTamanoLogo(sidebarColapsado);
        aplicarRotacionToggle(sidebarColapsado);
    }

    /** Anima el ancho del sidebar hacia el ancho expandido o colapsado. */
    private void aplicarAnchoSidebar(boolean colapsado) {
        double anchoDestino = colapsado ? SIDEBAR_ANCHO_COLAPSADO : SIDEBAR_ANCHO_EXPANDIDO;

        Timeline animacion = new Timeline(
                new KeyFrame(Duration.millis(180),
                        new KeyValue(sidebarRoot.prefWidthProperty(), anchoDestino),
                        new KeyValue(sidebarRoot.minWidthProperty(), anchoDestino),
                        new KeyValue(sidebarRoot.maxWidthProperty(), anchoDestino))
        );
        animacion.play();
    }

    /**
     * Muestra u oculta las etiquetas de texto del sidebar (tagline del
     * logo, nombre de cada modulo y "Cerrar Sesion"), dejando solo los
     * iconos visibles cuando el sidebar esta colapsado. Usa lookupAll
     * por styleClass para no depender de darle fx:id a cada Label.
     */
    private void aplicarVisibilidadEtiquetasSidebar(boolean visible) {
        for (Node nodo : sidebarRoot.lookupAll(".menu-sidebar-tagline")) {
            nodo.setVisible(visible);
            nodo.setManaged(visible);
        }
        for (Node nodo : sidebarRoot.lookupAll(".nav-item-label")) {
            nodo.setVisible(visible);
            nodo.setManaged(visible);
        }
        for (Node nodo : sidebarRoot.lookupAll(".nav-item-logout-label")) {
            nodo.setVisible(visible);
            nodo.setManaged(visible);
        }
    }

    /**
     * Centra el icono dentro de cada boton del sidebar cuando esta
     * colapsado (si no, el icono queda pegado a la izquierda porque
     * el alineamiento por defecto de un Button es CENTER_LEFT). Al
     * expandir, regresa al alineamiento normal para que el icono y
     * la etiqueta de texto queden en fila, pegados a la izquierda.
     */
    private void aplicarAlineacionBotonesSidebar(boolean colapsado) {
        Pos alineacion = colapsado ? Pos.CENTER : Pos.CENTER_LEFT;

        Button[] botonesNav = obtenerBotonesNav();
        for (Button boton : botonesNav) {
            if (boton != null) boton.setAlignment(alineacion);
        }
        if (btnCerrarSesion != null) btnCerrarSesion.setAlignment(alineacion);
    }

    /**
     * Encoge el logo del sidebar cuando esta colapsado para que quepa
     * dentro de los 76px de ancho sin desbordarse hacia el contenido;
     * lo regresa a su tamaño normal al expandir.
     */
    private void aplicarTamanoLogo(boolean colapsado) {
        if (imgLogoSidebar == null) return;
        double tamano = colapsado ? LOGO_TAMANO_COLAPSADO : LOGO_TAMANO_EXPANDIDO;
        imgLogoSidebar.setFitWidth(tamano);
        imgLogoSidebar.setFitHeight(tamano);
    }

    /**
     * Gira la flechita del boton de colapsar 180° para que apunte
     * hacia el lado en el que se puede volver a abrir el sidebar
     * (apunta a la izquierda cuando esta expandido, a la derecha
     * cuando esta colapsado).
     */
    private void aplicarRotacionToggle(boolean colapsado) {
        if (iconoToggleSidebar == null) return;
        RotateTransition giro = new RotateTransition(Duration.millis(180), iconoToggleSidebar);
        giro.setToAngle(colapsado ? 180 : 0);
        giro.play();
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