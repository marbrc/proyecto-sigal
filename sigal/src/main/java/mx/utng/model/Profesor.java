package mx.utng.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Modelo para representar un profesor dentro de la tabla
 * "Profesores registrados" (fx_profesores.fxml).
 *
 * Mapea EXACTAMENTE las columnas reales de tb_profesor:
 * ID_Profesor, Nombre, ApellidoPaterno, ApellidoMaterno,
 * CorreoElectronico, ID_Usuario.
 *
 * tb_profesor.ID_Usuario es NOT NULL con llave foránea hacia
 * tb_usuario (FK_Profesor_Usuario), por lo que todo profesor debe
 * estar vinculado a una cuenta de tb_usuario ya existente. Los campos
 * nombreUsuarioVinculado / rolUsuarioVinculado son datos de solo
 * lectura que vienen de un JOIN con tb_usuario (columnas NombreUsuario
 * y Rol) y se usan únicamente para mostrarlos en la tabla; no se
 * guardan como columnas propias de tb_profesor.
 */
public class Profesor {

    /** ID_Profesor real en tb_profesor (0 = todavía no se ha guardado en BD). */
    private int idProfesor;

    /** ID_Usuario vinculado (llave foránea obligatoria hacia tb_usuario). */
    private int idUsuario;

    private final SimpleStringProperty nombre;
    private final SimpleStringProperty apellidoPaterno;
    private final SimpleStringProperty apellidoMaterno;
    private final SimpleStringProperty correoElectronico;

    /** Solo lectura: viene del JOIN con tb_usuario (NombreUsuario). */
    private final SimpleStringProperty nombreUsuarioVinculado;
    /** Solo lectura: viene del JOIN con tb_usuario (Rol). */
    private final SimpleStringProperty rolUsuarioVinculado;

    /** Constructor vacío. */
    public Profesor() {
        this("", "", "", "", "", "");
    }

    /** Constructor completo. */
    public Profesor(String nombre, String apellidoPaterno, String apellidoMaterno,
                     String correoElectronico, String nombreUsuarioVinculado, String rolUsuarioVinculado) {
        this.nombre = new SimpleStringProperty(nombre == null ? "" : nombre);
        this.apellidoPaterno = new SimpleStringProperty(apellidoPaterno == null ? "" : apellidoPaterno);
        this.apellidoMaterno = new SimpleStringProperty(apellidoMaterno == null ? "" : apellidoMaterno);
        this.correoElectronico = new SimpleStringProperty(correoElectronico == null ? "" : correoElectronico);
        this.nombreUsuarioVinculado = new SimpleStringProperty(nombreUsuarioVinculado == null ? "" : nombreUsuarioVinculado);
        this.rolUsuarioVinculado = new SimpleStringProperty(rolUsuarioVinculado == null ? "" : rolUsuarioVinculado);
    }

    // ==================== Getters y setters ====================

    public int getIdProfesor() { return idProfesor; }
    public void setIdProfesor(int v) { this.idProfesor = v; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int v) { this.idUsuario = v; }

    public String getNombre() { return nombre.get(); }
    public void setNombre(String v) { nombre.set(v == null ? "" : v); }
    public SimpleStringProperty nombreProperty() { return nombre; }

    public String getApellidoPaterno() { return apellidoPaterno.get(); }
    public void setApellidoPaterno(String v) { apellidoPaterno.set(v == null ? "" : v); }
    public SimpleStringProperty apellidoPaternoProperty() { return apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno.get(); }
    public void setApellidoMaterno(String v) { apellidoMaterno.set(v == null ? "" : v); }
    public SimpleStringProperty apellidoMaternoProperty() { return apellidoMaterno; }

    public String getCorreoElectronico() { return correoElectronico.get(); }
    public void setCorreoElectronico(String v) { correoElectronico.set(v == null ? "" : v); }
    public SimpleStringProperty correoElectronicoProperty() { return correoElectronico; }

    public String getNombreUsuarioVinculado() { return nombreUsuarioVinculado.get(); }
    public void setNombreUsuarioVinculado(String v) { nombreUsuarioVinculado.set(v == null ? "" : v); }
    public SimpleStringProperty nombreUsuarioVinculadoProperty() { return nombreUsuarioVinculado; }

    public String getRolUsuarioVinculado() { return rolUsuarioVinculado.get(); }
    public void setRolUsuarioVinculado(String v) { rolUsuarioVinculado.set(v == null ? "" : v); }
    public SimpleStringProperty rolUsuarioVinculadoProperty() { return rolUsuarioVinculado; }

    /** Nombre completo (Nombre + ApellidoPaterno + ApellidoMaterno), usado en la tabla y en filtros. */
    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();
        if (getNombre() != null && !getNombre().isBlank()) {
            sb.append(getNombre().trim());
        }
        if (getApellidoPaterno() != null && !getApellidoPaterno().isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(getApellidoPaterno().trim());
        }
        if (getApellidoMaterno() != null && !getApellidoMaterno().isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(getApellidoMaterno().trim());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
