package com.marcosperboni.controle_estoque.infrastructure.nfe;

import com.marcosperboni.controle_estoque.application.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NfeXmlParserTest {

    private final NfeXmlParser parser = new NfeXmlParser();

    private static final String NFE_VALIDA = """
            <?xml version="1.0" encoding="UTF-8"?>
            <nfeProc xmlns="http://www.portalfiscal.inf.br/nfe">
              <NFe>
                <infNFe Id="NFe1234" versao="4.00">
                  <emit>
                    <xNome>Distribuidora ABC Ltda</xNome>
                  </emit>
                  <det nItem="1">
                    <prod>
                      <cProd>001</cProd>
                      <cEAN>7891000100103</cEAN>
                      <xProd>COCA-COLA REFRIGERANTE 2L</xProd>
                      <qCom>50.0000</qCom>
                      <vUnCom>4.5000</vUnCom>
                    </prod>
                  </det>
                  <det nItem="2">
                    <prod>
                      <cProd>002</cProd>
                      <cEAN>SEM GTIN</cEAN>
                      <xProd>PAO FRANCES UNIDADE</xProd>
                      <qCom>200.0000</qCom>
                      <vUnCom>0.4500</vUnCom>
                    </prod>
                  </det>
                </infNFe>
              </NFe>
            </nfeProc>
            """;

    @Test
    void deveExtrairFornecedorEItensDaNfe() {
        NfeDocumento documento = parser.parse(inputStream(NFE_VALIDA));

        assertThat(documento.fornecedor()).isEqualTo("Distribuidora ABC Ltda");
        assertThat(documento.itens()).hasSize(2);

        NfeItem primeiroItem = documento.itens().get(0);
        assertThat(primeiroItem.codigoBarras()).isEqualTo("7891000100103");
        assertThat(primeiroItem.nome()).isEqualTo("COCA-COLA REFRIGERANTE 2L");
        assertThat(primeiroItem.marca()).isEqualTo("COCA-COLA");
        assertThat(primeiroItem.precoCompraUnitario()).isEqualByComparingTo(new BigDecimal("4.50"));
        assertThat(primeiroItem.quantidade()).isEqualTo(50);
    }

    @Test
    void deveUsarCodigoDeProdutoQuandoNaoHaGtin() {
        NfeDocumento documento = parser.parse(inputStream(NFE_VALIDA));

        NfeItem segundoItem = documento.itens().get(1);
        assertThat(segundoItem.codigoBarras()).isEqualTo("002");
    }

    @Test
    void deveRejeitarXmlSemItens() {
        String xmlSemItens = """
                <?xml version="1.0" encoding="UTF-8"?>
                <NFe><infNFe><emit><xNome>Fornecedor</xNome></emit></infNFe></NFe>
                """;

        assertThatThrownBy(() -> parser.parse(inputStream(xmlSemItens)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deveRejeitarXmlComDoctypeExterno() {
        String xmlComDoctype = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE NFe [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <NFe><infNFe><det><prod><cProd>&xxe;</cProd></prod></det></infNFe></NFe>
                """;

        assertThatThrownBy(() -> parser.parse(inputStream(xmlComDoctype)))
                .isInstanceOf(BusinessException.class);
    }

    private InputStream inputStream(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }
}
