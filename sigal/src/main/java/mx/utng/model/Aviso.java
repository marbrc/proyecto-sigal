package mx.utng.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Modelo para representar un aviso / incidencia dentro de la pantalla
 * "Avisos" (fx_avisos.fxml). Mapea directamente a la tabla tb_aviso.
 */
public class Aviso {
//AVISOOOOOOS
    private final SimpleIntegerProperty idAviso;
    private final SimpleStringProperty fecha;          // dd/MM/yyyy, para mostrar en la tabla
    private final SimpleStringProperty espacio;         // NombreEspacio, o "General" si no aplica
    private final SimpleStringProperty tipoAviso;        // Información | Advertencia | Error | Éxito
    private final SimpleStringProperty descripcion;
    private final SimpleStringProperty comentarios;
    private final SimpleStringProperty estado;
    private final SimpleStringProperty horaInicio;
    private final SimpleStringProperty horaTermino;

    private Integer idEspacio;   // null = aviso general, no ligado a un espacio
    private int idUsuario;       // quien registro el aviso

    public Aviso(int idAviso, String fecha, String espacio, String tipoAviso,
                 String descripcion, String comentarios, String estado, String horaInicio, String horaTermino) {
        this.idAviso = new SimpleIntegerProperty(idAviso);
        this.fecha = new SimpleStringProperty(fecha);
        this.espacio = new SimpleStringProperty(espacio);
        this.tipoAviso = new SimpleStringProperty(tipoAviso);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.comentarios = new SimpleStringProperty(comentarios);
        this.estado = new SimpleStringProperty(estado);
        this.horaInicio = new SimpleStringProperty(horaInicio);
        this.horaTermino = new SimpleStringProperty(horaTermino);
    }

    public int getIdAviso() { return idAviso.get(); }
    public void setIdAviso(int v) { idAviso.set(v); }
    public SimpleIntegerProperty idAvisoProperty() { return idAviso; }

    public String getFecha() { return fecha.get(); }
    public void setFecha(String v) { fecha.set(v); }
    public SimpleStringProperty fechaProperty() { return fecha; }

    public String getEspacio() { return espacio.get(); }
    public void setEspacio(String v) { espacio.set(v); }
    public SimpleStringProperty espacioProperty() { return espacio; }

    public String getTipoAviso() { return tipoAviso.get(); }
    public void setTipoAviso(String v) { tipoAviso.set(v); }
    public SimpleStringProperty tipoAvisoProperty() { return tipoAviso; }

    public String getDescripcion() { return descripcion.get(); }
    public void setDescripcion(String v) { descripcion.set(v); }
    public SimpleStringProperty descripcionProperty() { return descripcion; }

    public String getComentarios() { return comentarios.get(); }
    public void setComentarios(String v) { comentarios.set(v); }
    public SimpleStringProperty comentariosProperty() { return comentarios; }

    public String getEstado() { return estado.get(); }
    public void setEstado(String v) { estado.set(v); }
    public SimpleStringProperty estadoProperty() { return estado; }

    public Integer getIdEspacio() { return idEspacio; }
    public void setIdEspacio(Integer idEspacio) { this.idEspacio = idEspacio; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getHoraInicio() { return horaInicio.get(); }
    public void setHoraInicio(String v) { horaInicio.set(v); }
    public SimpleStringProperty horaInicioProperty() { return horaInicio; }

    public String getHoraTermino() { return horaTermino.get(); }
    public void setHoraTermino(String v) { horaTermino.set(v); }
    public SimpleStringProperty horaTerminoProperty() { return horaTermino; }
}
