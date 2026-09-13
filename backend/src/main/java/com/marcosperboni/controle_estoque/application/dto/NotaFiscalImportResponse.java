package com.marcosperboni.controle_estoque.application.dto;

import java.util.List;

public record NotaFiscalImportResponse(
        String fornecedor,
        List<ProdutoResponse> produtosCriados,
        List<ProdutoResponse> produtosReabastecidos
) {
}
