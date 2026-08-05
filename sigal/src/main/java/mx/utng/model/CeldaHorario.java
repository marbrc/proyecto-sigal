package mx.utng.model;

/**
 * Representa una celda de la cuadrícula de horarios: un espacio en un día
 * y franja horaria específicos, con su estado y detalle (docente - materia).
 */
public class CeldaHorario {

    private EstadoCelda estado;
    private String detalle;

    public CeldaHorario() {
        this.estado = EstadoCelda.LIBRE;
        this.detalle = "";
    }

    public CeldaHorario(EstadoCelda estado, String detalle) {
        this.estado = estado;
        this.detalle = detalle == null ? "" : detalle;
    }

    public EstadoCelda getEstado() { return estado; }
    public void setEstado(EstadoCelda estado) { this.estado = estado; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle == null ? "" : detalle; }
}
