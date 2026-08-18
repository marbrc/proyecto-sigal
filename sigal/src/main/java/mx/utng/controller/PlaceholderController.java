package mx.utng.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * =================================================================
 * PlaceholderController
 * -----------------------------------------------------------------
 * Controlador compartido por las pantallas de Materias, Grupos,
 * Carreras y Área Académica mientras todavía no tienen su
 * funcionalidad real conectada a la base de datos.
 *
 * MenuController llama a configurar(nombreFxml, this) justo despues
 * de cargar el FXML, y aqui se decide que icono/titulo/descripcion
 * mostrar segun cual de las 4 pantallas se abrio.
 * =================================================================
 */
public class PlaceholderController {

    @FXML private Label lblIcono;
    @FXML private Label lblTitulo;
    @FXML private Label lblDescripcion;

    private MenuController menuController;

    public void configurar(String nombreFxml, MenuController menuController) {
        this.menuController = menuController;

        String icono;
        String titulo;
        String descripcion;

        switch (nombreFxml) {
            case "fx_materias":
                icono = "📘";
                titulo = "Materias";
                descripcion = "Aquí se administrará el catálogo de materias "
                        + "(clave, nombre, cuatrimestre y carrera a la que pertenece).";
                break;
            case "fx_grupos":
                icono = "👥";
                titulo = "Grupos";
                descripcion = "Aquí se administrarán los grupos "
                        + "(nombre, capacidad, cuatrimestre, turno y carrera).";
                break;
            case "fx_carreras":
                icono = "🎓";
                titulo = "Carreras";
                descripcion = "Aquí se administrará el catálogo de carreras "
                        + "y el área académica a la que pertenece cada una.";
                break;
            case "fx_area_academica":
                icono = "🏫";
                titulo = "Área Académica";
                descripcion = "Aquí se administrarán las áreas académicas "
                        + "del Edificio F y las carreras que agrupan.";
                break;
            default:
                icono = "🛠";
                titulo = "Próximamente";
                descripcion = "Esta sección todavía está en construcción.";
        }

        if (lblIcono != null) lblIcono.setText(icono);
        if (lblTitulo != null) lblTitulo.setText(titulo);
        if (lblDescripcion != null) lblDescripcion.setText(descripcion);
    }

    @FXML
    private void onVolverCatalogo(ActionEvent event) {
        if (menuController != null) {
            menuController.abrirModulo("fx_catalogo_academico");
        }
    }
}
