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
            u.setNotificaciones(rs.getString("Notificaciones"));            return u;
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

}