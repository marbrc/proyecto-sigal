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
 * Maneja tb_asignacion y su único catálogo relacionado, tb_espacio.
 */
public class AsignacionDAO {

    private static final DateTimeFormatter FORMATO_FECHA_UI = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ============================================================
    //  CATÁLOGO PARA EL COMBO DE ESPACIO
    // ============================================================
    /** Inserta espacios de prueba (Salas, Laboratorios, Aulas) si aún no existen. */
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

public void sembrarEspaciosDePrueba() {
    String[][] espacios = {
        {"Sala Audiovisual", "Sala"},
        {"Sala X", "Sala"},
        {"Laboratorio I", "Laboratorio"},
        {"Laboratorio II", "Laboratorio"},
        {"Laboratorio III", "Laboratorio"},
        {"Laboratorio IV", "Laboratorio"},
        {"Aula 1", "Aula"},
        {"Aula 2", "Aula"},
        {"Aula 3", "Aula"},
        {"Aula 4", "Aula"},
        {"Aula 5", "Aula"},
        {"Aula 6", "Aula"},
        {"Aula 7", "Aula"},
        {"Aula 8", "Aula"},
        {"Aula 9", "Aula"}
    };

    String sqlExiste = "SELECT COUNT(*) FROM tb_espacio WHERE NombreEspacio = ?";
    String sqlInsertar = "INSERT INTO tb_espacio (NombreEspacio, Tipo) VALUES (?, ?)";

    try (Connection con = Conexion.conectar();
         PreparedStatement psExiste = con.prepareStatement(sqlExiste);
         PreparedStatement psInsertar = con.prepareStatement(sqlInsertar)) {

        for (String[] espacio : espacios) {
            psExiste.setString(1, espacio[0]);
            try (ResultSet rs = psExiste.executeQuery()) {
                boolean yaExiste = rs.next() && rs.getInt(1) > 0;
                if (!yaExiste) {
                    psInsertar.setString(1, espacio[0]);
                    psInsertar.setString(2, espacio[1]);
                    psInsertar.executeUpdate();
                }
            }
        }

        System.out.println("[SIGAL] Espacios de prueba verificados/insertados.");

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
/** Nombres de profesores dados de alta en la sección de Profesores (tb_profesor). */
public java.util.List<String> listarNombresProfesores() {
    java.util.List<String> lista = new java.util.ArrayList<>();
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
public java.util.List<String> listarNombresSolicitantes() {
    java.util.List<String> lista = new java.util.ArrayList<>();
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
/**
 * true si ya existe una asignación vigente (no cancelada) en ese espacio
 * y fecha, cuyo horario se traslapa con el que se quiere guardar.
 *
 * @param idAsignacionExcluir al editar, pasa el ID de la asignación que
 *                             se está actualizando para no chocar consigo misma;
 *                             al crear una nueva, pasa -1.
 */
public boolean existeConflictoHorario(int idEspacio, java.time.LocalDate fecha,
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

/** Espacios de un tipo específico (Laboratorio/Aula/Sala), para el combo dependiente. */
public Map<String, Integer> listarEspaciosPorTipo(String tipo) {
    Map<String, Integer> mapa = new LinkedHashMap<>();
    String sql = "SELECT ID_Espacio, NombreEspacio FROM tb_espacio WHERE Tipo = ? ORDER BY NombreEspacio";

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

/** Tipo (Laboratorio/Aula/Sala) de un espacio ya guardado, para precargar el formulario al editar. */
public String obtenerTipoEspacio(String nombreEspacio) {
    String sql = "SELECT Tipo FROM tb_espacio WHERE NombreEspacio = ?";

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, nombreEspacio);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("Tipo");
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

    // ============================================================
    //  INSERTAR
    // ============================================================

    public boolean insertar(Asignaciones a, int idUsuario, int idEspacio) {
        String sql = """
                INSERT INTO tb_asignacion
                    (TipoUsuario, NombreSolicitante, Materia, Grupo, NumAlumnos, Fecha,
                     HoraInicio, HoraTermino, Actividad, Estado, Carrera, ID_Usuario, ID_Espacio)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getTipoSolicitante());
            ps.setString(2, a.getNombreSolicitante());
            ps.setString(3, a.getMateria());
            ps.setString(4, a.getGrupo());
            ps.setInt(5, Integer.parseInt(a.getNumAlumnos()));
            ps.setDate(6, Date.valueOf(LocalDate.parse(a.getFecha(), FORMATO_FECHA_UI)));
            ps.setTime(7, Time.valueOf(a.getHoraInicio() + ":00"));
            ps.setTime(8, Time.valueOf(a.getHoraTermino() + ":00"));
            ps.setString(9, a.getActividad());
            ps.setString(10, "Asignado");
            ps.setString(11, a.getCarrera());
            ps.setInt(12, idUsuario);
            ps.setInt(13, idEspacio);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) return false;

            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    int idGenerado = llaves.getInt(1);
                    a.setIdAsignacion(idGenerado);
                    a.setId("ASG-" + String.format("%04d", idGenerado));
                }
            }
            return true;

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
                    TipoUsuario = ?, NombreSolicitante = ?, Materia = ?, Grupo = ?, NumAlumnos = ?,
                    Fecha = ?, HoraInicio = ?, HoraTermino = ?, Actividad = ?, Carrera = ?,
                    ID_Usuario = ?, ID_Espacio = ?
                WHERE ID_Asignacion = ?
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, a.getTipoSolicitante());
            ps.setString(2, a.getNombreSolicitante());
            ps.setString(3, a.getMateria());
            ps.setString(4, a.getGrupo());
            ps.setInt(5, Integer.parseInt(a.getNumAlumnos()));
            ps.setDate(6, Date.valueOf(LocalDate.parse(a.getFecha(), FORMATO_FECHA_UI)));
            ps.setTime(7, Time.valueOf(a.getHoraInicio() + ":00"));
            ps.setTime(8, Time.valueOf(a.getHoraTermino() + ":00"));
            ps.setString(9, a.getActividad());
            ps.setString(10, a.getCarrera());
            ps.setInt(11, idUsuario);
            ps.setInt(12, idEspacio);
            ps.setInt(13, idAsignacion);

            return ps.executeUpdate() > 0;

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
                SELECT a.ID_Asignacion, a.TipoUsuario, a.NombreSolicitante, a.Materia, a.Grupo,
                       a.NumAlumnos, a.Fecha, a.HoraInicio, a.HoraTermino, a.Actividad, a.Estado,
                       a.Carrera, e.NombreEspacio
                FROM tb_asignacion a
                JOIN tb_espacio e ON e.ID_Espacio = a.ID_Espacio
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
    public boolean insertarRapido(int idUsuario, int idEspacio, LocalDate fecha,
                                   LocalTime horaInicio, LocalTime horaTermino,
                                   String tipoSolicitante, String nombreSolicitante, String materia) {
 
        String sql = """
                INSERT INTO tb_asignacion
                    (TipoUsuario, NombreSolicitante, Materia, Fecha, HoraInicio, HoraTermino,
                     Estado, ID_Usuario, ID_Espacio)
                VALUES (?, ?, ?, ?, ?, ?, 'Asignado', ?, ?)
                """;
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, tipoSolicitante);
            ps.setString(2, nombreSolicitante);
            if (materia == null || materia.isBlank()) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, materia);
            }
            ps.setDate(4, Date.valueOf(fecha));
            ps.setTime(5, Time.valueOf(horaInicio));
            ps.setTime(6, Time.valueOf(horaTermino));
            ps.setInt(7, idUsuario);
            ps.setInt(8, idEspacio);
 
            return ps.executeUpdate() > 0;
 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ObservableList<Asignaciones> listarDeHoy() {
        ObservableList<Asignaciones> lista = FXCollections.observableArrayList();

        String sql = """
                SELECT a.ID_Asignacion, a.TipoUsuario, a.NombreSolicitante, a.Materia, a.Grupo,
                       a.NumAlumnos, a.Fecha, a.HoraInicio, a.HoraTermino, a.Actividad, a.Estado,
                       a.Carrera, e.NombreEspacio
                FROM tb_asignacion a
                JOIN tb_espacio e ON e.ID_Espacio = a.ID_Espacio
                WHERE a.Fecha = CURDATE() AND a.Estado <> 'Cancelado'
                ORDER BY a.HoraInicio ASC
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

    private Asignaciones mapearFila(ResultSet rs) throws SQLException {
        String carrera = rs.getString("Carrera");
        if (carrera == null || carrera.isBlank()) carrera = "—";

        Asignaciones asg = new Asignaciones(
                "ASG-" + String.format("%04d", rs.getInt("ID_Asignacion")),
                rs.getDate("Fecha").toLocalDate().format(FORMATO_FECHA_UI),
                rs.getTime("HoraInicio").toLocalTime().toString().substring(0, 5),
                rs.getTime("HoraTermino").toLocalTime().toString().substring(0, 5),
                rs.getString("NombreEspacio"),
                rs.getString("TipoUsuario"),
                rs.getString("NombreSolicitante"),
                "—",
                carrera,
                rs.getString("Materia"),
                rs.getString("Grupo"),
                String.valueOf(rs.getInt("NumAlumnos")),
                rs.getString("Actividad"),
                rs.getString("Estado")
        );
        asg.setIdAsignacion(rs.getInt("ID_Asignacion"));
        return asg;
    }

    // ============================================================
    //  ELIMINAR
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
 /**
     * @param idEspacio si es null, trae de TODOS los espacios; si trae un
     *                  valor, filtra solo ese espacio.
     */
    public List<AsignacionHorario> listarParaHorario(Integer idEspacio, LocalDate desde, LocalDate hasta) {
        List<AsignacionHorario> lista = new ArrayList<>();
 
        String sql = """
                SELECT a.ID_Asignacion, a.NombreSolicitante, a.Materia, a.Fecha,
                       a.HoraInicio, a.HoraTermino, a.Estado,
                       e.NombreEspacio,
                       CONCAT_WS(' ', p.Nombre, p.ApellidoPaterno, p.ApellidoMaterno) AS NombreProfesor
                FROM tb_asignacion a
                JOIN tb_espacio e ON e.ID_Espacio = a.ID_Espacio
                LEFT JOIN tb_profesor p ON p.ID_Profesor = a.ID_Profesor
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