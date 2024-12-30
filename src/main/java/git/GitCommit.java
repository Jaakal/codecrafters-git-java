package git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class GitCommit {
  public static final byte[] createCommitContent(final String treeSha1Hash, final String parentCommitSha1Hash,
      final String authorName,
      final String authorEmail,
      final String authorTimestamp, final String committerName, final String committerEmail,
      final String committerTimestamp,
      final String commitMessage) throws IOException {
    final ByteArrayOutputStream contentStream = new ByteArrayOutputStream();

    contentStream.write("tree ".getBytes(StandardCharsets.UTF_8));
    contentStream
        .write(
            treeSha1Hash.getBytes(StandardCharsets.UTF_8));
    contentStream.write("\n".getBytes());

    contentStream.write("parent ".getBytes(StandardCharsets.UTF_8));
    contentStream.write(
        parentCommitSha1Hash.getBytes(StandardCharsets.UTF_8));
    contentStream.write("\n".getBytes());

    contentStream.write(("author " + authorName + " <" + authorEmail + "> ").getBytes(StandardCharsets.UTF_8));
    contentStream.write(authorTimestamp.getBytes());
    contentStream.write("\n".getBytes());

    contentStream.write(("committer " + committerName + " <" + committerEmail + "> ").getBytes(StandardCharsets.UTF_8));
    contentStream.write(committerTimestamp.getBytes());
    contentStream.write("\n\n".getBytes());

    contentStream.write(
        commitMessage.getBytes(StandardCharsets.UTF_8));
    contentStream.write("\n".getBytes());

    return contentStream.toByteArray();
  }

}
