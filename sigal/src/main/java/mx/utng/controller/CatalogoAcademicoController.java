package mx.utng.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * =================================================================
 * CatalogoAcademicoController
 * -----------------------------------------------------------------
 * Controlador de la pantalla "Catálogo Académico" (fx_catalogo_academico.fxml).
 *
 * Es solo un "hub" con 4 tarjetas grandes que llevan a los catálogos
 * de Materias, Grupos, Carreras y Área Académica. El sidebar y el
 * topbar los sigue manejando MenuController, que es quien carga este
 * FXML dentro de su contentPane.
 * =================================================================
 */
public class CatalogoAcademicoController {

    // Referencia al menú, para poder pedirle que cargue cada catálogo
    private MenuController menuController;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    @FXML
    private void onAbrirMaterias(ActionEvent event) {
        if (menuController != null) {
            menuController.abrirModulo("fx_materias");
        }
    }

    @FXML
    private void onAbrirGrupos(ActionEvent event) {
        if (menuController != null) {
            menuController.abrirModulo("fx_grupos");
        }
    }

    @FXML
    private void onAbrirCarreras(ActionEvent event) {
        if (menuController != null) {
            menuController.abrirModulo("fx_carreras");
        }
    }

    @FXML
    private void onAbrirAreaAcademica(ActionEvent event) {
        if (menuController != null) {
            menuController.abrirModulo("fx_area_academica");
        }
    }
}
