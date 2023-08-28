package almostrenoir.reposbrowser.shared.httpclient.webclient;

import almostrenoir.reposbrowser.shared.httpclient.HttpException;
import almostrenoir.reposbrowser.shared.httpclient.HttpRequest;
import almostrenoir.reposbrowser.shared.pagination.PaginatedResult;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebClientHttpClientTest {

    private static final int WIREMOCK_PORT = 8092;
    private static final String WIREMOCK_URL = "http://localhost:" + WIREMOCK_PORT;

    @Autowired
    private WebClientHttpClient httpClient;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(WIREMOCK_PORT);
        wireMockServer.start();
        configureFor(WIREMOCK_PORT);
    }

    @AfterEach
    void teardown() {
        wireMockServer.stop();
    }

    @Test
    void shouldMapBodyWhenGetWithLinkPagination() {
        String responseBody = "[{\"name\":\"Foo\", \"age\":35}, {\"name\":\"Bar\", \"age\":25}]";
        stubFor(get(urlEqualTo("/success"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        HttpRequest httpRequest = HttpRequest.builder().url(WIREMOCK_URL + "/success").build();
        PaginatedResult<TestResponse> response = httpClient
                .getWithLinkPagination(httpRequest, TestResponse.class)
                .block();

        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        TestResponse secondElement = response.getContent().get(1);
        assertEquals("Bar", secondElement.getName());
        assertEquals(25, secondElement.getAge());
    }

    @Test
    void shouldMarkIfNextPageNotExistWhenGetWithLinkPagination() {
        stubFor(get(urlEqualTo("/next-page-not-exist"))
                .willReturn(aResponse().withStatus(200)));

        HttpRequest httpRequest = HttpRequest.builder().url(WIREMOCK_URL + "/next-page-not-exist").build();
        PaginatedResult<TestResponse> response = httpClient
                .getWithLinkPagination(httpRequest, TestResponse.class)
                .block();

        assertNotNull(response);
        assertFalse(response.isNextPage());
    }

    @Test
    void shouldMarkIfNextPageExistWhenGetWithLinkPagination() {
        stubFor(get(urlEqualTo("/next-page"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Link", "<prev-address>; rel=\"prev\", <next-address>; rel=\"next\"")));

        HttpRequest httpRequest = HttpRequest.builder().url(WIREMOCK_URL + "/next-page").build();
        PaginatedResult<TestResponse> response = httpClient
                .getWithLinkPagination(httpRequest, TestResponse.class)
                .block();

        assertNotNull(response);
        assertTrue(response.isNextPage());
    }

    @Test
    void shouldAddHeadersWhenGetWithLinkPagination() {
        stubFor(get(urlEqualTo("/expect-header"))
                .willReturn(aResponse().withStatus(200)));

        HttpRequest httpRequest = HttpRequest.builder()
                .url(WIREMOCK_URL + "/expect-header")
                .header("Custom-Header", "SomeValue")
                .build();
        httpClient.getWithLinkPagination(httpRequest, TestResponse.class).block();

        verify(getRequestedFor(urlEqualTo("/expect-header"))
                .withHeader("Custom-Header", equalTo("SomeValue")));
    }

    @Test
    public void shouldThrowExceptionIfResourceNotFoundWhenGetWithLinkPagination() {
        stubFor(get(urlEqualTo("/not-found")).willReturn(aResponse().withStatus(404)));

        HttpRequest httpRequest = HttpRequest.builder().url(WIREMOCK_URL + "/not-found").build();
        assertThrows(HttpException.NotFound.class,
                () -> httpClient.getWithLinkPagination(httpRequest, TestResponse.class).block());
    }

    @Test
    public void shouldThrowExceptionIfServerErrorWhenGetWithLinkPagination() {
        stubFor(get(urlEqualTo("/server-error")).willReturn(aResponse().withStatus(500)));

        HttpRequest httpRequest = HttpRequest.builder().url(WIREMOCK_URL + "/server-error").build();
        assertThrows(HttpException.ServerError.class,
                () -> httpClient.getWithLinkPagination(httpRequest, TestResponse.class).block());
    }

    @Test
    public void shouldThrowExceptionIfTimeoutExceededWhenGetWithLinkPagination() {
        stubFor(get(urlEqualTo("/timeout"))
                .willReturn(aResponse()
                        .withFixedDelay(5000)
                        .withStatus(200)));

        HttpRequest httpRequest = HttpRequest.builder()
                .url(WIREMOCK_URL + "/timeout")
                .timeout(200)
                .build();
        assertThrows(HttpException.TimeoutExceeded.class,
                () -> httpClient.getWithLinkPagination(httpRequest, TestResponse.class).block());
    }

    @Data
    private static class TestResponse {
        private final String name;
        private final int age;
    }
}