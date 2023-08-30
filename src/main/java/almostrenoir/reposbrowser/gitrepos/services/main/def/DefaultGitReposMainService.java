package almostrenoir.reposbrowser.gitrepos.services.main.def;

import almostrenoir.reposbrowser.gitrepos.dtos.outgoing.GitRepoOutgoingDTO;
import almostrenoir.reposbrowser.gitrepos.services.main.GitReposMainService;
import almostrenoir.reposbrowser.gitrepos.services.usersrepos.UsersReposService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class DefaultGitReposMainService implements GitReposMainService {

    private final UsersReposService usersReposService;

    @Override
    public Flux<GitRepoOutgoingDTO> getUsersReposWithoutForks(String username) {
        return usersReposService.getAllWithoutForks(username).map(GitRepoOutgoingDTO::fromModel);
    }
}
