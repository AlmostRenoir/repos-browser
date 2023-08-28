package almostrenoir.reposbrowser.shared.httpclient;

import almostrenoir.reposbrowser.shared.pagination.PaginatedResult;
import reactor.core.publisher.Mono;

public interface HttpClient {
    <T> Mono<PaginatedResult<T>> getWithLinkPagination(HttpRequest httpRequest, Class<T> responseType);
}
