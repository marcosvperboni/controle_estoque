package com.marcosperboni.controle_estoque.web.controller;

import com.marcosperboni.controle_estoque.application.dto.NotaFiscalImportResponse;
import com.marcosperboni.controle_estoque.application.exception.BusinessException;
import com.marcosperboni.controle_estoque.application.service.NotaFiscalImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@RestController
@RequestMapping("/api/notas-fiscais")
public class NotaFiscalController {

    private final NotaFiscalImportService notaFiscalImportService;

    public NotaFiscalController(NotaFiscalImportService notaFiscalImportService) {
        this.notaFiscalImportService = notaFiscalImportService;
    }

    @PostMapping("/importar")
    public ResponseEntity<NotaFiscalImportResponse> importar(@RequestParam("arquivo") MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new BusinessException("Arquivo de NF-e nao pode estar vazio");
        }
        try {
            return ResponseEntity.ok(notaFiscalImportService.importar(arquivo.getInputStream()));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo enviado", e);
        }
    }
}
