package com.marcosperboni.controle_estoque.application.service;

import com.marcosperboni.controle_estoque.application.dto.ProdutoRequest;
import com.marcosperboni.controle_estoque.application.dto.ProdutoResponse;
import com.marcosperboni.controle_estoque.application.exception.BusinessException;
import com.marcosperboni.controle_estoque.application.exception.ResourceNotFoundException;
import com.marcosperboni.controle_estoque.domain.model.Produto;
import com.marcosperboni.controle_estoque.domain.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final BigDecimal limiarCompraPercentual;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            @Value("${app.estoque.limiar-compra-percentual}") BigDecimal limiarCompraPercentual
    ) {
        this.produtoRepository = produtoRepository;
        this.limiarCompraPercentual = limiarCompraPercentual;
    }

    public ProdutoResponse cadastrar(ProdutoRequest request) {
        produtoRepository.findByCodigoBarras(request.codigoBarras()).ifPresent(existente -> {
            throw new BusinessException("Ja existe um produto cadastrado com o codigo de barras " + request.codigoBarras());
        });

        Produto produto = new Produto(
                request.nome(), request.codigoBarras(), request.fornecedor(), request.marca(),
                request.dataValidade(), request.precoVarejo(), request.precoAtacado(),
                request.precoCompra(), request.precoVenda(), request.quantidade(), request.quantidadeMinima()
        );
        return toResponse(produtoRepository.save(produto));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return produtoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidadePorId(id));
    }

    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarEntidadePorId(id);

        if (produtoRepository.existsByCodigoBarrasAndIdNot(request.codigoBarras(), id)) {
            throw new BusinessException("Ja existe outro produto cadastrado com o codigo de barras " + request.codigoBarras());
        }

        produto.atualizarDados(
                request.nome(), request.codigoBarras(), request.fornecedor(), request.marca(),
                request.dataValidade(), request.precoVarejo(), request.precoAtacado(),
                request.precoCompra(), request.precoVenda(), request.quantidade(), request.quantidadeMinima()
        );
        return toResponse(produto);
    }

    public void excluir(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produto nao encontrado: " + id);
        }
        produtoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listaDeCompras() {
        return produtoRepository.findAll().stream()
                .filter(produto -> produto.isAbaixoDoLimiarDeCompra(limiarCompraPercentual))
                .map(this::toResponse)
                .toList();
    }

    private Produto buscarEntidadePorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado: " + id));
    }

    private ProdutoResponse toResponse(Produto produto) {
        return ProdutoResponse.from(produto, limiarCompraPercentual);
    }
}
