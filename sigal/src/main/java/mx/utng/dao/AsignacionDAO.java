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
 *
 * Maneja tb_asignacion y sus catálogos relacionados (tb_espacio,
 * tb_profesor, tb_carrera) para llenar los combos del formulario y
 * la tabla de asignaciones registradas.
 */
public class AsignacionDAO {
 
    private static final DateTimeFormatter FORMATO_FECHA_UI = DateTimeFormatter.ofPattern("dd/MM/yyyy");
 
    // ============================================================
    //  CATALOGOS PARA LOS COMBOS
    //  (texto que ve el usuario -> ID real que se guarda en la BD)
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
 
    // ============================================================
    //  INSERTAR (boton "Guardar")
    // ============================================================
 
    /**
     * Inserta una nueva asignación en tb_asignacion.
     *
     * @param a               datos capturados en el formulario
     * @param idUsuario       ID_Usuario en sesión (obligatorio en la BD)
     * @param idEspacio       ID_Espacio elegido en el combo (obligatorio)
     * @param idProfesor      ID_Profesor elegido, o null si "Sin profesor"
     * @param idCarrera       ID_Carrera elegida, o null si "Otra carrera"/no aplica
     * @param otraCarreraTexto texto libre cuando no hay ID_Carrera (puede ser null)
     * @return true si se guardó correctamente
     */
    public boolean insertar(Asignaciones a, int idUsuario, int idEspacio,
                             Integer idProfesor, Integer idCarrera, String otraCarreraTexto) {
 
        String sql = """
                INSERT INTO tb_asignacion
                    (TipoUsuario, NombreSolicitante, Materia, Grupo, NumAlumnos, Fecha,
                     HoraInicio, HoraTermino, Actividad, Estado, OtraCarrera,
                     ID_Carrera, ID_Profesor, ID_Usuario, ID_Espacio)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            // Estado en tb_asignacion es ENUM('Libre','Ocupado','Asignado','Cancelado');
            // una asignación recién guardada nace como "Asignado".
            ps.setString(10, "Asignado");
            ps.setString(11, otraCarreraTexto);
 
            if (idCarrera != null) ps.setInt(12, idCarrera); else ps.setNull(12, Types.INTEGER);
            if (idProfesor != null) ps.setInt(13, idProfesor); else ps.setNull(13, Types.INTEGER);
            ps.setInt(14, idUsuario);
            ps.setInt(15, idEspacio);
 
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                return false;
            }
 
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
    //  ACTUALIZAR (boton "Editar" -> "Guardar cambios")
    // ============================================================
 
    /**
     * Actualiza una asignación existente. No toca el campo Estado
     * (eso se maneja aparte, por ejemplo al cancelar una asignación).
     */
    public boolean actualizar(int idAsignacion, Asignaciones a, int idUsuario, int idEspacio,
                               Integer idProfesor, Integer idCarrera, String otraCarreraTexto) {
 
        String sql = """
                UPDATE tb_asignacion SET
                    TipoUsuario = ?, NombreSolicitante = ?, Materia = ?, Grupo = ?, NumAlumnos = ?,
                    Fecha = ?, HoraInicio = ?, HoraTermino = ?, Actividad = ?, OtraCarrera = ?,
                    ID_Carrera = ?, ID_Profesor = ?, ID_Usuario = ?, ID_Espacio = ?
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
            ps.setString(10, otraCarreraTexto);
 
            if (idCarrera != null) ps.setInt(11, idCarrera); else ps.setNull(11, Types.INTEGER);
            if (idProfesor != null) ps.setInt(12, idProfesor); else ps.setNull(12, Types.INTEGER);
            ps.setInt(13, idUsuario);
            ps.setInt(14, idEspacio);
            ps.setInt(15, idAsignacion);
 
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
                       a.OtraCarrera,
                       e.NombreEspacio,
                       CONCAT_WS(' ', p.Nombre, p.ApellidoPaterno, p.ApellidoMaterno) AS NombreProfesor,
                       c.NombreCarrera
                FROM tb_asignacion a
                JOIN tb_espacio e ON e.ID_Espacio = a.ID_Espacio
                LEFT JOIN tb_profesor p ON p.ID_Profesor = a.ID_Profesor
                LEFT JOIN tb_carrera c ON c.ID_Carrera = a.ID_Carrera
                ORDER BY a.Fecha DESC, a.HoraInicio DESC
                """;
 
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                String nombreProfesor = rs.getString("NombreProfesor");
                if (nombreProfesor == null || nombreProfesor.isBlank()) {
                    nombreProfesor = "—";
                }
 
                String carrera = rs.getString("NombreCarrera");
                if (carrera == null || carrera.isBlank()) {
                    String otra = rs.getString("OtraCarrera");
                    carrera = (otra == null || otra.isBlank()) ? "—" : otra;
                }
 
                Asignaciones asg = new Asignaciones(
                        "ASG-" + String.format("%04d", rs.getInt("ID_Asignacion")),
                        rs.getDate("Fecha").toLocalDate().format(FORMATO_FECHA_UI),
                        rs.getTime("HoraInicio").toLocalTime().toString().substring(0, 5),
                        rs.getTime("HoraTermino").toLocalTime().toString().substring(0, 5),
                        rs.getString("NombreEspacio"),
                        rs.getString("TipoUsuario"),
                        rs.getString("NombreSolicitante"),
                        nombreProfesor,
                        carrera,
                        rs.getString("Materia"),
                        rs.getString("Grupo"),
                        String.valueOf(rs.getInt("NumAlumnos")),
                        rs.getString("Actividad"),
                        rs.getString("Estado")
                );
                asg.setIdAsignacion(rs.getInt("ID_Asignacion"));
 
                lista.add(asg);
            }
 
        } catch (SQLException e) {
            e.printStackTrace();
        }
 
        return lista;
    }
 
    // ============================================================
    //  DASHBOARD (fx_inicio.fxml)
    // ============================================================

    /** Cuántas asignaciones hay hoy (para la tarjeta "Asignaciones"). Ignora las canceladas. */
    public int contarDeHoy() {
        String sql = "SELECT COUNT(*) AS Total FROM tb_asignacion WHERE Fecha = CURDATE() AND Estado <> 'Cancelado'";

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

    /** Las asignaciones de hoy, ordenadas por hora (para el modal de la tarjeta "Asignaciones"). */
    public ObservableList<Asignaciones> listarDeHoy() {
        ObservableList<Asignaciones> lista = FXCollections.observableArrayList();

        String sql = """
                SELECT a.ID_Asignacion, a.TipoUsuario, a.NombreSolicitante, a.Materia, a.Grupo,
                       a.NumAlumnos, a.Fecha, a.HoraInicio, a.HoraTermino, a.Actividad, a.Estado,
                       a.OtraCarrera,
                       e.NombreEspacio,
                       CONCAT_WS(' ', p.Nombre, p.ApellidoPaterno, p.ApellidoMaterno) AS NombreProfesor,
                       c.NombreCarrera
                FROM tb_asignacion a
                JOIN tb_espacio e ON e.ID_Espacio = a.ID_Espacio
                LEFT JOIN tb_profesor p ON p.ID_Profesor = a.ID_Profesor
                LEFT JOIN tb_carrera c ON c.ID_Carrera = a.ID_Carrera
                WHERE a.Fecha = CURDATE() AND a.Estado <> 'Cancelado'
                ORDER BY a.HoraInicio ASC
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String nombreProfesor = rs.getString("NombreProfesor");
                if (nombreProfesor == null || nombreProfesor.isBlank()) {
                    nombreProfesor = "—";
                }

                String carrera = rs.getString("NombreCarrera");
                if (carrera == null || carrera.isBlank()) {
                    String otra = rs.getString("OtraCarrera");
                    carrera = (otra == null || otra.isBlank()) ? "—" : otra;
                }

                Asignaciones asg = new Asignaciones(
                        "ASG-" + String.format("%04d", rs.getInt("ID_Asignacion")),
                        rs.getDate("Fecha").toLocalDate().format(FORMATO_FECHA_UI),
                        rs.getTime("HoraInicio").toLocalTime().toString().substring(0, 5),
                        rs.getTime("HoraTermino").toLocalTime().toString().substring(0, 5),
                        rs.getString("NombreEspacio"),
                        rs.getString("TipoUsuario"),
                        rs.getString("NombreSolicitante"),
                        nombreProfesor,
                        carrera,
                        rs.getString("Materia"),
                        rs.getString("Grupo"),
                        String.valueOf(rs.getInt("NumAlumnos")),
                        rs.getString("Actividad"),
                        rs.getString("Estado")
                );
                asg.setIdAsignacion(rs.getInt("ID_Asignacion"));

                lista.add(asg);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // ============================================================
    //  PANTALLA "HORARIOS" (fx_horarios.fxml)
    //  Reutiliza tb_asignacion: no hace falta una tabla aparte.
    // ============================================================
    /**
     * Trae las asignaciones vigentes ('Asignado' u 'Ocupado') dentro de un
     * rango de fechas, para pintar la cuadrícula de Horarios.
     *
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
 
    /**
     * Inserta una asignación rápida desde la cuadrícula de Horarios
     * (clic en una celda libre, o botón "Nueva Asignación" del módulo
     * Horarios). Solo pide los campos indispensables para que la
     * asignación quede completa y válida en tb_asignacion; el resto
     * de columnas (Grupo, NumAlumnos, Actividad, Carrera, Profesor
     * catalogado) quedan en NULL y se pueden completar después desde
     * la pantalla completa de Asignaciones si hace falta.
     */
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
 
    /**
     * "Libera" una celda de la cuadrícula de Horarios: en vez de borrar el
     * registro, lo marca como 'Cancelado' (mismo ENUM que ya usa
     * tb_asignacion) para conservar el historial.
     */
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
}