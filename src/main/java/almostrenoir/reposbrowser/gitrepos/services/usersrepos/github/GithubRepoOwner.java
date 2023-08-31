package almostrenoir.reposbrowser.gitrepos.services.usersrepos.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GithubRepoOwner {
    private final String login;

    public GithubRepoOwner(@JsonProperty("login") String login) {
        this.login = login;
    }
}
