package com.marcosperboni.controle_estoque.application.service;

import com.marcosperboni.controle_estoque.application.dto.NotaFiscalImportResponse;
import com.marcosperboni.controle_estoque.application.dto.ProdutoResponse;
import com.marcosperboni.controle_estoque.domain.model.Produto;
import com.marcosperboni.controle_estoque.domain.repository.ProdutoRepository;
import com.marcosperboni.controle_estoque.infrastructure.nfe.NfeDocumento;
import com.marcosperboni.controle_estoque.infrastructure.nfe.NfeItem;
import com.marcosperboni.controle_estoque.infrastructure.nfe.NfeXmlParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports an NF-e (electronic invoice) XML: known barcodes are restocked, unknown ones are
 * auto-registered. NF-e does not carry retail/wholesale sale price, so a default markup is
 * applied on top of the invoice's unit cost - the user can fine-tune each product afterwards.
 */
@Service
@Transactional
public class NotaFiscalImportService {

    private static final BigDecimal MARKUP_PADRAO = new BigDecimal("1.30");
    private static final BigDecimal DESCONTO_ATACADO = new BigDecimal("0.90");

    private final NfeXmlParser nfeXmlParser;
    private final ProdutoRepository produtoRepository;
    private final BigDecimal limiarCompraPercentual;

    public NotaFiscalImportService(
            NfeXmlParser nfeXmlParser,
            ProdutoRepository produtoRepository,
            @Value("${app.estoque.limiar-compra-percentual}") BigDecimal limiarCompraPercentual
    ) {
        this.nfeXmlParser = nfeXmlParser;
        this.produtoRepository = produtoRepository;
        this.limiarCompraPercentual = limiarCompraPercentual;
    }

    public NotaFiscalImportResponse importar(InputStream xmlInputStream) {
        NfeDocumento documento = nfeXmlParser.parse(xmlInputStream);

        List<ProdutoResponse> criados = new ArrayList<>();
        List<ProdutoResponse> reabastecidos = new ArrayList<>();

        for (NfeItem item : documento.itens()) {
            produtoRepository.findByCodigoBarras(item.codigoBarras()).ifPresentOrElse(
                    existente -> {
                        existente.adicionarAoEstoque(item.quantidade());
                        reabastecidos.add(ProdutoResponse.from(existente, limiarCompraPercentual));
                    },
                    () -> {
                        Produto novo = criarProdutoAPartirDaNfe(item, documento.fornecedor());
                        criados.add(ProdutoResponse.from(produtoRepository.save(novo), limiarCompraPercentual));
                    }
            );
        }

        return new NotaFiscalImportResponse(documento.fornecedor(), criados, reabastecidos);
    }

    private Produto criarProdutoAPartirDaNfe(NfeItem item, String fornecedor) {
        BigDecimal precoCompra = item.precoCompraUnitario();
        BigDecimal precoVenda = precoCompra.multiply(MARKUP_PADRAO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal precoAtacado = precoVenda.multiply(DESCONTO_ATACADO).setScale(2, RoundingMode.HALF_UP);
        int quantidadeMinima = Math.max(10, item.quantidade() / 5);

        return new Produto(
                item.nome(), item.codigoBarras(), fornecedor, item.marca(),
                null, precoVenda, precoAtacado, precoCompra, precoVenda,
                item.quantidade(), quantidadeMinima
        );
    }
}
