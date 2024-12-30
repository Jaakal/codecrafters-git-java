package git;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import constants.ASCII;

public class GitProtocolParser {
  private static void parseHeader(final ByteBuffer refsResponseBuffer) {
    final byte[] lengthBytes = new byte[4];
    refsResponseBuffer.get(lengthBytes);
    final int length = Integer.parseInt(new String(lengthBytes, StandardCharsets.UTF_8), 16);
    refsResponseBuffer.position(length + 4); // Flush packet
  }

  private static final boolean parseLine(final ByteBuffer refsResponseBuffer, final ArrayList<GitRef> gitRefs,
      final ArrayList<String> capabilities) {
    final byte[] lengthBytes = new byte[4];
    refsResponseBuffer.get(lengthBytes);
    int length = Integer.parseInt(new String(lengthBytes, StandardCharsets.UTF_8), 16);

    if (length == 0) {
      return false;
    }

    length -= 4; // length bytes
    final byte[] lineBytes = new byte[length];
    refsResponseBuffer.get(lineBytes);

    int index = 0;
    if (capabilities.size() == 0) {
      while (lineBytes[index++] != ASCII.NULL) {
      }

      // -1 in the end because of the null character
      capabilities
          .addAll(Arrays.asList((new String(lineBytes, index, length - index - 1, StandardCharsets.UTF_8)).split(" ")));

      length = index;
    }

    final String line = new String(lineBytes, 0, length - 1, StandardCharsets.UTF_8); // new line character
    final String[] lineParts = line.split(" ");

    gitRefs.add(new GitRef(lineParts[0], lineParts[1]));

    return true;
  }

  public static final GitRefs parseRefs(final byte[] getRefsResponse) {
    final ByteBuffer refsResponseBuffer = ByteBuffer.wrap(getRefsResponse).asReadOnlyBuffer();
    final ArrayList<GitRef> gitRefs = new ArrayList<>();
    final ArrayList<String> repositoryCapabilities = new ArrayList<>();

    GitProtocolParser.parseHeader(refsResponseBuffer);
    while (GitProtocolParser.parseLine(refsResponseBuffer, gitRefs, repositoryCapabilities)) {
    }

    return new GitRefs(gitRefs, repositoryCapabilities);
  }

  private static String getLineHexLength(final int lineLength) {
    // Convert to a 4-character hexadecimal string, zero-padded
    // Length includes the 4 characters of the length prefix itself
    return String.format("%04x", lineLength + 4);
  }

  public static final String buildUploadPackRequest(final GitRefs gitRefs) {
    final List<String> commitHashes = gitRefs.getRefs().stream().map((final GitRef gitRef) -> gitRef.getSha1Hash())
        .distinct()
        .toList();
    final List<String> capabilities = List.of();
    final StringBuilder request = new StringBuilder();

    // Add the first "want" line with capabilities
    if (!commitHashes.isEmpty()) {
      final StringBuilder firstLine = new StringBuilder();
      firstLine.append("want ").append(commitHashes.get(0));
      for (final String capability : capabilities) {
        firstLine.append(" ").append(capability);
      }
      firstLine.append("\n");
      request.append(GitProtocolParser.getLineHexLength(firstLine.length())).append(firstLine);
    }

    // Add subsequent "want" lines without capabilities
    for (int index = 1; index < commitHashes.size(); index++) {
      final String line = "want " + commitHashes.get(index) + "\n";
      request.append(getLineHexLength(line.length())).append(line);
    }

    // End the request with a flush packet
    request.append("00000009done\n");
    return request.toString();
  }
}
