package mx.utng.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Asignaciones {

    /** ID_Asignacion real de tb_asignacion (para editar/eliminar en la BD). */
    private int idAsignacion;
    private int ID_Carrera;
    private int ID_Grupo;
    private int ID_Materia;

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

    public String getIdCarrera() {
    return ID_Carrera.get();
}

public void setIdCarrera(String value) {
    ID_Carrera.set(value);
}

public StringProperty idCarreraProperty() {
    return ID_Carrera;
}
java


public String getIdGrupo() {
    return ID_Grupo.get();
}

public void setIdGrupo(String value) {
    ID_Grupo.set(value);
}

public StringProperty idGrupoProperty() {
    return ID_Grupo;
}
java


public String getIdMateria() {
    return ID_Materia.get();
}

public void setIdMateria(String value) {
    ID_Materia.set(value);
}

public StringProperty idMateriaProperty() {
    return ID_Materia;
}
}
