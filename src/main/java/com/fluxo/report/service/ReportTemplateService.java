package com.fluxo.report.service;

import com.fluxo.report.exception.ReportTemplateNotFoundException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ReportTemplateService {

    private static final String REPORT_TEMPLATE_PATH = "templates/report-template.docx";

    public Resource loadReportTemplate() {
        ClassPathResource resource = new ClassPathResource(REPORT_TEMPLATE_PATH);
        
        if (!resource.exists() || !resource.isReadable()) {
            throw new ReportTemplateNotFoundException("Template de relatório não encontrado ou não pode ser lido.");
        }

        return resource;
    }
}
