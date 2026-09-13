package com.marcosperboni.controle_estoque.infrastructure.nfe;

import java.util.List;

public record NfeDocumento(String fornecedor, List<NfeItem> itens) {
}
