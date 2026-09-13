package com.marcosperboni.controle_estoque.infrastructure.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Transparently encrypts/decrypts BigDecimal columns at rest.
 * Registered as a Spring bean so Hibernate resolves it through Spring's bean container
 * and CryptoService can be constructor-injected instead of using a static/JNDI lookup.
 */
@Converter
@Component
public class EncryptedBigDecimalConverter implements AttributeConverter<BigDecimal, String> {

    private final CryptoService cryptoService;

    public EncryptedBigDecimalConverter(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Override
    public String convertToDatabaseColumn(BigDecimal attribute) {
        if (attribute == null) {
            return null;
        }
        return cryptoService.encrypt(attribute.toPlainString());
    }

    @Override
    public BigDecimal convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return new BigDecimal(cryptoService.decrypt(dbData));
    }
}
