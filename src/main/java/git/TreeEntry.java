package git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import utils.DataUtils;

public class TreeEntry {
  private final GitMode mode;
  private final String name;
  private final byte[] sha1Hash;

  public TreeEntry(final GitMode mode, final String name, final byte[] sha1Hash) {
    this.mode = mode;
    this.name = name;
    this.sha1Hash = sha1Hash;
  }

  public final GitMode getMode() {
    return this.mode;
  }

  public final String getName() {
    return this.name;
  }

  public final byte[] getHash() {
    return this.sha1Hash;
  }

  public final byte[] serialize() throws IOException {
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    byteArrayOutputStream.write(this.mode.getValue().getBytes(StandardCharsets.UTF_8));
    byteArrayOutputStream.write(' ');

    byteArrayOutputStream.write(this.name.getBytes(StandardCharsets.UTF_8));
    byteArrayOutputStream.write(0);

    byteArrayOutputStream.write(this.sha1Hash);

    return byteArrayOutputStream.toByteArray();
  }
}
