package mx.utng.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import mx.utng.dao.AsignacionDAO;
import mx.utng.dao.EspacioDAO;
import mx.utng.model.EspacioRegistro;

public class HorarioImportService {

    private static final int COL_TIPO_ESPACIO = 0;
    private static final int COL_NOMBRE_ESPACIO = 1;
    private static final int COL_DIA = 2;
    private static final int COL_HORA_INICIO = 3;
    private static final int COL_HORA_FIN = 4;
    private static final int COL_GRUPO = 5;

    private static final String[] DIAS = {
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"
    };

    private static final int MAX_SEMANAS_CUATRIMESTRE = 30;

    private static final DateTimeFormatter[] FORMATOS_HORA = {
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    };

    private final EspacioDAO espacioDAO;
    private final AsignacionDAO asignacionDAO;
    private final DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("es-MX"));

    public HorarioImportService() {
        this(new EspacioDAO(), new AsignacionDAO());
    }

    public HorarioImportService(EspacioDAO espacioDAO, AsignacionDAO asignacionDAO) {
        this.espacioDAO = espacioDAO;
        this.asignacionDAO = asignacionDAO;
    }

    public ResultadoValidacion validar(File archivo, LocalDate inicioCuatrimestre, LocalDate finCuatrimestre)
            throws IOException {

        if (inicioCuatrimestre == null || finCuatrimestre == null) {
            throw new IllegalArgumentException("Selecciona el inicio y el fin del cuatrimestre.");
        }
        if (finCuatrimestre.isBefore(inicioCuatrimestre)) {
            throw new IllegalArgumentException("La fecha de fin del cuatrimestre debe ser posterior a la de inicio.");
        }
        if (finCuatrimestre.isAfter(inicioCuatrimestre.plusWeeks(MAX_SEMANAS_CUATRIMESTRE))) {
            throw new IllegalArgumentException(
                    "El rango del cuatrimestre es demasiado largo (máximo " + MAX_SEMANAS_CUATRIMESTRE + " semanas).");
        }

        Map<String, EspacioRegistro> catalogoEspacios = new LinkedHashMap<>();
        for (EspacioRegistro e : espacioDAO.listarTodos()) {
            catalogoEspacios.put(normalizar(e.getNombre()), e);
        }
        if (catalogoEspacios.isEmpty()) {
            throw new IllegalStateException(
                    "No hay espacios registrados en el catálogo. Registra al menos un espacio antes de importar.");
        }

        List<FilaHorario> validas = new ArrayList<>();
        List<FilaConError> conError = new ArrayList<>();
        int totalFilas = 0;

        try {
            try (FileInputStream fis = new FileInputStream(archivo);
                 Workbook workbook = WorkbookFactory.create(fis)) {

                Sheet sheet = workbook.getSheetAt(0);

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) {
                        continue;
                    }

                String tipoEspacio = texto(row, COL_TIPO_ESPACIO);
                String nombreEspacio = texto(row, COL_NOMBRE_ESPACIO);
                String diaTexto = texto(row, COL_DIA);
                String horaInicioTexto = texto(row, COL_HORA_INICIO);
                String horaFinTexto = texto(row, COL_HORA_FIN);
                String grupo = texto(row, COL_GRUPO);

                if (tipoEspacio.isBlank() && nombreEspacio.isBlank() && diaTexto.isBlank()
                        && horaInicioTexto.isBlank() && horaFinTexto.isBlank() && grupo.isBlank()) {
                    continue;
                }

                totalFilas++;
                int numeroFila = row.getRowNum() + 1;
                List<String> errores = new ArrayList<>();

                Integer idEspacio = null;
                if (nombreEspacio.isBlank()) {
                    errores.add("Fila " + numeroFila + ", columna \"Nombre del espacio\": está vacía.");
                } else {
                    EspacioRegistro espacio = catalogoEspacios.get(normalizar(nombreEspacio));
                    if (espacio == null) {
                        errores.add("Fila " + numeroFila + ", columna \"Nombre del espacio\": \"" + nombreEspacio
                                + "\" no existe en el catálogo (revisa que esté escrito exactamente igual, "
                                + "incluyendo mayúsculas/minúsculas y espacios).");
                    } else {
                        idEspacio = espacio.getIdEspacio();
                        if (tipoEspacio.isBlank()) {
                            errores.add("Fila " + numeroFila + ", columna \"Tipo de espacio\": está vacía.");
                        } else if (!normalizar(tipoEspacio).equals(normalizar(espacio.getTipo()))) {
                            errores.add("Fila " + numeroFila + ", columna \"Tipo de espacio\": escribiste \""
                                    + tipoEspacio + "\", pero \"" + nombreEspacio + "\" está registrado como \""
                                    + espacio.getTipo() + "\" en el catálogo.");
                        }
                    }
                }

                String diaCanon = canonizarDia(diaTexto);
                if (diaCanon == null) {
                    errores.add("Fila " + numeroFila + ", columna \"Día\": \"" + diaTexto
                            + "\" no es válido (usa Lunes, Martes, Miércoles, Jueves, Viernes o Sábado).");
                }

                LocalTime horaInicio = null;
                LocalTime horaFin = null;
                try {
                    horaInicio = parseHora(horaInicioTexto);
                } catch (DateTimeParseException e) {
                    errores.add("Fila " + numeroFila + ", columna \"Hora de inicio\": \"" + horaInicioTexto
                            + "\" no tiene un formato válido (ej. 8:00).");
                }
                try {
                    horaFin = parseHora(horaFinTexto);
                } catch (DateTimeParseException e) {
                    errores.add("Fila " + numeroFila + ", columna \"Hora de fin\": \"" + horaFinTexto
                            + "\" no tiene un formato válido (ej. 8:50).");
                }
                if (horaInicio != null && horaFin != null && !horaInicio.isBefore(horaFin)) {
                    errores.add("Fila " + numeroFila
                            + ": la \"Hora de inicio\" debe ser antes que la \"Hora de fin\".");
                }

                if (grupo.isBlank()) {
                    errores.add("Fila " + numeroFila + ", columna \"Grupo\": está vacía.");
                }

                FilaHorario fila = new FilaHorario(
                        tipoEspacio,
                        nombreEspacio,
                        diaCanon != null ? diaCanon : diaTexto,
                        horaInicio != null ? formatoHora(horaInicio) : horaInicioTexto,
                        horaFin != null ? formatoHora(horaFin) : horaFinTexto,
                        grupo,
                        idEspacio != null ? idEspacio : 0);

                if (!errores.isEmpty()) {
                    conError.add(new FilaConError(numeroFila, fila, String.join(" ", errores)));
                    continue;
                }

                String motivoBD = choqueConBaseDeDatos(idEspacio, diaCanon, horaInicio, horaFin,
                        inicioCuatrimestre, finCuatrimestre);
                if (motivoBD != null) {
                    conError.add(new FilaConError(numeroFila, fila, motivoBD));
                    continue;
                }

                String motivoArchivo = choqueConFilasDelArchivo(fila, validas);
                if (motivoArchivo != null) {
                    conError.add(new FilaConError(numeroFila, fila, motivoArchivo));
                    continue;
                }

                validas.add(fila);
            }
        }
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            // Apache POI a veces truena con errores internos poco claros (ej.
            // "Location is not set.") cuando el .xlsx no se pudo abrir de
            // verdad: esta seguido pasa si el archivo vive en OneDrive como
            // "solo en la nube" (no descargado), si esta abierto en Excel al
            // mismo tiempo, o si el archivo esta corrupto/no es un .xlsx real.
            throw new IOException(
                    "No se pudo abrir el archivo Excel. Verifica que: (1) el archivo esté completamente "
                            + "descargado en tu equipo y no sea un archivo 'solo en la nube' de OneDrive, "
                            + "(2) no esté abierto en Excel u otro programa en este momento, y "
                            + "(3) sea un archivo .xlsx válido (no .xls ni .csv renombrado). "
                            + "Detalle técnico: " + e.getClass().getSimpleName()
                            + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
        }

        return new ResultadoValidacion(totalFilas, validas, conError, inicioCuatrimestre, finCuatrimestre);
    }

    public ResultadoImportacion importar(List<FilaHorario> filas, LocalDate inicioCuatrimestre,
            LocalDate finCuatrimestre, int idUsuario) {
        int insertadas = 0;
        int fallidas = 0;

        for (FilaHorario fila : filas) {
            List<LocalDate> fechas = ocurrenciasDeDia(fila.dia(), inicioCuatrimestre, finCuatrimestre);
            LocalTime horaInicio = parseHora(fila.horaInicio());
            LocalTime horaFin = parseHora(fila.horaFin());

            for (LocalDate fecha : fechas) {
                boolean ok = asignacionDAO.insertarRapido(
                        idUsuario, fila.idEspacio(), fecha, horaInicio, horaFin, "Grupo", fila.grupo(), "");
                if (ok) {
                    insertadas++;
                } else {
                    fallidas++;
                }
            }
        }

        return new ResultadoImportacion(insertadas, fallidas);
    }

    private String choqueConBaseDeDatos(int idEspacio, String diaCanon, LocalTime horaInicio, LocalTime horaFin,
            LocalDate inicioCuatrimestre, LocalDate finCuatrimestre) {
        String horaInicioTxt = formatoHora(horaInicio);
        String horaFinTxt = formatoHora(horaFin);

        for (LocalDate fecha : ocurrenciasDeDia(diaCanon, inicioCuatrimestre, finCuatrimestre)) {
            boolean choque = asignacionDAO.existeConflictoHorario(idEspacio, fecha, horaInicioTxt, horaFinTxt, -1);
            if (choque) {
                return "El espacio ya está ocupado el " + diaCanon + " de " + horaInicioTxt + " a " + horaFinTxt
                        + " (choca el " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ").";
            }
        }
        return null;
    }

    private String choqueConFilasDelArchivo(FilaHorario fila, List<FilaHorario> validasHastaAhora) {
        LocalTime inicio = parseHora(fila.horaInicio());
        LocalTime fin = parseHora(fila.horaFin());

        for (FilaHorario otra : validasHastaAhora) {
            if (otra.idEspacio() == fila.idEspacio() && otra.dia().equals(fila.dia())) {
                LocalTime otroInicio = parseHora(otra.horaInicio());
                LocalTime otroFin = parseHora(otra.horaFin());
                if (inicio.isBefore(otroFin) && fin.isAfter(otroInicio)) {
                    return "Se cruza con otra fila del mismo archivo para el mismo espacio (" + fila.dia() + ").";
                }
            }
        }
        return null;
    }

    private List<LocalDate> ocurrenciasDeDia(String diaCanon, LocalDate inicio, LocalDate fin) {
        DayOfWeek objetivo = diaSemanaDe(diaCanon);
        List<LocalDate> fechas = new ArrayList<>();
        LocalDate cursor = inicio;
        while (cursor.getDayOfWeek() != objetivo && !cursor.isAfter(fin)) {
            cursor = cursor.plusDays(1);
        }
        while (!cursor.isAfter(fin)) {
            fechas.add(cursor);
            cursor = cursor.plusWeeks(1);
        }
        return fechas;
    }

    private DayOfWeek diaSemanaDe(String diaCanon) {
        return switch (diaCanon) {
            case "Lunes" -> DayOfWeek.MONDAY;
            case "Martes" -> DayOfWeek.TUESDAY;
            case "Miércoles" -> DayOfWeek.WEDNESDAY;
            case "Jueves" -> DayOfWeek.THURSDAY;
            case "Viernes" -> DayOfWeek.FRIDAY;
            case "Sábado" -> DayOfWeek.SATURDAY;
            default -> throw new IllegalArgumentException("Día no soportado: " + diaCanon);
        };
    }

    private String texto(Row row, int columna) {
        if (row == null) return "";
        Cell cell = row.getCell(columna);
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private LocalTime parseHora(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new DateTimeParseException("Hora vacía", "", 0);
        }
        String limpio = texto.trim().toUpperCase(Locale.ROOT)
                .replace("A. M.", "AM").replace("P. M.", "PM")
                .replace("A.M.", "AM").replace("P.M.", "PM");
        for (DateTimeFormatter formato : FORMATOS_HORA) {
            try {
                return LocalTime.parse(limpio, formato);
            } catch (DateTimeParseException ignored) { }
        }
        throw new DateTimeParseException("Formato de hora no reconocido: " + texto, limpio, 0);
    }

    private String formatoHora(LocalTime hora) {
        return hora.format(DateTimeFormatter.ofPattern("H:mm"));
    }

    private String canonizarDia(String texto) {
        if (texto == null || texto.isBlank()) return null;
        String limpio = normalizar(texto);
        for (String dia : DIAS) {
            if (normalizar(dia).equals(limpio)) return dia;
        }
        return switch (limpio) {
            case "lun" -> "Lunes";
            case "mar" -> "Martes";
            case "mie", "mier" -> "Miércoles";
            case "jue" -> "Jueves";
            case "vie" -> "Viernes";
            case "sab" -> "Sábado";
            default -> null;
        };
    }

    private String normalizar(String texto) {
        if (texto == null) return "";
        String sinAcentos = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase(Locale.ROOT);
    }

    public record FilaHorario(String tipoEspacio, String nombreEspacio, String dia, String horaInicio,
            String horaFin, String grupo, int idEspacio) { }

    public record FilaConError(int numeroFila, FilaHorario fila, String motivo) { }

    public record ResultadoValidacion(int totalFilas, List<FilaHorario> validas, List<FilaConError> conError,
            LocalDate inicioCuatrimestre, LocalDate finCuatrimestre) { }

    public record ResultadoImportacion(int insertadas, int fallidas) { }
}