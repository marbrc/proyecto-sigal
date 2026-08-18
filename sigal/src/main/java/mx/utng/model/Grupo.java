package mx.utng.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Modelo para la tabla "Grupos registrados" (fx_grupos.fxml).
 *
 * Mapea EXACTAMENTE las columnas reales de tb_grupo:
 * ID_Grupo, NombreGrupo, Capacidad, Cuatrimestre, Turno, ID_Carrera.
 *
 * tb_grupo.ID_Carrera es NOT NULL con llave foránea hacia
 * tb_carrera, así que todo grupo debe estar vinculado a una carrera
 * ya existente. nombreCarreraVinculada es un dato de solo lectura que
 * viene de un JOIN con tb_carrera, usado únicamente para mostrarlo en
 * la tabla.
 */
public class Grupo {

    /** ID_Grupo real en tb_grupo (0 = todavía no se ha guardado en BD). */
    private int idGrupo;

    /** ID_Carrera vinculada (llave foránea obligatoria hacia tb_carrera). */
    private int idCarrera;

    private final SimpleStringProperty nombreGrupo;
    private final SimpleIntegerProperty capacidad;
    private final SimpleIntegerProperty cuatrimestre;
    private final SimpleStringProperty turno;

    /** Solo lectura: viene del JOIN con tb_carrera (NombreCarrera). */
    private final SimpleStringProperty nombreCarreraVinculada;

    public Grupo() {
        this("", 0, 1, "Matutino", "");
    }

    public Grupo(String nombreGrupo, int capacidad, int cuatrimestre, String turno, String nombreCarreraVinculada) {
        this.nombreGrupo = new SimpleStringProperty(nombreGrupo == null ? "" : nombreGrupo);
        this.capacidad = new SimpleIntegerProperty(capacidad);
        this.cuatrimestre = new SimpleIntegerProperty(cuatrimestre);
        this.turno = new SimpleStringProperty(turno == null ? "" : turno);
        this.nombreCarreraVinculada = new SimpleStringProperty(nombreCarreraVinculada == null ? "" : nombreCarreraVinculada);
    }

    public int getIdGrupo() { return idGrupo; }
    public void setIdGrupo(int v) { this.idGrupo = v; }

    public int getIdCarrera() { return idCarrera; }
    public void setIdCarrera(int v) { this.idCarrera = v; }

    public String getNombreGrupo() { return nombreGrupo.get(); }
    public void setNombreGrupo(String v) { nombreGrupo.set(v == null ? "" : v); }
    public SimpleStringProperty nombreGrupoProperty() { return nombreGrupo; }

    public int getCapacidad() { return capacidad.get(); }
    public void setCapacidad(int v) { capacidad.set(v); }
    public SimpleIntegerProperty capacidadProperty() { return capacidad; }

    public int getCuatrimestre() { return cuatrimestre.get(); }
    public void setCuatrimestre(int v) { cuatrimestre.set(v); }
    public SimpleIntegerProperty cuatrimestreProperty() { return cuatrimestre; }

    public String getTurno() { return turno.get(); }
    public void setTurno(String v) { turno.set(v == null ? "" : v); }
    public SimpleStringProperty turnoProperty() { return turno; }

    public String getNombreCarreraVinculada() { return nombreCarreraVinculada.get(); }
    public void setNombreCarreraVinculada(String v) { nombreCarreraVinculada.set(v == null ? "" : v); }
    public SimpleStringProperty nombreCarreraVinculadaProperty() { return nombreCarreraVinculada; }

    @Override
    public String toString() {
        return getNombreGrupo();
    }
}
