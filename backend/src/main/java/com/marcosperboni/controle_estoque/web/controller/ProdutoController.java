package com.marcosperboni.controle_estoque.web.controller;

import com.marcosperboni.controle_estoque.application.dto.ProdutoRequest;
import com.marcosperboni.controle_estoque.application.dto.ProdutoResponse;
import com.marcosperboni.controle_estoque.application.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> cadastrar(@Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.cadastrar(request));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listar() {
        return ResponseEntity.ok(produtoService.listar());
    }

    @GetMapping("/lista-compras")
    public ResponseEntity<List<ProdutoResponse>> listaDeCompras() {
        return ResponseEntity.ok(produtoService.listaDeCompras());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.ok(produtoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
