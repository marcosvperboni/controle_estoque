package com.marcosperboni.controle_estoque.web.handler;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        List<String> detalhes
) {
    public static ApiError of(int status, String erro, String mensagem) {
        return new ApiError(Instant.now(), status, erro, mensagem, List.of());
    }

    public static ApiError of(int status, String erro, String mensagem, List<String> detalhes) {
        return new ApiError(Instant.now(), status, erro, mensagem, detalhes);
    }
}
