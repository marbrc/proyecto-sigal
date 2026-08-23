package mx.utng.dao;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
 
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mx.utng.database.Conexion;
import mx.utng.model.EspacioRegistro;
 
/**
 * Acceso a datos de la pantalla "Registro de Espacios" (fx_espacios.fxml).
 *
 * OJO: en tu base de datos la tabla se llama tb_espado (no tb_espacio) y
 * sus columnas de texto llevan el mismo sufijo "Espado":
 * ID_Espacio, ClaveEspado, NombreEspado, TipoEspado, CapacidadMaxima,
 * Estado, Descripcion. Este DAO usa esos nombres tal cual los tienes en
 * phpMyAdmin.
 */
public class EspacioDAO {


        public EspacioRegistro buscarPorId(int idEspacio) {
        String sql = """
                SELECT ID_Espacio, ClaveEspacio, NombreEspacio, TipoEspacio,
                       CapacidadMaxima, Estado, Descripcion
                FROM tb_espacio
                WHERE ID_Espacio = ?
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEspacio);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    EspacioRegistro espacio = new EspacioRegistro(
                            rs.getString("ClaveEspacio"),
                            rs.getString("NombreEspacio"),
                            rs.getString("TipoEspacio"),
                            rs.getInt("CapacidadMaxima"),
                            rs.getString("Estado"),
                            rs.getString("Descripcion")
                    );
                    espacio.setIdEspacio(rs.getInt("ID_Espacio"));
                    return espacio;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // ============================================================
    //  LISTAR (tabla "Espacios registrados")
    // ============================================================
 
    public ObservableList<EspacioRegistro> listarTodos() {
        ObservableList<EspacioRegistro> lista = FXCollections.observableArrayList();
 
        String sql = """
                SELECT ID_Espacio, ClaveEspacio, NombreEspacio, TipoEspacio,
                       CapacidadMaxima, Estado, Descripcion
                FROM tb_espacio
                ORDER BY NombreEspacio
                """;
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                EspacioRegistro espacio = new EspacioRegistro(
                        rs.getString("ClaveEspacio"),
                        rs.getString("NombreEspacio"),
                        rs.getString("TipoEspacio"),
                        rs.getInt("CapacidadMaxima"),
                        rs.getString("Estado"),
                        rs.getString("Descripcion")
                );
                espacio.setIdEspacio(rs.getInt("ID_Espacio"));
                lista.add(espacio);
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
        }
 
        return lista;
    }
 
    // ============================================================
    //  INSERTAR (boton "Guardar" en modo creacion)
    // ============================================================
 
    /**
     * Inserta un nuevo espacio en tb_espado.
     *
     * @return true si se guardo correctamente
     */
    public boolean insertar(EspacioRegistro e) {
        String sql = """
                INSERT INTO tb_espacio (ClaveEspacio, NombreEspacio, TipoEspacio, CapacidadMaxima, Estado, Descripcion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
 
            ps.setString(1, e.getClave());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getTipo());
            ps.setInt(4, e.getCapacidad());
            ps.setString(5, e.getEstado());
            ps.setString(6, e.getDescripcion());
 
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                return false;
            }
 
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    e.setIdEspacio(llaves.getInt(1));
                }
            }
            return true;
 
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
 
    // ============================================================
    //  ACTUALIZAR (boton "Editar" -> "Guardar cambios")
    // ============================================================
 
    public boolean actualizar(int idEspacio, EspacioRegistro e) {
        String sql = """
                UPDATE tb_espacio SET
                    ClaveEspacio = ?, NombreEspacio = ?, TipoEspacio = ?,
                    CapacidadMaxima = ?, Estado = ?, Descripcion = ?
                WHERE ID_Espacio = ?
                """;
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, e.getClave());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getTipo());
            ps.setInt(4, e.getCapacidad());
            ps.setString(5, e.getEstado());
            ps.setString(6, e.getDescripcion());
            ps.setInt(7, idEspacio);
 
            return ps.executeUpdate() > 0;
 
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
 
    // ============================================================
    //  VALIDAR CLAVE UNICA
    // ============================================================
 
    /**
     * @param idAExcluir ID_Espacio que se debe ignorar en la busqueda
     *                   (el propio espacio cuando se esta editando), o
     *                   null si es un espacio nuevo.
     */
    public boolean existeClave(String clave, Integer idAExcluir) {
        String sql = (idAExcluir == null)
                ? "SELECT 1 FROM tb_espacio WHERE ClaveEspacio = ?"
                : "SELECT 1 FROM tb_espacio WHERE ClaveEspacio = ? AND ID_Espacio <> ?";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, clave);
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
    //  ELIMINAR
    // ============================================================
 
    /**
     * Elimina un espacio. Si el espacio tiene asignaciones (o avisos)
     * registrados, la base de datos rechaza el borrado por la llave
     * foránea; en ese caso devolvemos false en vez de lanzar la
     * excepción hacia arriba.
     */
    public boolean eliminar(int idEspacio) {
        String sql = "DELETE FROM tb_espacio WHERE ID_Espacio = ?";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idEspacio);
            return ps.executeUpdate() > 0;
 
        } catch (SQLIntegrityConstraintViolationException ex) {
            // El espacio tiene registros relacionados (asignaciones, avisos, etc.)
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  DASHBOARD (fx_inicio.fxml)
    // ============================================================

    /** Total de espacios registrados (tarjeta "Espacios"). */
    public int contarTotal() {
        String sql = "SELECT COUNT(*) AS Total FROM tb_espacio";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("Total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Espacios "Disponible" en tb_espacio que además, ahora mismo,
     * no tienen ninguna asignación en curso (tarjeta "Disponibles").
     */
    public ObservableList<EspacioRegistro> listarDisponiblesAhora() {
        ObservableList<EspacioRegistro> lista = FXCollections.observableArrayList();

        String sql = """
                SELECT e.ID_Espacio, e.ClaveEspacio, e.NombreEspacio, e.TipoEspacio,
                       e.CapacidadMaxima, e.Estado, e.Descripcion
                FROM tb_espacio e
                WHERE e.Estado = 'Disponible'
                  AND NOT EXISTS (
                        SELECT 1 FROM tb_asignacion a
                        WHERE a.ID_Espacio = e.ID_Espacio
                          AND a.Fecha = CURDATE()
                          AND a.Estado IN ('Asignado', 'Ocupado')
                          AND CURTIME() BETWEEN a.HoraInicio AND a.HoraTermino
                  )
                ORDER BY e.NombreEspacio
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                EspacioRegistro espacio = new EspacioRegistro(
                        rs.getString("ClaveEspacio"),
                        rs.getString("NombreEspacio"),
                        rs.getString("TipoEspacio"),
                        rs.getInt("CapacidadMaxima"),
                        rs.getString("Estado"),
                        rs.getString("Descripcion")
                );
                espacio.setIdEspacio(rs.getInt("ID_Espacio"));
                lista.add(espacio);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    /** Igual que listarDisponiblesAhora(), pero solo el número (para la tarjeta). */
    public int contarDisponiblesAhora() {
        return listarDisponiblesAhora().size();
    }
}
 
