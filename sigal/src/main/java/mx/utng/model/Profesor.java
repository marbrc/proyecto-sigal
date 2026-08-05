package mx.utng.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Modelo simple para representar un profesor dentro de la tabla
 * "Profesores registrados"
 *
 * TODO: cuando conectes a base de datos, este modelo debería mapear
 * directamente a tu tabla "profesores" (o similar).
 */
public class Profesor {

    private final SimpleStringProperty nombreCompleto;
    private final SimpleStringProperty identificador;
    private final SimpleStringProperty correo;
    private final SimpleStringProperty carrera;
    private final SimpleStringProperty estado;

    public Profesor(String nombreCompleto, String identificador, String correo, String carrera, String estado) {
        this.nombreCompleto = new SimpleStringProperty(nombreCompleto);
        this.identificador = new SimpleStringProperty(identificador);
        this.correo = new SimpleStringProperty(correo);
        this.carrera = new SimpleStringProperty(carrera);
        this.estado = new SimpleStringProperty(estado);
    }

    public String getNombreCompleto() { return nombreCompleto.get(); }
    public void setNombreCompleto(String v) { nombreCompleto.set(v); }
    public SimpleStringProperty nombreCompletoProperty() { return nombreCompleto; }

    public String getIdentificador() { return identificador.get(); }
    public void setIdentificador(String v) { identificador.set(v); }
    public SimpleStringProperty identificadorProperty() { return identificador; }

    public String getCorreo() { return correo.get(); }
    public void setCorreo(String v) { correo.set(v); }
    public SimpleStringProperty correoProperty() { return correo; }

    public String getCarrera() { return carrera.get(); }
    public void setCarrera(String v) { carrera.set(v); }
    public SimpleStringProperty carreraProperty() { return carrera; }

    public String getEstado() { return estado.get(); }
    public void setEstado(String v) { estado.set(v); }
    public SimpleStringProperty estadoProperty() { return estado; }
}
