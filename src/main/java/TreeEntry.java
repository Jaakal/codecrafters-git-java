import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TreeEntry {
  private String mode;
  private String name;
  private byte[] hash;

  public TreeEntry(String mode, String name, byte[] hash) {
    this.mode = mode;
    this.name = name;
    this.hash = hash;
  }

  public String getMode() {
    return this.mode;
  }

  public String getName() {
    return this.name;
  }

  public byte[] getHash() {
    return this.hash;
  }

  public byte[] serialize() {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      byteArrayOutputStream.write(this.mode.getBytes(StandardCharsets.UTF_8));
      byteArrayOutputStream.write(' ');

      byteArrayOutputStream.write(this.name.getBytes(StandardCharsets.UTF_8));
      byteArrayOutputStream.write(0);

      byteArrayOutputStream.write(this.hash);

      return byteArrayOutputStream.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to serialize TreeEntry", e);
    }
  }

  @Override
  public String toString() {
    return this.mode + " " + this.name + "\0" + Util.bytesToHexString(this.hash);
  }
}
