package almostrenoir.reposbrowser.gitrepos.dtos.outgoing;

import almostrenoir.reposbrowser.gitrepos.GitRepo;
import lombok.Data;

import java.util.List;

@Data
public class GitRepoOutgoingDTO {
    private final String name;
    private final String owner;
    private final List<GitBranchOutgoingDTO> branches;

    public static GitRepoOutgoingDTO fromModel(GitRepo model) {
        return new GitRepoOutgoingDTO(
                model.getName(),
                model.getOwner(),
                model.getBranches().stream().map(GitBranchOutgoingDTO::fromModel).toList()
        );
    }
}
