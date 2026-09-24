package com.mbarca.ByR.exceptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> notFound(NotFoundException e) { return error(HttpStatus.NOT_FOUND, e.getMessage()); }
    @ExceptionHandler(RepositoryException.class)
    public ResponseEntity<?> conflict(RepositoryException e) { return error(HttpStatus.CONFLICT, e.getMessage()); }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> invalid(IllegalArgumentException e) { return error(HttpStatus.BAD_REQUEST, e.getMessage()); }
    @ExceptionHandler({JsonProcessingException.class, MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class, org.springframework.http.converter.HttpMessageNotReadableException.class})
    public ResponseEntity<?> malformed(Exception e) { return error(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos"); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException e) {
        return error(HttpStatus.BAD_REQUEST, "Revisá los campos obligatorios y la longitud de las contraseñas");
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> constraints(ConstraintViolationException e) {
        return error(HttpStatus.BAD_REQUEST, "Revisá los campos obligatorios y los valores numéricos");
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> tooLarge(MaxUploadSizeExceededException e) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, "Las imágenes superan el tamaño máximo de 30 MB por solicitud");
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception e) {
        log.error("Error procesando la solicitud", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo completar la operación");
    }
}
