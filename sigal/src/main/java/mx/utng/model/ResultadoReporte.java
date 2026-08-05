package mx.utng.model;

import javafx.collections.ObservableList;

/**
 * Paquete con todo lo que necesita pintar fx_reportes.fxml de una sola
 * vez: las 4 tarjetas resumen + la lista de filas para la tabla y la
 * gráfica.
 */
public class ResultadoReporte {

    private final int totalEspacios;
    private final int espaciosConAsignaciones;
    private final double promedioAsignacionesPorEspacio;
    private final String espacioMasAsignado;
    private final ObservableList<Reporte> filas;

    public ResultadoReporte(int totalEspacios, int espaciosConAsignaciones,
                             double promedioAsignacionesPorEspacio, String espacioMasAsignado,
                             ObservableList<Reporte> filas) {
        this.totalEspacios = totalEspacios;
        this.espaciosConAsignaciones = espaciosConAsignaciones;
        this.promedioAsignacionesPorEspacio = promedioAsignacionesPorEspacio;
        this.espacioMasAsignado = espacioMasAsignado;
        this.filas = filas;
    }

    public int getTotalEspacios() { return totalEspacios; }
    public int getEspaciosConAsignaciones() { return espaciosConAsignaciones; }
    public double getPromedioAsignacionesPorEspacio() { return promedioAsignacionesPorEspacio; }
    public String getEspacioMasAsignado() { return espacioMasAsignado; }
    public ObservableList<Reporte> getFilas() { return filas; }
}
