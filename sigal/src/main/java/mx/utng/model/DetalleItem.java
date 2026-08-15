package mx.utng.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Representa una fila dentro del modal de detalle
 * (espacios, asignaciones, disponibilidad o avisos).
 */
public class DetalleItem {

    private final StringProperty nombre;
    private final StringProperty tipo;
    private final StringProperty capacidad;
    private final StringProperty estado;

    public DetalleItem(String nombre, String tipo, String capacidad, String estado) {
        this.nombre = new SimpleStringProperty(nombre);
        this.tipo = new SimpleStringProperty(tipo);
        this.capacidad = new SimpleStringProperty(capacidad);
        this.estado = new SimpleStringProperty(estado);
    }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String v) { nombre.set(v); }
    public StringProperty nombreProperty() { return nombre; }

    public String getTipo() { return tipo.get(); }
    public void setTipo(String v) { tipo.set(v); }
    public StringProperty tipoProperty() { return tipo; }

    public String getCapacidad() { return capacidad.get(); }
    public void setCapacidad(String v) { capacidad.set(v); }
    public StringProperty capacidadProperty() { return capacidad; }

    public String getEstado() { return estado.get(); }
    public void setEstado(String v) { estado.set(v); }
    public StringProperty estadoProperty() { return estado; }
}
