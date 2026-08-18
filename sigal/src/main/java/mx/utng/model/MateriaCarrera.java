package mx.utng.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Modelo para la tabla "Materias por carrera" (fx_materias.fxml).
 *
 * Mapea EXACTAMENTE las columnas reales de tb_materia_carrera:
 * ID_MateriaCarrera, ID_Materia, ID_Carrera, Cuatrimestre, ID_Profesor.
 *
 * tb_materia_carrera es la tabla que en verdad guarda "a qué carrera
 * y en qué cuatrimestre pertenece cada materia" (tb_materia sólo
 * tiene Nombre y Descripcion). ID_Profesor es opcional (puede ser
 * NULL). nombreMateria, nombreCarrera y nombreProfesor son datos de
 * solo lectura que vienen de los JOIN con tb_materia, tb_carrera y
 * tb_profesor, usados únicamente para mostrarlos en la tabla.
 */
public class MateriaCarrera {

    /** ID_MateriaCarrera real (0 = todavía no se ha guardado en BD). */
    private int idMateriaCarrera;

    /** ID_Materia vinculada (llave foránea obligatoria hacia tb_materia). */
    private int idMateria;

    /** ID_Carrera vinculada (llave foránea obligatoria hacia tb_carrera). */
    private int idCarrera;

    /** ID_Profesor vinculado (llave foránea opcional hacia tb_profesor; puede ser null). */
    private Integer idProfesor;

    private final SimpleIntegerProperty cuatrimestre;

    /** Solo lectura: viene del JOIN con tb_materia (Nombre). */
    private final SimpleStringProperty nombreMateria;
    /** Solo lectura: viene del JOIN con tb_carrera (NombreCarrera). */
    private final SimpleStringProperty nombreCarrera;
    /** Solo lectura: viene del LEFT JOIN con tb_profesor (Nombre + ApellidoPaterno), puede quedar vacío. */
    private final SimpleStringProperty nombreProfesor;

    public MateriaCarrera() {
        this(1, "", "", "");
    }

    public MateriaCarrera(int cuatrimestre, String nombreMateria, String nombreCarrera, String nombreProfesor) {
        this.cuatrimestre = new SimpleIntegerProperty(cuatrimestre);
        this.nombreMateria = new SimpleStringProperty(nombreMateria == null ? "" : nombreMateria);
        this.nombreCarrera = new SimpleStringProperty(nombreCarrera == null ? "" : nombreCarrera);
        this.nombreProfesor = new SimpleStringProperty(nombreProfesor == null ? "" : nombreProfesor);
    }

    public int getIdMateriaCarrera() { return idMateriaCarrera; }
    public void setIdMateriaCarrera(int v) { this.idMateriaCarrera = v; }

    public int getIdMateria() { return idMateria; }
    public void setIdMateria(int v) { this.idMateria = v; }

    public int getIdCarrera() { return idCarrera; }
    public void setIdCarrera(int v) { this.idCarrera = v; }

    public Integer getIdProfesor() { return idProfesor; }
    public void setIdProfesor(Integer v) { this.idProfesor = v; }

    public int getCuatrimestre() { return cuatrimestre.get(); }
    public void setCuatrimestre(int v) { cuatrimestre.set(v); }
    public SimpleIntegerProperty cuatrimestreProperty() { return cuatrimestre; }

    public String getNombreMateria() { return nombreMateria.get(); }
    public void setNombreMateria(String v) { nombreMateria.set(v == null ? "" : v); }
    public SimpleStringProperty nombreMateriaProperty() { return nombreMateria; }

    public String getNombreCarrera() { return nombreCarrera.get(); }
    public void setNombreCarrera(String v) { nombreCarrera.set(v == null ? "" : v); }
    public SimpleStringProperty nombreCarreraProperty() { return nombreCarrera; }

    public String getNombreProfesor() { return nombreProfesor.get(); }
    public void setNombreProfesor(String v) { nombreProfesor.set(v == null ? "" : v); }
    public SimpleStringProperty nombreProfesorProperty() { return nombreProfesor; }

    @Override
    public String toString() {
        return getNombreMateria() + " (" + getNombreCarrera() + ")";
    }
}
