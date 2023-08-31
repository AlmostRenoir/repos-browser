package almostrenoir.reposbrowser.gitrepos;

import almostrenoir.reposbrowser.gitrepos.dtos.outgoing.GitBranchOutgoingDTO;
import almostrenoir.reposbrowser.gitrepos.dtos.outgoing.GitRepoOutgoingDTO;
import almostrenoir.reposbrowser.gitrepos.services.main.GitReposMainService;
import almostrenoir.reposbrowser.shared.errorresult.HttpErrorResult;
import almostrenoir.reposbrowser.shared.exceptions.DataNotFoundException;
import almostrenoir.reposbrowser.shared.exceptions.ExternalServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(GitReposController.class)
class GitReposControllerTest {

    private static final String GET_USERS_REPOS_WITHOUT_FORKS_URI = "/git-repos/by-user/%s/no-forks";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GitReposMainService gitReposMainService;

    @Test
    void shouldSerializeResponseWhenGetUsersReposWithoutForks() throws JsonProcessingException {
        GitBranchOutgoingDTO branch = new GitBranchOutgoingDTO("master", "111111");
        GitRepoOutgoingDTO repo = new GitRepoOutgoingDTO("snake", "foobar", List.of(branch));
        when(gitReposMainService.getUsersReposWithoutForks(eq("foobar"))).thenReturn(Flux.just(repo));

        String expectedResponse = objectMapper.writeValueAsString(List.of(repo));

        webTestClient.get()
                .uri(getGetUsersReposWithoutForksUri("foobar"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(expectedResponse);
    }

    @Test
    void shouldReturn400IfUsernameBlankWhenGetUsersReposWithoutForks() throws JsonProcessingException {
        HttpErrorResult httpErrorResult = new HttpErrorResult(400, "[Username cannot be blank]");
        String expectedResponse = objectMapper.writeValueAsString(httpErrorResult);

        webTestClient.get()
                .uri(getGetUsersReposWithoutForksUri(" "))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().json(expectedResponse);
    }

    @Test
    void shouldReturn404IfUserNotFoundWhenGetUsersReposWithoutForks() throws JsonProcessingException {
        when(gitReposMainService.getUsersReposWithoutForks(eq("fooobar")))
                .thenReturn(Flux.error(new DataNotFoundException("There is no user with given username")));

        HttpErrorResult httpErrorResult = new HttpErrorResult(404, "There is no user with given username");
        String expectedResponse = objectMapper.writeValueAsString(httpErrorResult);

        webTestClient.get()
                .uri(getGetUsersReposWithoutForksUri("fooobar"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().json(expectedResponse);
    }

    @Test
    void shouldReturn406IfAcceptApplicationXMLWhenGetUsersWithoutForks() throws JsonProcessingException {
//        TODO: find why handleNotAcceptableStatusException from GlobalExceptionHandler is not triggered
        HttpErrorResult httpErrorResult = new HttpErrorResult(406, "Desired content type is not acceptable");
        String expectedResponse = objectMapper.writeValueAsString(httpErrorResult);

        webTestClient.get()
                .uri(getGetUsersReposWithoutForksUri("foobar"))
                .header("Accept", "application/xml")
                .exchange()
                .expectStatus().isEqualTo(406)
                .expectBody().json(expectedResponse);
    }

    @Test
    void shouldReturn500IfProblemWithGithubWhenGetUsersReposWithoutForks() throws JsonProcessingException {
        when(gitReposMainService.getUsersReposWithoutForks(eq("foobar")))
                .thenReturn(Flux.error(new ExternalServiceException("Github is currently unavailable")));

        HttpErrorResult httpErrorResult = new HttpErrorResult(500, "Github is currently unavailable");
        String expectedResponse = objectMapper.writeValueAsString(httpErrorResult);

        webTestClient.get()
                .uri(getGetUsersReposWithoutForksUri("foobar"))
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody().json(expectedResponse);
    }

    private String getGetUsersReposWithoutForksUri(String username) {
        return String.format(GET_USERS_REPOS_WITHOUT_FORKS_URI, username);
    }

}