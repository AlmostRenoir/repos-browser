package almostrenoir.reposbrowser.gitrepos.services.main;

import almostrenoir.reposbrowser.gitrepos.dtos.outgoing.GitRepoOutgoingDTO;
import reactor.core.publisher.Flux;

public interface GitReposMainService {
    Flux<GitRepoOutgoingDTO> getUsersReposWithoutForks(String username);
}
