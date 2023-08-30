package almostrenoir.reposbrowser.gitrepos;

import lombok.Data;
import reactor.core.publisher.Flux;

@Data
public class GitRepo {
    private final String name;
    private final String owner;
    private final Flux<GitBranch> branches;
}
