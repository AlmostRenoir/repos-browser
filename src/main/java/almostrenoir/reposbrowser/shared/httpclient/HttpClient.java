package almostrenoir.reposbrowser.shared.httpclient;

import reactor.core.publisher.Flux;

public interface HttpClient {
    <T> Flux<T> getAllWithLinkPagination(HttpRequest httpRequest, Class<T> responseType);
}
