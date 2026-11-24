package br.com.cyberconexo.exception;

import java.time.Instant;

/** Formato único de erro devolvido pela API. O front lê o campo "message". */
public record ErrorResponse(int status, String error, String message, Instant timestamp) { }
