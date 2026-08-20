package mx.utng.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class Reporte {

    // Datos del resumen
    private final SimpleStringProperty espacio;
    private final SimpleDoubleProperty horasDisponibles;
    private final SimpleDoubleProperty horasOcupadas;
    private final SimpleDoubleProperty porcentajeOcupacion;

    // Datos del detalle
    private final SimpleStringProperty fecha;
    private final SimpleStringProperty horario;
    private final SimpleStringProperty solicitante;
    private final SimpleStringProperty grupo;
    private final SimpleStringProperty estado;
    private final SimpleStringProperty motivo;
    private final SimpleStringProperty carrera;
    private final SimpleStringProperty materia;

    /*
     * Constructor para el resumen de ocupación.
     */
    public Reporte(
            String espacio,
            double horasDisponibles,
            double horasOcupadas
    ) {
        this(
                null,
                null,
                espacio,
                null,
                null,
                null,
                null,
                null,
                null,
                horasDisponibles,
                horasOcupadas
        );
    }

    /*
     * Constructor para el detalle de asignaciones.
     *
     * Las horas disponibles y ocupadas se dejan en cero porque
     * este constructor representa una fila de detalle.
     */
    public Reporte(
            String fecha,
            String horario,
            String espacio,
            String solicitante,
            String grupo,
            String estado,
            String motivo,
            String carrera,
            String materia
    ) {
        this(
                fecha,
                horario,
                espacio,
                solicitante,
                grupo,
                estado,
                motivo,
                carrera,
                materia,
                0.0,
                0.0
        );
    }

    /*
     * Constructor interno que inicializa todos los campos.
     */
    private Reporte(
            String fecha,
            String horario,
            String espacio,
            String solicitante,
            String grupo,
            String estado,
            String motivo,
            String carrera,
            String materia,
            double horasDisponibles,
            double horasOcupadas
    ) {
        this.fecha = new SimpleStringProperty(valorSeguro(fecha));
        this.horario = new SimpleStringProperty(valorSeguro(horario));
        this.espacio = new SimpleStringProperty(valorSeguro(espacio));
        this.solicitante = new SimpleStringProperty(valorSeguro(solicitante));
        this.grupo = new SimpleStringProperty(valorSeguro(grupo));
        this.estado = new SimpleStringProperty(valorSeguro(estado));
        this.motivo = new SimpleStringProperty(valorSeguro(motivo));
        this.carrera = new SimpleStringProperty(valorSeguro(carrera));
        this.materia = new SimpleStringProperty(valorSeguro(materia));

        this.horasDisponibles =
                new SimpleDoubleProperty(horasDisponibles);

        this.horasOcupadas =
                new SimpleDoubleProperty(horasOcupadas);

        double porcentaje = horasDisponibles <= 0
                ? 0
                : (horasOcupadas / horasDisponibles) * 100.0;

        if (porcentaje > 100) {
            porcentaje = 100;
        }

        this.porcentajeOcupacion =
                new SimpleDoubleProperty(porcentaje);
    }

    private static String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }

    // Getters del detalle

    public String getFecha() {
        return fecha.get();
    }

    public SimpleStringProperty fechaProperty() {
        return fecha;
    }

    public String getHorario() {
        return horario.get();
    }

    public SimpleStringProperty horarioProperty() {
        return horario;
    }

    public String getEspacio() {
        return espacio.get();
    }

    public SimpleStringProperty espacioProperty() {
        return espacio;
    }

    public String getSolicitante() {
        return solicitante.get();
    }

    public SimpleStringProperty solicitanteProperty() {
        return solicitante;
    }

    public String getGrupo() {
        return grupo.get();
    }

    public SimpleStringProperty grupoProperty() {
        return grupo;
    }

    public String getEstado() {
        return estado.get();
    }

    public SimpleStringProperty estadoProperty() {
        return estado;
    }

    public String getMotivo() {
        return motivo.get();
    }

    public SimpleStringProperty motivoProperty() {
        return motivo;
    }

    public String getCarrera() {
        return carrera.get();
    }

    public SimpleStringProperty carreraProperty() {
        return carrera;
    }

    public String getMateria() {
        return materia.get();
    }

    public SimpleStringProperty materiaProperty() {
        return materia;
    }

    // Getters del resumen

    public double getHorasDisponibles() {
        return horasDisponibles.get();
    }

    public SimpleDoubleProperty horasDisponiblesProperty() {
        return horasDisponibles;
    }

    public double getHorasOcupadas() {
        return horasOcupadas.get();
    }

    public SimpleDoubleProperty horasOcupadasProperty() {
        return horasOcupadas;
    }

    public double getPorcentajeOcupacion() {
        return porcentajeOcupacion.get();
    }

    public SimpleDoubleProperty porcentajeOcupacionProperty() {
        return porcentajeOcupacion;
    }

    public String getPorcentajeTexto() {
        return String.format(
                "%.1f %%",
                getPorcentajeOcupacion()
        );
    }
}
