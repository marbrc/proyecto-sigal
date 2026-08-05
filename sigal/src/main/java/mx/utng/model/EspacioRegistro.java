package mx.utng.model;
 
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
 
/**
 * Modelo simple para representar un espacio (aula, laboratorio o sala)
 * dentro de la tabla "Espacios registrados".
 *
 * TODO: cuando conectes a base de datos, este modelo debería mapear
 * directamente a tu tabla "espacios" (o similar).
 */
public class EspacioRegistro {
 
    /** ID_Espacio real en tb_espado (0 = todavía no se ha guardado en BD). */
    private int idEspacio;
 
    private final SimpleStringProperty clave;
    private final SimpleStringProperty nombre;
    private final SimpleStringProperty tipo;
    private final SimpleIntegerProperty capacidad;
    private final SimpleStringProperty estado;
    private final SimpleStringProperty descripcion;
 
    public EspacioRegistro(String clave, String nombre, String tipo, int capacidad, String estado, String descripcion) {
        this.clave = new SimpleStringProperty(clave);
        this.nombre = new SimpleStringProperty(nombre);
        this.tipo = new SimpleStringProperty(tipo);
        this.capacidad = new SimpleIntegerProperty(capacidad);
        this.estado = new SimpleStringProperty(estado);
        this.descripcion = new SimpleStringProperty(descripcion == null ? "" : descripcion);
    }
 
    public String getClave() { return clave.get(); }
    public void setClave(String v) { clave.set(v); }
    public SimpleStringProperty claveProperty() { return clave; }
 
    public String getNombre() { return nombre.get(); }
    public void setNombre(String v) { nombre.set(v); }
    public SimpleStringProperty nombreProperty() { return nombre; }
 
    public String getTipo() { return tipo.get(); }
    public void setTipo(String v) { tipo.set(v); }
    public SimpleStringProperty tipoProperty() { return tipo; }
 
    public int getCapacidad() { return capacidad.get(); }
    public void setCapacidad(int v) { capacidad.set(v); }
    public SimpleIntegerProperty capacidadProperty() { return capacidad; }
 
    public String getEstado() { return estado.get(); }
    public void setEstado(String v) { estado.set(v); }
    public SimpleStringProperty estadoProperty() { return estado; }
 
    public String getDescripcion() { return descripcion.get(); }
    public void setDescripcion(String v) { descripcion.set(v); }
    public SimpleStringProperty descripcionProperty() { return descripcion; }
 
    public int getIdEspacio() { return idEspacio; }
    public void setIdEspacio(int v) { this.idEspacio = v; }
}
 