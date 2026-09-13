package com.marcosperboni.controle_estoque.infrastructure.nfe;

import com.marcosperboni.controle_estoque.application.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses the {@code det/prod} lines of a Brazilian NF-e (electronic invoice) XML using
 * the JDK's built-in DOM parser (no third-party dependency needed for this document size).
 */
@Component
public class NfeXmlParser {

    public NfeDocumento parse(InputStream xmlInputStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Hardened against XXE: no DOCTYPE, no external entities, since the XML is user-uploaded.
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlInputStream);
            document.getDocumentElement().normalize();

            String fornecedor = readText(document, "emit", "xNome").orElse("Fornecedor nao informado");

            NodeList itens = document.getElementsByTagName("det");
            if (itens.getLength() == 0) {
                throw new BusinessException("XML nao contem itens (det/prod) de uma NF-e valida");
            }

            List<NfeItem> resultado = new ArrayList<>();
            for (int i = 0; i < itens.getLength(); i++) {
                Node det = itens.item(i);
                if (det.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element prod = firstChild((Element) det, "prod")
                        .orElseThrow(() -> new BusinessException("Item da NF-e sem tag <prod>"));

                String codigoBarras = firstNonBlank(
                        childText(prod, "cEAN"),
                        childText(prod, "cEANTrib"),
                        childText(prod, "cProd")
                ).orElseThrow(() -> new BusinessException("Item da NF-e sem codigo de produto"));

                String nome = childText(prod, "xProd").orElse("Produto sem descricao");
                BigDecimal precoCompra = childText(prod, "vUnCom")
                        .map(BigDecimal::new)
                        .map(v -> v.setScale(2, RoundingMode.HALF_UP))
                        .orElse(BigDecimal.ZERO);
                int quantidade = childText(prod, "qCom")
                        .map(BigDecimal::new)
                        .map(BigDecimal::intValue)
                        .orElse(0);

                String marca = extrairMarca(nome);

                resultado.add(new NfeItem(codigoBarras, nome, marca, precoCompra, quantidade));
            }

            return new NfeDocumento(fornecedor, resultado);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Falha ao interpretar o XML da NF-e: " + e.getMessage());
        }
    }

    /**
     * NF-e has no dedicated "brand" field, so the first token of the product description
     * is used as a best-effort brand guess (e.g. "COCA-COLA REFRIGERANTE 2L" -> "COCA-COLA").
     */
    private String extrairMarca(String nomeProduto) {
        String[] tokens = nomeProduto.trim().split("\\s+");
        return tokens.length > 0 ? tokens[0] : "N/A";
    }

    private java.util.Optional<Element> firstChild(Element parent, String tagName) {
        NodeList children = parent.getElementsByTagName(tagName);
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getParentNode() == parent && node instanceof Element element) {
                return java.util.Optional.of(element);
            }
        }
        return java.util.Optional.empty();
    }

    private java.util.Optional<String> readText(Document document, String parentTag, String childTag) {
        NodeList parents = document.getElementsByTagName(parentTag);
        if (parents.getLength() == 0) {
            return java.util.Optional.empty();
        }
        return childText((Element) parents.item(0), childTag);
    }

    private java.util.Optional<String> childText(Element parent, String tagName) {
        NodeList children = parent.getElementsByTagName(tagName);
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getParentNode() == parent) {
                String text = node.getTextContent();
                return text == null || text.isBlank() ? java.util.Optional.empty() : java.util.Optional.of(text.trim());
            }
        }
        return java.util.Optional.empty();
    }

    @SafeVarargs
    private java.util.Optional<String> firstNonBlank(java.util.Optional<String>... valores) {
        for (java.util.Optional<String> valor : valores) {
            if (valor.isPresent() && !"SEM GTIN".equalsIgnoreCase(valor.get())) {
                return valor;
            }
        }
        for (java.util.Optional<String> valor : valores) {
            if (valor.isPresent()) {
                return valor;
            }
        }
        return java.util.Optional.empty();
    }
}
