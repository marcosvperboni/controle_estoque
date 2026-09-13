package com.marcosperboni.controle_estoque.application.service;

import com.marcosperboni.controle_estoque.application.dto.ProdutoRequest;
import com.marcosperboni.controle_estoque.application.dto.ProdutoResponse;
import com.marcosperboni.controle_estoque.application.exception.BusinessException;
import com.marcosperboni.controle_estoque.application.exception.ResourceNotFoundException;
import com.marcosperboni.controle_estoque.domain.model.Produto;
import com.marcosperboni.controle_estoque.domain.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    private ProdutoService produtoService;

    private Produto produtoExistente;

    @BeforeEach
    void setUp() throws Exception {
        produtoService = new ProdutoService(produtoRepository, new BigDecimal("0.30"));
        produtoExistente = new Produto(
                "Refrigerante 2L", "7891000100103", "Distribuidora ABC", "Marca X",
                null, new BigDecimal("9.90"), new BigDecimal("8.90"),
                new BigDecimal("5.00"), new BigDecimal("9.90"), 100, 20
        );
        setId(produtoExistente, 1L);
    }

    private void setId(Produto produto, Long id) throws Exception {
        Field campoId = Produto.class.getDeclaredField("id");
        campoId.setAccessible(true);
        campoId.set(produto, id);
    }

    private ProdutoRequest requestValido() {
        return new ProdutoRequest(
                "Refrigerante 2L", "7891000100103", "Distribuidora ABC", "Marca X",
                null, new BigDecimal("9.90"), new BigDecimal("8.90"),
                new BigDecimal("5.00"), new BigDecimal("9.90"), 100, 20
        );
    }

    @Test
    void deveCadastrarProdutoComMargemEMarkupCalculados() {
        when(produtoRepository.findByCodigoBarras(anyString())).thenReturn(Optional.empty());
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> {
            Produto p = invocation.getArgument(0);
            setId(p, 10L);
            return p;
        });

        ProdutoResponse response = produtoService.cadastrar(requestValido());

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.precoCompra()).isEqualByComparingTo("5.00");
        assertThat(response.markup()).isEqualByComparingTo("1.9800");
        assertThat(response.margem()).isEqualByComparingTo("0.4949");
    }

    @Test
    void deveRejeitarCadastroComCodigoDeBarrasDuplicado() {
        when(produtoRepository.findByCodigoBarras("7891000100103")).thenReturn(Optional.of(produtoExistente));

        assertThatThrownBy(() -> produtoService.cadastrar(requestValido()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ja existe um produto");

        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveBuscarProdutoPorId() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoExistente));

        ProdutoResponse response = produtoService.buscarPorId(1L);

        assertThat(response.nome()).isEqualTo("Refrigerante 2L");
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoEncontrado() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveAtualizarProduto() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoExistente));
        when(produtoRepository.existsByCodigoBarrasAndIdNot("7891000100103", 1L)).thenReturn(false);

        ProdutoRequest request = new ProdutoRequest(
                "Refrigerante 2L Atualizado", "7891000100103", "Distribuidora ABC", "Marca X",
                null, new BigDecimal("10.90"), new BigDecimal("9.90"),
                new BigDecimal("5.50"), new BigDecimal("10.90"), 80, 20
        );

        ProdutoResponse response = produtoService.atualizar(1L, request);

        assertThat(response.nome()).isEqualTo("Refrigerante 2L Atualizado");
        assertThat(response.quantidade()).isEqualTo(80);
    }

    @Test
    void deveRejeitarAtualizacaoComCodigoDeBarrasDeOutroProduto() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoExistente));
        when(produtoRepository.existsByCodigoBarrasAndIdNot("7899999999999", 1L)).thenReturn(true);

        ProdutoRequest request = new ProdutoRequest(
                "Refrigerante 2L", "7899999999999", "Distribuidora ABC", "Marca X",
                null, new BigDecimal("9.90"), new BigDecimal("8.90"),
                new BigDecimal("5.00"), new BigDecimal("9.90"), 100, 20
        );

        assertThatThrownBy(() -> produtoService.atualizar(1L, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deveExcluirProduto() {
        when(produtoRepository.existsById(1L)).thenReturn(true);

        produtoService.excluir(1L);

        verify(produtoRepository).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoAoExcluirProdutoInexistente() {
        when(produtoRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> produtoService.excluir(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(produtoRepository, never()).deleteById(any());
    }

    @Test
    void deveListarApenasProdutosAbaixoDoLimiarDeCompra() throws Exception {
        Produto estoqueBaixo = new Produto(
                "Item critico", "1111111111111", "Fornecedor Y", "Marca Y",
                null, new BigDecimal("20.00"), new BigDecimal("18.00"),
                new BigDecimal("10.00"), new BigDecimal("20.00"), 5, 20
        );
        setId(estoqueBaixo, 2L);

        when(produtoRepository.findAll()).thenReturn(List.of(produtoExistente, estoqueBaixo));

        List<ProdutoResponse> resultado = produtoService.listaDeCompras();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nome()).isEqualTo("Item critico");
    }
}
