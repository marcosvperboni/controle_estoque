package com.marcosperboni.controle_estoque.web.handler;

import com.marcosperboni.controle_estoque.application.exception.BusinessException;
import com.marcosperboni.controle_estoque.application.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();

        ApiError erro = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Dados invalidos",
                "Um ou mais campos estao invalidos",
                detalhes
        );
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNaoEncontrado(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(HttpStatus.NOT_FOUND.value(), "Nao encontrado", ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleNegocio(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(HttpStatus.CONFLICT.value(), "Regra de negocio violada", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleArquivoGrande(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiError.of(HttpStatus.PAYLOAD_TOO_LARGE.value(), "Arquivo muito grande", "O arquivo enviado excede o limite permitido"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenerico(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro interno", "Ocorreu um erro inesperado"));
    }
}
