package com.fluxo.report.service;

import com.fluxo.report.exception.ReportStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;
    private final String baseUrl;

    public FileStorageService(@Value("${app.storage.base-url:https://storage.example.com/reports/}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.uploadDir = Paths.get("uploads/reports/").toAbsolutePath();
        initDirectory();
    }

    private void initDirectory() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new ReportStorageException("Não foi possível criar o diretório de uploads.", e);
        }
    }

    /**
     * Salva um arquivo no storage e retorna a URL do arquivo.
     *
     * @param file O arquivo a ser salvo
     * @return A URL completa do arquivo armazenado
     * @throws ReportStorageException Se houver erro ao salvar o arquivo
     */
    public String saveFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + ".pdf";
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return baseUrl + fileName;
        } catch (IOException e) {
            throw new ReportStorageException("Erro ao salvar o arquivo no storage.", e);
        }
    }

    /**
     * Deleta um arquivo do storage baseado na URL.
     *
     * @param fileUrl A URL do arquivo a ser deletado
     * @throws ReportStorageException Se houver erro ao deletar o arquivo
     */
    public void deleteFile(String fileUrl) {
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = uploadDir.resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new ReportStorageException("Erro ao deletar o arquivo do storage.", e);
        }
    }
}
