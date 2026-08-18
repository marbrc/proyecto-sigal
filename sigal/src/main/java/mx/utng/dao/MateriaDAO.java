package mx.utng.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mx.utng.database.Conexion;
import mx.utng.model.Materia;

/**
 * Acceso a datos de la pantalla "Materias" (fx_materias.fxml).
 *
 * Usa EXACTAMENTE las columnas reales de tb_materia:
 * ID_Materia, Nombre, Descripcion.
 */
public class MateriaDAO {

    private static final String SELECT_BASE = """
            SELECT ID_Materia, Nombre, Descripcion
            FROM tb_materia
            """;

    // ============================================================
    //  LISTAR / BUSCAR
    // ============================================================

    public ObservableList<Materia> listarMaterias() {
        ObservableList<Materia> lista = FXCollections.observableArrayList();

        String sql = SELECT_BASE + " ORDER BY Nombre";

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

    /** Recarga la tabla "Materias registradas" desde la base de datos. */
    public ObservableList<Materia> cargarTabla() {
        return listarMaterias();
    }

    public ObservableList<Materia> buscarPorNombre(String texto) {
        ObservableList<Materia> lista = FXCollections.observableArrayList();

        String filtro = "%" + (texto == null ? "" : texto.trim()) + "%";
        String sql = SELECT_BASE + " WHERE Nombre LIKE ? OR Descripcion LIKE ? ORDER BY Nombre";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, filtro);
            ps.setString(2, filtro);

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
    //  INSERTAR
    // ============================================================

    public boolean insertarMateria(Materia m) {
        String sql = "INSERT INTO tb_materia (Nombre, Descripcion) VALUES (?, ?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, m.getNombre());
            ps.setString(2, blankToNull(m.getDescripcion()));

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                return false;
            }

            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    m.setIdMateria(llaves.getInt(1));
                }
            }
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  ACTUALIZAR
    // ============================================================

    public boolean actualizarMateria(int idMateria, Materia m) {
        String sql = "UPDATE tb_materia SET Nombre = ?, Descripcion = ? WHERE ID_Materia = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, m.getNombre());
            ps.setString(2, blankToNull(m.getDescripcion()));
            ps.setInt(3, idMateria);

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
     * Elimina una materia. tb_materia_carrera tiene ON DELETE CASCADE
     * hacia ID_Materia, así que sus relaciones con carreras se borran
     * automáticamente; tb_asignacion.ID_Materia tiene ON DELETE SET
     * NULL, así que las asignaciones existentes simplemente quedan sin
     * materia. Por eso el borrado normalmente no lanza excepción de
     * llave foránea, pero igual se captura por seguridad.
     */
    public boolean eliminarMateria(int idMateria) {
        String sql = "DELETE FROM tb_materia WHERE ID_Materia = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMateria);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  VALIDAR NOMBRE ÚNICO (Nombre tiene UNIQUE KEY en la BD)
    // ============================================================

    public boolean existeNombre(String nombre, Integer idAExcluir) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }

        String sql = (idAExcluir == null)
                ? "SELECT 1 FROM tb_materia WHERE Nombre = ?"
                : "SELECT 1 FROM tb_materia WHERE Nombre = ? AND ID_Materia <> ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
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
    //  Utilidades privadas
    // ============================================================

    private Materia mapearFila(ResultSet rs) throws SQLException {
        Materia m = new Materia(rs.getString("Nombre"), rs.getString("Descripcion"));
        m.setIdMateria(rs.getInt("ID_Materia"));
        return m;
    }

    private String blankToNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
