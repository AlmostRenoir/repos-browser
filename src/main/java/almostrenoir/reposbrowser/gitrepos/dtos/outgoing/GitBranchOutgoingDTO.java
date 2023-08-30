package almostrenoir.reposbrowser.gitrepos.dtos.outgoing;

import almostrenoir.reposbrowser.gitrepos.GitBranch;
import lombok.Data;

@Data
public class GitBranchOutgoingDTO {
    private final String name;
    private final String lastCommitSHA;

    public static GitBranchOutgoingDTO fromModel(GitBranch model) {
        return new GitBranchOutgoingDTO(model.getName(), model.getLastCommitSHA());
    }
}
