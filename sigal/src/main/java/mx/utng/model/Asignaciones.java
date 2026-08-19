package mx.utng.model;
 
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
 
public class Asignaciones {
 
    /** ID_Asignacion real de tb_asignacion (para editar/eliminar en la BD). */
    private int idAsignacion;
 
    /** IDs reales de catálogo (tb_carrera, tb_grupo, tb_materia) — solo para uso interno del DAO. */
    private int idCarrera;
    private int idGrupo;
    private int idMateria;
 
    private final StringProperty id;
    private final StringProperty fecha;
    private final StringProperty horaInicio;
    private final StringProperty horaTermino;
    private final StringProperty espacio;
 
    private final StringProperty tipoSolicitante;   // Profesor / Administrativo / Alumno / Otro
    private final StringProperty nombreSolicitante;
    private final StringProperty profesor;
    private final StringProperty carrera;
    private final StringProperty materia;
    private final StringProperty grupo;
    private final StringProperty numAlumnos;
    private final StringProperty actividad;
 
    private final StringProperty estado; // Libre / Ocupado / Asignado / Cancelado (ENUM de tb_asignacion)
 
    public Asignaciones() {
        this("", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
 
    public Asignaciones(String id, String fecha, String horaInicio, String horaTermino,
                         String espacio, String tipoSolicitante, String nombreSolicitante,
                         String profesor, String carrera, String materia, String grupo,
                         String numAlumnos, String actividad, String estado) {
        this.id = new SimpleStringProperty(id);
        this.fecha = new SimpleStringProperty(fecha);
        this.horaInicio = new SimpleStringProperty(horaInicio);
        this.horaTermino = new SimpleStringProperty(horaTermino);
        this.espacio = new SimpleStringProperty(espacio);
        this.tipoSolicitante = new SimpleStringProperty(tipoSolicitante);
        this.nombreSolicitante = new SimpleStringProperty(nombreSolicitante);
        this.profesor = new SimpleStringProperty(profesor);
        this.carrera = new SimpleStringProperty(carrera);
        this.materia = new SimpleStringProperty(materia);
        this.grupo = new SimpleStringProperty(grupo);
        this.numAlumnos = new SimpleStringProperty(numAlumnos);
        this.actividad = new SimpleStringProperty(actividad);
        this.estado = new SimpleStringProperty(estado);
    }
 
    /** Texto combinado "08:00 - 09:30" que se muestra en la columna Hora de la tabla. */
    public String getHora() {
        if (horaInicio.get() == null || horaInicio.get().isEmpty()) {
            return "";
        }
        return horaInicio.get() + " - " + horaTermino.get();
    }
 
    // ---------------------------------------------------------------
    // Getters / Setters + métodos *Property() (requeridos por PropertyValueFactory)
    // ---------------------------------------------------------------
 
    public int getIdAsignacion() { return idAsignacion; }
    public void setIdAsignacion(int value) { this.idAsignacion = value; }
 
    public String getId() { return id.get(); }
    public void setId(String value) { id.set(value); }
    public StringProperty idProperty() { return id; }
 
    public String getFecha() { return fecha.get(); }
    public void setFecha(String value) { fecha.set(value); }
    public StringProperty fechaProperty() { return fecha; }
 
    public String getHoraInicio() { return horaInicio.get(); }
    public void setHoraInicio(String value) { horaInicio.set(value); }
    public StringProperty horaInicioProperty() { return horaInicio; }
 
    public String getHoraTermino() { return horaTermino.get(); }
    public void setHoraTermino(String value) { horaTermino.set(value); }
    public StringProperty horaTerminoProperty() { return horaTermino; }
 
    public String getEspacio() { return espacio.get(); }
    public void setEspacio(String value) { espacio.set(value); }
    public StringProperty espacioProperty() { return espacio; }
 
    public String getTipoSolicitante() { return tipoSolicitante.get(); }
    public void setTipoSolicitante(String value) { tipoSolicitante.set(value); }
    public StringProperty tipoSolicitanteProperty() { return tipoSolicitante; }
 
    public String getNombreSolicitante() { return nombreSolicitante.get(); }
    public void setNombreSolicitante(String value) { nombreSolicitante.set(value); }
    public StringProperty nombreSolicitanteProperty() { return nombreSolicitante; }
 
    public String getProfesor() { return profesor.get(); }
    public void setProfesor(String value) { profesor.set(value); }
    public StringProperty profesorProperty() { return profesor; }
 
    public String getCarrera() { return carrera.get(); }
    public void setCarrera(String value) { carrera.set(value); }
    public StringProperty carreraProperty() { return carrera; }
 
    public String getMateria() { return materia.get(); }
    public void setMateria(String value) { materia.set(value); }
    public StringProperty materiaProperty() { return materia; }
 
    public String getGrupo() { return grupo.get(); }
    public void setGrupo(String value) { grupo.set(value); }
    public StringProperty grupoProperty() { return grupo; }
 
    public String getNumAlumnos() { return numAlumnos.get(); }
    public void setNumAlumnos(String value) { numAlumnos.set(value); }
    public StringProperty numAlumnosProperty() { return numAlumnos; }
 
    public String getActividad() { return actividad.get(); }
    public void setActividad(String value) { actividad.set(value); }
    public StringProperty actividadProperty() { return actividad; }
 
    public String getEstado() { return estado.get(); }
    public void setEstado(String value) { estado.set(value); }
    public StringProperty estadoProperty() { return estado; }
 
    // ---------------------------------------------------------------
    // IDs de catálogo (int simples, sin Property: no se muestran en tabla)
    // ---------------------------------------------------------------
 
    public int getIdCarrera() { return idCarrera; }
    public void setIdCarrera(int value) { this.idCarrera = value; }
 
    public int getIdGrupo() { return idGrupo; }
    public void setIdGrupo(int value) { this.idGrupo = value; }
 
    public int getIdMateria() { return idMateria; }
    public void setIdMateria(int value) { this.idMateria = value; }
 
    // ---------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------
 
    /** true si todavía no se ha guardado en la BD (no tiene ID_Asignacion real). */
    public boolean esNueva() {
        return idAsignacion <= 0;
    }
 
    @Override
    public String toString() {
        return "Asignaciones{" +
                "id=" + getId() +
                ", fecha=" + getFecha() +
                ", hora=" + getHora() +
                ", espacio=" + getEspacio() +
                ", solicitante=" + getNombreSolicitante() +
                ", materia=" + getMateria() +
                ", grupo=" + getGrupo() +
                ", estado=" + getEstado() +
                '}';
    }
 
    /**
     * Dos asignaciones se consideran iguales si son la misma fila de tb_asignacion
     * (mismo ID_Asignacion). Útil para que la tabla y las listas de JavaFX detecten
     * bien selección/eliminación después de refrescar los datos.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Asignaciones otra)) return false;
        return this.idAsignacion == otra.idAsignacion;
    }
 
    @Override
    public int hashCode() {
        return Integer.hashCode(idAsignacion);
    }
}
 