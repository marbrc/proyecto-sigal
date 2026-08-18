package mx.utng.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Modelo para la tabla "Materias registradas" (fx_materias.fxml).
 *
 * Mapea EXACTAMENTE las columnas reales de tb_materia:
 * ID_Materia, Nombre, Descripcion.
 */
public class Materia {

    /** ID_Materia real en tb_materia (0 = todavía no se ha guardado en BD). */
    private int idMateria;

    private final SimpleStringProperty nombre;
    private final SimpleStringProperty descripcion;

    public Materia() {
        this("", "");
    }

    public Materia(String nombre, String descripcion) {
        this.nombre = new SimpleStringProperty(nombre == null ? "" : nombre);
        this.descripcion = new SimpleStringProperty(descripcion == null ? "" : descripcion);
    }

    public int getIdMateria() { return idMateria; }
    public void setIdMateria(int v) { this.idMateria = v; }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String v) { nombre.set(v == null ? "" : v); }
    public SimpleStringProperty nombreProperty() { return nombre; }

    public String getDescripcion() { return descripcion.get(); }
    public void setDescripcion(String v) { descripcion.set(v == null ? "" : v); }
    public SimpleStringProperty descripcionProperty() { return descripcion; }

    @Override
    public String toString() {
        return getNombre();
    }
}
