package git;

public class GitObjectInfo {
  private final GitObjectType type;
  private final int size;

  public GitObjectInfo(final GitObjectType type, final int size) {
    this.type = type;
    this.size = size;
  }

  public final GitObjectType getType() {
    return this.type;
  }

  public final int getSize() {
    return this.size;
  }
}
