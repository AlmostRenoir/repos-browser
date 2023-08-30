package almostrenoir.reposbrowser.gitrepos.services.usersrepos;

import almostrenoir.reposbrowser.gitrepos.GitRepo;
import reactor.core.publisher.Flux;

public interface UsersReposService {
    Flux<GitRepo> getAllWithoutForks(String username);
}
