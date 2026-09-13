package com.marcosperboni.controle_estoque.infrastructure.crypto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoServiceTest {

    private final CryptoService cryptoService = new CryptoService("chave-de-teste-0123456789abcdef");

    @Test
    void deveCriptografarEDescriptografarMantendoOValorOriginal() {
        String original = "1234.56";

        String criptografado = cryptoService.encrypt(original);
        String decifrado = cryptoService.decrypt(criptografado);

        assertThat(criptografado).isNotEqualTo(original);
        assertThat(decifrado).isEqualTo(original);
    }

    @Test
    void deveGerarCifradoDiferenteACadaChamadaMesmoComOMesmoValor() {
        String original = "500.00";

        String primeiraChamada = cryptoService.encrypt(original);
        String segundaChamada = cryptoService.encrypt(original);

        assertThat(primeiraChamada).isNotEqualTo(segundaChamada);
        assertThat(cryptoService.decrypt(primeiraChamada)).isEqualTo(original);
        assertThat(cryptoService.decrypt(segundaChamada)).isEqualTo(original);
    }
}
