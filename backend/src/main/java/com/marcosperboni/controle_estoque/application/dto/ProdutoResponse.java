package com.marcosperboni.controle_estoque.application.dto;

import com.marcosperboni.controle_estoque.domain.model.Produto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ProdutoResponse(
        Long id,
        String nome,
        String codigoBarras,
        String fornecedor,
        String marca,
        LocalDate dataValidade,
        BigDecimal precoVarejo,
        BigDecimal precoAtacado,
        BigDecimal precoCompra,
        BigDecimal precoVenda,
        BigDecimal margem,
        BigDecimal markup,
        int quantidade,
        int quantidadeMinima,
        boolean abaixoDoLimiarDeCompra,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ProdutoResponse from(Produto produto, BigDecimal limiarPercentual) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getCodigoBarras(),
                produto.getFornecedor(),
                produto.getMarca(),
                produto.getDataValidade(),
                produto.getPrecoVarejo(),
                produto.getPrecoAtacado(),
                produto.getPrecoCompra(),
                produto.getPrecoVenda(),
                produto.getMargem(),
                produto.getMarkup(),
                produto.getQuantidade(),
                produto.getQuantidadeMinima(),
                produto.isAbaixoDoLimiarDeCompra(limiarPercentual),
                produto.getCriadoEm(),
                produto.getAtualizadoEm()
        );
    }
}
