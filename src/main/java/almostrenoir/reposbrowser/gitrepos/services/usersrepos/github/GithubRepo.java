package almostrenoir.reposbrowser.gitrepos.services.usersrepos.github;

import lombok.Data;

@Data
public class GithubRepo {
    private final String name;
    private final GithubRepoOwner owner;
    private final boolean fork;
    private final String branches_url;
}
