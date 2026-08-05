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
import mx.utng.model.DetalleItem;



public class InicioController {


    
    // ---- Tarjetas del dashboard ----
    @FXML private VBox cardEspacios;
    @FXML private VBox cardReservaciones;
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
        cargarReservaciones();
        cargarAvisos();
    }

  

    /**
     * Actualiza los 4 numeros grandes de las tarjetas del dashboard.
     *
     * Los Labels de esos numeros no tienen fx:id en el FXML actual
     * (estan en Scene Builder), asi que se localizan por su
     * styleClass dentro de cada tarjeta usando lookup() y esto NO
     * requiere modificar el FXML ni agregar fx:id nuevos.
     *
     * TODO: reemplazar los valores fijos (24, 12, 8, 3) por el
     * resultado real de las consultas SQL cuando conecte MySQL, por
     * ejemplo:
     *   int totalEspacios = espacioDAO.contarEspacios();
     *   actualizarNumeroTarjeta(cardEspacios, ".stat-number-blue", totalEspacios);
     */
    private void cargarEstadisticas() {
        actualizarNumeroTarjeta(cardEspacios, ".stat-number-blue", 24);
        actualizarNumeroTarjeta(cardReservaciones, ".stat-number-purple", 12);
        actualizarNumeroTarjeta(cardDisponibles, ".stat-number-green", 8);
        actualizarNumeroTarjeta(cardAvisos, ".stat-number-orange", 3);
    }

    /**
     * Carga las reservaciones del dia que se muestran en el panel
     * "Asignaciones del dia"
     *
     * las filas de ese panel (Laboratorio TI-1,
     * Laboratorio WAN, Sala Audiovisual) estan escritas directo en
     * el FXML y su contenedor VBox no tiene fx:id, asi que por ahora
     * este metodo no puede reemplazar esas filas dinamicamente sin
     * tocar el diseno. Cuando quiera que esta lista sea 100% dinamica,
     * agregaré fx:id="panelReservaciones" al VBox que las contiene y
     * aqui se conecta la reconstruccion real de las filas a partir de
     * la base de datos
     */
    private void cargarReservaciones() {
        // Simulacion de lo que vendria de la base de datos:
        // List<Reservacion> reservacionesHoy = reservacionDAO.obtenerDelDia(LocalDate.now());
        // (pendiente de conectar al VBox real, RECUERDAMEEE ver comentario arriba)
    }

    /**
     * Carga los avisos recientes que se muestran en el panel
     * "Avisos Recientes"
     *
     *  mismo caso que cargarReservaciones() -- el VBox
     * de avisos tampoco tiene fx:id en el FXML actua Cuando agregue
     * fx:id="panelAvisos", aqui se conecta la carga real desde la
     * tabla de avisos
     */
    private void cargarAvisos() {
        // Simulacion de lo que vendria de la base de datos:
        // List<Aviso> avisosActivos = avisoDAO.obtenerActivos();
        // (pendiente de conectar al VBox real, ver comentario arriba)
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
        ObservableList<DetalleItem> datos = FXCollections.observableArrayList(
                new DetalleItem("Laboratorio TI-1", "Laboratorio de Computo", "30", "Disponible"),
                new DetalleItem("Laboratorio TI-2", "Laboratorio de Computo", "30", "Disponible"),
                new DetalleItem("Laboratorio WAN", "Laboratorio de Redes", "25", "Reservado"),
                new DetalleItem("Aula 101", "Aula", "40", "Disponible"),
                new DetalleItem("Aula 102", "Aula", "40", "Disponible"),
                new DetalleItem("Sala Audiovisual", "Aula Especializada", "20", "Ocupado"),
                new DetalleItem("Laboratorio Fotografia", "Aula Especializada", "15", "Disponible")
        );
        abrirModal("Espacios registrados", "24 espacios en total - Edificio F", datos);
    }

    @FXML
    private void onCardReservaciones(MouseEvent event) {
        ObservableList<DetalleItem> datos = FXCollections.observableArrayList(
                new DetalleItem("Laboratorio TI-1", "08:00 AM - 09:30 AM", "30", "Reservado"),
                new DetalleItem("Laboratorio WAN", "10:00 AM - 11:30 AM", "25", "Reservado"),
                new DetalleItem("Sala Audiovisual", "12:00 PM - 01:30 PM", "20", "Ocupado")
        );
        abrirModal("Reservaciones de hoy", "25 de Julio de 2026 - 12 reservaciones", datos);
    }

    @FXML
    private void onCardDisponibles(MouseEvent event) {
        ObservableList<DetalleItem> datos = FXCollections.observableArrayList(
                new DetalleItem("Aula 101", "Aula", "40", "Disponible"),
                new DetalleItem("Aula 102", "Aula", "40", "Disponible"),
                new DetalleItem("Laboratorio Fotografia", "Aula Especializada", "15", "Disponible")
        );
        abrirModal("Espacios disponibles ahora", "8 espacios libres en este momento", datos);
    }

    @FXML
    private void onCardAvisos(MouseEvent event) {
        ObservableList<DetalleItem> datos = FXCollections.observableArrayList(
                new DetalleItem("Mantenimiento programado", "Sistema", "-", "Activo"),
                new DetalleItem("Nuevo laboratorio disponible", "Laboratorios", "-", "Activo"),
                new DetalleItem("Recordatorio de cancelacion", "General", "-", "Activo")
        );
        abrirModal("Avisos activos", "3 avisos vigentes", datos);
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
        VBox[] tarjetas = { cardEspacios, cardReservaciones, cardDisponibles, cardAvisos };

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
    VBox[] tarjetas = { cardEspacios, cardReservaciones, cardDisponibles, cardAvisos };

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


