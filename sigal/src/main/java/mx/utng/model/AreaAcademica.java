package mx.utng.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Modelo para la tabla "Áreas académicas registradas"
 * (fx_area_academica.fxml).
 *
 * Mapea EXACTAMENTE las columnas reales de tb_area_academica:
 * ID_Area, NombreArea.
 *
 * totalCarreras es un dato de solo lectura que viene de un
 * LEFT JOIN + COUNT con tb_carrera, usado únicamente para mostrarlo
 * en la tabla (cuántas carreras agrupa cada área).
 */
public class AreaAcademica {

    /** ID_Area real en tb_area_academica (0 = todavía no se ha guardado en BD). */
    private int idArea;

    private final SimpleStringProperty nombreArea;

    /** Solo lectura: COUNT(tb_carrera.ID_Carrera) agrupadas por esta área. */
    private final SimpleIntegerProperty totalCarreras;

    public AreaAcademica() {
        this("");
    }

    public AreaAcademica(String nombreArea) {
        this.nombreArea = new SimpleStringProperty(nombreArea == null ? "" : nombreArea);
        this.totalCarreras = new SimpleIntegerProperty(0);
    }

    public int getIdArea() { return idArea; }
    public void setIdArea(int v) { this.idArea = v; }

    public String getNombreArea() { return nombreArea.get(); }
    public void setNombreArea(String v) { nombreArea.set(v == null ? "" : v); }
    public SimpleStringProperty nombreAreaProperty() { return nombreArea; }

    public int getTotalCarreras() { return totalCarreras.get(); }
    public void setTotalCarreras(int v) { totalCarreras.set(v); }
    public SimpleIntegerProperty totalCarrerasProperty() { return totalCarreras; }

    @Override
    public String toString() {
        return getNombreArea();
    }
}
