package mx.utng.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Lee el Excel de horarios y valida cada fila contra el catálogo de espacios
 * y las asignaciones ya existentes (choque de espacio y choque de grupo).
 *
 * La conexión real con el catálogo de espacios y la tabla de asignaciones de
 * SIGAL debe inyectarse aquí (tus DAOs, ej. AsignacionDAO/EspaciosController);
 * se dejan marcados con TODO los puntos donde va esa consulta real.
 */
public class HorarioImportService {

    private static final int COL_TIPO_ESPACIO = 0;
    private static final int COL_NOMBRE_ESPACIO = 1;
    private static final int COL_DIA = 2;
    private static final int COL_HORA_INICIO = 3;
    private static final int COL_HORA_FIN = 4;
    private static final int COL_GRUPO = 5;

    public ResultadoValidacion validar(File archivo) throws IOException {
        List<FilaHorario> validas = new ArrayList<>();
        List<FilaConError> conError = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(archivo);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int totalFilas = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // encabezado
                }
                if (esFilaVacia(row)) {
                    continue;
                }
                totalFilas++;

                FilaHorario fila = leerFila(row);
                String motivoConflicto = detectarConflicto(fila);

                if (motivoConflicto == null) {
                    validas.add(fila);
                } else {
                    conError.add(new FilaConError(row.getRowNum() + 1, fila, motivoConflicto));
                }
            }

            return new ResultadoValidacion(totalFilas, validas, conError);
        }
    }

    private FilaHorario leerFila(Row row) {
        return new FilaHorario(
                textoCelda(row, COL_TIPO_ESPACIO),
                textoCelda(row, COL_NOMBRE_ESPACIO),
                textoCelda(row, COL_DIA),
                textoCelda(row, COL_HORA_INICIO),
                textoCelda(row, COL_HORA_FIN),
                textoCelda(row, COL_GRUPO)
        );
    }

    /**
     * Devuelve el motivo del conflicto si lo hay, o null si la fila es válida.
     * TODO: reemplazar las validaciones de existencia/choque por consultas
     * reales al catálogo de espacios y a la tabla de asignaciones de SIGAL
     * (por ejemplo usando tus clases AsignacionDAO / EspaciosController).
     */
    private String detectarConflicto(FilaHorario fila) {
        if (fila.nombreEspacio().isBlank() || fila.dia().isBlank()
                || fila.horaInicio().isBlank() || fila.horaFin().isBlank()) {
            return "Faltan datos obligatorios en la fila.";
        }

        // TODO: existeEspacioEnCatalogo(fila.tipoEspacio(), fila.nombreEspacio())
        // TODO: hayChoqueDeEspacio(fila.nombreEspacio(), fila.dia(), fila.horaInicio(), fila.horaFin())
        //       -> "El espacio ya está asignado en ese horario."
        // TODO: hayChoqueDeGrupo(fila.grupo(), fila.dia(), fila.horaInicio(), fila.horaFin())
        //       -> "El grupo ya tiene clase asignada en ese horario."

        return null; // sin conflicto detectado (placeholder mientras se conecta el catálogo real)
    }

    private boolean esFilaVacia(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != org.apache.poi.ss.usermodel.CellType.BLANK
                    && !textoCelda(row, cell.getColumnIndex()).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String textoCelda(Row row, int columna) {
        Cell cell = row.getCell(columna);
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    // ------------------------------------------------------------------
    // Modelos de resultado
    // ------------------------------------------------------------------

    public record FilaHorario(
            String tipoEspacio,
            String nombreEspacio,
            String dia,
            String horaInicio,
            String horaFin,
            String grupo) {
    }

    public record FilaConError(int numeroFila, FilaHorario fila, String motivo) {
    }

    public record ResultadoValidacion(
            int totalFilas,
            List<FilaHorario> validas,
            List<FilaConError> conError) {
    }
}
