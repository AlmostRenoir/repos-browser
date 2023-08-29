package almostrenoir.reposbrowser.shared.httpclient;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {

    @Test
    void shouldCopyRemainingFieldsWhenWithNewUrl() {
        HttpRequest httpRequest = HttpRequest.builder()
                .accept(ContentType.JSON)
                .timeout(300)
                .url("testing.test/test")
                .build();

        HttpRequest httpRequestWithNewUrl = httpRequest.withNewUrl("testing.test/other-route");

        assertEquals(httpRequest.getHeaders(), httpRequestWithNewUrl.getHeaders());
        assertEquals(httpRequest.getTimeout(), httpRequest.getTimeout());
        assertNotEquals(httpRequest.getUrl(), httpRequestWithNewUrl.getUrl());
    }

    @Test
    void shouldAllowManyHeadersWithSameNameWhenBuild() {
        HttpRequest httpRequest = HttpRequest.builder()
                .url("testing.test/test")
                .header("Custom-Header", "First Value")
                .header("Custom-Header", "Second Value")
                .build();

        assertEquals(2, httpRequest.getHeaders().size());
    }
}