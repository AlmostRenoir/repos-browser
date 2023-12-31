package almostrenoir.reposbrowser.shared.httpclient;

public enum ContentType {
    JSON("application/json"),
    GITHUB_JSON("application/vnd.github+json");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
