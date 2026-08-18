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
import mx.utng.model.AreaAcademica;

/**
 * Acceso a datos de la pantalla "Área Académica" (fx_area_academica.fxml).
 *
 * Usa EXACTAMENTE las columnas reales de tb_area_academica:
 * ID_Area, NombreArea.
 *
 * Sigue el mismo patrón que ProfesorDAO: SELECT base reutilizado,
 * ObservableList para la tabla y manejo de la excepción de llave
 * foránea al eliminar (una carrera puede depender de un área).
 */
public class AreaAcademicaDAO {

    /** SELECT base con el conteo de carreras que agrupa cada área (LEFT JOIN + COUNT). */
    private static final String SELECT_BASE = """
            SELECT a.ID_Area, a.NombreArea, COUNT(c.ID_Carrera) AS TotalCarreras
            FROM tb_area_academica a
            LEFT JOIN tb_carrera c ON c.ID_Area = a.ID_Area
            GROUP BY a.ID_Area, a.NombreArea
            """;

    // ============================================================
    //  LISTAR / BUSCAR
    // ============================================================

    public ObservableList<AreaAcademica> listarAreas() {
        ObservableList<AreaAcademica> lista = FXCollections.observableArrayList();

        String sql = SELECT_BASE + " ORDER BY a.NombreArea";

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

    /** Recarga la tabla "Áreas académicas registradas" desde la base de datos. */
    public ObservableList<AreaAcademica> cargarTabla() {
        return listarAreas();
    }

    public ObservableList<AreaAcademica> buscarPorNombre(String texto) {
        ObservableList<AreaAcademica> lista = FXCollections.observableArrayList();

        String filtro = "%" + (texto == null ? "" : texto.trim()) + "%";
        String sql = SELECT_BASE + " HAVING a.NombreArea LIKE ? ORDER BY a.NombreArea";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, filtro);

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

    public boolean insertarArea(AreaAcademica a) {
        String sql = "INSERT INTO tb_area_academica (NombreArea) VALUES (?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getNombreArea());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                return false;
            }

            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    a.setIdArea(llaves.getInt(1));
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

    public boolean actualizarArea(int idArea, AreaAcademica a) {
        String sql = "UPDATE tb_area_academica SET NombreArea = ? WHERE ID_Area = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, a.getNombreArea());
            ps.setInt(2, idArea);

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
     * Elimina un área académica. Si el área tiene carreras registradas
     * (tb_carrera.ID_Area), la base de datos rechaza el borrado por la
     * llave foránea; en ese caso devolvemos false en vez de lanzar la
     * excepción hacia arriba.
     */
    public boolean eliminarArea(int idArea) {
        String sql = "DELETE FROM tb_area_academica WHERE ID_Area = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idArea);
            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException ex) {
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  VALIDAR NOMBRE ÚNICO (NombreArea tiene UNIQUE KEY en la BD)
    // ============================================================

    public boolean existeNombre(String nombre, Integer idAExcluir) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }

        String sql = (idAExcluir == null)
                ? "SELECT 1 FROM tb_area_academica WHERE NombreArea = ?"
                : "SELECT 1 FROM tb_area_academica WHERE NombreArea = ? AND ID_Area <> ?";

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
    //  ÁREAS DISPONIBLES PARA VINCULAR (ComboBox de Carreras)
    // ============================================================

    /**
     * Lista las áreas de tb_area_academica disponibles para vincular
     * una carrera. La clave del mapa es el texto que se muestra en el
     * ComboBox (NombreArea) y el valor es el ID_Area real.
     */
    public Map<String, Integer> listarAreasParaVincular() {
        Map<String, Integer> mapa = new LinkedHashMap<>();

        String sql = "SELECT ID_Area, NombreArea FROM tb_area_academica ORDER BY NombreArea";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                mapa.put(rs.getString("NombreArea"), rs.getInt("ID_Area"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mapa;
    }

    // ============================================================
    //  Utilidades privadas
    // ============================================================

    private AreaAcademica mapearFila(ResultSet rs) throws SQLException {
        AreaAcademica a = new AreaAcademica(rs.getString("NombreArea"));
        a.setIdArea(rs.getInt("ID_Area"));
        a.setTotalCarreras(rs.getInt("TotalCarreras"));
        return a;
    }
}
