package almostrenoir.reposbrowser.gitrepos.services.usersrepos.github;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GithubRepoTest {

    @Test
    void shouldRemoveUnnecessarySectionWhenGetCorrectBranchesUrl() {
        GithubRepoOwner githubRepoOwner = new GithubRepoOwner("foobar");
        GithubRepo githubRepo = new GithubRepo(
                "snake",
                githubRepoOwner,
                false,
                "https://api.github.com/repos/foobar/snake/branches{/branch}"
        );

        String result = githubRepo.getCorrectBranchesUrl();

        String expected = "https://api.github.com/repos/foobar/snake/branches";
        assertEquals(expected, result);
    }

}