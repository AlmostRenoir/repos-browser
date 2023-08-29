package almostrenoir.reposbrowser.shared.httpclient.webclient;

import almostrenoir.reposbrowser.shared.httpclient.HttpClient;
import almostrenoir.reposbrowser.shared.httpclient.HttpException;
import almostrenoir.reposbrowser.shared.httpclient.HttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
public class WebClientHttpClient implements HttpClient {
    private final WebClient webClient;

    public WebClientHttpClient(WebClient.Builder webClientBuilder) {
        webClient = webClientBuilder.build();
    }

    @Override
    public <T> Flux<T> getAllWithLinkPagination(HttpRequest httpRequest, Class<T> responseType) {
        return webClient.get()
                .uri(httpRequest.getUrl())
                .headers(headers -> addAllHeaders(headers, httpRequest.getHeaders()))
                .exchangeToFlux(clientResponse -> handlePagination(clientResponse, httpRequest, responseType))
                .timeout(Duration.ofMillis(httpRequest.getTimeout()))
                .onErrorMap(TimeoutException.class, ex -> new HttpException.TimeoutExceeded());
    }

    private <T> Flux<T> handlePagination(ClientResponse clientResponse, HttpRequest httpRequest, Class<T> elementType) {
        if (HttpStatus.NOT_FOUND.equals(clientResponse.statusCode())) return Flux.error(new HttpException.NotFound());
        if (clientResponse.statusCode().is5xxServerError()) return Flux.error(new HttpException.ServerError());

        return clientResponse.bodyToFlux(elementType)
                .concatWith(clientResponse
                        .headers()
                        .header(HttpHeaders.LINK)
                        .stream()
                        .filter(link -> link.contains("rel=\"next\""))
                        .findFirst()
                        .map(link -> {
                            String nextUrl = link.substring(link.indexOf('<') + 1, link.indexOf('>'));
                            return getAllWithLinkPagination(httpRequest.withNewUrl(nextUrl), elementType);
                        })
                        .orElse(Flux.empty()));
    }

    private void addAllHeaders(HttpHeaders outputHeaders, List<HttpRequest.Header> inputHeaders) {
        inputHeaders.forEach(header -> outputHeaders.add(header.name(), header.value()));
    }

}
