public class GitRef {
  private String hash;
  private String name;

  public GitRef(String hash, String name) {
    this.hash = hash;
    this.name = name;
  }

  public String getHash() {
    return this.hash;
  }

  public String getName() {
    return this.name;
  }
}
