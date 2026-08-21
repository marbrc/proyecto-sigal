package mx.utng.dao;
 
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mx.utng.database.Conexion;
import mx.utng.model.AsignacionHorario;
import mx.utng.model.Asignaciones;
 
/**
 * Acceso a datos de la pantalla "Asignaciones" (fx_asignaciones.fxml).
 * Maneja tb_asignacion y sus catálogos relacionados: tb_espacio, tb_materia,
 * tb_grupo, tb_carrera y tb_profesor.
 */
public class AsignacionDAO {
 
    private static final DateTimeFormatter FORMATO_FECHA_UI = DateTimeFormatter.ofPattern("dd/MM/yyyy");
 
    // ============================================================
    //  CATÁLOGOS PARA LOS COMBOS DEL FORMULARIO
    // ============================================================
 
    public Map<String, Integer> listarEspacios() {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        String sql = "SELECT ID_Espacio, NombreEspacio FROM tb_espacio ORDER BY NombreEspacio";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                mapa.put(rs.getString("NombreEspacio"), rs.getInt("ID_Espacio"));
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mapa;
    }
    public Integer obtenerCapacidadEspacio(int idEspacio) {
    String sql = "SELECT CapacidadMaxima FROM tb_espacio WHERE ID_Espacio = ?";
    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idEspacio);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("CapacidadMaxima");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}
 
    public Map<String, Integer> listarProfesores() {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        String sql = "SELECT ID_Profesor, Nombre, ApellidoPaterno, ApellidoMaterno "
                + "FROM tb_profesor ORDER BY Nombre";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                String nombreCompleto = (rs.getString("Nombre") + " "
                        + rs.getString("ApellidoPaterno") + " "
                        + rs.getString("ApellidoMaterno")).trim();
                mapa.put(nombreCompleto, rs.getInt("ID_Profesor"));
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mapa;
    }
 public java.util.List<String> listarNombresPersonalPorTipo(String tipoPersonal) {
    java.util.List<String> lista = new java.util.ArrayList<>();
    String sql = "SELECT Nombre, ApellidoPaterno, ApellidoMaterno FROM tb_profesor "
               + "WHERE TipoPersonal = ? ORDER BY Nombre";

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, tipoPersonal);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nombreCompleto = (rs.getString("Nombre") + " "
                        + rs.getString("ApellidoPaterno") + " "
                        + rs.getString("ApellidoMaterno")).trim();
                lista.add(nombreCompleto);
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return lista;
}
public List<String> listarMateriasPorCuatrimestreYCarrera(int cuatrimestre, int idCarrera) {
    List<String> lista = new ArrayList<>();
    String sql = """
            SELECT m.Nombre
            FROM tb_materia_carrera mc
            JOIN tb_materia m ON m.ID_Materia = mc.ID_Materia
            WHERE mc.Cuatrimestre = ? AND mc.ID_Carrera = ?
            ORDER BY m.Nombre
            """;

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, cuatrimestre);
        ps.setInt(2, idCarrera);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(rs.getString("Nombre"));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return lista;
}
public List<String> listarGruposPorCuatrimestre(int cuatrimestre) {
    List<String> lista = new ArrayList<>();
    String sql = "SELECT NombreGrupo FROM tb_grupo WHERE Cuatrimestre = ? ORDER BY NombreGrupo";

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, cuatrimestre);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(rs.getString("NombreGrupo"));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return lista;
}
    /** Nombre -> ID_Carrera, para llenar/relacionar cmbCarrera con el catálogo real. */
    public Map<String, Integer> listarCarreras() {
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
 
    /** Nombres de materias del catálogo (tb_materia), para llenar cmbMateria. */
    public List<String> listarMaterias() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT Nombre FROM tb_materia ORDER BY Nombre";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                lista.add(rs.getString("Nombre"));
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
 
    /** Nombres de grupos del catálogo (tb_grupo), para llenar cmbGrupo. */
    public List<String> listarGrupos() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT NombreGrupo FROM tb_grupo ORDER BY NombreGrupo";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                lista.add(rs.getString("NombreGrupo"));
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
 
    /** Nombres de profesores dados de alta en la sección de Profesores (tb_profesor). */
    public List<String> listarNombresProfesores() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT Nombre, ApellidoPaterno, ApellidoMaterno FROM tb_profesor ORDER BY Nombre";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                String nombreCompleto = (rs.getString("Nombre") + " "
                        + rs.getString("ApellidoPaterno") + " "
                        + rs.getString("ApellidoMaterno")).trim();
                lista.add(nombreCompleto);
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
 
    /** Nombres de solicitantes ya usados antes, para sugerirlos en el combo (sin duplicados). */
    public List<String> listarNombresSolicitantes() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT NombreSolicitante FROM tb_asignacion "
                   + "WHERE NombreSolicitante IS NOT NULL AND NombreSolicitante <> '' "
                   + "ORDER BY NombreSolicitante";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                lista.add(rs.getString("NombreSolicitante"));
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
 
    /** Espacios de un tipo específico (Aula común / Lab. de cómputo / etc.), para el combo dependiente. */
    public Map<String, Integer> listarEspaciosPorTipo(String tipo) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        String sql = "SELECT ID_Espacio, NombreEspacio FROM tb_espacio WHERE TipoEspacio = ? ORDER BY NombreEspacio";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mapa.put(rs.getString("NombreEspacio"), rs.getInt("ID_Espacio"));
                }
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mapa;
    }
 
    /** Tipo de un espacio ya guardado, para precargar el formulario al editar. */
    public String obtenerTipoEspacio(String nombreEspacio) {
        String sql = "SELECT TipoEspacio FROM tb_espacio WHERE NombreEspacio = ?";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, nombreEspacio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("TipoEspacio");
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
 
    /**
     * true si ya existe una asignación vigente (no cancelada) en ese espacio
     * y fecha, cuyo horario se traslapa con el que se quiere guardar.
     *
     * @param idAsignacionExcluir al editar, pasa el ID de la asignación que
     *                             se está actualizando para no chocar consigo misma;
     *                             al crear una nueva, pasa -1.
     */
    public boolean existeConflictoHorario(int idEspacio, LocalDate fecha,
                                           String horaInicio, String horaTermino,
                                           int idAsignacionExcluir) {
        String sql = """
                SELECT COUNT(*) FROM tb_asignacion
                WHERE ID_Espacio = ?
                  AND Fecha = ?
                  AND Estado <> 'Cancelado'
                  AND ID_Asignacion <> ?
                  AND HoraInicio < ?
                  AND HoraTermino > ?
                """;
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idEspacio);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setInt(3, idAsignacionExcluir);
            ps.setTime(4, Time.valueOf(horaTermino + ":00"));
            ps.setTime(5, Time.valueOf(horaInicio + ":00"));
 
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // ante la duda, no dejar guardar y que revises el error en consola
        }
    }
 
    // ============================================================
    //  RESOLVER CATÁLOGOS (Materia / Grupo / Carrera)
    //  cmbMateria, cmbGrupo y cmbCarrera solo dejan elegir nombres que
    //  ya existen en el catálogo, así que aquí los convertimos a su
    //  ID_Materia / ID_Grupo / ID_Carrera real para guardarlos en
    //  tb_asignacion. Si el modelo ya trae el ID (porque el controller
    //  lo puso), se usa ese directo; si no, se busca por nombre.
    // ============================================================
 public Asignaciones buscarPorId(int idAsignacion) {
    for (Asignaciones a : listarTodas()) {
        if (a.getIdAsignacion() == idAsignacion) {
            return a;
        }
    }
    return null;
}
    private Integer buscarIdMateria(Connection con, String nombreMateria) throws SQLException {
        if (nombreMateria == null || nombreMateria.isBlank()) return null;
        String sql = "SELECT ID_Materia FROM tb_materia WHERE Nombre = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreMateria.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }
 
    private Integer buscarIdGrupo(Connection con, String nombreGrupo) throws SQLException {
        if (nombreGrupo == null || nombreGrupo.isBlank()) return null;
        String sql = "SELECT ID_Grupo FROM tb_grupo WHERE NombreGrupo = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreGrupo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }
 
    private Integer buscarIdCarrera(Connection con, String nombreCarrera) throws SQLException {
        if (nombreCarrera == null || nombreCarrera.isBlank()) return null;
        String sql = "SELECT ID_Carrera FROM tb_carrera WHERE NombreCarrera = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreCarrera.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }
 
    /** Usa el ID que ya trae el modelo (idMateria > 0); si no, lo busca por nombre en tb_materia. */
    private Integer resolverIdMateria(Connection con, Asignaciones a) throws SQLException {
        if (a.getIdMateria() > 0) return a.getIdMateria();
        return buscarIdMateria(con, a.getMateria());
    }
 
    private Integer resolverIdGrupo(Connection con, Asignaciones a) throws SQLException {
        if (a.getIdGrupo() > 0) return a.getIdGrupo();
        return buscarIdGrupo(con, a.getGrupo());
    }
 
    private Integer resolverIdCarrera(Connection con, Asignaciones a) throws SQLException {
        if (a.getIdCarrera() > 0) return a.getIdCarrera();
        return buscarIdCarrera(con, a.getCarrera());
    }
 
    // ============================================================
    //  INSERTAR
    // ============================================================
 
    public boolean insertar(Asignaciones a, int idUsuario, int idEspacio) {
 
        String sql = """
                INSERT INTO tb_asignacion
                    (TipoUsuario, NombreSolicitante, ID_Materia, ID_Grupo, NumAlumnos, Fecha,
                     HoraInicio, HoraTermino, Actividad, Estado, ID_Carrera, ID_Usuario, ID_Espacio)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
 
        try (Connection con = Conexion.conectar()) {
 
            Integer idMateria = resolverIdMateria(con, a);
            Integer idGrupo = resolverIdGrupo(con, a);
            Integer idCarrera = resolverIdCarrera(con, a);
 
            if (idMateria == null) {
                System.err.println("[Asignaciones] No se encontró la materia \"" + a.getMateria() + "\" en tb_materia.");
                return false;
            }
            if (idGrupo == null) {
                System.err.println("[Asignaciones] No se encontró el grupo \"" + a.getGrupo() + "\" en tb_grupo.");
                return false;
            }
            if (idCarrera == null) {
                System.err.println("[Asignaciones] No se encontró la carrera \"" + a.getCarrera() + "\" en tb_carrera.");
                return false;
            }
 
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
 
                ps.setString(1, a.getTipoSolicitante());
                ps.setString(2, a.getNombreSolicitante());
                ps.setInt(3, idMateria);
                ps.setInt(4, idGrupo);
                ps.setInt(5, Integer.parseInt(a.getNumAlumnos()));
                ps.setDate(6, Date.valueOf(LocalDate.parse(a.getFecha(), FORMATO_FECHA_UI)));
                ps.setTime(7, Time.valueOf(a.getHoraInicio() + ":00"));
                ps.setTime(8, Time.valueOf(a.getHoraTermino() + ":00"));
                ps.setString(9, a.getActividad());
                ps.setString(10, "Asignado");
                ps.setInt(11, idCarrera);
                ps.setInt(12, idUsuario);
                ps.setInt(13, idEspacio);
 
                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) return false;
 
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGenerado = rs.getInt(1);
                        a.setIdAsignacion(idGenerado);
                        a.setId("ASG-" + String.format("%04d", idGenerado));
                    }
                }
                return true;
            }
 
        } catch (NumberFormatException e) {
            System.err.println("NumAlumnos no contiene un número válido.");
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    // ============================================================
    //  ACTUALIZAR
    // ============================================================
 
    public boolean actualizar(int idAsignacion, Asignaciones a, int idUsuario, int idEspacio) {
 
        String sql = """
                UPDATE tb_asignacion SET
                    TipoUsuario = ?, NombreSolicitante = ?, ID_Materia = ?, ID_Grupo = ?, NumAlumnos = ?,
                    Fecha = ?, HoraInicio = ?, HoraTermino = ?, Actividad = ?, ID_Carrera = ?,
                    ID_Usuario = ?, ID_Espacio = ?
                WHERE ID_Asignacion = ?
                """;
 
        try (Connection con = Conexion.conectar()) {
 
            Integer idMateria = resolverIdMateria(con, a);
            Integer idGrupo = resolverIdGrupo(con, a);
            Integer idCarrera = resolverIdCarrera(con, a);
 
            if (idMateria == null) {
                System.err.println("[Asignaciones] No se encontró la materia \"" + a.getMateria() + "\" en tb_materia.");
                return false;
            }
            if (idGrupo == null) {
                System.err.println("[Asignaciones] No se encontró el grupo \"" + a.getGrupo() + "\" en tb_grupo.");
                return false;
            }
            if (idCarrera == null) {
                System.err.println("[Asignaciones] No se encontró la carrera \"" + a.getCarrera() + "\" en tb_carrera.");
                return false;
            }
 
            try (PreparedStatement ps = con.prepareStatement(sql)) {
 
                ps.setString(1, a.getTipoSolicitante());
                ps.setString(2, a.getNombreSolicitante());
                ps.setInt(3, idMateria);
                ps.setInt(4, idGrupo);
                ps.setInt(5, Integer.parseInt(a.getNumAlumnos()));
                ps.setDate(6, Date.valueOf(LocalDate.parse(a.getFecha(), FORMATO_FECHA_UI)));
                ps.setTime(7, Time.valueOf(a.getHoraInicio() + ":00"));
                ps.setTime(8, Time.valueOf(a.getHoraTermino() + ":00"));
                ps.setString(9, a.getActividad());
                ps.setInt(10, idCarrera);
                ps.setInt(11, idUsuario);
                ps.setInt(12, idEspacio);
                ps.setInt(13, idAsignacion);
 
                return ps.executeUpdate() > 0;
            }
 
        } catch (NumberFormatException e) {
            System.err.println("NumAlumnos no contiene un número válido.");
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    // ============================================================
    //  LISTAR (tabla "Asignaciones registradas")
    // ============================================================
 
public ObservableList<Asignaciones> listarTodas() {

    ObservableList<Asignaciones> lista = FXCollections.observableArrayList();

    String sql = """
            SELECT a.ID_Asignacion, a.TipoUsuario, a.NombreSolicitante,
                   m.Nombre AS Materia, g.NombreGrupo AS Grupo,
                   a.NumAlumnos, a.Fecha, a.HoraInicio, a.HoraTermino, a.Actividad, a.Estado,
                   c.NombreCarrera AS Carrera, e.NombreEspacio,
                   mc.Cuatrimestre AS Cuatrimestre
            FROM tb_asignacion a
            LEFT JOIN tb_carrera c ON c.ID_Carrera = a.ID_Carrera
            LEFT JOIN tb_grupo g ON g.ID_Grupo = a.ID_Grupo
            LEFT JOIN tb_materia m ON m.ID_Materia = a.ID_Materia
            LEFT JOIN tb_materia_carrera mc ON mc.ID_Materia = a.ID_Materia AND mc.ID_Carrera = a.ID_Carrera
            INNER JOIN tb_espacio e ON e.ID_Espacio = a.ID_Espacio
            ORDER BY a.Fecha DESC, a.HoraInicio DESC
            """;

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
 
    // ============================================================
    //  DASHBOARD (fx_inicio.fxml)
    // ============================================================
 
    public int contarDeHoy() {
        String sql = "SELECT COUNT(*) AS Total FROM tb_asignacion WHERE Fecha = CURDATE() AND Estado <> 'Cancelado'";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            if (rs.next()) return rs.getInt("Total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
 
    public boolean cancelar(int idAsignacion) {
        String sql = "UPDATE tb_asignacion SET Estado = 'Cancelado' WHERE ID_Asignacion = ?";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idAsignacion);
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /** Carreras que tienen al menos un grupo en el cuatrimestre indicado. */
public Map<String, Integer> listarCarrerasPorCuatrimestre(int cuatrimestre) {
    Map<String, Integer> mapa = new LinkedHashMap<>();
    String sql = "SELECT DISTINCT c.ID_Carrera, c.NombreCarrera " +
                 "FROM tb_carrera c " +
                 "INNER JOIN tb_grupo g ON g.ID_Carrera = c.ID_Carrera " +
                 "WHERE g.Cuatrimestre = ? " +
                 "ORDER BY c.NombreCarrera";
    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, cuatrimestre);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mapa.put(rs.getString("NombreCarrera"), rs.getInt("ID_Carrera"));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return mapa;
}
public List<String> listarGruposPorCuatrimestreYCarrera(int cuatrimestre, int idCarrera) {
    List<String> lista = new ArrayList<>();
    String sql = "SELECT NombreGrupo FROM tb_grupo WHERE Cuatrimestre = ? AND ID_Carrera = ? ORDER BY NombreGrupo";
    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, cuatrimestre);
        ps.setInt(2, idCarrera);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(rs.getString("NombreGrupo"));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return lista;
}

/** Grupos de un cuatrimestre Y carrera específicos. */

 
    /**
     * Inserción rápida usada por la pantalla de Horarios (clic directo sobre una
     * celda libre): solo pide lo indispensable. Grupo y Carrera quedan en NULL
     * (son opcionales en tb_asignacion); Materia es opcional y se resuelve por
     * nombre contra tb_materia si se captura.
     */
    public boolean insertarRapido(int idUsuario, int idEspacio, LocalDate fecha,
                                   LocalTime horaInicio, LocalTime horaTermino,
                                   String tipoSolicitante, String nombreSolicitante, String materia) {
 
        String sql = """
                INSERT INTO tb_asignacion
                    (TipoUsuario, NombreSolicitante, ID_Materia, Fecha, HoraInicio, HoraTermino,
                     Estado, ID_Usuario, ID_Espacio)
                VALUES (?, ?, ?, ?, ?, ?, 'Asignado', ?, ?)
                """;
 
        try (Connection con = Conexion.conectar()) {
 
            Integer idMateria = (materia == null || materia.isBlank()) ? null : buscarIdMateria(con, materia);
 
            try (PreparedStatement ps = con.prepareStatement(sql)) {
 
                ps.setString(1, tipoSolicitante);
                ps.setString(2, nombreSolicitante);
                if (idMateria == null) {
                    ps.setNull(3, Types.INTEGER);
                } else {
                    ps.setInt(3, idMateria);
                }
                ps.setDate(4, Date.valueOf(fecha));
                ps.setTime(5, Time.valueOf(horaInicio));
                ps.setTime(6, Time.valueOf(horaTermino));
                ps.setInt(7, idUsuario);
                ps.setInt(8, idEspacio);
 
                return ps.executeUpdate() > 0;
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    public ObservableList<Asignaciones> listarDeHoy() {
 
        ObservableList<Asignaciones> lista = FXCollections.observableArrayList();
 
        String sql = """
        SELECT a.ID_Asignacion, a.TipoUsuario, a.NombreSolicitante,
               m.Nombre AS Materia, g.NombreGrupo AS Grupo,
               a.NumAlumnos, a.Fecha, a.HoraInicio, a.HoraTermino, a.Actividad, a.Estado,
               c.NombreCarrera AS Carrera, e.NombreEspacio,
               mc.Cuatrimestre AS Cuatrimestre
        FROM tb_asignacion a
        LEFT JOIN tb_carrera c ON c.ID_Carrera = a.ID_Carrera
        LEFT JOIN tb_grupo g ON g.ID_Grupo = a.ID_Grupo
        LEFT JOIN tb_materia m ON m.ID_Materia = a.ID_Materia
        LEFT JOIN tb_materia_carrera mc ON mc.ID_Materia = a.ID_Materia AND mc.ID_Carrera = a.ID_Carrera
        INNER JOIN tb_espacio e ON e.ID_Espacio = a.ID_Espacio
        ORDER BY a.Fecha DESC, a.HoraInicio DESC
        """;
 
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
     * Asignaciones de un dia especifico (para el calendario del Inicio:
     * al hacer clic en un dia se listan las asignaciones de esa fecha).
     */
public ObservableList<Asignaciones> listarPorFecha(LocalDate fecha) {

    ObservableList<Asignaciones> lista = FXCollections.observableArrayList();

    String sql = """
            SELECT a.ID_Asignacion, a.TipoUsuario, a.NombreSolicitante,
                   m.Nombre AS Materia, g.NombreGrupo AS Grupo,
                   a.NumAlumnos, a.Fecha, a.HoraInicio, a.HoraTermino, a.Actividad, a.Estado,
                   c.NombreCarrera AS Carrera, e.NombreEspacio,
                   mc.Cuatrimestre AS Cuatrimestre
            FROM tb_asignacion a
            LEFT JOIN tb_carrera c ON c.ID_Carrera = a.ID_Carrera
            LEFT JOIN tb_grupo g ON g.ID_Grupo = a.ID_Grupo
            LEFT JOIN tb_materia m ON m.ID_Materia = a.ID_Materia
            LEFT JOIN tb_materia_carrera mc ON mc.ID_Materia = a.ID_Materia AND mc.ID_Carrera = a.ID_Carrera
            INNER JOIN tb_espacio e ON e.ID_Espacio = a.ID_Espacio
            WHERE a.Fecha = ? AND a.Estado <> 'Cancelado'
            ORDER BY a.HoraInicio ASC
            """;

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setDate(1, Date.valueOf(fecha));

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
     * Dias del mes (1-31) que tienen al menos una asignacion activa, para
     * pintar el puntito de "Asignado" debajo del numero en el calendario
     * del Inicio.
     */
    public java.util.Set<Integer> diasConAsignacionesEnMes(java.time.YearMonth mes) {

        java.util.Set<Integer> dias = new java.util.HashSet<>();

        String sql = """
                SELECT DISTINCT DAY(a.Fecha) AS Dia
                FROM tb_asignacion a
                WHERE a.Estado <> 'Cancelado'
                  AND a.Fecha BETWEEN ? AND ?
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(mes.atDay(1)));
            ps.setDate(2, Date.valueOf(mes.atEndOfMonth()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dias.add(rs.getInt("Dia"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dias;
    }



    ///672 CAMBIAR AQUIII MAAAR MAR MAR PONER La parte esaa de ls logica asi bn padre aqui empiezaaaaa

        /**
     * Proximas asignaciones activas a partir de este momento (incluye las
     * que siguen en curso hoy), ordenadas por fecha y hora. Usado en el
     * panel lateral de la pantalla de Bienvenida.
     */
    public ObservableList<Asignaciones> listarProximas(int limite) {

        ObservableList<Asignaciones> lista = FXCollections.observableArrayList();

        String sql = """
                SELECT a.ID_Asignacion, a.TipoUsuario, a.NombreSolicitante,
                       m.Nombre AS Materia, g.NombreGrupo AS Grupo,
                       a.NumAlumnos, a.Fecha, a.HoraInicio, a.HoraTermino, a.Actividad, a.Estado,
                       c.NombreCarrera AS Carrera, e.NombreEspacio
                FROM tb_asignacion a
                LEFT JOIN tb_carrera c ON c.ID_Carrera = a.ID_Carrera
                LEFT JOIN tb_grupo g ON g.ID_Grupo = a.ID_Grupo
                LEFT JOIN tb_materia m ON m.ID_Materia = a.ID_Materia
                INNER JOIN tb_espacio e ON e.ID_Espacio = a.ID_Espacio
                WHERE (a.Fecha > CURDATE() OR (a.Fecha = CURDATE() AND a.HoraTermino >= CURTIME()))
                  AND a.Estado <> 'Cancelado'
                ORDER BY a.Fecha ASC, a.HoraInicio ASC
                LIMIT ?
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limite);

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


// Y AQUI TERMINA SISISISIISI 718 OKISIIISS



private Asignaciones mapearFila(ResultSet rs) throws SQLException {

    Date fechaBD = rs.getDate("Fecha");
    Time horaInicioBD = rs.getTime("HoraInicio");
    Time horaTerminoBD = rs.getTime("HoraTermino");

    String carrera = rs.getString("Carrera");
    if (carrera == null || carrera.isBlank()) carrera = "—";

    String materia = rs.getString("Materia");
    if (materia == null || materia.isBlank()) materia = "—";

    String grupo = rs.getString("Grupo");
    if (grupo == null || grupo.isBlank()) grupo = "—";

    int cuatrimestreBD = rs.getInt("Cuatrimestre");
    boolean cuatrimestreNulo = rs.wasNull();

    Asignaciones asg = new Asignaciones(
            "ASG-" + String.format("%04d", rs.getInt("ID_Asignacion")),
            fechaBD.toLocalDate().format(FORMATO_FECHA_UI),
            horaInicioBD.toLocalTime().toString().substring(0, 5),
            horaTerminoBD.toLocalTime().toString().substring(0, 5),
            rs.getString("NombreEspacio"),
            rs.getString("TipoUsuario"),
            rs.getString("NombreSolicitante"),
            "—",
            carrera,
            materia,
            grupo,
            String.valueOf(rs.getInt("NumAlumnos")),
            rs.getString("Actividad"),
            rs.getString("Estado")
    );
    asg.setIdAsignacion(rs.getInt("ID_Asignacion"));
    asg.setCuatrimestre(cuatrimestreNulo ? "" : String.valueOf(cuatrimestreBD));
    return asg;
}

    // ============================================================
    //  ELIMINAR (borrar)
    // ============================================================
 
    public boolean eliminar(int idAsignacion) {
        String sql = "DELETE FROM tb_asignacion WHERE ID_Asignacion = ?";
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, idAsignacion);
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
    // ============================================================
    //  HORARIOS (fx_horarios.fxml)
    // ============================================================
 
    /**
     * @param idEspacio si es null, trae de TODOS los espacios; si trae un
     *                  valor, filtra solo ese espacio.
     */
    public List<AsignacionHorario> listarParaHorario(Integer idEspacio, LocalDate desde, LocalDate hasta) {
        List<AsignacionHorario> lista = new ArrayList<>();
 
        String sql = """
                SELECT a.ID_Asignacion, a.NombreSolicitante, m.Nombre AS Materia, a.Fecha,
                       a.HoraInicio, a.HoraTermino, a.Estado,
                       e.NombreEspacio,
                       CONCAT_WS(' ', p.Nombre, p.ApellidoPaterno, p.ApellidoMaterno) AS NombreProfesor
                FROM tb_asignacion a
                JOIN tb_espacio e ON e.ID_Espacio = a.ID_Espacio
                LEFT JOIN tb_profesor p ON p.ID_Profesor = a.ID_Profesor
                LEFT JOIN tb_materia m ON m.ID_Materia = a.ID_Materia
                WHERE a.Fecha BETWEEN ? AND ?
                  AND a.Estado IN ('Asignado','Ocupado')
                  AND (? IS NULL OR a.ID_Espacio = ?)
                ORDER BY a.Fecha, a.HoraInicio
                """;
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            if (idEspacio != null) {
                ps.setInt(3, idEspacio);
                ps.setInt(4, idEspacio);
            } else {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            }
 
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nombreProfesor = rs.getString("NombreProfesor");
                    String nombreSolicitante = rs.getString("NombreSolicitante");
                    String docente = (nombreProfesor != null && !nombreProfesor.isBlank())
                            ? nombreProfesor
                            : (nombreSolicitante == null ? "" : nombreSolicitante);
 
                    lista.add(new AsignacionHorario(
                            rs.getInt("ID_Asignacion"),
                            rs.getDate("Fecha").toLocalDate(),
                            rs.getTime("HoraInicio").toLocalTime(),
                            rs.getTime("HoraTermino").toLocalTime(),
                            rs.getString("NombreEspacio"),
                            docente,
                            rs.getString("Materia"),
                            rs.getString("Estado")
                    ));
                }
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
        }
 
        return lista;
    }
}