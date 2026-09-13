package com.marcosperboni.controle_estoque.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotaFiscalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String NFE_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <nfeProc xmlns="http://www.portalfiscal.inf.br/nfe">
              <NFe>
                <infNFe Id="NFe1234" versao="4.00">
                  <emit><xNome>Distribuidora ABC Ltda</xNome></emit>
                  <det nItem="1">
                    <prod>
                      <cProd>001</cProd>
                      <cEAN>2000000000001</cEAN>
                      <xProd>COCA-COLA REFRIGERANTE 2L</xProd>
                      <qCom>50.0000</qCom>
                      <vUnCom>4.5000</vUnCom>
                    </prod>
                  </det>
                </infNFe>
              </NFe>
            </nfeProc>
            """;

    @Test
    void deveImportarNfeECadastrarProdutoAutomaticamente() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "nfe.xml", "text/xml", NFE_XML.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/notas-fiscais/importar").file(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fornecedor").value("Distribuidora ABC Ltda"))
                .andExpect(jsonPath("$.produtosCriados", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.produtosCriados[0].codigoBarras").value("2000000000001"))
                .andExpect(jsonPath("$.produtosCriados[0].quantidade").value(50));
    }

    @Test
    void deveReabastecerProdutoExistenteAoImportarNfeComMesmoCodigoDeBarras() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "nfe.xml", "text/xml", NFE_XML.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/notas-fiscais/importar").file(arquivo)).andExpect(status().isOk());

        mockMvc.perform(multipart("/api/notas-fiscais/importar").file(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtosCriados", org.hamcrest.Matchers.hasSize(0)))
                .andExpect(jsonPath("$.produtosReabastecidos", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.produtosReabastecidos[0].quantidade").value(100));
    }

    @Test
    void deveRejeitarArquivoVazio() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "vazio.xml", "text/xml", new byte[0]);

        mockMvc.perform(multipart("/api/notas-fiscais/importar").file(arquivo))
                .andExpect(status().isConflict());
    }
}
