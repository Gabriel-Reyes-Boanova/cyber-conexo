package br.com.cyberconexo.exception;

import org.springframework.http.HttpStatus;

/** Erro de negócio que vira resposta HTTP com mensagem em português. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
