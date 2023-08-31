package almostrenoir.reposbrowser.gitrepos;

import almostrenoir.reposbrowser.gitrepos.dtos.outgoing.GitRepoOutgoingDTO;
import almostrenoir.reposbrowser.gitrepos.services.main.GitReposMainService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("git-repos")
@CrossOrigin
@Validated
@RequiredArgsConstructor
public class GitReposController {

    private final GitReposMainService gitReposMainService;

    @GetMapping("by-user/{username}/no-forks")
    public Flux<GitRepoOutgoingDTO> getUsersReposWithoutForks(
            @PathVariable @NotBlank(message = "Username cannot be blank") String username
    ) {
        return gitReposMainService.getUsersReposWithoutForks(username);
    }

}
