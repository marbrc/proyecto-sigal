package mx.utng.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mx.utng.database.Conexion;
import mx.utng.model.Aviso;

/**
 * Acceso a datos de la pantalla "Avisos" (fx_avisos.fxml).
 *
 * Maneja tb_aviso y su relación opcional con tb_espacio (un aviso puede
 * ser general -ID_Espacio nulo- o estar ligado a un espacio concreto).
 */
public class AvisoDAO {

    private static final DateTimeFormatter FORMATO_FECHA_UI = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ============================================================
    //  CATALOGO PARA EL COMBO "Espacio"
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

    // ============================================================
    //  INSERTAR (dialogo "Nuevo aviso")
    // ============================================================

    /**
     * Inserta un nuevo aviso en tb_aviso. Nace siempre en estado "No leído".
     *
     * @param a          datos capturados en el formulario (tipo, descripcion, comentarios)
     * @param idEspacio  ID_Espacio elegido, o null si el aviso es general
     * @param idUsuario  ID_Usuario en sesion (quien registra el aviso)
     * @return true si se guardo correctamente
     */
public boolean insertar(Aviso nuevo, Integer idEspacio, int idUsuario) {
    String sql = "INSERT INTO tb_aviso (FechaHora, HoraInicio, HoraTermino, ID_Espacio, ID_Usuario, TipoAviso, Descripcion, Comentarios, Estado) "
               + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection con = Conexion.conectar();
         PreparedStatement pstmt = con.prepareStatement(sql)) {

        String fechaOriginal = nuevo.getFecha();
        LocalDate fecha = LocalDate.parse(fechaOriginal, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        pstmt.setString(1, fecha.toString());
        pstmt.setString(2, nuevo.getHoraInicio());
        pstmt.setString(3, nuevo.getHoraTermino());

        if (idEspacio != null) {
            pstmt.setInt(4, idEspacio);
        } else {
            pstmt.setNull(4, java.sql.Types.INTEGER);
        }

        pstmt.setInt(5, idUsuario);
        pstmt.setString(6, nuevo.getTipoAviso());
        pstmt.setString(7, nuevo.getDescripcion());
        pstmt.setString(8, nuevo.getComentarios());
        pstmt.setString(9, nuevo.getEstado());

        return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}



    // ============================================================
    //  LISTAR (tabla "Historial de avisos")
    // ============================================================

public List<Aviso> listarTodos() {
    List<Aviso> lista = new ArrayList<>();
    String sql = "SELECT a.ID_Aviso, a.FechaHora, a.HoraInicio, a.HoraTermino, "
               + "e.NombreEspacio, a.TipoAviso, a.Descripcion, a.Comentarios, a.Estado "
               + "FROM tb_aviso a "
               + "LEFT JOIN tb_espacio e ON a.ID_Espacio = e.ID_Espacio "
               + "ORDER BY a.ID_Aviso DESC";

    try (Connection con = Conexion.conectar();
         Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            String fechaCruda = rs.getString("FechaHora");
            String fechaFormateada;
            try {
                LocalDate f = LocalDate.parse(fechaCruda.substring(0, 10));
                fechaFormateada = f.format(FORMATO_FECHA_UI);
            } catch (Exception ex) {
                fechaFormateada = fechaCruda;
            }

            String nombreEspacio = rs.getString("NombreEspacio");
            if (nombreEspacio == null || nombreEspacio.isBlank()) {
                nombreEspacio = "General";
            }

            Aviso aviso = new Aviso(
                    rs.getInt("ID_Aviso"),
                    fechaFormateada,
                    nombreEspacio,
                    rs.getString("TipoAviso"),
                    rs.getString("Descripcion"),
                    rs.getString("Comentarios"),
                    rs.getString("Estado"),
                    rs.getString("HoraInicio"),
                    rs.getString("HoraTermino")
            );
            lista.add(aviso);
        }   
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return lista;
}
    // ============================================================
    //  RESUMEN (conteo por tipo de aviso, para el panel lateral)
    // ============================================================

    public Map<String, Integer> contarPorTipo() {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        conteo.put("Información", 0);
        conteo.put("Advertencia", 0);
        conteo.put("Error", 0);
        conteo.put("Éxito", 0);

        String sql = "SELECT TipoAviso, COUNT(*) AS Total FROM tb_aviso GROUP BY TipoAviso";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                conteo.put(rs.getString("TipoAviso"), rs.getInt("Total"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conteo;
    }

    // ============================================================
    //  MARCAR COMO LEIDO
    // ============================================================

    public boolean marcarComoLeido(int idAviso) {
        String sql = "UPDATE tb_aviso SET Estado = 'Leído' WHERE ID_Aviso = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAviso);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Marca varios avisos como leidos de una sola vez (boton "Marcar como leidos"). */
    public boolean marcarVariosComoLeidos(List<Integer> idsAviso) {
        if (idsAviso == null || idsAviso.isEmpty()) {
            return true;
        }

        String placeholders = String.join(",", idsAviso.stream().map(id -> "?").toArray(String[]::new));
        String sql = "UPDATE tb_aviso SET Estado = 'Leído' WHERE ID_Aviso IN (" + placeholders + ")";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < idsAviso.size(); i++) {
                ps.setInt(i + 1, idsAviso.get(i));
            }
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  DASHBOARD (fx_inicio.fxml)
    // ============================================================

    /** Cuántos avisos siguen sin leerse (tarjeta "Avisos"). */
    public int contarNoLeidos() {
        String sql = "SELECT COUNT(*) AS Total FROM tb_aviso WHERE Estado = 'No leído'";

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

    /** Avisos sin leer, para el modal de la tarjeta "Avisos". */
    public ObservableList<Aviso> listarNoLeidos() {
        ObservableList<Aviso> lista = FXCollections.observableArrayList();

        String sql = """
                SELECT av.ID_Aviso, av.TipoAviso, av.Descripcion, av.Comentarios,
                       av.FechaHora,av.HoraInicio, av.HoraTermino, av.Estado, av.ID_Espacio,
                       e.NombreEspacio
                FROM tb_aviso av
                LEFT JOIN tb_espacio e ON e.ID_Espacio = av.ID_Espacio
                WHERE av.Estado = 'No leído'
                ORDER BY av.FechaHora DESC, av.ID_Aviso DESC
                """;

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String nombreEspacio = rs.getString("NombreEspacio");
                if (nombreEspacio == null || nombreEspacio.isBlank()) {
                    nombreEspacio = "General";
                }
                String comentarios = rs.getString("Comentarios");

                Aviso aviso = new Aviso(
                        rs.getInt("ID_Aviso"),
                        rs.getTimestamp("FechaHora").toLocalDateTime().format(FORMATO_FECHA_UI),
                        nombreEspacio,
                        rs.getString("TipoAviso"),
                        rs.getString("Descripcion"),
                        comentarios == null ? "" : comentarios,
                        rs.getString("Estado"),
                        rs.getString("HoraInicio"),
                        rs.getString("HoraTermino")
                );

                int idEspacio = rs.getInt("ID_Espacio");
                aviso.setIdEspacio(rs.wasNull() ? null : idEspacio);

                lista.add(aviso);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // ============================================================
    //  ELIMINAR
    // ============================================================

    public boolean eliminar(int idAviso) {
        String sql = "DELETE FROM tb_aviso WHERE ID_Aviso = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAviso);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    //--------------------------------------
    //-   BUSCAR
    //--------------------------------------
    public ObservableList<Aviso> buscarAvisos(
        Integer idEspacio,
        String tipoAviso,
        String descripcion,
        String comentarios,
        String estado,
        LocalDate fechaDesde,
        LocalDate fechaHasta
) {

    ObservableList<Aviso> lista = FXCollections.observableArrayList();

    StringBuilder sql = new StringBuilder("""
        SELECT
            a.ID_Aviso,
            a.FechaHora,
            a.HoraInicio,
            a.HoraTermino,
            a.TipoAviso,
            a.Descripcion,
            a.Comentarios,
            a.Estado,
            e.NombreEspacio
        FROM tb_aviso a
        LEFT JOIN tb_espacio e ON e.ID_Espacio = a.ID_Espacio
        WHERE 1 = 1
    """);

    List<Object> parametros = new ArrayList<>();

    if (idEspacio != null) {
        sql.append(" AND a.ID_Espacio = ? ");
        parametros.add(idEspacio);
    }

    if (tipoAviso != null && !tipoAviso.isBlank()) {
        sql.append(" AND a.TipoAviso LIKE ? ");
        parametros.add("%" + tipoAviso + "%");
    }

    if (descripcion != null && !descripcion.isBlank()) {
        sql.append(" AND a.Descripcion LIKE ? ");
        parametros.add("%" + descripcion + "%");
    }

    if (comentarios != null && !comentarios.isBlank()) {
        sql.append(" AND a.Comentarios LIKE ? ");
        parametros.add("%" + comentarios + "%");
    }

    if (estado != null && !estado.isBlank()) {
        sql.append(" AND a.Estado = ? ");
        parametros.add(estado);
    }

    if (fechaDesde != null) {
        sql.append(" AND a.FechaHora >= ? ");
        // FechaHora es DATETIME, Date.valueOf compara por fecha (00:00:00)
        parametros.add(java.sql.Date.valueOf(fechaDesde));
    }

    if (fechaHasta != null) {
        sql.append(" AND a.FechaHora <= ? ");
        parametros.add(java.sql.Date.valueOf(fechaHasta));
    }

    sql.append(" ORDER BY a.FechaHora DESC ");

    try (Connection con = Conexion.conectar();
         PreparedStatement ps = con.prepareStatement(sql.toString())) {

        for (int i = 0; i < parametros.size(); i++) {
            ps.setObject(i + 1, parametros.get(i));
        }

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {

                int idAviso = rs.getInt("ID_Aviso");

                // Convertir DATETIME a String compatible con tu columna colFecha (String)
                var fechaHora = rs.getTimestamp("FechaHora").toLocalDateTime();
                String fecha = fechaHora.toLocalDate().format(FORMATO_FECHA_UI); // "dd/MM/yyyy"; // "yyyy-MM-dd HH:mm"

                String espacio = rs.getString("NombreEspacio");
                if (espacio == null || espacio.isBlank()) espacio = "—";

                String horaInicio = rs.getString("HoraInicio");
                String horaTermino = rs.getString("HoraTermino");

                String tipo = rs.getString("TipoAviso");
                String desc = rs.getString("Descripcion");

                // Comentarios puede venir NULL
                String com = rs.getString("Comentarios");
                if (com == null) com = "";

                String est = rs.getString("Estado");

                Aviso fila = new Aviso(
                        idAviso,
                        fecha,
                        espacio,
                        tipo,
                        desc,
                        com,
                        est,
                        horaInicio,
                        horaTermino
                );

                lista.add(fila);
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return lista;
}


}
