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
import mx.utng.model.Grupo;

/**
 * Acceso a datos de la pantalla "Grupos" (fx_grupos.fxml).
 *
 * Usa EXACTAMENTE las columnas reales de tb_grupo:
 * ID_Grupo, NombreGrupo, Capacidad, Cuatrimestre, Turno, ID_Carrera.
 *
 * tb_grupo.ID_Carrera es NOT NULL con llave foránea hacia
 * tb_carrera, así que todas las consultas que listan grupos hacen
 * INNER JOIN con tb_carrera para traer también NombreCarrera (solo
 * lectura, para mostrarlo en la tabla y en los filtros).
 */
public class GrupoDAO {

    private static final String SELECT_BASE = """
            SELECT g.ID_Grupo, g.NombreGrupo, g.Capacidad, g.Cuatrimestre, g.Turno, g.ID_Carrera,
                   c.NombreCarrera
            FROM tb_grupo g
            INNER JOIN tb_carrera c ON c.ID_Carrera = g.ID_Carrera
            """;

    // ============================================================
    //  LISTAR / BUSCAR
    // ============================================================

    public ObservableList<Grupo> listarGrupos() {
        ObservableList<Grupo> lista = FXCollections.observableArrayList();

        String sql = SELECT_BASE + " ORDER BY g.NombreGrupo";

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

    /** Recarga la tabla "Grupos registrados" desde la base de datos. */
    public ObservableList<Grupo> cargarTabla() {
        return listarGrupos();
    }

    public ObservableList<Grupo> buscarPorNombre(String texto) {
        ObservableList<Grupo> lista = FXCollections.observableArrayList();

        String filtro = "%" + (texto == null ? "" : texto.trim()) + "%";
        String sql = SELECT_BASE + " WHERE g.NombreGrupo LIKE ? OR c.NombreCarrera LIKE ? ORDER BY g.NombreGrupo";

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

    /**
     * Filtra grupos por carrera y/o turno. Cualquiera de los dos
     * parámetros puede ser null para no filtrar por ese criterio.
     */
    public ObservableList<Grupo> filtrarGrupos(Integer idCarrera, String turno) {
        ObservableList<Grupo> lista = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder(SELECT_BASE).append(" WHERE 1 = 1 ");
        if (idCarrera != null) {
            sql.append(" AND g.ID_Carrera = ? ");
        }
        if (turno != null && !turno.isBlank()) {
            sql.append(" AND g.Turno = ? ");
        }
        sql.append(" ORDER BY g.NombreGrupo");

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int indice = 1;
            if (idCarrera != null) {
                ps.setInt(indice++, idCarrera);
            }
            if (turno != null && !turno.isBlank()) {
                ps.setString(indice++, turno);
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

    public boolean insertarGrupo(Grupo g, int idCarrera) {
        String sql = """
                INSERT INTO tb_grupo (NombreGrupo, Capacidad, Cuatrimestre, Turno, ID_Carrera)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, g.getNombreGrupo());
            ps.setInt(2, g.getCapacidad());
            ps.setInt(3, g.getCuatrimestre());
            ps.setString(4, g.getTurno());
            ps.setInt(5, idCarrera);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                return false;
            }

            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    g.setIdGrupo(llaves.getInt(1));
                }
            }
            g.setIdCarrera(idCarrera);
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  ACTUALIZAR
    // ============================================================

    public boolean actualizarGrupo(int idGrupo, Grupo g, int idCarrera) {
        String sql = """
                UPDATE tb_grupo SET
                    NombreGrupo = ?, Capacidad = ?, Cuatrimestre = ?, Turno = ?, ID_Carrera = ?
                WHERE ID_Grupo = ?
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, g.getNombreGrupo());
            ps.setInt(2, g.getCapacidad());
            ps.setInt(3, g.getCuatrimestre());
            ps.setString(4, g.getTurno());
            ps.setInt(5, idCarrera);
            ps.setInt(6, idGrupo);

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
     * Elimina un grupo. Si el grupo tiene asignaciones registradas
     * (tb_asignacion.ID_Grupo), la base de datos rechaza el borrado
     * por la llave foránea; en ese caso devolvemos false en vez de
     * lanzar la excepción hacia arriba.
     */
    public boolean eliminarGrupo(int idGrupo) {
        String sql = "DELETE FROM tb_grupo WHERE ID_Grupo = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idGrupo);
            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException ex) {
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  CARRERAS DISPONIBLES PARA VINCULAR (ComboBox del formulario)
    // ============================================================

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

    private Grupo mapearFila(ResultSet rs) throws SQLException {
        Grupo g = new Grupo(
                rs.getString("NombreGrupo"),
                rs.getInt("Capacidad"),
                rs.getInt("Cuatrimestre"),
                rs.getString("Turno"),
                rs.getString("NombreCarrera")
        );
        g.setIdGrupo(rs.getInt("ID_Grupo"));
        g.setIdCarrera(rs.getInt("ID_Carrera"));
        return g;
    }
}
