package mx.utng.model;
 
import java.time.LocalDate;
import java.time.LocalTime;
 
/**
 * DTO liviano usado solo por la pantalla de Horarios (HorarioController)
 * para pintar la cuadrícula.
 *
 * OJO: esto NO es una tabla nueva en la base de datos. Los datos siguen
 * viviendo en tb_asignacion (la misma tabla que usa la pantalla de
 * Asignaciones); esta clase solo evita mezclar el formato de fecha/hora
 * que usa Asignaciones.java (fecha como texto "dd/MM/yyyy", pensado para
 * mostrarse en una tabla) con lo que necesita Horarios (LocalDate/LocalTime,
 * para calcular en qué día y franja de la cuadrícula cae cada asignación).
 */
public class AsignacionHorario {
 
    private final int idAsignacion;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final LocalTime horaTermino;
    private final String espacio;
    private final String docente;
    private final String materia;
    private final String estado; // 'Asignado' u 'Ocupado' (las 'Canceladas' no se consultan)
 
    public AsignacionHorario(int idAsignacion, LocalDate fecha, LocalTime horaInicio, LocalTime horaTermino,
                              String espacio, String docente, String materia, String estado) {
        this.idAsignacion = idAsignacion;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaTermino = horaTermino;
        this.espacio = espacio;
        this.docente = docente;
        this.materia = materia;
        this.estado = estado;
    }
 
    public int getIdAsignacion() { return idAsignacion; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraTermino() { return horaTermino; }
    public String getEspacio() { return espacio; }
    public String getDocente() { return docente; }
    public String getMateria() { return materia; }
    public String getEstado() { return estado; }

    
}