package git;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public final class GitHttpClient {
  private GitHttpClient() {
  }

  public static byte[] fetchReferences() throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(GitArguments.getCliArgument(GitCliArguments.URL) + "/info/refs?service=git-upload-pack"))
        .GET()
        .build();
    HttpResponse<byte[]> response = client.send(request, BodyHandlers.ofByteArray());

    if (response.statusCode() != 200) {
      throw new RuntimeException("Failed to fetch references from " + GitArguments.getCliArgument(GitCliArguments.URL)
          + ": " + response.statusCode());
    }

    return response.body();
  }

  public static byte[] fetchPackfile(GitRefs gitRefs)
      throws IOException, InterruptedException {
    String payload = GitProtocolParser.buildUploadPackRequest(gitRefs);
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(GitArguments.getCliArgument(GitCliArguments.URL) + "/git-upload-pack"))
        .POST(BodyPublishers.ofString(payload))
        .header("Content-Type", "application/x-git-upload-pack-request")
        .build();
    HttpResponse<byte[]> response = client.send(request,
        BodyHandlers.ofByteArray());

    if (response.statusCode() != 200) {
      throw new RuntimeException("Failed to fetch packfile from " + GitArguments.getCliArgument(GitCliArguments.URL)
          + ": " + response.statusCode());
    }

    return response.body();
  }
}
