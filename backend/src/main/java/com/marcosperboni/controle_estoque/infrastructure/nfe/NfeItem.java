package com.marcosperboni.controle_estoque.infrastructure.nfe;

import java.math.BigDecimal;

/**
 * One line item ({@code det/prod}) extracted from a Brazilian NF-e (electronic invoice) XML.
 */
public record NfeItem(
        String codigoBarras,
        String nome,
        String marca,
        BigDecimal precoCompraUnitario,
        int quantidade
) {
}
