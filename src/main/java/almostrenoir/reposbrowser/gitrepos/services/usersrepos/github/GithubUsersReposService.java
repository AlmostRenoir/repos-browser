package almostrenoir.reposbrowser.gitrepos.services.usersrepos.github;

import almostrenoir.reposbrowser.gitrepos.GitBranch;
import almostrenoir.reposbrowser.gitrepos.GitRepo;
import almostrenoir.reposbrowser.gitrepos.services.usersrepos.UsersReposService;
import almostrenoir.reposbrowser.shared.exceptions.DataNotFoundException;
import almostrenoir.reposbrowser.shared.exceptions.ExternalServiceException;
import almostrenoir.reposbrowser.shared.httpclient.ContentType;
import almostrenoir.reposbrowser.shared.httpclient.HttpClient;
import almostrenoir.reposbrowser.shared.httpclient.HttpException;
import almostrenoir.reposbrowser.shared.httpclient.HttpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubUsersReposService implements UsersReposService {

    public static final String USERS_REPOS_URL = "https://api.github.com/users/%s/repos";
    public static final String API_VERSION_HEADER_NAME = "X-GitHub-Api-Version";
    public static final String API_VERSION_HEADER_VALUE = "2022-11-28";
    public static final int GET_USERS_REPOS_TIMEOUT = 5000;
    public static final int GET_REPOS_BRANCHES_TIMEOUT = 3000;

    private final HttpClient httpClient;

    @Override
    public Flux<GitRepo> getAllWithoutForks(String username) {
        HttpRequest httpRequest = HttpRequest.builder()
                .url(getUsersReposUrl(username))
                .accept(ContentType.GITHUB_JSON)
                .header(API_VERSION_HEADER_NAME, API_VERSION_HEADER_VALUE)
                .timeout(GET_USERS_REPOS_TIMEOUT)
                .build();

        return httpClient.getAllWithLinkPagination(httpRequest, GithubRepo.class)
                .onErrorMap(
                        HttpException.NotFound.class,
                        ex -> new DataNotFoundException("There is no user with given username")
                ).onErrorMap(HttpException.ServerError.class, this::onGetUsersReposServerError)
                .onErrorMap(HttpException.TimeoutExceeded.class, this::onGetUsersReposTimeout)
                .filter(githubRepo -> !githubRepo.isFork())
                .flatMap(this::collectBranches);
    }

    private String getUsersReposUrl(String username) {
        return String.format(USERS_REPOS_URL, username);
    }

    private ExternalServiceException onGetUsersReposServerError(Throwable throwable) {
        log.warn("Github returned with server error when fetching users repos");
        return new ExternalServiceException("Github is currently unavailable");
    }

    private ExternalServiceException onGetUsersReposTimeout(Throwable throwable) {
        log.warn("Github exceeded timeout ({} ms) when fetching users repos", GET_USERS_REPOS_TIMEOUT);
        return new ExternalServiceException("Github is currently unavailable");
    }

    private Mono<GitRepo> collectBranches(GithubRepo githubRepo) {
        HttpRequest httpRequest = HttpRequest.builder()
                .url(githubRepo.getCorrectBranchesUrl())
                .accept(ContentType.GITHUB_JSON)
                .header(API_VERSION_HEADER_NAME, API_VERSION_HEADER_VALUE)
                .timeout(GET_REPOS_BRANCHES_TIMEOUT)
                .build();

        return httpClient.getAllWithLinkPagination(httpRequest, GithubBranch.class)
                .onErrorMap(HttpException.ServerError.class, this::onGetReposBranchesServerError)
                .onErrorMap(HttpException.TimeoutExceeded.class, this::onGetReposBranchesTimeout)
                .map(githubBranch -> new GitBranch(githubBranch.getName(), githubBranch.getCommit().getSha()))
                .collectList()
                .map(branches -> new GitRepo(githubRepo.getName(), githubRepo.getOwner().getLogin(), branches));
    }

    private ExternalServiceException onGetReposBranchesServerError(Throwable throwable) {
        log.warn("Github returned with server error when fetching repos branches");
        return new ExternalServiceException("Github is currently unavailable");
    }

    private ExternalServiceException onGetReposBranchesTimeout(Throwable throwable) {
        log.warn("Github exceeded timeout ({} ms) when fetching repos branches", GET_REPOS_BRANCHES_TIMEOUT);
        return new ExternalServiceException("Github is currently unavailable");
    }

}
