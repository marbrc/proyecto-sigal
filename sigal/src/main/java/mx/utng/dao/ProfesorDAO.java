package mx.utng.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mx.utng.database.Conexion;
import mx.utng.model.Profesor;

/**
 * Acceso a datos de la pantalla "Profesores" (fx_profesores.fxml).
 *
 * Usa EXACTAMENTE las columnas reales de tb_profesor:
 * ID_Profesor, Nombre, ApellidoPaterno, ApellidoMaterno,
 * CorreoElectronico, ID_Usuario.
 *
 * tb_profesor.ID_Usuario es NOT NULL y tiene llave foránea hacia
 * tb_usuario (FK_Profesor_Usuario), así que todas las consultas que
 * listan profesores hacen INNER JOIN con tb_usuario para traer
 * también NombreUsuario y Rol (solo lectura, para mostrarlos en la
 * tabla y en los filtros).
 */
public class ProfesorDAO {

    /** SELECT base reutilizado por listarProfesores/buscarProfesorPorId/buscarPorNombre/filtros. */
    private static final String SELECT_BASE = """
        SELECT p.ID_Profesor, p.Nombre, p.ApellidoPaterno, p.ApellidoMaterno,
               p.CorreoElectronico, p.ID_Usuario, p.TipoPersonal,
               u.NombreUsuario, u.Rol
        FROM tb_profesor p
        INNER JOIN tb_usuario u ON u.ID_Usuario = p.ID_Usuario
        """;

    // ============================================================
    //  LISTAR (tabla "Profesores registrados")
    // ============================================================

    /** Trae todos los profesores registrados, ordenados por nombre. */
    public ObservableList<Profesor> listarProfesores() {
        ObservableList<Profesor> lista = FXCollections.observableArrayList();

        String sql = SELECT_BASE + " ORDER BY p.Nombre, p.ApellidoPaterno";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Recarga la tabla "Profesores registrados" desde la base de datos.
     * Es un alias explícito de listarProfesores(), pensado para usarse
     * cada vez que el JTable/TableView de profesores debe refrescarse
     * (al iniciar la pantalla o al presionar "Refrescar").
     */
    public ObservableList<Profesor> cargarTabla() {
        return listarProfesores();
    }

    // ============================================================
    //  BUSCAR POR ID
    // ============================================================

    public Profesor buscarProfesorPorId(int idProfesor) {
        String sql = SELECT_BASE + " WHERE p.ID_Profesor = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProfesor);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearFila(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ============================================================
    //  BUSCAR POR NOMBRE (usado por el cuadro de búsqueda)
    // ============================================================

    /**
     * Busca profesores cuyo nombre, apellidos o correo coincidan
     * (parcialmente, sin distinguir mayúsculas) con el texto dado.
     */
    public ObservableList<Profesor> buscarPorNombre(String texto) {
        ObservableList<Profesor> lista = FXCollections.observableArrayList();

        String filtro = "%" + (texto == null ? "" : texto.trim()) + "%";

        String sql = SELECT_BASE + """
                 WHERE p.Nombre LIKE ?
                    OR p.ApellidoPaterno LIKE ?
                    OR p.ApellidoMaterno LIKE ?
                    OR p.CorreoElectronico LIKE ?
                 ORDER BY p.Nombre, p.ApellidoPaterno
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setString(3, filtro);
            ps.setString(4, filtro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // ============================================================
    //  FILTROS (usuario vinculado / rol) para los ComboBox del panel
    //  "Profesores registrados"
    // ============================================================

    /**
     * Filtra profesores por ID_Usuario vinculado y/o por Rol de ese
     * usuario. Cualquiera de los dos parámetros puede ser null para
     * no filtrar por ese criterio.
     */
    public ObservableList<Profesor> filtrarProfesores(Integer idUsuario, String rol) {
        ObservableList<Profesor> lista = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder(SELECT_BASE).append(" WHERE 1 = 1 ");
        if (idUsuario != null) {
            sql.append(" AND p.ID_Usuario = ? ");
        }
        if (rol != null && !rol.isBlank()) {
            sql.append(" AND u.Rol = ? ");
        }
        sql.append(" ORDER BY p.Nombre, p.ApellidoPaterno");

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int indice = 1;
            if (idUsuario != null) {
                ps.setInt(indice++, idUsuario);
            }
            if (rol != null && !rol.isBlank()) {
                ps.setString(indice++, rol);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // ============================================================
    //  INSERTAR (botón "Guardar" en modo creación)
    // ============================================================

    /**
     * Inserta un nuevo profesor en tb_profesor.
     *
     * @param p         datos del profesor (nombre, apellidos, correo)
     * @param idUsuario ID_Usuario al que se vincula (obligatorio, llave foránea)
     * @return true si se guardó correctamente
     */
public boolean insertarProfesor(Profesor p, int idUsuario, String tipoPersonal) {
    String sql = """
            INSERT INTO tb_profesor (Nombre, ApellidoPaterno, ApellidoMaterno, CorreoElectronico, ID_Usuario, TipoPersonal)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setString(1, p.getNombre());
        ps.setString(2, p.getApellidoPaterno());
        ps.setString(3, blankToNull(p.getApellidoMaterno()));
        ps.setString(4, blankToNull(p.getCorreoElectronico()));
        ps.setInt(5, idUsuario);
        ps.setString(6, tipoPersonal);

        int filasAfectadas = ps.executeUpdate();
        if (filasAfectadas == 0) {
            return false;
        }

        try (ResultSet llaves = ps.getGeneratedKeys()) {
            if (llaves.next()) {
                p.setIdProfesor(llaves.getInt(1));
            }
        }
        p.setIdUsuario(idUsuario);
        return true;

    } catch (SQLException ex) {
        ex.printStackTrace();
        return false;
    }
}

public boolean actualizarProfesor(int idProfesor, Profesor p, int idUsuario, String tipoPersonal) {
    String sql = """
            UPDATE tb_profesor SET
                Nombre = ?, ApellidoPaterno = ?, ApellidoMaterno = ?,
                CorreoElectronico = ?, ID_Usuario = ?, TipoPersonal = ?
            WHERE ID_Profesor = ?
            """;

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, p.getNombre());
        ps.setString(2, p.getApellidoPaterno());
        ps.setString(3, blankToNull(p.getApellidoMaterno()));
        ps.setString(4, blankToNull(p.getCorreoElectronico()));
        ps.setInt(5, idUsuario);
        ps.setString(6, tipoPersonal);
        ps.setInt(7, idProfesor);

        return ps.executeUpdate() > 0;

    } catch (SQLException ex) {
        ex.printStackTrace();
        return false;
    }
}

    // ============================================================
    //  ELIMINAR
    // ============================================================

    /**
     * Elimina un profesor. Si el profesor tiene asignaciones
     * registradas (tb_asignacion.ID_Profesor), la base de datos
     * rechaza el borrado por la llave foránea; en ese caso devolvemos
     * false en vez de lanzar la excepción hacia arriba.
     */
    public boolean eliminarProfesor(int idProfesor) {
        String sql = "DELETE FROM tb_profesor WHERE ID_Profesor = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProfesor);
            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException ex) {
            // El profesor tiene asignaciones (u otros registros) relacionados
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  VALIDAR CORREO ÚNICO
    // ============================================================

    /**
     * @param correo     correo a validar (se ignora si viene vacío/null)
     * @param idAExcluir ID_Profesor que se debe ignorar en la búsqueda
     *                   (el propio profesor cuando se está editando), o
     *                   null si es un profesor nuevo.
     */
    public boolean existeCorreo(String correo, Integer idAExcluir) {
        if (correo == null || correo.isBlank()) {
            return false;
        }

        String sql = (idAExcluir == null)
                ? "SELECT 1 FROM tb_profesor WHERE CorreoElectronico = ?"
                : "SELECT 1 FROM tb_profesor WHERE CorreoElectronico = ? AND ID_Profesor <> ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo);
            if (idAExcluir != null) {
                ps.setInt(2, idAExcluir);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  USUARIOS DISPONIBLES PARA VINCULAR (ComboBox del formulario)
    // ============================================================

    /**
     * Lista los usuarios de tb_usuario disponibles para vincular a un
     * profesor. La clave del mapa es el texto que se muestra en el
     * ComboBox ("NombreUsuario — Nombre ApellidoPaterno (Rol)") y el
     * valor es el ID_Usuario real, siguiendo el mismo patrón que
     * AsignacionDAO.listarProfesores() usa en el resto del sistema.
     */
    public Map<String, Integer> listarUsuariosParaVincular() {
        Map<String, Integer> mapa = new LinkedHashMap<>();

        String sql = """
                SELECT ID_Usuario, NombreUsuario, Nombre, ApellidoPaterno, Rol
                FROM tb_usuario
                ORDER BY NombreUsuario
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String texto = rs.getString("NombreUsuario") + " — "
                        + rs.getString("Nombre") + " " + rs.getString("ApellidoPaterno")
                        + " (" + rs.getString("Rol") + ")";
                mapa.put(texto, rs.getInt("ID_Usuario"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mapa;
    }

    // ============================================================
    //  Utilidades privadas
    // ============================================================
    private String blankToNull(String valor) {
    return (valor == null || valor.isBlank()) ? null : valor.trim();
}

private Profesor mapearFila(ResultSet rs) throws SQLException {
    Profesor p = new Profesor(
            rs.getString("Nombre"),
            rs.getString("ApellidoPaterno"),
            rs.getString("ApellidoMaterno"),
            rs.getString("CorreoElectronico"),
            rs.getString("TipoPersonal"),
            rs.getString("NombreUsuario"),
            rs.getString("Rol")
    );
    p.setIdProfesor(rs.getInt("ID_Profesor"));
    p.setIdUsuario(rs.getInt("ID_Usuario"));
    return p;
}
}

