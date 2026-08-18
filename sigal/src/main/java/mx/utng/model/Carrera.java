package mx.utng.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Modelo para la tabla "Carreras registradas" (fx_carreras.fxml).
 *
 * Mapea EXACTAMENTE las columnas reales de tb_carrera:
 * ID_Carrera, NombreCarrera, ID_Area.
 *
 * tb_carrera.ID_Area es una llave foránea hacia tb_area_academica
 * (puede ser NULL en la tabla, pero el formulario la pide siempre
 * para no dejar carreras "huérfanas"). nombreAreaVinculada es un dato
 * de solo lectura que viene de un LEFT JOIN con tb_area_academica,
 * usado únicamente para mostrarlo en la tabla.
 */
public class Carrera {

    /** ID_Carrera real en tb_carrera (0 = todavía no se ha guardado en BD). */
    private int idCarrera;

    /** ID_Area vinculada (llave foránea hacia tb_area_academica; puede ser null). */
    private Integer idArea;

    private final SimpleStringProperty nombreCarrera;

    /** Solo lectura: viene del LEFT JOIN con tb_area_academica (NombreArea). */
    private final SimpleStringProperty nombreAreaVinculada;

    public Carrera() {
        this("", "");
    }

    public Carrera(String nombreCarrera, String nombreAreaVinculada) {
        this.nombreCarrera = new SimpleStringProperty(nombreCarrera == null ? "" : nombreCarrera);
        this.nombreAreaVinculada = new SimpleStringProperty(nombreAreaVinculada == null ? "" : nombreAreaVinculada);
    }

    public int getIdCarrera() { return idCarrera; }
    public void setIdCarrera(int v) { this.idCarrera = v; }

    public Integer getIdArea() { return idArea; }
    public void setIdArea(Integer v) { this.idArea = v; }

    public String getNombreCarrera() { return nombreCarrera.get(); }
    public void setNombreCarrera(String v) { nombreCarrera.set(v == null ? "" : v); }
    public SimpleStringProperty nombreCarreraProperty() { return nombreCarrera; }

    public String getNombreAreaVinculada() { return nombreAreaVinculada.get(); }
    public void setNombreAreaVinculada(String v) { nombreAreaVinculada.set(v == null ? "" : v); }
    public SimpleStringProperty nombreAreaVinculadaProperty() { return nombreAreaVinculada; }

    @Override
    public String toString() {
        return getNombreCarrera();
    }
}
