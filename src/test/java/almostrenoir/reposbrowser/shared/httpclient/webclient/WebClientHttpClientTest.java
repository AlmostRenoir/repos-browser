package almostrenoir.reposbrowser.shared.httpclient.webclient;

import almostrenoir.reposbrowser.shared.httpclient.HttpException;
import almostrenoir.reposbrowser.shared.httpclient.HttpRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

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
    void shouldMapBodyToImmutableWhenGetAllWithLinkPagination() {
        String responseBody = "[{\"name\":\"Foo\", \"age\":35}, {\"name\":\"Bar\", \"age\":25}]";
        stubFor(get(urlEqualTo("/success"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        HttpRequest httpRequest = HttpRequest.builder().url(WIREMOCK_URL + "/success").build();
        List<TestResponse> response = httpClient
                .getAllWithLinkPagination(httpRequest, TestResponse.class)
                .collectList().block();

        assertNotNull(response);
        assertEquals(2, response.size());
        TestResponse secondElement = response.get(1);
        assertEquals("Bar", secondElement.getName());
        assertEquals(25, secondElement.getAge());
    }

    @Test
    void shouldFetchUntilNextPageExistWhenGetAllWithLinkPagination() {
        String firstPageBody = "[{\"name\":\"Foo\", \"age\":35}, {\"name\":\"Bar\", \"age\":25}]";
        stubFor(get(urlEqualTo("/paginated?page=1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Link", "<" + WIREMOCK_URL + "/paginated?page=2>; rel=\"next\"")
                        .withBody(firstPageBody)));

        String secondPageBody = "[{\"name\":\"John\", \"age\":45}, {\"name\":\"Jane\", \"age\":37}]";
        stubFor(get(urlEqualTo("/paginated?page=2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Link", "<" + WIREMOCK_URL + "/paginated?page=1>; rel=\"prev\"")
                        .withHeader("Link", "<" + WIREMOCK_URL + "/paginated?page=3>; rel=\"next\"")
                        .withHeader("Content-Type", "application/json")
                        .withBody(secondPageBody)));

        String thirdPageBody = "[{\"name\":\"Aaron\", \"age\":56}]";
        stubFor(get(urlEqualTo("/paginated?page=3"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Link", "<" + WIREMOCK_URL + "/paginated?page=2>; rel=\"prev\"")
                        .withBody(thirdPageBody)));

        HttpRequest httpRequest = HttpRequest.builder().url(WIREMOCK_URL + "/paginated?page=1").build();
        List<TestResponse> response = httpClient
                .getAllWithLinkPagination(httpRequest, TestResponse.class)
                .collectList().block();

        assertNotNull(response);
        assertEquals(5, response.size());
    }

    @Test
    void shouldAddHeadersWhenGetAllWithLinkPagination() {
        stubFor(get(urlEqualTo("/expect-header"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Link", "<" + WIREMOCK_URL + "/expect-header?page=2>; rel=\"next\"")));
        stubFor(get(urlEqualTo("/expect-header?page=2")).willReturn(aResponse().withStatus(200)));

        HttpRequest httpRequest = HttpRequest.builder()
                .url(WIREMOCK_URL + "/expect-header")
                .header("Custom-Header", "SomeValue")
                .build();
        httpClient.getAllWithLinkPagination(httpRequest, TestResponse.class).collectList().block();

        verify(getRequestedFor(urlEqualTo("/expect-header"))
                .withHeader("Custom-Header", equalTo("SomeValue")));
        verify(getRequestedFor(urlEqualTo("/expect-header?page=2"))
                .withHeader("Custom-Header", equalTo("SomeValue")));
    }

    @Test
    public void shouldThrowExceptionIfResourceNotFoundWhenGetAllWithLinkPagination() {
        stubFor(get(urlEqualTo("/not-found")).willReturn(aResponse().withStatus(404)));

        HttpRequest httpRequest = HttpRequest.builder().url(WIREMOCK_URL + "/not-found").build();
        assertThrows(HttpException.NotFound.class,
                () -> httpClient.getAllWithLinkPagination(httpRequest, TestResponse.class).collectList().block());
    }

    @Test
    public void shouldThrowExceptionIfServerErrorWhenGetAllWithLinkPagination() {
        stubFor(get(urlEqualTo("/server-error")).willReturn(aResponse().withStatus(500)));

        HttpRequest httpRequest = HttpRequest.builder().url(WIREMOCK_URL + "/server-error").build();
        assertThrows(HttpException.ServerError.class,
                () -> httpClient.getAllWithLinkPagination(httpRequest, TestResponse.class).collectList().block());
    }

    @Test
    public void shouldThrowExceptionIfTimeoutExceededWhenGetAllWithLinkPagination() {
        stubFor(get(urlEqualTo("/timeout"))
                .willReturn(aResponse()
                        .withFixedDelay(5000)
                        .withStatus(200)));

        HttpRequest httpRequest = HttpRequest.builder()
                .url(WIREMOCK_URL + "/timeout")
                .timeout(200)
                .build();
        assertThrows(HttpException.TimeoutExceeded.class,
                () -> httpClient.getAllWithLinkPagination(httpRequest, TestResponse.class).collectList().block());
    }

    @Data
    private static class TestResponse {
        private final String name;
        private final int age;
    }
}