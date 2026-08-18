package mx.utng.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mx.utng.database.Conexion;
import mx.utng.model.Carrera;

/**
 * Acceso a datos de la pantalla "Carreras" (fx_carreras.fxml).
 *
 * Usa EXACTAMENTE las columnas reales de tb_carrera:
 * ID_Carrera, NombreCarrera, ID_Area.
 *
 * tb_carrera.ID_Area tiene llave foránea hacia tb_area_academica
 * (permite NULL en la BD), así que todas las consultas que listan
 * carreras hacen LEFT JOIN con tb_area_academica para traer también
 * NombreArea (solo lectura, para mostrarlo en la tabla).
 */
public class CarreraDAO {

    private static final String SELECT_BASE = """
            SELECT c.ID_Carrera, c.NombreCarrera, c.ID_Area, a.NombreArea
            FROM tb_carrera c
            LEFT JOIN tb_area_academica a ON a.ID_Area = c.ID_Area
            """;

    // ============================================================
    //  LISTAR / BUSCAR
    // ============================================================

    public ObservableList<Carrera> listarCarreras() {
        ObservableList<Carrera> lista = FXCollections.observableArrayList();

        String sql = SELECT_BASE + " ORDER BY c.NombreCarrera";

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

    /** Recarga la tabla "Carreras registradas" desde la base de datos. */
    public ObservableList<Carrera> cargarTabla() {
        return listarCarreras();
    }

    public ObservableList<Carrera> buscarPorNombre(String texto) {
        ObservableList<Carrera> lista = FXCollections.observableArrayList();

        String filtro = "%" + (texto == null ? "" : texto.trim()) + "%";
        String sql = SELECT_BASE + " WHERE c.NombreCarrera LIKE ? ORDER BY c.NombreCarrera";

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

    /** Filtra carreras por área académica (ID_Area). null = sin filtrar por área. */
    public ObservableList<Carrera> filtrarPorArea(Integer idArea) {
        ObservableList<Carrera> lista = FXCollections.observableArrayList();

        String sql = SELECT_BASE + (idArea != null ? " WHERE c.ID_Area = ? " : " ") + " ORDER BY c.NombreCarrera";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (idArea != null) {
                ps.setInt(1, idArea);
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
    //  INSERTAR
    // ============================================================

    public boolean insertarCarrera(Carrera c, Integer idArea) {
        String sql = "INSERT INTO tb_carrera (NombreCarrera, ID_Area) VALUES (?, ?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNombreCarrera());
            if (idArea != null) {
                ps.setInt(2, idArea);
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                return false;
            }

            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    c.setIdCarrera(llaves.getInt(1));
                }
            }
            c.setIdArea(idArea);
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  ACTUALIZAR
    // ============================================================

    public boolean actualizarCarrera(int idCarrera, Carrera c, Integer idArea) {
        String sql = "UPDATE tb_carrera SET NombreCarrera = ?, ID_Area = ? WHERE ID_Carrera = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombreCarrera());
            if (idArea != null) {
                ps.setInt(2, idArea);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, idCarrera);

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
     * Elimina una carrera. Si tiene grupos, materias asociadas
     * (tb_materia_carrera) o asignaciones (tb_asignacion) registradas,
     * la base de datos rechaza el borrado por la llave foránea; en ese
     * caso devolvemos false en vez de lanzar la excepción hacia arriba.
     */
    public boolean eliminarCarrera(int idCarrera) {
        String sql = "DELETE FROM tb_carrera WHERE ID_Carrera = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCarrera);
            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException ex) {
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  VALIDAR NOMBRE (regla de negocio: sin duplicados, aunque la
    //  columna NombreCarrera no tiene UNIQUE KEY en la BD)
    // ============================================================

    public boolean existeNombre(String nombre, Integer idAExcluir) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }

        String sql = (idAExcluir == null)
                ? "SELECT 1 FROM tb_carrera WHERE NombreCarrera = ?"
                : "SELECT 1 FROM tb_carrera WHERE NombreCarrera = ? AND ID_Carrera <> ?";

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
    //  CARRERAS DISPONIBLES PARA VINCULAR (ComboBox de Grupos)
    // ============================================================

    /**
     * Lista las carreras de tb_carrera disponibles para vincular un
     * grupo. La clave del mapa es el texto que se muestra en el
     * ComboBox (NombreCarrera) y el valor es el ID_Carrera real.
     */
    public Map<String, Integer> listarCarrerasParaVincular() {
        Map<String, Integer> mapa = new LinkedHashMap<>();

        String sql = "SELECT ID_Carrera, NombreCarrera FROM tb_carrera ORDER BY NombreCarrera";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                mapa.put(rs.getString("NombreCarrera"), rs.getInt("ID_Carrera"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mapa;
    }

    // ============================================================
    //  Utilidades privadas
    // ============================================================

    private Carrera mapearFila(ResultSet rs) throws SQLException {
        Carrera c = new Carrera(rs.getString("NombreCarrera"), rs.getString("NombreArea"));
        c.setIdCarrera(rs.getInt("ID_Carrera"));
        int idArea = rs.getInt("ID_Area");
        c.setIdArea(rs.wasNull() ? null : idArea);
        return c;
    }
}
