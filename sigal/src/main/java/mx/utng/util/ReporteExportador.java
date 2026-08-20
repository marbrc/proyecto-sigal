package mx.utng.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import mx.utng.model.Reporte;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ReporteExportador {

    private static final String[] ENCABEZADOS = {
            "Fecha",
            "Horario",
            "Espacio",
            "Solicitante",
            "Grupo",
            "Estado",
            "Motivo",
            "Carrera",
            "Materia"
    };

    private ReporteExportador() {
        // Evita crear objetos de esta clase.
    }

    /*
     * =========================
     * EXPORTAR A PDF
     * =========================
     */
    public static void exportarPDF(
            Object resumen,
            ObservableList<Reporte> detalle,
            String rango,
            File destino
    ) throws IOException {

        PdfWriter writer = new PdfWriter(destino);
        PdfDocument pdf = new PdfDocument(writer);

        Document documento = new Document(
                pdf,
                PageSize.A4.rotate()
        );

        documento.setMargins(25, 25, 25, 25);

        Paragraph titulo = new Paragraph(
                "SIGAL - Detalle del reporte de ocupación"
        )
                .setBold()
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);

        documento.add(titulo);

        if (rango != null && !rango.isBlank()) {
            documento.add(
                    new Paragraph(rango)
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.CENTER)
            );
        }

        documento.add(new Paragraph(" "));

        Table tabla = new Table(
                UnitValue.createPercentArray(new float[]{
                        10, 12, 15, 17, 12, 10, 18, 15, 15
                })
        );

        tabla.setWidth(UnitValue.createPercentValue(100));

        for (String encabezado : ENCABEZADOS) {
            Cell celda = new Cell()
                    .add(new Paragraph(encabezado).setBold())
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER);

            tabla.addHeaderCell(celda);
        }

        if (detalle != null) {
            for (Reporte reporte : detalle) {
                tabla.addCell(celdaPDF(reporte.getFecha()));
                tabla.addCell(celdaPDF(reporte.getHorario()));
                tabla.addCell(celdaPDF(reporte.getEspacio()));
                tabla.addCell(celdaPDF(reporte.getSolicitante()));
                tabla.addCell(celdaPDF(reporte.getGrupo()));
                tabla.addCell(celdaPDF(reporte.getEstado()));
                tabla.addCell(celdaPDF(reporte.getMotivo()));
                tabla.addCell(celdaPDF(reporte.getCarrera()));
                tabla.addCell(celdaPDF(reporte.getMateria()));
            }
        }

        documento.add(tabla);
        documento.close();
    }

    private static Cell celdaPDF(String texto) {
        return new Cell()
                .add(new Paragraph(valor(texto)).setFontSize(8))
                .setTextAlignment(TextAlignment.LEFT);
    }

    /*
     * =========================
     * EXPORTAR A EXCEL
     * =========================
     */
    public static void exportarExcel(
            Object resumen,
            ObservableList<Reporte> detalle,
            String rango,
            File destino
    ) throws IOException {

        try (Workbook libro = new XSSFWorkbook()) {

            Sheet hoja = libro.createSheet("Detalle");

            CellStyle estiloTitulo = libro.createCellStyle();
            estiloTitulo.setAlignment(HorizontalAlignment.CENTER);

            org.apache.poi.ss.usermodel.Font fuenteTitulo =
                    libro.createFont();

            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);

            estiloTitulo.setFont(fuenteTitulo);

            CellStyle estiloEncabezado = libro.createCellStyle();
            estiloEncabezado.setFillForegroundColor(
                    IndexedColors.DARK_BLUE.getIndex()
            );
            estiloEncabezado.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );
            estiloEncabezado.setAlignment(
                    HorizontalAlignment.CENTER
            );
            estiloEncabezado.setBorderBottom(BorderStyle.THIN);

            org.apache.poi.ss.usermodel.Font fuenteEncabezado =
                    libro.createFont();

            fuenteEncabezado.setBold(true);
            fuenteEncabezado.setColor(
                    IndexedColors.WHITE.getIndex()
            );

            estiloEncabezado.setFont(fuenteEncabezado);

            Row filaTitulo = hoja.createRow(0);
            org.apache.poi.ss.usermodel.Cell celdaTitulo =
                    filaTitulo.createCell(0);

            celdaTitulo.setCellValue(
                    "SIGAL - Detalle del reporte de ocupación"
            );
            celdaTitulo.setCellStyle(estiloTitulo);

            hoja.addMergedRegion(
                    new org.apache.poi.ss.util.CellRangeAddress(
                            0,
                            0,
                            0,
                            ENCABEZADOS.length - 1
                    )
            );

            Row filaRango = hoja.createRow(1);
            filaRango.createCell(0).setCellValue(valor(rango));

            Row filaEncabezados = hoja.createRow(3);

            for (int i = 0; i < ENCABEZADOS.length; i++) {
                org.apache.poi.ss.usermodel.Cell celda =
                        filaEncabezados.createCell(i);

                celda.setCellValue(ENCABEZADOS[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            int numeroFila = 4;

            if (detalle != null) {
                for (Reporte reporte : detalle) {

                    Row fila = hoja.createRow(numeroFila++);

                    fila.createCell(0).setCellValue(
                            valor(reporte.getFecha())
                    );
                    fila.createCell(1).setCellValue(
                            valor(reporte.getHorario())
                    );
                    fila.createCell(2).setCellValue(
                            valor(reporte.getEspacio())
                    );
                    fila.createCell(3).setCellValue(
                            valor(reporte.getSolicitante())
                    );
                    fila.createCell(4).setCellValue(
                            valor(reporte.getGrupo())
                    );
                    fila.createCell(5).setCellValue(
                            valor(reporte.getEstado())
                    );
                    fila.createCell(6).setCellValue(
                            valor(reporte.getMotivo())
                    );
                    fila.createCell(7).setCellValue(
                            valor(reporte.getCarrera())
                    );
                    fila.createCell(8).setCellValue(
                            valor(reporte.getMateria())
                    );
                }
            }

            for (int i = 0; i < ENCABEZADOS.length; i++) {
                hoja.autoSizeColumn(i);
            }

            try (FileOutputStream salida =
                         new FileOutputStream(destino)) {

                libro.write(salida);
            }
        }
    }

    /*
     * =========================
     * EXPORTAR A WORD
     * =========================
     */
    public static void exportarWord(
            Object resumen,
            ObservableList<Reporte> detalle,
            String rango,
            File destino
    ) throws IOException {

        try (XWPFDocument documento = new XWPFDocument()) {

            org.apache.poi.xwpf.usermodel.XWPFParagraph titulo =
                    documento.createParagraph();

            titulo.setAlignment(
                    org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
            );

            org.apache.poi.xwpf.usermodel.XWPFRun textoTitulo =
                    titulo.createRun();

            textoTitulo.setBold(true);
            textoTitulo.setFontSize(16);
            textoTitulo.setText(
                    "SIGAL - Detalle del reporte de ocupación"
            );

            if (rango != null && !rango.isBlank()) {
                org.apache.poi.xwpf.usermodel.XWPFParagraph parrafoRango =
                        documento.createParagraph();

                parrafoRango.setAlignment(
                        org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
                );

                parrafoRango.createRun().setText(rango);
            }

            documento.createParagraph();

            XWPFTable tabla = documento.createTable(
                    detalle == null
                            ? 1
                            : detalle.size() + 1,
                    ENCABEZADOS.length
            );

            tabla.setWidth("100%");

            XWPFTableRow filaEncabezado =
                    tabla.getRow(0);

            for (int i = 0; i < ENCABEZADOS.length; i++) {
                establecerTextoCelda(
                        filaEncabezado.getCell(i),
                        ENCABEZADOS[i],
                        true
                );
            }

            if (detalle != null) {

                for (int fila = 0; fila < detalle.size(); fila++) {

                    Reporte reporte = detalle.get(fila);
                    XWPFTableRow filaWord =
                            tabla.getRow(fila + 1);

                    establecerTextoCelda(
                            filaWord.getCell(0),
                            reporte.getFecha(),
                            false
                    );
                    establecerTextoCelda(
                            filaWord.getCell(1),
                            reporte.getHorario(),
                            false
                    );
                    establecerTextoCelda(
                            filaWord.getCell(2),
                            reporte.getEspacio(),
                            false
                    );
                    establecerTextoCelda(
                            filaWord.getCell(3),
                            reporte.getSolicitante(),
                            false
                    );
                    establecerTextoCelda(
                            filaWord.getCell(4),
                            reporte.getGrupo(),
                            false
                    );
                    establecerTextoCelda(
                            filaWord.getCell(5),
                            reporte.getEstado(),
                            false
                    );
                    establecerTextoCelda(
                            filaWord.getCell(6),
                            reporte.getMotivo(),
                            false
                    );
                    establecerTextoCelda(
                            filaWord.getCell(7),
                            reporte.getCarrera(),
                            false
                    );
                    establecerTextoCelda(
                            filaWord.getCell(8),
                            reporte.getMateria(),
                            false
                    );
                }
            }

            try (FileOutputStream salida =
                         new FileOutputStream(destino)) {

                documento.write(salida);
            }
        }
    }

    private static void establecerTextoCelda(
            XWPFTableCell celda,
            String texto,
            boolean encabezado
    ) {
        celda.removeParagraph(0);

        org.apache.poi.xwpf.usermodel.XWPFParagraph parrafo =
                celda.addParagraph();

        org.apache.poi.xwpf.usermodel.XWPFRun run =
                parrafo.createRun();

        run.setText(valor(texto));
        run.setFontSize(8);

        if (encabezado) {
            run.setBold(true);
        }
    }

    private static String valor(String texto) {
        return texto == null ? "" : texto;
    }
}
