package almostrenoir.reposbrowser.shared.errorresult;

import lombok.Data;

@Data
public class HttpErrorResult {
    private final int status;
    private final String message;
}
