package almostrenoir.reposbrowser.gitrepos;

import lombok.Data;

@Data
public class GitBranch {
    private final String name;
    private final String lastCommitSHA;
}
