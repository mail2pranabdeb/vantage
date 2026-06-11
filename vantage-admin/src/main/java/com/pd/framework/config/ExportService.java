package com.pd.framework.config;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExportService {

    public static class Column {
        private final String key;
        private final String label;
        public Column(String key, String label) { this.key = key; this.label = label; }
        public String getKey() { return key; }
        public String getLabel() { return label; }
    }

    public void export(String filename, List<Column> columns, List<Map<String, Object>> rows,
                       String format, HttpServletResponse response) throws Exception {
        switch (format.toUpperCase()) {
            case "PDF":  exportPdf(filename, columns, rows, response); break;
            case "CSV":  exportCsv(filename, columns, rows, response); break;
            case "EXCEL": exportExcel(filename, columns, rows, response); break;
            default: throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    public void exportPdf(String filename, List<Column> columns, List<Map<String, Object>> rows,
                          HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".pdf\"");

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        Font cellFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);

        PdfPTable table = new PdfPTable(columns.size());
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        for (Column col : columns) {
            PdfPCell cell = new PdfPCell(new Phrase(col.getLabel(), headerFont));
            cell.setBackgroundColor(new Color(70, 130, 180));
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (Map<String, Object> row : rows) {
            for (Column col : columns) {
                Object val = row.get(col.getKey());
                PdfPCell cell = new PdfPCell(new Phrase(val != null ? val.toString() : "", cellFont));
                cell.setPadding(4);
                table.addCell(cell);
            }
        }

        document.add(table);
        document.close();
    }

    public void exportCsv(String filename, List<Column> columns, List<Map<String, Object>> rows,
                          HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".csv\"");

        OutputStream os = response.getOutputStream();
        os.write('\uFEFF');
        String header = columns.stream().map(c -> escapeCsv(c.getLabel())).collect(Collectors.joining(",")) + "\n";
        os.write(header.getBytes(StandardCharsets.UTF_8));
        for (Map<String, Object> row : rows) {
            String line = columns.stream()
                    .map(c -> escapeCsv(row.get(c.getKey()) != null ? row.get(c.getKey()).toString() : ""))
                    .collect(Collectors.joining(",")) + "\n";
            os.write(line.getBytes(StandardCharsets.UTF_8));
        }
        os.flush();
    }

    public void exportExcel(String filename, List<Column> columns, List<Map<String, Object>> rows,
                            HttpServletResponse response) throws Exception {
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".xls\"");
        OutputStream os = response.getOutputStream();
        os.write('\uFEFF');
        os.write((columns.stream().map(Column::getLabel).collect(Collectors.joining("\t")) + "\n").getBytes(StandardCharsets.UTF_8));
        for (Map<String, Object> row : rows) {
            os.write((columns.stream()
                    .map(c -> row.get(c.getKey()) != null ? row.get(c.getKey()).toString().replace('\t', ' ') : "")
                    .collect(Collectors.joining("\t")) + "\n").getBytes(StandardCharsets.UTF_8));
        }
        os.flush();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }
}
