package almostrenoir.reposbrowser.shared.httpclient.webclient;

import almostrenoir.reposbrowser.shared.httpclient.HttpClient;
import almostrenoir.reposbrowser.shared.httpclient.HttpException;
import almostrenoir.reposbrowser.shared.httpclient.HttpRequest;
import almostrenoir.reposbrowser.shared.pagination.PaginatedResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Component
public class WebClientHttpClient implements HttpClient {
    private final WebClient webClient;

    public WebClientHttpClient(WebClient.Builder webClientBuilder) {
        webClient = webClientBuilder.build();
    }

    @Override
    public <T> Mono<PaginatedResult<T>> getWithLinkPagination(HttpRequest httpRequest, Class<T> responseType) {
        return webClient.get()
                .uri(httpRequest.getUrl())
                .headers(headers -> addAllHeaders(headers, httpRequest.getHeaders()))
                .exchangeToMono(response -> handleGetWithLinkPaginationResponse(response, responseType))
                .timeout(Duration.ofMillis(httpRequest.getTimeout()))
                .onErrorMap(TimeoutException.class, ex -> new HttpException.TimeoutExceeded());
    }

    private <T> Mono<PaginatedResult<T>> handleGetWithLinkPaginationResponse(
            ClientResponse response, Class<T> responseType
    ) {
        if (HttpStatus.NOT_FOUND.equals(response.statusCode())) return Mono.error(new HttpException.NotFound());
        if (response.statusCode().is5xxServerError()) return Mono.error(new HttpException.ServerError());

        HttpHeaders headers = response.headers().asHttpHeaders();
        Optional<List<String>> linkHeader = Optional.ofNullable(headers.get(HttpHeaders.LINK));
        boolean nextPage = linkHeader
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch(link -> link.contains("rel=\"next\""));

        Flux<T> responseBody = response.bodyToFlux(responseType);
        return responseBody.collectList().flatMap(content -> Mono.just(new PaginatedResult<>(content, nextPage)));
    }

    private void addAllHeaders(HttpHeaders outputHeaders, List<HttpRequest.Header> inputHeaders) {
        inputHeaders.forEach(header -> outputHeaders.add(header.name(), header.value()));
    }

}
