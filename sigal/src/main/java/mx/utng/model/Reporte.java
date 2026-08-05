package mx.utng.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Una fila del reporte de ocupación: un espacio con sus horas
 * disponibles/ocupadas dentro del periodo elegido en fx_reportes.fxml.
 */
public class Reporte {

    private final SimpleStringProperty espacio;
    private final SimpleDoubleProperty horasDisponibles;
    private final SimpleDoubleProperty horasOcupadas;
    private final SimpleDoubleProperty porcentajeOcupacion;

    public Reporte(String espacio, double horasDisponibles, double horasOcupadas) {
        this.espacio = new SimpleStringProperty(espacio);
        this.horasDisponibles = new SimpleDoubleProperty(horasDisponibles);
        this.horasOcupadas = new SimpleDoubleProperty(horasOcupadas);

        double porcentaje = horasDisponibles <= 0 ? 0 : (horasOcupadas / horasDisponibles) * 100.0;
        if (porcentaje > 100) {
            porcentaje = 100;
        }
        this.porcentajeOcupacion = new SimpleDoubleProperty(porcentaje);
    }

    public String getEspacio() { return espacio.get(); }
    public SimpleStringProperty espacioProperty() { return espacio; }

    public double getHorasDisponibles() { return horasDisponibles.get(); }
    public SimpleDoubleProperty horasDisponiblesProperty() { return horasDisponibles; }

    public double getHorasOcupadas() { return horasOcupadas.get(); }
    public SimpleDoubleProperty horasOcupadasProperty() { return horasOcupadas; }

    public double getPorcentajeOcupacion() { return porcentajeOcupacion.get(); }
    public SimpleDoubleProperty porcentajeOcupacionProperty() { return porcentajeOcupacion; }

    /** Listo para mostrarse en la tabla, ej. "62.5 %". */
    public String getPorcentajeTexto() {
        return String.format("%.1f %%", getPorcentajeOcupacion());
    }
}
