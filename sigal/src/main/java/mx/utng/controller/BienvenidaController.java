package mx.utng.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    private Label lblTemp;

    @FXML
    private Label lblClima;

    @FXML
    private Label lblSaludo;

    @FXML
    private Button btnIniciarSesion;

    @FXML
    private Button btnRegistrarse;

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
    

    private final String[] fondos = {
        "/mx/utng/view/assets/background.jpg",
        "/mx/utng/view/assets/background1.png",
        "/mx/utng/view/assets/background2.png",
        "/mx/utng/view/assets/background3.png"
    };

    private int fondoActual = 0;        







    // =========================================================
    // MÉTODO PRINCIPAL
    // =========================================================

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        iniciarReloj();

        configurarClima();

        configurarSaludo();

        iniciarAnimaciones();
        
        iniciarCarrusel();

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
    // CLIMA
    // =========================================================

    /**
     * Información temporal.
     * Más adelante será reemplazada por una API.
     */
    private void configurarClima() {

        lblTemp.setText("24°C");

        lblClima.setText("Parcialmente nublado");
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
        stage.setMaximized(false);
        stage.setMaximized(true);
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
        stage.setMaximized(true);
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
            new KeyFrame(Duration.seconds(30), e -> cambiarFondo())
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