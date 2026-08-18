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
import mx.utng.model.MateriaCarrera;

/**
 * Acceso a datos de la pantalla "Materias" (fx_materias.fxml), que en
 * realidad administra la tabla tb_materia_carrera: la relación real
 * entre una materia (tb_materia), la carrera a la que pertenece
 * (tb_carrera), el cuatrimestre en el que se imparte y,
 * opcionalmente, el profesor asignado (tb_profesor).
 *
 * Usa EXACTAMENTE las columnas reales de tb_materia_carrera:
 * ID_MateriaCarrera, ID_Materia, ID_Carrera, Cuatrimestre, ID_Profesor.
 * La combinación (ID_Materia, ID_Carrera) es UNIQUE KEY en la BD, así
 * que una misma materia sólo puede registrarse una vez por carrera.
 */
public class MateriaCarreraDAO {

    private static final String SELECT_BASE = """
            SELECT mc.ID_MateriaCarrera, mc.ID_Materia, mc.ID_Carrera, mc.Cuatrimestre, mc.ID_Profesor,
                   m.Nombre AS NombreMateria,
                   c.NombreCarrera,
                   p.Nombre AS NombreProfesor, p.ApellidoPaterno AS ApellidoPaternoProfesor
            FROM tb_materia_carrera mc
            INNER JOIN tb_materia m ON m.ID_Materia = mc.ID_Materia
            INNER JOIN tb_carrera c ON c.ID_Carrera = mc.ID_Carrera
            LEFT JOIN tb_profesor p ON p.ID_Profesor = mc.ID_Profesor
            """;

    // ============================================================
    //  LISTAR / BUSCAR / FILTRAR
    // ============================================================

    public ObservableList<MateriaCarrera> listarRelaciones() {
        ObservableList<MateriaCarrera> lista = FXCollections.observableArrayList();

        String sql = SELECT_BASE + " ORDER BY c.NombreCarrera, mc.Cuatrimestre, m.Nombre";

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

    /** Recarga la tabla "Materias por carrera" desde la base de datos. */
    public ObservableList<MateriaCarrera> cargarTabla() {
        return listarRelaciones();
    }

    public ObservableList<MateriaCarrera> buscarPorMateria(String texto) {
        ObservableList<MateriaCarrera> lista = FXCollections.observableArrayList();

        String filtro = "%" + (texto == null ? "" : texto.trim()) + "%";
        String sql = SELECT_BASE + " WHERE m.Nombre LIKE ? ORDER BY c.NombreCarrera, mc.Cuatrimestre, m.Nombre";

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

    /**
     * Filtra por carrera y/o cuatrimestre. Cualquiera de los dos
     * parámetros puede ser null para no filtrar por ese criterio.
     * Pensado para los ComboBox "Filtrar por carrera" / "Filtrar por
     * cuatrimestre" del panel derecho, dado que hay muchas materias
     * (103 materias, 224 relaciones) y sin filtros es difícil ubicarlas.
     */
    public ObservableList<MateriaCarrera> filtrar(Integer idCarrera, Integer cuatrimestre) {
        ObservableList<MateriaCarrera> lista = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder(SELECT_BASE).append(" WHERE 1 = 1 ");
        if (idCarrera != null) {
            sql.append(" AND mc.ID_Carrera = ? ");
        }
        if (cuatrimestre != null) {
            sql.append(" AND mc.Cuatrimestre = ? ");
        }
        sql.append(" ORDER BY c.NombreCarrera, mc.Cuatrimestre, m.Nombre");

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int indice = 1;
            if (idCarrera != null) {
                ps.setInt(indice++, idCarrera);
            }
            if (cuatrimestre != null) {
                ps.setInt(indice++, cuatrimestre);
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

    public boolean insertarRelacion(int idMateria, int idCarrera, int cuatrimestre, Integer idProfesor) {
        String sql = """
                INSERT INTO tb_materia_carrera (ID_Materia, ID_Carrera, Cuatrimestre, ID_Profesor)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idMateria);
            ps.setInt(2, idCarrera);
            ps.setInt(3, cuatrimestre);
            if (idProfesor != null) {
                ps.setInt(4, idProfesor);
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException ex) {
            // Ya existe esa materia registrada en esa misma carrera (UQ_Materia_Carrera)
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  ACTUALIZAR
    // ============================================================

    public boolean actualizarRelacion(int idMateriaCarrera, int idMateria, int idCarrera, int cuatrimestre, Integer idProfesor) {
        String sql = """
                UPDATE tb_materia_carrera SET
                    ID_Materia = ?, ID_Carrera = ?, Cuatrimestre = ?, ID_Profesor = ?
                WHERE ID_MateriaCarrera = ?
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMateria);
            ps.setInt(2, idCarrera);
            ps.setInt(3, cuatrimestre);
            if (idProfesor != null) {
                ps.setInt(4, idProfesor);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, idMateriaCarrera);

            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException ex) {
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  ELIMINAR
    // ============================================================

    public boolean eliminarRelacion(int idMateriaCarrera) {
        String sql = "DELETE FROM tb_materia_carrera WHERE ID_MateriaCarrera = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMateriaCarrera);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  VALIDAR RELACIÓN ÚNICA (UQ_Materia_Carrera: ID_Materia + ID_Carrera)
    // ============================================================

    /**
     * @param idAExcluir ID_MateriaCarrera que se debe ignorar en la
     *                    búsqueda (la propia relación cuando se está
     *                    editando), o null si es una relación nueva.
     */
    public boolean existeRelacion(int idMateria, int idCarrera, Integer idAExcluir) {
        String sql = (idAExcluir == null)
                ? "SELECT 1 FROM tb_materia_carrera WHERE ID_Materia = ? AND ID_Carrera = ?"
                : "SELECT 1 FROM tb_materia_carrera WHERE ID_Materia = ? AND ID_Carrera = ? AND ID_MateriaCarrera <> ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMateria);
            ps.setInt(2, idCarrera);
            if (idAExcluir != null) {
                ps.setInt(3, idAExcluir);
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
    //  PROFESORES DISPONIBLES PARA VINCULAR (ComboBox del formulario)
    // ============================================================

    /**
     * Lista los profesores de tb_profesor disponibles para vincular
     * una relación materia-carrera. La clave del mapa es el texto que
     * se muestra en el ComboBox ("Nombre ApellidoPaterno") y el valor
     * es el ID_Profesor real.
     */
    public Map<String, Integer> listarProfesoresParaVincular() {
        Map<String, Integer> mapa = new LinkedHashMap<>();

        String sql = """
                SELECT ID_Profesor, Nombre, ApellidoPaterno
                FROM tb_profesor
                ORDER BY Nombre, ApellidoPaterno
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String texto = rs.getString("Nombre") + " " + rs.getString("ApellidoPaterno");
                mapa.put(texto, rs.getInt("ID_Profesor"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mapa;
    }

    // ============================================================
    //  Utilidades privadas
    // ============================================================

    private MateriaCarrera mapearFila(ResultSet rs) throws SQLException {
        String nombreProfesor = rs.getString("NombreProfesor");
        String apellidoProfesor = rs.getString("ApellidoPaternoProfesor");
        String nombreProfesorCompleto = (nombreProfesor == null)
                ? null
                : (nombreProfesor + " " + (apellidoProfesor == null ? "" : apellidoProfesor)).trim();

        MateriaCarrera mc = new MateriaCarrera(
                rs.getInt("Cuatrimestre"),
                rs.getString("NombreMateria"),
                rs.getString("NombreCarrera"),
                nombreProfesorCompleto
        );
        mc.setIdMateriaCarrera(rs.getInt("ID_MateriaCarrera"));
        mc.setIdMateria(rs.getInt("ID_Materia"));
        mc.setIdCarrera(rs.getInt("ID_Carrera"));
        int idProfesor = rs.getInt("ID_Profesor");
        mc.setIdProfesor(rs.wasNull() ? null : idProfesor);
        return mc;
    }
}
