package almostrenoir.reposbrowser;

import almostrenoir.reposbrowser.shared.errorresult.HttpErrorResult;
import almostrenoir.reposbrowser.shared.exceptions.DataNotFoundException;
import almostrenoir.reposbrowser.shared.exceptions.ExternalServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    protected Mono<ResponseEntity<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errorMessages = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        HttpErrorResult httpErrorResult = new HttpErrorResult(400, errorMessages.toString());
        return Mono.just(ResponseEntity.status(httpErrorResult.getStatus()).body(httpErrorResult));
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<HttpErrorResult> handleDataNotFoundException(DataNotFoundException ex) {
        HttpErrorResult httpErrorResult = new HttpErrorResult(404, ex.getMessage());
        return ResponseEntity.status(httpErrorResult.getStatus()).body(httpErrorResult);
    }

    @Override
    protected Mono<ResponseEntity<Object>> handleNotAcceptableStatusException(
            NotAcceptableStatusException ex, HttpHeaders headers, HttpStatusCode status, ServerWebExchange exchange
    ) {
        HttpErrorResult httpErrorResult = new HttpErrorResult(406, "Desired content type is not acceptable");
        return Mono.just(ResponseEntity.status(httpErrorResult.getStatus()).body(httpErrorResult));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<HttpErrorResult> handleExternalServiceException(ExternalServiceException ex) {
        HttpErrorResult httpErrorResult = new HttpErrorResult(500, ex.getMessage());
        return ResponseEntity.status(httpErrorResult.getStatus()).body(httpErrorResult);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpErrorResult> handleUnexpectedException(Exception ex) {
        log.error("An unexpected error has occurred: {}", ex.getMessage());
        HttpErrorResult httpErrorResult = new HttpErrorResult(500, "An unexpected error has occurred");
        return ResponseEntity.status(httpErrorResult.getStatus()).body(httpErrorResult);
    }

}
