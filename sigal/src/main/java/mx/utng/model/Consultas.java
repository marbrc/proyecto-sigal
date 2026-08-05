package mx.utng.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo que representa una fila de resultado en la pantalla de
 * Consultas (fx_consultas.fxml). Cada fila es una asignación ya
 * registrada en tb_asignacion, mostrada con los datos ya "traducidos"
 * a texto (nombre del espacio, del profesor, de la carrera, etc.)
 * para poderla mostrar directo en la tabla.
 */
public class Consultas {

    /** ID_Asignacion real de tb_asignacion. */
    private int idAsignacion;

    private final StringProperty horario;
    private final StringProperty espacio;
    private final StringProperty solicitante; // Profesor si tiene, si no el nombre del solicitante
    private final StringProperty grupo;
    private final StringProperty estado;
    private final StringProperty motivo;
    private final StringProperty carrera;
    private final StringProperty materia;

    public Consultas() {
        this("", "", "", "", "", "", "", "");
    }

    public Consultas(String horario, String espacio, String solicitante, String grupo,
                      String estado, String motivo, String carrera, String materia) {
        this.horario = new SimpleStringProperty(horario);
        this.espacio = new SimpleStringProperty(espacio);
        this.solicitante = new SimpleStringProperty(solicitante);
        this.grupo = new SimpleStringProperty(grupo);
        this.estado = new SimpleStringProperty(estado);
        this.motivo = new SimpleStringProperty(motivo);
        this.carrera = new SimpleStringProperty(carrera);
        this.materia = new SimpleStringProperty(materia);
    }

    public int getIdAsignacion() { return idAsignacion; }
    public void setIdAsignacion(int value) { this.idAsignacion = value; }

    public String getHorario() { return horario.get(); }
    public void setHorario(String value) { horario.set(value); }
    public StringProperty horarioProperty() { return horario; }

    public String getEspacio() { return espacio.get(); }
    public void setEspacio(String value) { espacio.set(value); }
    public StringProperty espacioProperty() { return espacio; }

    public String getSolicitante() { return solicitante.get(); }
    public void setSolicitante(String value) { solicitante.set(value); }
    public StringProperty solicitanteProperty() { return solicitante; }

    public String getGrupo() { return grupo.get(); }
    public void setGrupo(String value) { grupo.set(value); }
    public StringProperty grupoProperty() { return grupo; }

    public String getEstado() { return estado.get(); }
    public void setEstado(String value) { estado.set(value); }
    public StringProperty estadoProperty() { return estado; }

    public String getMotivo() { return motivo.get(); }
    public void setMotivo(String value) { motivo.set(value); }
    public StringProperty motivoProperty() { return motivo; }

    public String getCarrera() { return carrera.get(); }
    public void setCarrera(String value) { carrera.set(value); }
    public StringProperty carreraProperty() { return carrera; }

    public String getMateria() { return materia.get(); }
    public void setMateria(String value) { materia.set(value); }
    public StringProperty materiaProperty() { return materia; }
}
