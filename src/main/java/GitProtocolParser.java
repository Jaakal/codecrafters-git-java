import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GitProtocolParser {
  private static void parseHeader(ByteBuffer response) {
    byte[] lengthBytes = new byte[4];
    response.get(lengthBytes);
    int length = Integer.parseInt(new String(lengthBytes, StandardCharsets.UTF_8), 16);
    response.position(length + 4); // Flush packet
  }

  private static boolean parseLine(ByteBuffer response, ArrayList<GitRef> refs, ArrayList<String> capabilities) {
    byte[] lengthBytes = new byte[4];
    response.get(lengthBytes);
    int length = Integer.parseInt(new String(lengthBytes, StandardCharsets.UTF_8), 16);

    if (length == 0) {
      return false;
    }

    length -= 4; // length bytes
    byte[] line = new byte[length];
    response.get(line);

    int index = 0;
    if (capabilities.size() == 0) {
      while (line[index++] != ASCII.NULL) {
      }

      // null character
      capabilities
          .addAll(Arrays.asList((new String(line, index, length - index - 1, StandardCharsets.UTF_8)).split(" ")));

      length = index;
    }

    String lineString = new String(line, 0, length - 1, StandardCharsets.UTF_8); // new line character
    String[] parts = lineString.split(" ");

    refs.add(new GitRef(parts[0], parts[1]));

    return true;
  }

  public static GitRefs parseRefs(byte[] _response) {
    ByteBuffer response = ByteBuffer.wrap(_response).asReadOnlyBuffer();
    ArrayList<GitRef> refs = new ArrayList<>();
    ArrayList<String> capabilities = new ArrayList<>();

    GitProtocolParser.parseHeader(response);

    while (GitProtocolParser.parseLine(response, refs, capabilities)) {
    }

    return new GitRefs(refs, capabilities);
  }

  private static String getHexLength(int length) {
    // Length includes the 4 characters of the length prefix itself
    length += 4;
    // Convert to a 4-character hexadecimal string, zero-padded
    return String.format("%04x", length);
  }

  public static String buildUploadPackRequest(GitRefs refs) {
    List<String> commitHashes = refs.getRefs().stream().map((GitRef ref) -> ref.getHash()).distinct().toList();
    // List<String> capabilities = List.of("multi_ack", "thin-pack", "ofs-delta");
    List<String> capabilities = List.of();

    StringBuilder request = new StringBuilder();

    // Add the first "want" line with capabilities
    if (!commitHashes.isEmpty()) {
      StringBuilder firstLine = new StringBuilder();
      firstLine.append("want ").append(commitHashes.get(0));
      for (String capability : capabilities) {
        firstLine.append(" ").append(capability);
      }
      firstLine.append("\n");
      request.append(GitProtocolParser.getHexLength(firstLine.length())).append(firstLine);
    }

    // Add subsequent "want" lines without capabilities
    for (int i = 1; i < commitHashes.size(); i++) {
      String line = "want " + commitHashes.get(i) + "\n";
      request.append(getHexLength(line.length())).append(line);
    }

    // End the request with a flush packet
    request.append("00000009done\n");
    return request.toString();
  }
}
