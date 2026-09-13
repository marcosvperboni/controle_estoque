package com.marcosperboni.controle_estoque.domain.model;

import com.marcosperboni.controle_estoque.infrastructure.crypto.EncryptedBigDecimalConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "produtos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "codigo_barras", nullable = false, unique = true, length = 50)
    private String codigoBarras;

    @Column(nullable = false, length = 150)
    private String fornecedor;

    @Column(nullable = false, length = 100)
    private String marca;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(name = "preco_varejo", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoVarejo;

    @Column(name = "preco_atacado", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoAtacado;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(name = "preco_compra", nullable = false, columnDefinition = "TEXT")
    private BigDecimal precoCompra;

    @Column(name = "preco_venda", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoVenda;

    @Convert(converter = EncryptedBigDecimalConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private BigDecimal margem;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal markup;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "quantidade_minima", nullable = false)
    private int quantidadeMinima;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    public Produto(String nome, String codigoBarras, String fornecedor, String marca,
                    LocalDate dataValidade, BigDecimal precoVarejo, BigDecimal precoAtacado,
                    BigDecimal precoCompra, BigDecimal precoVenda, int quantidade, int quantidadeMinima) {
        this.nome = nome;
        this.codigoBarras = codigoBarras;
        this.fornecedor = fornecedor;
        this.marca = marca;
        this.dataValidade = dataValidade;
        this.precoVarejo = precoVarejo;
        this.precoAtacado = precoAtacado;
        this.precoVenda = precoVenda;
        this.quantidade = quantidade;
        this.quantidadeMinima = quantidadeMinima;
        definirPrecoCompra(precoCompra);
    }

    @PrePersist
    void aoPersistir() {
        Instant agora = Instant.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        this.atualizadoEm = Instant.now();
    }

    public void atualizarDados(String nome, String codigoBarras, String fornecedor, String marca,
                                LocalDate dataValidade, BigDecimal precoVarejo, BigDecimal precoAtacado,
                                BigDecimal precoCompra, BigDecimal precoVenda, int quantidade, int quantidadeMinima) {
        this.nome = nome;
        this.codigoBarras = codigoBarras;
        this.fornecedor = fornecedor;
        this.marca = marca;
        this.dataValidade = dataValidade;
        this.precoVarejo = precoVarejo;
        this.precoAtacado = precoAtacado;
        this.precoVenda = precoVenda;
        this.quantidade = quantidade;
        this.quantidadeMinima = quantidadeMinima;
        definirPrecoCompra(precoCompra);
    }

    public void adicionarAoEstoque(int quantidadeRecebida) {
        this.quantidade += quantidadeRecebida;
    }

    /**
     * Recomputes margin and markup from cost/sale price whenever the cost price changes,
     * keeping the two derived indicators consistent instead of trusting client-sent values.
     */
    private void definirPrecoCompra(BigDecimal precoCompra) {
        this.precoCompra = precoCompra;
        if (precoCompra != null && precoCompra.compareTo(BigDecimal.ZERO) > 0 && this.precoVenda != null) {
            this.markup = this.precoVenda.divide(precoCompra, 4, RoundingMode.HALF_UP);
            this.margem = this.precoVenda.subtract(precoCompra)
                    .divide(this.precoVenda, 4, RoundingMode.HALF_UP);
        } else {
            this.markup = BigDecimal.ZERO;
            this.margem = BigDecimal.ZERO;
        }
    }

    public boolean isAbaixoDoLimiarDeCompra(BigDecimal limiarPercentual) {
        if (quantidadeMinima <= 0) {
            return false;
        }
        BigDecimal limite = BigDecimal.valueOf(quantidadeMinima).multiply(limiarPercentual);
        return BigDecimal.valueOf(quantidade).compareTo(limite) <= 0;
    }
}
