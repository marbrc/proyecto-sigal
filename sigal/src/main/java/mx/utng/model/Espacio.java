package mx.utng.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo que representa un espacio (laboratorio, sala o aula) dentro
 * del sistema SIGAL, usado por la pantalla "Disponibilidad completa"
 * (fx_disponibilidad.fxml).
 */
public class Espacio {

    private final StringProperty nombre;
    private final StringProperty tipo;
    private final StringProperty capacidad;
    private final StringProperty estado;          // Disponible / Ocupado / Mantenimiento / Cancelado
    private final StringProperty ocupacionActual;  // Quién/qué lo tiene ocupado ahora mismo (o "—")
    private final StringProperty proximoHorario;   // Próxima franja libre u ocupada
    private final StringProperty encargado;

    public Espacio(String nombre, String tipo, String capacidad, String estado,
                    String ocupacionActual, String proximoHorario, String encargado) {
        this.nombre = new SimpleStringProperty(nombre);
        this.tipo = new SimpleStringProperty(tipo);
        this.capacidad = new SimpleStringProperty(capacidad);
        this.estado = new SimpleStringProperty(estado);
        this.ocupacionActual = new SimpleStringProperty(ocupacionActual);
        this.proximoHorario = new SimpleStringProperty(proximoHorario);
        this.encargado = new SimpleStringProperty(encargado);
    }

    public String getNombre() { return nombre.get(); }
    public StringProperty nombreProperty() { return nombre; }

    public String getTipo() { return tipo.get(); }
    public StringProperty tipoProperty() { return tipo; }

    public String getCapacidad() { return capacidad.get(); }
    public StringProperty capacidadProperty() { return capacidad; }

    public String getEstado() { return estado.get(); }
    public StringProperty estadoProperty() { return estado; }

    public String getOcupacionActual() { return ocupacionActual.get(); }
    public StringProperty ocupacionActualProperty() { return ocupacionActual; }

    public String getProximoHorario() { return proximoHorario.get(); }
    public StringProperty proximoHorarioProperty() { return proximoHorario; }

    public String getEncargado() { return encargado.get(); }
    public StringProperty encargadoProperty() { return encargado; }
}
