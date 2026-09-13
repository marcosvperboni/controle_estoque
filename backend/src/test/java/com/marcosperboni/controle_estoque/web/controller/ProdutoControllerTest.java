package com.marcosperboni.controle_estoque.web.controller;

import tools.jackson.databind.ObjectMapper;
import com.marcosperboni.controle_estoque.application.dto.ProdutoRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ProdutoRequest requestValido(String codigoBarras) {
        return new ProdutoRequest(
                "Refrigerante 2L", codigoBarras, "Distribuidora ABC", "Marca X",
                null, new BigDecimal("9.90"), new BigDecimal("8.90"),
                new BigDecimal("5.00"), new BigDecimal("9.90"), 100, 20
        );
    }

    private long cadastrarProduto(String codigoBarras) throws Exception {
        String json = mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido(codigoBarras))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    @Test
    void deveCadastrarProduto() throws Exception {
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido("1000000000001"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Refrigerante 2L"))
                .andExpect(jsonPath("$.markup").value(1.98))
                .andExpect(jsonPath("$.precoCompra").value(5.00));
    }

    @Test
    void deveRejeitarCadastroComCamposInvalidos() throws Exception {
        ProdutoRequest invalido = new ProdutoRequest(
                "", "", "", "", null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, -1, -1
        );

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarCadastroComCodigoDeBarrasDuplicado() throws Exception {
        cadastrarProduto("1000000000002");

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido("1000000000002"))))
                .andExpect(status().isConflict());
    }

    @Test
    void deveListarProdutosCadastrados() throws Exception {
        cadastrarProduto("1000000000003");

        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deveBuscarProdutoPorId() throws Exception {
        long id = cadastrarProduto("1000000000004");

        mockMvc.perform(get("/api/produtos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void deveRetornar404AoBuscarProdutoInexistente() throws Exception {
        mockMvc.perform(get("/api/produtos/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarProduto() throws Exception {
        long id = cadastrarProduto("1000000000005");
        ProdutoRequest atualizacao = new ProdutoRequest(
                "Refrigerante 2L Promo", "1000000000005", "Distribuidora ABC", "Marca X",
                null, new BigDecimal("8.90"), new BigDecimal("7.90"),
                new BigDecimal("4.50"), new BigDecimal("8.90"), 50, 20
        );

        mockMvc.perform(put("/api/produtos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Refrigerante 2L Promo"))
                .andExpect(jsonPath("$.quantidade").value(50));
    }

    @Test
    void deveExcluirProduto() throws Exception {
        long id = cadastrarProduto("1000000000006");

        mockMvc.perform(delete("/api/produtos/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/produtos/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarSomenteProdutosAbaixoDoLimiarDeCompra() throws Exception {
        ProdutoRequest estoqueCritico = new ProdutoRequest(
                "Item critico", "1000000000007", "Fornecedor Y", "Marca Y",
                null, new BigDecimal("20.00"), new BigDecimal("18.00"),
                new BigDecimal("10.00"), new BigDecimal("20.00"), 2, 20
        );
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estoqueCritico)))
                .andExpect(status().isCreated());
        cadastrarProduto("1000000000008");

        mockMvc.perform(get("/api/produtos/lista-compras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigoBarras=='1000000000007')]").exists())
                .andExpect(jsonPath("$[?(@.codigoBarras=='1000000000008')]").doesNotExist());
    }
}
