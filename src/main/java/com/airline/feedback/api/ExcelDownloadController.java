package com.airline.feedback.api;

import com.airline.feedback.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping("/api")
public class ExcelDownloadController {

    private final ExcelExportService excelExportService;
    private final String excelFilePath;

    public ExcelDownloadController(ExcelExportService excelExportService,
                                   @Value("${app.excel.export-path:airline_cases.xlsx}") String excelFilePath) {
        this.excelExportService = excelExportService;
        this.excelFilePath = excelFilePath;
    }

    @GetMapping("/export/excel")
    public ResponseEntity<Resource> downloadExcel() {
        // Re-export to ensure latest data
        excelExportService.exportAll();

        File file = new File(excelFilePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"airline_cases.xlsx\"")
                .body(resource);
    }
}
