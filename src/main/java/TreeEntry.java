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
}
