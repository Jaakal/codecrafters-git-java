import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class HttpClientHelper {
  public static byte[] fetch(String url) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url + "/info/refs?service=git-upload-pack"))
        .GET()
        .build();
    HttpResponse<byte[]> response = client.send(request, BodyHandlers.ofByteArray());
    if (response.statusCode() != 200) {
      throw new RuntimeException("Failed to fetch data from " + url + ": " + response.statusCode());
    }
    return response.body();
  }

  public static byte[] fetchPackfile(String repoUrl, GitRefs refs)
      throws IOException, InterruptedException {
    String payload = GitProtocolParser.buildUploadPackRequest(refs);
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(repoUrl + "/git-upload-pack"))
        .POST(BodyPublishers.ofString(payload))
        .header("Content-Type", "application/x-git-upload-pack-request")
        .build();
    HttpResponse<byte[]> response = client.send(request,
        BodyHandlers.ofByteArray());
    if (response.statusCode() != 200) {
      throw new RuntimeException("Failed to fetch packfile: " + response.statusCode());
    }
    return response.body();
  }
}
