package com.airline.feedback.service;

import com.airline.feedback.model.AirlineCase;
import com.airline.feedback.repo.AirlineCaseRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports all airline cases to an Excel (.xlsx) file.
 * Called automatically whenever a case is created, updated, or deleted.
 */
@Service
public class ExcelExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private static final String[] HEADERS = {
        "Ticket Number", "Type", "Status", "Customer Name", "Customer Email",
        "Flight Number", "Journey Date", "Contact Channel", "Message",
        "Category", "Sentiment Score", "Priority", "Assigned Agent",
        "Triage Suggestion", "Resolution", "Created At", "Updated At", "Resolved At"
    };

    private final AirlineCaseRepository repository;
    private final String excelFilePath;

    public ExcelExportService(AirlineCaseRepository repository,
                              @Value("${app.excel.export-path:airline_cases.xlsx}") String excelFilePath) {
        this.repository = repository;
        this.excelFilePath = excelFilePath;
        log.info("Excel export configured → {}", excelFilePath);
    }

    /**
     * Re-exports ALL cases from the database to the Excel file.
     * This ensures the Excel file is always a perfect mirror of the DB.
     */
    public synchronized void exportAll() {
        List<AirlineCase> cases = repository.findAll();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Airline Cases");

            // === Header row with styling ===
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // === Data rows ===
            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);
            wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);

            int rowNum = 1;
            for (AirlineCase c : cases) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;

                row.createCell(col++).setCellValue(safe(c.getTicketNumber()));
                row.createCell(col++).setCellValue(c.getType() != null ? c.getType().name() : "");
                row.createCell(col++).setCellValue(c.getStatus() != null ? c.getStatus().name() : "");
                row.createCell(col++).setCellValue(safe(c.getCustomerName()));
                row.createCell(col++).setCellValue(safe(c.getCustomerEmail()));
                row.createCell(col++).setCellValue(safe(c.getFlightNumber()));
                row.createCell(col++).setCellValue(c.getJourneyDate() != null ? c.getJourneyDate().toString() : "");
                row.createCell(col++).setCellValue(safe(c.getContactChannel()));

                Cell msgCell = row.createCell(col++);
                msgCell.setCellValue(safe(c.getMessage()));
                msgCell.setCellStyle(wrapStyle);

                row.createCell(col++).setCellValue(safe(c.getCategory()));
                row.createCell(col++).setCellValue(c.getSentimentScore());
                row.createCell(col++).setCellValue(c.getPriority());
                row.createCell(col++).setCellValue(safe(c.getAssignedAgent()));

                Cell triageCell = row.createCell(col++);
                triageCell.setCellValue(safe(c.getTriageSuggestion()));
                triageCell.setCellStyle(wrapStyle);

                Cell resCell = row.createCell(col++);
                resCell.setCellValue(safe(c.getResolutionText()));
                resCell.setCellStyle(wrapStyle);

                row.createCell(col++).setCellValue(c.getCreatedAt() != null ? DATE_FMT.format(c.getCreatedAt()) : "");
                row.createCell(col++).setCellValue(c.getUpdatedAt() != null ? DATE_FMT.format(c.getUpdatedAt()) : "");
                row.createCell(col++).setCellValue(c.getResolvedAt() != null ? DATE_FMT.format(c.getResolvedAt()) : "");
            }

            // Auto-size columns for readability
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
                // Cap width at 50 characters to prevent message columns being too wide
                if (sheet.getColumnWidth(i) > 50 * 256) {
                    sheet.setColumnWidth(i, 50 * 256);
                }
            }

            // Freeze the header row
            sheet.createFreezePane(0, 1);

            // Auto-filter
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            // Write to file
            File outFile = new File(excelFilePath);
            File parentDir = outFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                workbook.write(fos);
            }

            log.info("✅ Excel exported: {} cases → {}", cases.size(), excelFilePath);

        } catch (IOException e) {
            log.error("❌ Failed to export Excel file: {}", e.getMessage(), e);
        }
    }

    private String safe(String val) {
        return val != null ? val : "";
    }
}
