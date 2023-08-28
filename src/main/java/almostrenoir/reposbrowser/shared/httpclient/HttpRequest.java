package almostrenoir.reposbrowser.shared.httpclient;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Builder
public class HttpRequest {

  private final String url;

  private final List<Header> headers;

  @Builder.Default
  private final int timeout = 1000;

  public List<Header> getHeaders() {
    return Collections.unmodifiableList(headers);
  }

  public static class HttpRequestBuilder {
    private final List<Header> headers = new ArrayList<>();

    public HttpRequestBuilder accept(ContentType contentType) {
      return header("Accept", contentType.getValue());
    }

    public HttpRequestBuilder header(String name, String value) {
      headers.add(new Header(name, value));
      return this;
    }
  }

  public record Header(String name, String value) {}
}
