package almostrenoir.reposbrowser.gitrepos;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class GitRepo {
    private final String name;
    private final String owner;
    private final List<GitBranch> branches;

    public List<GitBranch> getBranches() {
        return Collections.unmodifiableList(branches);
    }
}
