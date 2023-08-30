package almostrenoir.reposbrowser.gitrepos.services.usersrepos.github;

import lombok.Data;

@Data
public class GithubBranch {
    private final String name;
    private final GithubCommit commit;
}
