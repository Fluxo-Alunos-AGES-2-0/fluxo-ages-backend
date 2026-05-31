package com.fluxo.report.service;

import com.fluxo.report.exception.ReportTemplateNotFoundException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ReportTemplateService {

    private static final String REPORT_TEMPLATE_FILENAME = "report-template.docx";
    private static final Path REPORT_TEMPLATE_PATH = Paths.get("downloads/templates", REPORT_TEMPLATE_FILENAME).toAbsolutePath();

    public Resource loadReportTemplate() {
        try {
            if (!Files.exists(REPORT_TEMPLATE_PATH) || !Files.isRegularFile(REPORT_TEMPLATE_PATH)) {
                throw new ReportTemplateNotFoundException("Template de relatório não encontrado.");
            }

            return new ByteArrayResource(Files.readAllBytes(REPORT_TEMPLATE_PATH));
        } catch (IOException ex) {
            throw new ReportTemplateNotFoundException("Template de relatório não encontrado.", ex);
        }
    }
}
