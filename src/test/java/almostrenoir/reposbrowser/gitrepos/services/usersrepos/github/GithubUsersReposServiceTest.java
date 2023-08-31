package almostrenoir.reposbrowser.gitrepos.services.usersrepos.github;

import almostrenoir.reposbrowser.gitrepos.GitBranch;
import almostrenoir.reposbrowser.gitrepos.GitRepo;
import almostrenoir.reposbrowser.shared.exceptions.DataNotFoundException;
import almostrenoir.reposbrowser.shared.exceptions.ExternalServiceException;
import almostrenoir.reposbrowser.shared.httpclient.ContentType;
import almostrenoir.reposbrowser.shared.httpclient.HttpClient;
import almostrenoir.reposbrowser.shared.httpclient.HttpException;
import almostrenoir.reposbrowser.shared.httpclient.HttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubUsersReposServiceTest {

    @Mock
    private HttpClient httpClient;

    @InjectMocks
    private GithubUsersReposService usersReposService;

    @Test
    void shouldCollectBranchesWhenGetAllWithoutForks() {
        HttpRequest reposRequest = buildGetUsersReposRequest();
        List<GithubRepo> githubRepos = createGithubRepos();
        when(httpClient.getAllWithLinkPagination(eq(reposRequest), eq(GithubRepo.class)))
                .thenReturn(Flux.fromIterable(githubRepos));

        HttpRequest calculatorBranchesRequest = buildGetReposBranchesRequest(githubRepos.get(0).getCorrectBranchesUrl());
        GithubBranch calculatorMasterBranch = new GithubBranch("master", new GithubCommit("123456"));
        when(httpClient.getAllWithLinkPagination(eq(calculatorBranchesRequest), eq(GithubBranch.class)))
                .thenReturn(Flux.just(calculatorMasterBranch));

        HttpRequest graphDrawerBranchesRequest = buildGetReposBranchesRequest(githubRepos.get(1).getCorrectBranchesUrl());
        List<GithubBranch> graphDrawerBranches = createGraphDrawerBranches();
        when(httpClient.getAllWithLinkPagination(eq(graphDrawerBranchesRequest), eq(GithubBranch.class)))
                .thenReturn(Flux.fromIterable(graphDrawerBranches));

        List<GitRepo> result = usersReposService.getAllWithoutForks("foobar").collectList().block();

        assertNotNull(result);
        assertEquals(githubRepos.size(), result.size());
        assertEquals(githubRepos.get(0).getName(), result.get(0).getName());
        List<GitBranch> graphDrawerResultBranches = result.get(1).getBranches();
        assertNotNull(graphDrawerResultBranches);
        assertEquals(graphDrawerBranches.size(), graphDrawerResultBranches.size());
        assertEquals(
                graphDrawerBranches.get(0).getCommit().getSha(),
                graphDrawerResultBranches.get(0).getLastCommitSHA()
        );
    }

    private List<GithubRepo> createGithubRepos() {
        GithubRepo calculatorRepo = createCalculatorRepo();
        GithubRepoOwner githubRepoOwner = new GithubRepoOwner("foobar");
        GithubRepo graphDrawerRepo = new GithubRepo(
                "graph-drawer",
                githubRepoOwner,
                false,
                "https://api.github.com/repos/foobar/graph-drawer/branches{/branch}"
        );
        return List.of(calculatorRepo, graphDrawerRepo);
    }

    private List<GithubBranch> createGraphDrawerBranches() {
        GithubBranch master = new GithubBranch("master", new GithubCommit("111111"));
        GithubBranch layoutChanges = new GithubBranch("layout-changes", new GithubCommit("222222"));
        return List.of(master, layoutChanges);
    }

    @Test
    void shouldFilterOutForksWhenGetAllWithoutForks() {
        HttpRequest reposRequest = buildGetUsersReposRequest();
        List<GithubRepo> githubRepos = createGithubReposWithSomeForks();
        when(httpClient.getAllWithLinkPagination(eq(reposRequest), eq(GithubRepo.class)))
                .thenReturn(Flux.fromIterable(githubRepos));
        when(httpClient.getAllWithLinkPagination(AdditionalMatchers.not(eq(reposRequest)), any()))
                .thenReturn(Flux.empty());

        List<GitRepo> result = usersReposService.getAllWithoutForks("foobar").collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    private List<GithubRepo> createGithubReposWithSomeForks() {
        GithubRepoOwner githubRepoOwner = new GithubRepoOwner("foobar");
        GithubRepo first = new GithubRepo("first", githubRepoOwner, true, "");
        GithubRepo second = new GithubRepo("second", githubRepoOwner, false, "");
        GithubRepo third = new GithubRepo("third", githubRepoOwner, true, "");
        return List.of(first, second, third);
    }

    @Test
    void shouldThrowExceptionIfUserNotFoundWhenGetAllWithoutForks() {
        when(httpClient.getAllWithLinkPagination(any(), any())).thenReturn(Flux.error(new HttpException.NotFound()));

        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> usersReposService.getAllWithoutForks("foobar").collectList().block());
        assertEquals("There is no user with given username", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfServerErrorWhileFetchingReposWhenGetAllWithoutForks() {
        when(httpClient.getAllWithLinkPagination(any(), any())).thenReturn(Flux.error(new HttpException.ServerError()));

        ExternalServiceException exception = assertThrows(ExternalServiceException.class,
                () -> usersReposService.getAllWithoutForks("foobar").collectList().block());
        assertEquals("Github is currently unavailable", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfServerErrorWhileFetchingBranchesWhenGetAllWithoutForks() {
        HttpRequest reposRequest = buildGetUsersReposRequest();
        GithubRepo githubRepo = createCalculatorRepo();
        when(httpClient.getAllWithLinkPagination(eq(reposRequest), eq(GithubRepo.class)))
                .thenReturn(Flux.just(githubRepo));

        HttpRequest calculatorBranchesRequest = buildGetReposBranchesRequest(githubRepo.getCorrectBranchesUrl());
        when(httpClient.getAllWithLinkPagination(eq(calculatorBranchesRequest), any()))
                .thenReturn(Flux.error(new HttpException.ServerError()));

        ExternalServiceException exception = assertThrows(ExternalServiceException.class,
                () -> usersReposService.getAllWithoutForks("foobar").collectList().block());
        assertEquals("Github is currently unavailable", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfTimeoutExceededWhileFetchingReposWhenGetAllWithoutForks() {
        when(httpClient.getAllWithLinkPagination(any(), any()))
                .thenReturn(Flux.error(new HttpException.TimeoutExceeded()));

        ExternalServiceException exception = assertThrows(ExternalServiceException.class,
                () -> usersReposService.getAllWithoutForks("foobar").collectList().block());
        assertEquals("Github is currently unavailable", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfTimeoutExceededWhileFetchingBranchesWhenGetAllWithoutForks() {
        HttpRequest reposRequest = buildGetUsersReposRequest();
        GithubRepo githubRepo = createCalculatorRepo();
        when(httpClient.getAllWithLinkPagination(eq(reposRequest), eq(GithubRepo.class)))
                .thenReturn(Flux.just(githubRepo));

        HttpRequest calculatorBranchesRequest = buildGetReposBranchesRequest(githubRepo.getCorrectBranchesUrl());
        when(httpClient.getAllWithLinkPagination(eq(calculatorBranchesRequest), any()))
                .thenReturn(Flux.error(new HttpException.TimeoutExceeded()));

        ExternalServiceException exception = assertThrows(ExternalServiceException.class,
                () -> usersReposService.getAllWithoutForks("foobar").collectList().block());
        assertEquals("Github is currently unavailable", exception.getMessage());
    }

    private HttpRequest buildGetUsersReposRequest() {
        String url = String.format(GithubUsersReposService.USERS_REPOS_URL, "foobar");
        return HttpRequest.builder()
                .url(url)
                .accept(ContentType.GITHUB_JSON)
                .header(GithubUsersReposService.API_VERSION_HEADER_NAME, GithubUsersReposService.API_VERSION_HEADER_VALUE)
                .timeout(GithubUsersReposService.GET_USERS_REPOS_TIMEOUT)
                .build();
    }

    private HttpRequest buildGetReposBranchesRequest(String url) {
        return HttpRequest.builder()
                .url(url)
                .accept(ContentType.GITHUB_JSON)
                .header(GithubUsersReposService.API_VERSION_HEADER_NAME, GithubUsersReposService.API_VERSION_HEADER_VALUE)
                .timeout(GithubUsersReposService.GET_REPOS_BRANCHES_TIMEOUT)
                .build();
    }

    private GithubRepo createCalculatorRepo() {
        GithubRepoOwner githubRepoOwner = new GithubRepoOwner("foobar");
        return new GithubRepo(
                "calculator",
                githubRepoOwner,
                false,
                "https://api.github.com/repos/foobar/calculator/branches{/branch}"
        );
    }

}