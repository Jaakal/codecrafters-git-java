package git;

public class GitRef {
  private final String sha1Hash;
  private final String name;

  public GitRef(final String sha1Hash, final String name) {
    this.sha1Hash = sha1Hash;
    this.name = name;
  }

  public final String getSha1Hash() {
    return this.sha1Hash;
  }

  public final String getName() {
    return this.name;
  }
}
