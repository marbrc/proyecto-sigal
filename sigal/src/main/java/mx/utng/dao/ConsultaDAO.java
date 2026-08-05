package mx.utng.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mx.utng.database.Conexion;
import mx.utng.model.Consultas;

/**
 * Acceso a datos de la pantalla "Consultas" (fx_consultas.fxml).
 *
 * Arma una consulta a tb_asignacion (con sus catálogos relacionados:
 * tb_espacio, tb_profesor, tb_carrera) agregando condiciones WHERE
 * solo para los filtros que el usuario sí llenó. Los combos de
 * Profesor/Carrera/Espacio se llenan reutilizando los catálogos que
 * ya tiene AsignacionDAO, para no repetir el mismo código dos veces.
 */
public class ConsultaDAO {

    private static final DateTimeFormatter FORMATO_FECHA_UI = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Busca asignaciones aplicando los filtros recibidos. Cualquier
     * filtro que venga null o vacío ("Todos") simplemente no se agrega
     * a la consulta.
     *
     * NOTA: el filtro "Tipo de espacio" todavía no se aplica aquí,
     * porque tb_espacio todavía no tiene confirmada una columna para
     * el tipo (Laboratorio de cómputo / especializado / etc. son por
     * ahora solo texto en la pantalla de Registro de Espacios, no se
     * guardan en la base de datos). En cuanto esa columna exista, se
     * agrega el filtro aquí también.
     */
    public ObservableList<Consultas> buscar(Integer idProfesor, Integer idCarrera, Integer idEspacio,
                                             String solicitante, String materia, String grupo,
                                             String estado, LocalDate fechaDesde, LocalDate fechaHasta) {

        ObservableList<Consultas> lista = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder("""
                SELECT a.ID_Asignacion, a.NombreSolicitante, a.Materia, a.Grupo, a.Fecha,
                       a.HoraInicio, a.HoraTermino, a.Actividad, a.Estado, a.OtraCarrera,
                       e.NombreEspacio,
                       CONCAT_WS(' ', p.Nombre, p.ApellidoPaterno, p.ApellidoMaterno) AS NombreProfesor,
                       c.NombreCarrera
                FROM tb_asignacion a
                JOIN tb_espacio e ON e.ID_Espacio = a.ID_Espacio
                LEFT JOIN tb_profesor p ON p.ID_Profesor = a.ID_Profesor
                LEFT JOIN tb_carrera c ON c.ID_Carrera = a.ID_Carrera
                WHERE 1 = 1
                """);

        List<Object> parametros = new ArrayList<>();

        if (idProfesor != null) {
            sql.append(" AND a.ID_Profesor = ? ");
            parametros.add(idProfesor);
        }
        if (idCarrera != null) {
            sql.append(" AND a.ID_Carrera = ? ");
            parametros.add(idCarrera);
        }
        if (idEspacio != null) {
            sql.append(" AND a.ID_Espacio = ? ");
            parametros.add(idEspacio);
        }
        if (solicitante != null && !solicitante.isBlank()) {
            sql.append(" AND a.NombreSolicitante LIKE ? ");
            parametros.add("%" + solicitante + "%");
        }
        if (materia != null && !materia.isBlank()) {
            sql.append(" AND a.Materia LIKE ? ");
            parametros.add("%" + materia + "%");
        }
        if (grupo != null && !grupo.isBlank()) {
            sql.append(" AND a.Grupo LIKE ? ");
            parametros.add("%" + grupo + "%");
        }
        if (estado != null && !estado.isBlank()) {
            sql.append(" AND a.Estado = ? ");
            parametros.add(estado);
        }
        if (fechaDesde != null) {
            sql.append(" AND a.Fecha >= ? ");
            parametros.add(Date.valueOf(fechaDesde));
        }
        if (fechaHasta != null) {
            sql.append(" AND a.Fecha <= ? ");
            parametros.add(Date.valueOf(fechaHasta));
        }

        sql.append(" ORDER BY a.Fecha DESC, a.HoraInicio DESC ");

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String nombreProfesor = rs.getString("NombreProfesor");
                    if (nombreProfesor == null || nombreProfesor.isBlank()) {
                        nombreProfesor = rs.getString("NombreSolicitante");
                    }

                    String carrera = rs.getString("NombreCarrera");
                    if (carrera == null || carrera.isBlank()) {
                        String otra = rs.getString("OtraCarrera");
                        carrera = (otra == null || otra.isBlank()) ? "—" : otra;
                    }

                    String horario = rs.getTime("HoraInicio").toLocalTime().toString().substring(0, 5)
                            + " - " + rs.getTime("HoraTermino").toLocalTime().toString().substring(0, 5);

                    Consultas fila = new Consultas(
                            horario,
                            rs.getString("NombreEspacio"),
                            nombreProfesor,
                            rs.getString("Grupo"),
                            rs.getString("Estado"),
                            rs.getString("Actividad"),
                            carrera,
                            rs.getString("Materia")
                    );
                    fila.setIdAsignacion(rs.getInt("ID_Asignacion"));

                    lista.add(fila);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

}
