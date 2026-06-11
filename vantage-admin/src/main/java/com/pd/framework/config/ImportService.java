package com.pd.framework.config;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.*;

@Service
public class ImportService {

    public static class ImportPreview {
        private List<String> headers;
        private List<Map<String, String>> rows;
        private int totalRows;
        private String sheetName;
        public ImportPreview() {}
        public ImportPreview(List<String> headers, List<Map<String, String>> rows, int totalRows, String sheetName) {
            this.headers = headers;
            this.rows = rows;
            this.totalRows = totalRows;
            this.sheetName = sheetName;
        }
        public List<String> getHeaders() { return headers; }
        public void setHeaders(List<String> headers) { this.headers = headers; }
        public List<Map<String, String>> getRows() { return rows; }
        public void setRows(List<Map<String, String>> rows) { this.rows = rows; }
        public int getTotalRows() { return totalRows; }
        public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
        public String getSheetName() { return sheetName; }
        public void setSheetName(String sheetName) { this.sheetName = sheetName; }
    }

    public ImportPreview preview(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) filename = "import.csv";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".csv")) return previewCsv(file);
        if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) return previewExcel(file);
        throw new IllegalArgumentException("Unsupported format. Use CSV or Excel (.xlsx/.xls).");
    }

    private ImportPreview previewCsv(MultipartFile file) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream())) {
            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setHeader().setSkipHeaderRecord(true).setTrim(true).build()
                    .parse(reader);
            List<String> headers = parser.getHeaderNames();
            List<Map<String, String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                for (String h : headers) row.put(h, record.get(h));
                rows.add(row);
            }
            return new ImportPreview(headers, rows, rows.size(), "Sheet1");
        }
    }

    private ImportPreview previewExcel(MultipartFile file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new IllegalArgumentException("File is empty");
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) { cell.setCellType(CellType.STRING); headers.add(cell.getStringCellValue().trim()); }
            List<Map<String, String>> rows = new ArrayList<>();
            DataFormatter fmt = new DataFormatter();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    rowMap.put(headers.get(j), cell != null ? fmt.formatCellValue(cell).trim() : "");
                }
                rows.add(rowMap);
            }
            return new ImportPreview(headers, rows, rows.size(), sheet.getSheetName());
        }
    }
}
