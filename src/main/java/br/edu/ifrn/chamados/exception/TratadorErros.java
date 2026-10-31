package br.edu.ifrn.chamados.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class TratadorErros {
    public record Erro(Instant timestamp, int status, String mensagem, String caminho) {}

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Erro> naoEncontrado(RuntimeException erro, HttpServletRequest request) {
        return resposta(404, erro.getMessage(), request);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Erro> conflito(RuntimeException erro, HttpServletRequest request) {
        return resposta(409, erro.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Erro> integridade(HttpServletRequest request) {
        return resposta(409, "Registro duplicado ou vinculado a outros dados.", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> validacao(MethodArgumentNotValidException erro, HttpServletRequest request) {
        String mensagem = erro.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                .sorted().reduce((a, b) -> a + "; " + b).orElse("Dados inválidos.");
        return resposta(400, mensagem, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, IllegalArgumentException.class,
            PropertyReferenceException.class, InvalidDataAccessApiUsageException.class})
    public ResponseEntity<Erro> dadosInvalidos(HttpServletRequest request) {
        return resposta(400, "Confira os campos, os filtros e a ordenação da requisição.", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Erro> rotaInexistente(HttpServletRequest request) {
        return resposta(404, "Rota não encontrada.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Erro> metodoInvalido(HttpServletRequest request) {
        return resposta(405, "Método não permitido nesta rota.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Erro> inesperado(Exception erro, HttpServletRequest request) {
        log.error("Falha ao processar {}", request.getRequestURI(), erro);
        return resposta(500, "Não foi possível concluir a operação.", request);
    }

    private ResponseEntity<Erro> resposta(int status, String mensagem, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new Erro(Instant.now(), status, mensagem, request.getRequestURI()));
    }
}
