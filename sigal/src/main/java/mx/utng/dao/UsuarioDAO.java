package mx.utng.dao;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
 
import mx.utng.database.Conexion;
import mx.utng.model.Usuario;
 
public class UsuarioDAO {
 
    public Usuario validar(String usuario, String contrasena) {
        String sql = """
                SELECT *
                FROM tb_usuario
                WHERE NombreUsuario = ?
                AND Contrasena = ?
                AND Estado = 'Activo'
                """;
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, usuario);
            ps.setString(2, contrasena);
 
          ResultSet rs = ps.executeQuery();
          if (rs.next()) { 
            Usuario u = new Usuario();
 
            u.setIdUsuario(rs.getInt("ID_Usuario"));
            u.setNombre(rs.getString("Nombre"));
            u.setApellidoPaterno(rs.getString("ApellidoPaterno"));
            u.setApellidoMaterno(rs.getString("ApellidoMaterno"));
            u.setNombre(rs.getString("Nombre"));            u.setCorreoElectronico(rs.getString("CorreoElectronico"));
            u.setRol(rs.getString("Rol"));
            u.setEstado(rs.getString("Estado"));
            u.setTema(rs.getString("Tema"));
            u.setNotificaciones(rs.getString("Notificaciones"));
            u.setFotoPerfil(rs.getBytes("FotoPerfil"));
            return u;
        }
        
        return null;
 
        } catch (SQLException e) {
 
            e.printStackTrace();
            return null;
 
        }
 
    }
 
    /** Busca un usuario activo que coincida con NombreUsuario y CorreoElectronico (para "olvidé mi contraseña"). */
       /** Busca un usuario activo que coincida con NombreUsuario y CorreoElectronico (para "olvidé mi contraseña"). */
    public Usuario buscarPorUsuarioYCorreo(String nombreUsuario, String correo) {
        String sql = """
                SELECT *
                FROM tb_usuario
                WHERE NombreUsuario = ?
                AND CorreoElectronico = ?
                AND Estado = 'Activo'
                """;
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, nombreUsuario);
            ps.setString(2, correo);
            ResultSet rs = ps.executeQuery();
 
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("ID_Usuario"));
                u.setNombre(rs.getString("Nombre"));
                u.setNombreUsuario(rs.getString("NombreUsuario"));
                u.setCorreoElectronico(rs.getString("CorreoElectronico"));
                return u;
            }
            return null;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
 
 
    // ============================================================
    //  PANTALLA "MI CUENTA"
    // ============================================================
 
    /** Trae los datos actuales del usuario en sesión, para precargar el formulario. */
    public Usuario obtenerPorId(int idUsuario) {
        String sql = "SELECT * FROM tb_usuario WHERE ID_Usuario = ?";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
 
            if (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("ID_Usuario"));
                u.setNombre(rs.getString("Nombre"));
                u.setApellidoPaterno(rs.getString("ApellidoPaterno"));
                u.setApellidoMaterno(rs.getString("ApellidoMaterno"));
                u.setNombreUsuario(rs.getString("NombreUsuario"));
                u.setCorreoElectronico(rs.getString("CorreoElectronico"));
                u.setRol(rs.getString("Rol"));
                u.setEstado(rs.getString("Estado"));
                u.setTema(rs.getString("Tema"));
                u.setNotificaciones(String.valueOf(rs.getBoolean("Notificaciones")));
                u.setFotoPerfil(rs.getBytes("FotoPerfil"));
                return u;
            }
            return null;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
 
    /** true si YA existe otro usuario (distinto de idUsuario) con ese NombreUsuario. */
    public boolean existeNombreUsuario(String nombreUsuario, int idUsuario) {
        String sql = "SELECT COUNT(*) FROM tb_usuario WHERE NombreUsuario = ? AND ID_Usuario <> ?";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, nombreUsuario);
            ps.setInt(2, idUsuario);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // ante la duda, no dejar guardar
        }
    }
 
    /** Actualiza nombre, apellidos, usuario y correo (todo menos la contraseña). */
    public boolean actualizarDatos(int idUsuario, String nombre, String apellidoPaterno,
                                    String apellidoMaterno, String nombreUsuario, String correoElectronico) {
 
        String sql = """
                UPDATE tb_usuario SET
                    Nombre = ?, ApellidoPaterno = ?, ApellidoMaterno = ?,
                    NombreUsuario = ?, CorreoElectronico = ?
                WHERE ID_Usuario = ?
                """;
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, nombre);
            ps.setString(2, apellidoPaterno);
            ps.setString(3, apellidoMaterno);
            ps.setString(4, nombreUsuario);
            ps.setString(5, correoElectronico);
            ps.setInt(6, idUsuario);
 
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    /** true si la contraseña ingresada coincide con la que hay guardada para ese usuario. */
    public boolean validarContrasenaActual(int idUsuario, String contrasenaIngresada) {
        String sql = "SELECT COUNT(*) FROM tb_usuario WHERE ID_Usuario = ? AND Contrasena = ?";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idUsuario);
            ps.setString(2, contrasenaIngresada);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    /** Actualiza únicamente la contraseña. Se debe llamar solo tras validarContrasenaActual(...). */
    public boolean actualizarContrasena(int idUsuario, String contrasenaNueva) {
        String sql = "UPDATE tb_usuario SET Contrasena = ? WHERE ID_Usuario = ?";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, contrasenaNueva);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    public boolean actualizarTema(int idUsuario, String tema) {
        String sql = "UPDATE tb_usuario SET Tema = ? WHERE ID_Usuario = ?";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, tema);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    /**
     * Guarda si las notificaciones (mantenimiento, recordatorios, reportes)
     * están activadas. tb_usuario.Notificaciones hoy es un solo tinyint(1),
     * así que representa "¿al menos una notificación activada?" en conjunto;
     * si más adelante se necesita guardar cada switch por separado, hay que
     * agregar columnas nuevas (ej. NotifMantenimiento, NotifRecordatorios,
     * NotifReportes) y un método que las reciba.
     */
    public boolean actualizarNotificaciones(int idUsuario, boolean activas) {
        String sql = "UPDATE tb_usuario SET Notificaciones = ? WHERE ID_Usuario = ?";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setBoolean(1, activas);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    /** Guarda (o borra, si fotoBytes es null) la foto de perfil del usuario. */
    public boolean actualizarFotoPerfil(int idUsuario, byte[] fotoBytes) {
        String sql = "UPDATE tb_usuario SET FotoPerfil = ? WHERE ID_Usuario = ?";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            if (fotoBytes == null) {
                ps.setNull(1, java.sql.Types.BLOB);
            } else {
                ps.setBytes(1, fotoBytes);
            }
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /** true si ya existe una cuenta con ese correo. */
    public boolean existeCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM tb_usuario WHERE CorreoElectronico = ?";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, correo);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }
 
    /** Inserta un nuevo usuario. */
    public boolean registrar(String nombre, String apellidoPaterno, String apellidoMaterno,
                              String nombreUsuario, String correo, String contrasena) {
        String sql = """
                INSERT INTO tb_usuario
                    (Nombre, ApellidoPaterno, ApellidoMaterno, NombreUsuario, CorreoElectronico, Contrasena)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, nombre);
            ps.setString(2, apellidoPaterno);
            ps.setString(3, apellidoMaterno);
            ps.setString(4, nombreUsuario);
            ps.setString(5, correo);
            ps.setString(6, contrasena);
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    public java.util.List<Usuario> listarUsuarios(int idExcluir) {
        String sql = "SELECT * FROM tb_usuario WHERE ID_Usuario <> ? ORDER BY Nombre, ApellidoPaterno";
        java.util.List<Usuario> lista = new java.util.ArrayList<>();
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idExcluir);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("ID_Usuario"));
                u.setNombre(rs.getString("Nombre"));
                u.setApellidoPaterno(rs.getString("ApellidoPaterno"));
                u.setApellidoMaterno(rs.getString("ApellidoMaterno"));
                u.setNombreUsuario(rs.getString("NombreUsuario"));
                u.setCorreoElectronico(rs.getString("CorreoElectronico"));
                u.setRol(rs.getString("Rol"));
                u.setEstado(rs.getString("Estado"));
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
 
    /** "Elimina" (desactiva) la cuenta de un usuario: Estado = 'Inactivo'. Ya no puede iniciar sesión, pero conserva su historial. */
    public boolean desactivarUsuario(int idUsuario) {
        String sql = "UPDATE tb_usuario SET Estado = 'Inactivo' WHERE ID_Usuario = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    /**
     * Elimina definitivamente la cuenta (borrado real de tb_usuario).
     * Si la cuenta tiene asignaciones registradas (tb_asignacion.ID_Usuario),
     * la restricción de llave foránea hace que el DELETE falle y este método
     * devuelva false; en ese caso conviene usar desactivarUsuario en su lugar.
     */
    public boolean eliminarUsuario(int idUsuario) {
        String sql = "DELETE FROM tb_usuario WHERE ID_Usuario = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    /** Reactiva una cuenta previamente desactivada. */
    public boolean reactivarUsuario(int idUsuario) {
        String sql = "UPDATE tb_usuario SET Estado = 'Activo' WHERE ID_Usuario = ?";
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
}