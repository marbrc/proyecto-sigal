package mx.utng.model;

/**
 * Estados posibles de una celda dentro de la cuadrícula de Horarios.
 */
public enum EstadoCelda {
    LIBRE("Libre"),
    RESERVADO("Reservado"),
    OCUPADO("Ocupado");

    private final String etiqueta;

    EstadoCelda(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
