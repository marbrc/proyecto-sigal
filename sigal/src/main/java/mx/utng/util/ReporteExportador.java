package mx.utng.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import mx.utng.model.Reporte;
import mx.utng.model.ResultadoReporte;

/**
 * Genera el archivo de descarga de la pantalla "Reportes" en PDF, Excel
 * (.xlsx) o Word (.docx), a partir del mismo ResultadoReporte que ya
 * está pintado en pantalla (no vuelve a consultar la base de datos).
 */
public class ReporteExportador {

    private ReporteExportador() {
    }

    // ============================================================
    //  PDF
    // ============================================================
    public static void exportarPDF(ResultadoReporte resultado, String rangoTexto, File destino) throws IOException {

        try (PDDocument documento = new PDDocument()) {

            PDPage pagina = new PDPage(PDRectangle.LETTER);
            documento.addPage(pagina);

            PDType1Font fuenteTitulo = PDType1Font.HELVETICA_BOLD;
            PDType1Font fuenteNormal = PDType1Font.HELVETICA;
            PDType1Font fuenteNegrita = PDType1Font.HELVETICA_BOLD;

            float margen = 50;
            float anchoUtil = pagina.getMediaBox().getWidth() - margen * 2;
            float y = pagina.getMediaBox().getHeight() - margen;

            PDPageContentStream cs = new PDPageContentStream(documento, pagina);

            // ---- Encabezado ----
            y = escribir(cs, fuenteTitulo, 16, margen, y, "SIGAL - Reporte de ocupación de espacios");
            y -= 6;
            y = escribir(cs, fuenteNormal, 10, margen, y, rangoTexto);
            y -= 14;

            // ---- Tarjetas resumen ----
            y = escribir(cs, fuenteNegrita, 11, margen, y, "Resumen del periodo");
            y -= 4;
            y = escribir(cs, fuenteNormal, 10, margen, y,
                    "Total de espacios: " + resultado.getTotalEspacios());
            y = escribir(cs, fuenteNormal, 10, margen, y,
                    "Espacios con asignaciones: " + resultado.getEspaciosConAsignaciones());
            y = escribir(cs, fuenteNormal, 10, margen, y,
                    String.format("Promedio de asignaciones por espacio: %.1f", resultado.getPromedioAsignacionesPorEspacio()));
            y = escribir(cs, fuenteNormal, 10, margen, y,
                    "Espacio más asignado: " + resultado.getEspacioMasAsignado());
            y -= 16;

            // ---- Tabla ----
            float[] anchoCol = { anchoUtil * 0.32f, anchoUtil * 0.24f, anchoUtil * 0.22f, anchoUtil * 0.22f };
            String[] encabezados = { "Espacio", "Horas disponibles", "Horas ocupadas", "Ocupación" };

            y = escribirFilaTabla(cs, fuenteNegrita, margen, y, anchoCol, encabezados);
            cs.moveTo(margen, y + 12);
            cs.lineTo(margen + anchoUtil, y + 12);
            cs.stroke();

            for (Reporte fila : resultado.getFilas()) {

                if (y < margen + 40) {
                    // Se acabó la página: cerramos y abrimos otra.
                    cs.close();
                    pagina = new PDPage(PDRectangle.LETTER);
                    documento.addPage(pagina);
                    cs = new PDPageContentStream(documento, pagina);
                    y = pagina.getMediaBox().getHeight() - margen;
                }

                String[] valores = {
                        fila.getEspacio(),
                        String.format("%.1f h", fila.getHorasDisponibles()),
                        String.format("%.1f h", fila.getHorasOcupadas()),
                        fila.getPorcentajeTexto()
                };
                y = escribirFilaTabla(cs, fuenteNormal, margen, y, anchoCol, valores);
            }

            cs.close();
            documento.save(destino);
        }
    }

    private static float escribir(PDPageContentStream cs, PDType1Font fuente, float tamano,
                                   float x, float y, String texto) throws IOException {
        cs.beginText();
        cs.setFont(fuente, tamano);
        cs.newLineAtOffset(x, y);
        cs.showText(texto == null ? "" : texto);
        cs.endText();
        return y - (tamano + 6);
    }

    private static float escribirFilaTabla(PDPageContentStream cs, PDType1Font fuente,
                                            float margen, float y, float[] anchoCol, String[] valores) throws IOException {
        float x = margen;
        for (int i = 0; i < valores.length; i++) {
            cs.beginText();
            cs.setFont(fuente, 9.5f);
            cs.newLineAtOffset(x, y);
            cs.showText(valores[i] == null ? "" : valores[i]);
            cs.endText();
            x += anchoCol[i];
        }
        return y - 16;
    }

    // ============================================================
    //  EXCEL (.xlsx)
    // ============================================================
    public static void exportarExcel(ResultadoReporte resultado, String rangoTexto, File destino) throws IOException {

        try (XSSFWorkbook libro = new XSSFWorkbook()) {

            XSSFSheet hoja = libro.createSheet("Reporte");

            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            CellStyle estiloTitulo = libro.createCellStyle();
            estiloTitulo.setFont(fuenteTitulo);

            Font fuenteEncabezado = libro.createFont();
            fuenteEncabezado.setBold(true);
            CellStyle estiloEncabezado = libro.createCellStyle();
            estiloEncabezado.setFont(fuenteEncabezado);

            int filaIdx = 0;

            filaIdx = celda(hoja, filaIdx, "SIGAL - Reporte de ocupación de espacios", estiloTitulo);
            filaIdx = celda(hoja, filaIdx, rangoTexto, null);
            filaIdx++;

            filaIdx = celda(hoja, filaIdx, "Total de espacios:", estiloEncabezado, String.valueOf(resultado.getTotalEspacios()));
            filaIdx = celda(hoja, filaIdx, "Espacios con asignaciones:", estiloEncabezado, String.valueOf(resultado.getEspaciosConAsignaciones()));
            filaIdx = celda(hoja, filaIdx, "Promedio de asignaciones por espacio:", estiloEncabezado,
                    String.format("%.1f", resultado.getPromedioAsignacionesPorEspacio()));
            filaIdx = celda(hoja, filaIdx, "Espacio más asignado:", estiloEncabezado, resultado.getEspacioMasAsignado());
            filaIdx++;

            Row encabezado = hoja.createRow(filaIdx++);
            String[] titulos = { "Espacio", "Horas disponibles", "Horas ocupadas", "% de ocupación" };
            for (int i = 0; i < titulos.length; i++) {
                Cell c = encabezado.createCell(i);
                c.setCellValue(titulos[i]);
                c.setCellStyle(estiloEncabezado);
            }

            for (Reporte f : resultado.getFilas()) {
                Row fila = hoja.createRow(filaIdx++);
                fila.createCell(0).setCellValue(f.getEspacio());
                fila.createCell(1).setCellValue(f.getHorasDisponibles());
                fila.createCell(2).setCellValue(f.getHorasOcupadas());
                fila.createCell(3).setCellValue(f.getPorcentajeTexto());
            }

            for (int i = 0; i < titulos.length; i++) {
                hoja.autoSizeColumn(i);
            }

            try (FileOutputStream out = new FileOutputStream(destino)) {
                libro.write(out);
            }
        }
    }

    private static int celda(XSSFSheet hoja, int filaIdx, String texto, CellStyle estilo) {
        Row fila = hoja.createRow(filaIdx);
        Cell c = fila.createCell(0);
        c.setCellValue(texto);
        if (estilo != null) {
            c.setCellStyle(estilo);
        }
        return filaIdx + 1;
    }

    private static int celda(XSSFSheet hoja, int filaIdx, String etiqueta, CellStyle estilo, String valor) {
        Row fila = hoja.createRow(filaIdx);
        Cell c0 = fila.createCell(0);
        c0.setCellValue(etiqueta);
        if (estilo != null) {
            c0.setCellStyle(estilo);
        }
        fila.createCell(1).setCellValue(valor);
        return filaIdx + 1;
    }

    // ============================================================
    //  WORD (.docx)
    // ============================================================
    public static void exportarWord(ResultadoReporte resultado, String rangoTexto, File destino) throws IOException {

        try (XWPFDocument documento = new XWPFDocument()) {

            XWPFParagraph titulo = documento.createParagraph();
            titulo.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun runTitulo = titulo.createRun();
            runTitulo.setText("SIGAL - Reporte de ocupación de espacios");
            runTitulo.setBold(true);
            runTitulo.setFontSize(16);

            XWPFParagraph subtitulo = documento.createParagraph();
            XWPFRun runSubtitulo = subtitulo.createRun();
            runSubtitulo.setText(rangoTexto);
            runSubtitulo.setFontSize(10);
            runSubtitulo.setColor("666666");

            XWPFParagraph resumenTitulo = documento.createParagraph();
            XWPFRun runResumenTitulo = resumenTitulo.createRun();
            runResumenTitulo.setBold(true);
            runResumenTitulo.setText("Resumen del periodo");

            agregarLinea(documento, "Total de espacios: " + resultado.getTotalEspacios());
            agregarLinea(documento, "Espacios con asignaciones: " + resultado.getEspaciosConAsignaciones());
            agregarLinea(documento, String.format("Promedio de asignaciones por espacio: %.1f", resultado.getPromedioAsignacionesPorEspacio()));
            agregarLinea(documento, "Espacio más asignado: " + resultado.getEspacioMasAsignado());

            documento.createParagraph();

            String[] titulos = { "Espacio", "Horas disponibles", "Horas ocupadas", "% de ocupación" };
            XWPFTable tabla = documento.createTable(resultado.getFilas().size() + 1, titulos.length);

            XWPFTableRow filaEncabezado = tabla.getRow(0);
            for (int i = 0; i < titulos.length; i++) {
                filaEncabezado.getCell(i).setText(titulos[i]);
            }

            int i = 1;
            for (Reporte f : resultado.getFilas()) {
                XWPFTableRow fila = tabla.getRow(i++);
                fila.getCell(0).setText(f.getEspacio());
                fila.getCell(1).setText(String.format("%.1f h", f.getHorasDisponibles()));
                fila.getCell(2).setText(String.format("%.1f h", f.getHorasOcupadas()));
                fila.getCell(3).setText(f.getPorcentajeTexto());
            }

            try (FileOutputStream out = new FileOutputStream(destino)) {
                documento.write(out);
            }
        }
    }

    private static void agregarLinea(XWPFDocument documento, String texto) {
        XWPFParagraph p = documento.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(texto);
        r.setFontSize(10);
    }
}
