package com.marcosperboni.controle_estoque.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoRequest(
        @NotBlank(message = "nome e obrigatorio") @Size(max = 150) String nome,
        @NotBlank(message = "codigoBarras e obrigatorio") @Size(max = 50) String codigoBarras,
        @NotBlank(message = "fornecedor e obrigatorio") @Size(max = 150) String fornecedor,
        @NotBlank(message = "marca e obrigatoria") @Size(max = 100) String marca,
        LocalDate dataValidade,
        @NotNull(message = "precoVarejo e obrigatorio") @DecimalMin(value = "0.0", inclusive = false) BigDecimal precoVarejo,
        @NotNull(message = "precoAtacado e obrigatorio") @DecimalMin(value = "0.0", inclusive = false) BigDecimal precoAtacado,
        @NotNull(message = "precoCompra e obrigatorio") @DecimalMin(value = "0.0", inclusive = false) BigDecimal precoCompra,
        @NotNull(message = "precoVenda e obrigatorio") @DecimalMin(value = "0.0", inclusive = false) BigDecimal precoVenda,
        @NotNull(message = "quantidade e obrigatoria") @Min(0) Integer quantidade,
        @NotNull(message = "quantidadeMinima e obrigatoria") @Min(0) Integer quantidadeMinima
) {
}
