package almostrenoir.reposbrowser.gitrepos.services.usersrepos.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GithubCommit {
    private final String sha;

    public GithubCommit(@JsonProperty("sha") String sha) {
        this.sha = sha;
    }
}
