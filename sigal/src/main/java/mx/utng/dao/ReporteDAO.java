package mx.utng.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mx.utng.database.Conexion;
import mx.utng.model.Consultas;
import mx.utng.model.Reporte;
import mx.utng.model.ResultadoReporte;

/**
 * Acceso a datos de la pantalla "Reportes" (fx_reportes.fxml).
 *
 * IMPORTANTE (dos cosas que se quedan pendientes a propósito):
 *
 * 1) tb_espacio en tu base de datos real todavía solo tiene ID_Espacio
 *    y NombreEspacio confirmados (mismo caso ya documentado en
 *    ConsultaDAO.java). Por eso este reporte NO filtra ni muestra
 *    "Tipo de espacio" — en cuanto esa columna exista, se agrega aquí
 *    igual que en los demás módulos.
 *
 * 2) "Horas disponibles" es una ESTIMACIÓN, no un dato real de la BD:
 *    cuenta los días hábiles (lunes a viernes) del periodo elegido y
 *    los multiplica por las horas de operación diaria del plantel
 *    (ver HORAS_OPERACION_DIA). Ajusta esa constante a tu horario real,
 *    o cuando tengas una tabla de horarios de operación por espacio,
 *    reemplaza este cálculo por una consulta real a esa tabla.
 */
public class ReporteDAO {

    /** Horas que un espacio está disponible por día hábil (ej. 7:00 a 21:00 = 14h). Ajustable. */
    private static final double HORAS_OPERACION_DIA = 14.0;

    /** Para el combo "Espacio" del filtro. */
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

    /**
     * Genera el reporte de ocupación entre [desde, hasta], opcionalmente
     * limitado a un solo espacio. Cuenta como "ocupadas" las asignaciones
     * que no estén Canceladas.
     */
    public ResultadoReporte generar(LocalDate desde, LocalDate hasta, Integer idEspacioFiltro) {

        double horasDisponiblesPorEspacio = diasHabiles(desde, hasta) * HORAS_OPERACION_DIA;

        StringBuilder sql = new StringBuilder("""
                SELECT e.ID_Espacio, e.NombreEspacio,
                       COALESCE(SUM(TIMESTAMPDIFF(MINUTE, a.HoraInicio, a.HoraTermino)), 0) AS MinutosOcupados,
                       COUNT(a.ID_Asignacion) AS NumAsignaciones
                FROM tb_espacio e
                LEFT JOIN tb_asignacion a
                       ON a.ID_Espacio = e.ID_Espacio
                      AND a.Estado <> 'Cancelado'
                      AND a.Fecha BETWEEN ? AND ?
                """);

        if (idEspacioFiltro != null) {
            sql.append(" WHERE e.ID_Espacio = ? ");
        }
        sql.append(" GROUP BY e.ID_Espacio, e.NombreEspacio ORDER BY e.NombreEspacio ");

        ObservableList<Reporte> filas = FXCollections.observableArrayList();
        int totalEspacios = 0;
        int espaciosConAsignaciones = 0;
        int totalAsignaciones = 0;
        String espacioMasAsignado = "—";
        int maxAsignaciones = 0;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            if (idEspacioFiltro != null) {
                ps.setInt(3, idEspacioFiltro);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    totalEspacios++;

                    String nombreEspacio = rs.getString("NombreEspacio");
                    double horasOcupadas = rs.getInt("MinutosOcupados") / 60.0;
                    int numAsignaciones = rs.getInt("NumAsignaciones");

                    if (numAsignaciones > 0) {
                        espaciosConAsignaciones++;
                        totalAsignaciones += numAsignaciones;
                    }
                    if (numAsignaciones > maxAsignaciones) {
                        maxAsignaciones = numAsignaciones;
                        espacioMasAsignado = nombreEspacio;
                    }

                    filas.add(new Reporte(nombreEspacio, horasDisponiblesPorEspacio, horasOcupadas));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        double promedio = espaciosConAsignaciones == 0
                ? 0
                : (double) totalAsignaciones / espaciosConAsignaciones;

        return new ResultadoReporte(totalEspacios, espaciosConAsignaciones, promedio, espacioMasAsignado, filas);
    }

    private long diasHabiles(LocalDate desde, LocalDate hasta) {
        long dias = 0;
        LocalDate cursor = desde;
        while (!cursor.isAfter(hasta)) {
            DayOfWeek dia = cursor.getDayOfWeek();
            if (dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY) {
                dias++;
            }
            cursor = cursor.plusDays(1);
        }
        return Math.max(dias, 1);
    }
    
public ObservableList<Reporte> listarDetalle(
        LocalDate desde,
        LocalDate hasta,
        Integer idEspacio
) {
    ObservableList<Reporte> lista =
            FXCollections.observableArrayList();

    StringBuilder sql = new StringBuilder("""
            SELECT
                a.Fecha,
                a.HoraInicio,
                a.HoraTermino,
                e.NombreEspacio AS Espacio,
                a.NombreSolicitante AS Solicitante,
                g.NombreGrupo AS Grupo,
                a.Estado,
                a.Actividad AS Motivo,
                c.NombreCarrera AS Carrera,
                m.Nombre AS Materia
            FROM tb_asignacion a
            LEFT JOIN tb_carrera c
                ON c.ID_Carrera = a.ID_Carrera
            LEFT JOIN tb_grupo g
                ON g.ID_Grupo = a.ID_Grupo
            LEFT JOIN tb_materia m
                ON m.ID_Materia = a.ID_Materia
            INNER JOIN tb_espacio e
                ON e.ID_Espacio = a.ID_Espacio
            WHERE a.Fecha BETWEEN ? AND ?
            """);

    if (idEspacio != null) {
        sql.append(" AND a.ID_Espacio = ?");
    }

    sql.append("""
            ORDER BY a.Fecha ASC, a.HoraInicio ASC
            """);

    try (
            Connection con = Conexion.conectar();
            PreparedStatement ps =
                    con.prepareStatement(sql.toString())
    ) {
        ps.setDate(1, Date.valueOf(desde));
        ps.setDate(2, Date.valueOf(hasta));

        if (idEspacio != null) {
            ps.setInt(3, idEspacio);
        }

        try (ResultSet rs = ps.executeQuery()) {

            DateTimeFormatter formatoFecha =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy");

            while (rs.next()) {

                String fecha = rs.getDate("Fecha")
                        .toLocalDate()
                        .format(formatoFecha);

                String horario =
                        rs.getTime("HoraInicio")
                        + " - "
                        + rs.getTime("HoraTermino");

                lista.add(new Reporte(
                        fecha,
                        horario,
                        rs.getString("Espacio"),
                        rs.getString("Solicitante"),
                        rs.getString("Grupo"),
                        rs.getString("Estado"),
                        rs.getString("Motivo"),
                        rs.getString("Carrera"),
                        rs.getString("Materia")
                ));
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return lista;
}

}
