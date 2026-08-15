package mx.utng.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import mx.utng.dao.AsignacionDAO;
import mx.utng.dao.AvisoDAO;
import mx.utng.dao.EspacioDAO;
import mx.utng.model.Asignaciones;
import mx.utng.model.Aviso;
import mx.utng.model.DetalleItem;
import mx.utng.model.EspacioRegistro;



public class InicioController {


    
    // ---- Tarjetas del dashboard ----
    @FXML private VBox cardEspacios;
    @FXML private VBox cardAsignaciones;
    @FXML private VBox cardDisponibles;
    @FXML private VBox cardAvisos;

    // ---- Modal de detalle ----
    @FXML private StackPane modalOverlay;
    @FXML private Label lblModalTitulo;
    @FXML private Label lblModalSubtitulo;
    @FXML private Button btnCerrarModal;
    @FXML private Button btnAgregarFila;
    @FXML private Button btnEliminarFila;
    @FXML private Button btnGuardarCambios;

    // ---- Tabla del modal ----
    @FXML private TableView<DetalleItem> tablaDetalle;
    @FXML private TableColumn<DetalleItem, String> colNombre;
    @FXML private TableColumn<DetalleItem, String> colTipo;
    @FXML private TableColumn<DetalleItem, String> colCapacidad;
    @FXML private TableColumn<DetalleItem, String> colEstado;


    //====================================================
    // VARIABLES GLOBALES
    //====================================================

     /** Duracion  de las transiciones cortas  */
    private static final Duration DURACION_CORTA = Duration.millis(160);

      /** Duracion  de las transiciones del modal. */
    private static final Duration DURACION_MODAL = Duration.millis(220);

    /** Nombre de usuario actualmente en sesion (aqui esta simulado por ahora) */
    private String usuarioActual = "Usuario";

    /** Rol del usuario actualmente en sesion (tmb simulado por ahora) */
    private String rolActual = "Usuario";

    private final EspacioDAO espacioDAO = new EspacioDAO();
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final AvisoDAO avisoDAO = new AvisoDAO();




    @FXML
    public void initialize() {

        configurarTabla();
        configurarEfectosTarjetas();
        cargarDashboard();
        reproducirEntradaDashboard();

    }


    
    //====================================================
    // DASHBOARD
    //====================================================

    /**
     * Pone la carga completa del Dashboard: usuario, estadisticas,
     * asignaciones y avisos, es el unico metodo que necesitamss
     * volver a llamar si quiero refrescar todo de golpe (por ejemplo
     * despues de guardar cambios en un modulo)
     */
    private void cargarDashboard() {
        cargarEstadisticas();
        cargarAsignaciones();
        cargarAvisos();
    }

  

    /**
     * Actualiza los 4 numeros grandes de las tarjetas del dashboard.
     *
     * Los Labels de esos numeros no tienen fx:id en el FXML actual
     * (estan en Scene Builder), asi que se localizan por su
     * styleClass dentro de cada tarjeta usando lookup() y esto NO
     * requiere modificar el FXML ni agregar fx:id nuevos.
     */
    private void cargarEstadisticas() {
        actualizarNumeroTarjeta(cardEspacios, ".stat-number-blue", espacioDAO.contarTotal());
        actualizarNumeroTarjeta(cardAsignaciones, ".stat-number-purple", asignacionDAO.contarDeHoy());
        actualizarNumeroTarjeta(cardDisponibles, ".stat-number-green", espacioDAO.contarDisponiblesAhora());
        actualizarNumeroTarjeta(cardAvisos, ".stat-number-orange", avisoDAO.contarNoLeidos());
    }

    /**
     * Carga las asignaciones del dia que se muestran en el panel
     * "Asignaciones del dia"
     *
     * Las filas de ese panel (Laboratorio TI-1, Laboratorio WAN, etc.)
     * siguen escritas directo en el FXML porque su contenedor VBox no
     * tiene fx:id todavia, asi que este metodo por ahora no puede
     * reemplazar esas filas dinamicamente sin tocar el diseno del
     * FXML. Los NUMEROS de la tarjeta y el MODAL de detalle
     * ("Asignaciones de hoy") si ya usan datos reales (ver
     * cargarEstadisticas() y onCardAsignaciones()).
     */
    private void cargarAsignaciones() {
        // Pendiente: agregar fx:id="panelAsignaciones" al VBox del
        // panel "Asignaciones del dia" en fx_inicio.fxml para poder
        // reconstruir sus filas aqui con asignacionDAO.listarDeHoy().
    }

    /**
     * Carga los avisos recientes que se muestran en el panel
     * "Avisos Recientes"
     *
     * Mismo caso que cargarAsignaciones(): el VBox de avisos tampoco
     * tiene fx:id en el FXML todavia. Los numeros y el modal de
     * detalle ya usan datos reales.
     */
    private void cargarAvisos() {
        // Pendiente: agregar fx:id="panelAvisos" al VBox del panel
        // "Avisos Recientes" en fx_inicio.fxml para reconstruir sus
        // filas aqui con avisoDAO.listarNoLeidos().
    }

    /**
     * Refresca todo el Dashboard de golpe. Pensado para llamarse
     * despues de que un modulo hijo (por ejemplo, Registro de
     * Espacios) guarde cambios que afecten a las tarjetas o listas
     * del Inicio
     */
    public void actualizarDashboard() {
        cargarDashboard();
    }





    
    //====================================================
    // TARJETAS
    //====================================================

    @FXML
    private void onCardEspacios(MouseEvent event) {
        ObservableList<EspacioRegistro> espacios = espacioDAO.listarTodos();

        ObservableList<DetalleItem> datos = FXCollections.observableArrayList();
        for (EspacioRegistro e : espacios) {
            datos.add(new DetalleItem(e.getNombre(), e.getTipo(), String.valueOf(e.getCapacidad()), e.getEstado()));
        }

        abrirModal("Espacios registrados", espacios.size() + " espacios en total", datos);
    }

    @FXML
    private void onCardAsignaciones(MouseEvent event) {
        ObservableList<Asignaciones> asignaciones = asignacionDAO.listarDeHoy();

        ObservableList<DetalleItem> datos = FXCollections.observableArrayList();
        for (Asignaciones a : asignaciones) {
            datos.add(new DetalleItem(
                    a.getEspacio(),
                    a.getHoraInicio() + " - " + a.getHoraTermino(),
                    a.getNumAlumnos(),
                    a.getEstado()));
        }

        String hoyTexto = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es", "MX")));
        abrirModal("Asignaciones de hoy", hoyTexto + " - " + asignaciones.size() + " asignaciones", datos);
    }

    @FXML
    private void onCardDisponibles(MouseEvent event) {
        ObservableList<EspacioRegistro> disponibles = espacioDAO.listarDisponiblesAhora();

        ObservableList<DetalleItem> datos = FXCollections.observableArrayList();
        for (EspacioRegistro e : disponibles) {
            datos.add(new DetalleItem(e.getNombre(), e.getTipo(), String.valueOf(e.getCapacidad()), e.getEstado()));
        }

        abrirModal("Espacios disponibles ahora", disponibles.size() + " espacios libres en este momento", datos);
    }

    @FXML
    private void onCardAvisos(MouseEvent event) {
        ObservableList<Aviso> avisos = avisoDAO.listarNoLeidos();

        ObservableList<DetalleItem> datos = FXCollections.observableArrayList();
        for (Aviso a : avisos) {
            datos.add(new DetalleItem(a.getDescripcion(), a.getTipoAviso(), "-", a.getEstado()));
        }

        abrirModal("Avisos sin leer", avisos.size() + " avisos pendientes de revisar", datos);
    }

    /**
     * Busca dentro de una tarjeta (por su styleClass, sin necesidad de
     * fx:id) el Label del numero grande y le asigna un nuevo valor
     */
    private void actualizarNumeroTarjeta(VBox tarjeta, String selectorCss, int valor) {
        if (tarjeta == null) return;
        Node nodo = tarjeta.lookup(selectorCss);
       if (nodo instanceof Label) { Label numero = (Label) nodo; numero.setText(String.valueOf(valor));
        }   
    }

    //====================================================
    // MODAL
    //====================================================

    /**
     * Muestra el modal de detalle con el titulo, subtitulo y datos
     * indicados, animando su entrada con fade + escalado
     */
    private void abrirModal(String titulo, String subtitulo, ObservableList<DetalleItem> datos) {
        lblModalTitulo.setText(titulo);
        lblModalSubtitulo.setText(subtitulo);
        tablaDetalle.setItems(datos);

        modalOverlay.setVisible(true);
        modalOverlay.setMouseTransparent(false);

        StackPane tarjetaModal = (StackPane) modalOverlay.getChildren().get(0);
        animarAperturaModal(tarjetaModal);
    }

    @FXML
    private void onCerrarModal(ActionEvent event) {
        StackPane tarjetaModal = (StackPane) modalOverlay.getChildren().get(0);
        animarCierreModal(tarjetaModal);
    }


    //====================================================
    // TABLA
    //====================================================

    /**
     * Deja las 4 columnas de la tabla del modal listas para edicion
     * directa (doble clic en una celda) y conecta los commits de
     * edicion al modelo DetalleItem
     */
    private void configurarTabla() {
        colNombre.setCellFactory(TextFieldTableCell.forTableColumn());
        colTipo.setCellFactory(TextFieldTableCell.forTableColumn());
        colCapacidad.setCellFactory(TextFieldTableCell.forTableColumn());
        colEstado.setCellFactory(TextFieldTableCell.forTableColumn());

        colNombre.setOnEditCommit(e -> e.getRowValue().setNombre(e.getNewValue()));
        colTipo.setOnEditCommit(e -> e.getRowValue().setTipo(e.getNewValue()));
        colCapacidad.setOnEditCommit(e -> e.getRowValue().setCapacidad(e.getNewValue()));
        colEstado.setOnEditCommit(e -> e.getRowValue().setEstado(e.getNewValue()));
    }

    @FXML
    private void onAgregarFila(ActionEvent event) {
        tablaDetalle.getItems().add(new DetalleItem("Nuevo espacio", "Tipo", "0", "Disponible"));
    }

    @FXML
    private void onEliminarFila(ActionEvent event) {
        DetalleItem seleccionado = tablaDetalle.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            tablaDetalle.getItems().remove(seleccionado);
        } else {
            mostrarNotificacion(
                    "Nada que eliminar",
                    "Selecciona primero una fila de la tabla.",
                    Alert.AlertType.INFORMATION);
        }
    }















    //====================================================
    // ANIMACIONES
    //====================================================

    /**
     * Animacion de entrada del modal: aparece con fade-in y un
     * ligero efecto de escalado desde 90% hasta 100%
     */
    private void animarAperturaModal(StackPane tarjetaModal) {
        FadeTransition fade = new FadeTransition(DURACION_MODAL, modalOverlay);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        tarjetaModal.setScaleX(0.9);
        tarjetaModal.setScaleY(0.9);
        ScaleTransition escala = new ScaleTransition(DURACION_MODAL, tarjetaModal);
        escala.setFromX(0.9);
        escala.setFromY(0.9);
        escala.setToX(1.0);
        escala.setToY(1.0);

        ParallelTransition entrada = new ParallelTransition(fade, escala);
        entrada.play();
    }

    /**
     * Animacion de cierre del modal: fade-out y al terminar se oculta
     * por completo (visible=false) para no seguir capturando clics
     */
    private void animarCierreModal(StackPane tarjetaModal) {
        FadeTransition fade = new FadeTransition(DURACION_MODAL, modalOverlay);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(evento -> {
            modalOverlay.setVisible(false);
            modalOverlay.setMouseTransparent(true);
        });
        fade.play();
    }

    /**
     * Animacion de entrada del Dashboard: las 4 tarjetas aparecen una
     * despues de otra (fade + pequeno desplazamiento hacia arriba)
     * dando una sensacion de carga suav
     */
    private void reproducirEntradaDashboard() {
        VBox[] tarjetas = { cardEspacios, cardAsignaciones, cardDisponibles, cardAvisos };

        SequentialTransition secuencia = new SequentialTransition();
        secuencia.setDelay(Duration.millis(80));

        for (VBox tarjeta : tarjetas) {
            if (tarjeta == null) continue;

            tarjeta.setOpacity(0.0);
            tarjeta.setTranslateY(14.0);

            FadeTransition fade = new FadeTransition(Duration.millis(280), tarjeta);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);

            TranslateTransition subida = new TranslateTransition(Duration.millis(280), tarjeta);
            subida.setFromY(14.0);
            subida.setToY(0.0);

            ParallelTransition entradaTarjeta = new ParallelTransition(fade, subida);
            entradaTarjeta.setDelay(Duration.millis(60));

            secuencia.getChildren().add(entradaTarjeta);
        }

        secuencia.play();
    }


    
    /// este metodo de notis si se va a borrar
    /**
     * TODO: aqui conectamos mi  DAO para guardar tablaDetalle.getItems()
     * en tu base de datos real (INSERT/UPDATE/DELETE segun
     * corresponda comparando contra el estado previo)
     */
    @FXML
    private void onGuardarCambios(ActionEvent event) {
        int totalFilas = tablaDetalle.getItems().size();
        System.out.println("Guardar cambios -> " + totalFilas + " filas");

        mostrarNotificacion(
                "Cambios guardados",
                "Se guardaron " + totalFilas + " registros correctamente.",
                Alert.AlertType.INFORMATION);
    }



    //====================================================
    // EFECTOS
    //====================================================

    /**
     * Agrega un ligero efecto de "levantado" a las tarjetas del dashboard.
     */
    private void configurarEfectosTarjetas() {
    VBox[] tarjetas = { cardEspacios, cardAsignaciones, cardDisponibles, cardAvisos };

    for (VBox tarjeta : tarjetas) {
        if (tarjeta == null) continue;
            aplicarEfectoHover(tarjeta);
        }
    }

    private void aplicarEfectoHover(Node nodo) {
        nodo.setOnMouseEntered(evento -> {
            TranslateTransition subida = new TranslateTransition(DURACION_CORTA, nodo);
            subida.setToY(-4.0);

            ScaleTransition escala = new ScaleTransition(DURACION_CORTA, nodo);
            escala.setToX(1.02);
            escala.setToY(1.02);

            new ParallelTransition(subida, escala).play();
        });

        nodo.setOnMouseExited(evento -> {
        TranslateTransition bajada = new TranslateTransition(DURACION_CORTA, nodo);
        bajada.setToY(0.0);

        ScaleTransition escala = new ScaleTransition(DURACION_CORTA, nodo);
        escala.setToX(1.0);
        escala.setToY(1.0);

        new ParallelTransition(bajada, escala).play();
        });
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


}


